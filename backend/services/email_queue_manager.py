"""
Service de gestion des emails avec système de priorités
Gère une queue d'emails avec retry automatique et priorités
"""

from datetime import datetime, timedelta
from extensions import db
from models import EmailQueue, EmailLog
import logging

logger = logging.getLogger(__name__)


class EmailQueueManager:
    """Manager pour traiter les emails selon les priorités"""
    
    # Configuration des tentatives par niveau de priorité
    RETRY_CONFIG = {
        1: {'max_attempts': 5, 'delay_minutes': 10},      # CRITIQUE
        2: {'max_attempts': 3, 'delay_minutes': 60},      # STANDARD
        3: {'max_attempts': 2, 'delay_minutes': 360}      # MARKETING (6h)
    }
    
    # Délai max avant traitement
    MAX_DELAY = {
        1: 5,      # 5 minutes
        2: 30,     # 30 minutes
        3: 1440    # 24 heures
    }
    
    PRIORITY_NAMES = {
        1: 'CRITIQUE',
        2: 'STANDARD',
        3: 'MARKETING'
    }
    
    @staticmethod
    def add_to_queue(to_email, subject, body, priority=2, email_type='general', 
                     user_id=None, template_name=None, metadata=None, delay_minutes=0):
        """
        Ajoute un email à la queue
        
        Args:
            to_email (str): Email du destinataire
            subject (str): Sujet du mail
            body (str): Corps du mail en HTML
            priority (int): 1=critique, 2=standard, 3=marketing
            email_type (str): Type d'email (confirmation, relance, etc)
            user_id (int): ID utilisateur associé
            template_name (str): Nom du template utilisé
            metadata (dict): Données contextuelles
            delay_minutes (int): Délai avant traitement en minutes
            
        Returns:
            EmailQueue: Item ajouté
        """
        # Valider la priorité
        if priority not in RETRY_CONFIG:
            logger.warning(f"Priority invalide: {priority}, utilisant 2 par défaut")
            priority = 2
        
        # Calculer le timing
        scheduled_for = (datetime.utcnow() + timedelta(minutes=delay_minutes)) if delay_minutes else datetime.utcnow()
        
        # Créer l'item
        queue_item = EmailQueue(
            to_email=to_email,
            subject=subject,
            body=body,
            priority=priority,
            type=email_type,
            user_id=user_id,
            template_name=template_name,
            scheduled_for=scheduled_for,
            metadata=metadata or {},
            max_attempts=EmailQueueManager.RETRY_CONFIG[priority]['max_attempts']
        )
        
        try:
            db.session.add(queue_item)
            db.session.commit()
            
            priority_name = EmailQueueManager.PRIORITY_NAMES.get(priority, 'UNKNOWN')
            logger.info(
                f"✉️ [{priority_name}] Email ajouté à queue | "
                f"To: {to_email} | Type: {email_type} | "
                f"Scheduled: {scheduled_for.strftime('%Y-%m-%d %H:%M:%S')}"
            )
            return queue_item
            
        except Exception as e:
            logger.error(f"❌ Erreur lors de l'ajout à la queue: {str(e)}")
            db.session.rollback()
            raise
    
    @staticmethod
    def process_queue():
        """
        Traite les emails en attente selon leur priorité
        À appeler toutes les 5 minutes par APScheduler
        """
        try:
            # Récupère les emails prêts à être traités
            # Triés par: priorité (1,2,3) puis par création (FIFO)
            pending_emails = EmailQueue.query.filter(
                EmailQueue.status == 'pending',
                EmailQueue.scheduled_for <= datetime.utcnow(),
                EmailQueue.attempt_count < EmailQueue.max_attempts
            ).order_by(
                EmailQueue.priority.asc(),     # P1 avant P2 avant P3
                EmailQueue.created_at.asc()    # FIFO dans chaque niveau
            ).limit(50).all()  # Limit 50 par batch pour éviter surcharge
            
            if not pending_emails:
                logger.debug("📧 Queue: aucun email à traiter")
                return
            
            sent_count = 0
            for email in pending_emails:
                if EmailQueueManager._send_email(email):
                    sent_count += 1
            
            logger.info(f"📧 Queue processed: {sent_count}/{len(pending_emails)} emails envoyés")
            
            # Nettoyer les anciens emails en erreur
            EmailQueueManager._cleanup_failed_emails()
            
        except Exception as e:
            logger.error(f"❌ Erreur lors du traitement de la queue: {str(e)}")
    
    @staticmethod
    def _send_email(queue_item):
        """
        Envoie un email de la queue
        
        Args:
            queue_item (EmailQueue): Item à envoyer
            
        Returns:
            bool: True si envoyé avec succès
        """
        priority_name = EmailQueueManager.PRIORITY_NAMES.get(queue_item.priority, 'UNKNOWN')
        
        try:
            # Importer ici pour éviter dépendance circulaire
            from services.mail_service import _send
            
            # Tentative d'envoi
            success = _send(
                to_email=queue_item.to_email,
                to_name=queue_item.to_email.split('@')[0],
                subject=queue_item.subject,
                body=queue_item.body
            )
            
            if success:
                # Mise à jour du statut
                queue_item.status = 'sent'
                queue_item.sent_at = datetime.utcnow()
                
                # Log détaillé
                email_log = EmailLog(
                    email_queue_id=queue_item.id,
                    to_email=queue_item.to_email,
                    subject=queue_item.subject,
                    priority=queue_item.priority,
                    email_type=queue_item.type,
                    status='sent',
                    attempt_number=queue_item.attempt_count + 1,
                    mail_provider='gmail'  # À adapter selon votre config
                )
                db.session.add(email_log)
                db.session.commit()
                
                logger.info(
                    f"✅ [{priority_name}] Email envoyé avec succès | "
                    f"To: {queue_item.to_email}"
                )
                return True
            else:
                EmailQueueManager._handle_retry(queue_item, "Send returned False")
                return False
                
        except Exception as e:
            error_msg = str(e)
            logger.error(
                f"❌ [{priority_name}] Erreur lors de l'envoi à {queue_item.to_email}: {error_msg}"
            )
            EmailQueueManager._handle_retry(queue_item, error_msg)
            return False
    
    @staticmethod
    def _handle_retry(queue_item, error_msg=None):
        """
        Gère les retries automatiques pour un email échoué
        
        Args:
            queue_item (EmailQueue): Item ayant échoué
            error_msg (str): Message d'erreur
        """
        queue_item.attempt_count += 1
        queue_item.error_message = error_msg or "Unknown error"
        queue_item.last_attempt_at = datetime.utcnow()
        
        priority_name = EmailQueueManager.PRIORITY_NAMES.get(queue_item.priority, 'UNKNOWN')
        
        if queue_item.attempt_count >= queue_item.max_attempts:
            # Max tentatives atteint
            queue_item.status = 'failed'
            
            # Log d'erreur
            email_log = EmailLog(
                email_queue_id=queue_item.id,
                to_email=queue_item.to_email,
                subject=queue_item.subject,
                priority=queue_item.priority,
                email_type=queue_item.type,
                status='failed',
                error_message=error_msg,
                attempt_number=queue_item.attempt_count,
                mail_provider='gmail'
            )
            db.session.add(email_log)
            db.session.commit()
            
            logger.warning(
                f"⚠️ [{priority_name}] Email échoué après {queue_item.max_attempts} tentatives | "
                f"To: {queue_item.to_email} | Error: {error_msg}"
            )
        else:
            # Reschedule avec délai exponentiel
            delay = EmailQueueManager.RETRY_CONFIG[queue_item.priority]['delay_minutes']
            queue_item.scheduled_for = datetime.utcnow() + timedelta(minutes=delay)
            
            logger.info(
                f"🔄 [{priority_name}] Retry #{queue_item.attempt_count} "
                f"planifié dans {delay} min pour {queue_item.to_email}"
            )
            
            db.session.commit()
    
    @staticmethod
    def _cleanup_failed_emails():
        """Supprime les emails en erreur après 7 jours"""
        try:
            old_date = datetime.utcnow() - timedelta(days=7)
            old_failed = EmailQueue.query.filter(
                EmailQueue.status == 'failed',
                EmailQueue.last_attempt_at < old_date
            ).delete()
            
            if old_failed:
                db.session.commit()
                logger.info(f"🧹 Cleanup: {old_failed} anciens emails échoués supprimés")
                
        except Exception as e:
            logger.error(f"Erreur lors du cleanup: {str(e)}")
            db.session.rollback()
    
    @staticmethod
    def get_queue_stats():
        """Retourne les statistiques de la queue"""
        try:
            stats = {
                'timestamp': datetime.utcnow().isoformat(),
                'pending': EmailQueue.query.filter_by(status='pending').count(),
                'sent_today': EmailQueue.query.filter(
                    EmailQueue.status == 'sent',
                    EmailQueue.sent_at >= datetime.utcnow() - timedelta(hours=24)
                ).count(),
                'failed': EmailQueue.query.filter_by(status='failed').count(),
                'by_priority': {
                    'p1_pending': EmailQueue.query.filter_by(priority=1, status='pending').count(),
                    'p1_sent': EmailQueue.query.filter(
                        EmailQueue.priority == 1,
                        EmailQueue.status == 'sent',
                        EmailQueue.sent_at >= datetime.utcnow() - timedelta(hours=24)
                    ).count(),
                    'p2_pending': EmailQueue.query.filter_by(priority=2, status='pending').count(),
                    'p2_sent': EmailQueue.query.filter(
                        EmailQueue.priority == 2,
                        EmailQueue.status == 'sent',
                        EmailQueue.sent_at >= datetime.utcnow() - timedelta(hours=24)
                    ).count(),
                    'p3_pending': EmailQueue.query.filter_by(priority=3, status='pending').count(),
                    'p3_sent': EmailQueue.query.filter(
                        EmailQueue.priority == 3,
                        EmailQueue.status == 'sent',
                        EmailQueue.sent_at >= datetime.utcnow() - timedelta(hours=24)
                    ).count(),
                }
            }
            
            # Calculer le taux de succès
            total_processed = stats['sent_today'] + stats['failed']
            if total_processed > 0:
                stats['success_rate'] = round((stats['sent_today'] / total_processed) * 100, 1)
            else:
                stats['success_rate'] = 0
            
            return stats
            
        except Exception as e:
            logger.error(f"Erreur lors de la récupération des stats: {str(e)}")
            return {}
    
    @staticmethod
    def get_failed_emails(limit=20):
        """Retourne les emails ayant échoué (pour debugging)"""
        try:
            failed = EmailQueue.query.filter_by(
                status='failed'
            ).order_by(
                EmailQueue.last_attempt_at.desc()
            ).limit(limit).all()
            
            return [item.to_dict() for item in failed]
            
        except Exception as e:
            logger.error(f"Erreur lors de la récupération des failed emails: {str(e)}")
            return []
    
    @staticmethod
    def cancel_pending_for_user(user_id):
        """Annule tous les emails en attente pour un utilisateur"""
        try:
            cancelled = EmailQueue.query.filter(
                EmailQueue.user_id == user_id,
                EmailQueue.status == 'pending'
            ).update({'status': 'cancelled'})
            
            db.session.commit()
            logger.info(f"✂️ {cancelled} emails annulés pour l'utilisateur {user_id}")
            return cancelled
            
        except Exception as e:
            logger.error(f"Erreur lors de l'annulation: {str(e)}")
            db.session.rollback()
            return 0
