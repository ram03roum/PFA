import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './messages.html',
  styleUrls: ['./messages.css']
})
export class MessagesComponent implements OnInit {

  private apiUrl = 'http://localhost:5000/api';

  messages: any[] = [];
  prioritizedMessages: any[] = [];
  selectedMessage: any = null;
  replyText = '';
  isLoading = true;
  isLoadingPriority = false;
  isSending = false;
  sendSuccess = '';
  sendError = '';
  filterCategory = 'all';
  showPriority = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    console.log('📨 MessagesComponent initialisé');
    this.loadMessages();
  }

  // ── Chargement messages normaux ───────────────────────────
  loadMessages() {
    this.isLoading = true;
    console.log('🔄 Chargement des messages...');
    this.http.get<any>(`${this.apiUrl}/admin/messages`)
      .subscribe({
        next: res => {
          this.messages = res.messages;
          this.isLoading = false;
          console.log(`✅ ${this.messages.length} message(s) chargé(s)`);
          console.log(`📊 Non lus : ${this.unreadCount}`);
          console.table(this.messages.map(m => ({
          id:        m.id,
          nom:       m.name,
          categorie: m.category || '⏳ en cours',
          priorite:  m.priority || '⏳ en cours',
          sentiment: m.sentiment || '⏳ en cours',
          lu:        m.is_read ? '✅' : '❌'
        })));
        },
        error: err => {this.isLoading = false
        console.error('❌ Erreur chargement messages:', err);
        }
      });
  }
 // ── Chargement messages prioritisés ───────────────────────
  loadPrioritizedMessages() {
    this.isLoadingPriority = true;
    console.log('🚨 Chargement des messages par priorité...');

    this.http.get<any>(`${this.apiUrl}/admin/messages/prioritized`).subscribe({
      next: res => {
        this.prioritizedMessages = res.messages;
        this.isLoadingPriority   = false;
        console.log(`✅ ${this.prioritizedMessages.length} message(s) prioritisé(s)`);
        console.table(this.prioritizedMessages.map((m, i) => ({
          rang:            `#${i + 1}`,
          nom:             m.name,
          score_priorite:  m.priority_score,
          niveau:          this.getPriorityLabel(m.priority_score),
          categorie_ia:    m.category,
          segment_client:  m.client_segment,
          churn_risk:      m.churn_risk,
        })));
      },
      error: err => {
        this.isLoadingPriority = false;
        console.error('❌ Erreur chargement priorités:', err);
      }
    });
  }

  // ── Toggle vue priorité ───────────────────────────────────
  togglePriorityView() {
    this.showPriority = !this.showPriority;
    console.log(`👁️ Vue priorité : ${this.showPriority ? 'activée' : 'désactivée'}`);

    if (this.showPriority && this.prioritizedMessages.length === 0) {
      this.loadPrioritizedMessages();
    }
  }
    // ── Ouvrir un message ─────────────────────────────────────
  openMessage(msg: any) {
    this.selectedMessage = msg;
    this.replyText = msg.suggested_reply || '';
    this.sendSuccess = '';
    this.sendError = '';

    // Marque comme lu
    if (!msg.is_read) {
      this.http.put(`${this.apiUrl}/admin/messages/${msg.id}/read`, {})
        .subscribe(() => msg.is_read = true);
    }
  }

  closeMessage() {
    this.selectedMessage = null;
    this.replyText = '';
  }

  sendReply() {
    if (!this.replyText.trim()) return;

    this.isSending = true;
    this.sendSuccess = '';
    this.sendError = '';

    this.http.post(`${this.apiUrl}/admin/messages/${this.selectedMessage.id}/reply`, {
      reply: this.replyText
    }).subscribe({
      next: () => {
        this.isSending = false;
        this.sendSuccess = '✅ Réponse envoyée au client avec succès !';
      },
      error: () => {
        this.isSending = false;
        this.sendError = '❌ Erreur lors de l\'envoi. Réessayez.';
      }
    });
  }

  markAllRead() {
    this.http.put(`${this.apiUrl}/admin/messages/read-all`, {})
      .subscribe(() => this.messages.forEach(m => m.is_read = true));
  }

  get filteredMessages() {
    if (this.filterCategory === 'all') return this.messages;
    return this.messages.filter(m => m.category === this.filterCategory);
  }

  get unreadCount() {
    return this.messages.filter(m => !m.is_read).length;
  }

  getCategoryLabel(category: string): string {
    const labels: any = {
      'urgent':        '🔴 Urgent',
      'reclamation':   '🟠 Réclamation',
      'demande_devis': '🟢 Devis',
      'info':          '🔵 Info',
    };
    return labels[category] || '🔵 Info';
  }

  getCategoryClass(category: string): string {
    const classes: any = {
      'urgent':        'badge-urgent',
      'reclamation':   'badge-reclamation',
      'demande_devis': 'badge-devis',
      'info':          'badge-info',
    };
    return classes[category] || 'badge-info';
  }
  // ── Helpers priorité Gemini ───────────────────────────────

  getPriorityClass(priority: string): string {
    const classes: any = {
      'haute':   'priority-haute',
      'moyenne': 'priority-moyenne',
      'basse':   'priority-basse',
    };
    return classes[priority] || 'priority-moyenne';
  }

  // ── Helpers score priorité global ────────────────────────
  getPriorityScoreClass(score: number): string {
    if (score >= 80) return 'priority-critical';
    if (score >= 60) return 'priority-high';
    if (score >= 40) return 'priority-medium';
    return 'priority-low';
  }

  getPriorityLabel(score: number): string {
    if (score >= 80) return '🔴 Critique';
    if (score >= 60) return '🟠 Haute';
    if (score >= 40) return '🟡 Moyenne';
    return '🟢 Basse';
  }

  // ── Helpers churn ─────────────────────────────────────────
  getChurnClass(risk: string): string {
    const map: any = {
      'Critique': 'churn-critique',
      'Élevé':    'churn-eleve',
      'Moyen':    'churn-moyen',
      'Faible':   'churn-faible'
    };
    return map[risk] || 'churn-faible';
  }
}

