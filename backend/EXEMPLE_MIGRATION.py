"""
FICHIER DE MIGRATION: À créer dans backend/migrations/versions/

Créez un nouveau fichier avec ce contenu:
XXXX_add_email_queue_system.py

(remplacez XXXX par l'ID de version approprié)

Avant de l'utiliser:
1. cd backend
2. flask db migrate -m "add email queue system"
3. flask db upgrade
"""

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import mysql

# revision identifiers, used by Alembic.
revision = 'xxxxx'  # À générer automatiquement
down_revision = 'exxxxx'  # À adapter selon votre historique
branch_labels = None
depends_on = None


def upgrade():
    # Créer la table email_queues
    op.create_table(
        'email_queues',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('to_email', sa.String(length=255), nullable=False),
        sa.Column('subject', sa.String(length=255), nullable=False),
        sa.Column('body', sa.Text(), nullable=False),
        sa.Column('template_name', sa.String(length=100), nullable=True),
        sa.Column('priority', sa.Integer(), nullable=False, server_default='2'),
        sa.Column('type', sa.String(length=50), nullable=True),
        sa.Column('user_id', sa.Integer(), nullable=True),
        sa.Column('status', sa.String(length=20), nullable=False, server_default='pending'),
        sa.Column('attempt_count', sa.Integer(), nullable=False, server_default='0'),
        sa.Column('max_attempts', sa.Integer(), nullable=False, server_default='3'),
        sa.Column('last_attempt_at', sa.DateTime(), nullable=True),
        sa.Column('error_message', sa.Text(), nullable=True),
        sa.Column('scheduled_for', sa.DateTime(), nullable=False),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.Column('sent_at', sa.DateTime(), nullable=True),
        sa.Column('metadata', mysql.JSON(), nullable=True),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    
    # Créer les index
    op.create_index('ix_email_queues_to_email', 'email_queues', ['to_email'])
    op.create_index('ix_email_queues_priority', 'email_queues', ['priority'])
    op.create_index('ix_email_queues_status', 'email_queues', ['status'])
    op.create_index('ix_email_queues_scheduled_for', 'email_queues', ['scheduled_for'])
    op.create_index('ix_email_queues_created_at', 'email_queues', ['created_at'])
    op.create_index('ix_email_queues_user_id', 'email_queues', ['user_id'])
    op.create_index('ix_email_queues_type', 'email_queues', ['type'])
    
    # Créer la table email_logs
    op.create_table(
        'email_logs',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('email_queue_id', sa.Integer(), nullable=True),
        sa.Column('to_email', sa.String(length=255), nullable=False),
        sa.Column('subject', sa.String(length=255), nullable=False),
        sa.Column('priority', sa.Integer(), nullable=True),
        sa.Column('email_type', sa.String(length=50), nullable=True),
        sa.Column('status', sa.String(length=20), nullable=True),
        sa.Column('status_code', sa.String(length=50), nullable=True),
        sa.Column('error_message', sa.Text(), nullable=True),
        sa.Column('attempt_number', sa.Integer(), nullable=True),
        sa.Column('processed_at', sa.DateTime(), nullable=False),
        sa.Column('mail_provider', sa.String(length=50), nullable=False, server_default='gmail'),
        sa.Column('message_id', sa.String(length=255), nullable=True),
        sa.ForeignKeyConstraint(['email_queue_id'], ['email_queues.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    
    # Créer les index
    op.create_index('ix_email_logs_email_queue_id', 'email_logs', ['email_queue_id'])
    op.create_index('ix_email_logs_to_email', 'email_logs', ['to_email'])
    op.create_index('ix_email_logs_email_type', 'email_logs', ['email_type'])
    op.create_index('ix_email_logs_status', 'email_logs', ['status'])
    op.create_index('ix_email_logs_processed_at', 'email_logs', ['processed_at'])


def downgrade():
    op.drop_index('ix_email_logs_processed_at', table_name='email_logs')
    op.drop_index('ix_email_logs_status', table_name='email_logs')
    op.drop_index('ix_email_logs_email_type', table_name='email_logs')
    op.drop_index('ix_email_logs_to_email', table_name='email_logs')
    op.drop_index('ix_email_logs_email_queue_id', table_name='email_logs')
    op.drop_table('email_logs')
    
    op.drop_index('ix_email_queues_type', table_name='email_queues')
    op.drop_index('ix_email_queues_user_id', table_name='email_queues')
    op.drop_index('ix_email_queues_created_at', table_name='email_queues')
    op.drop_index('ix_email_queues_scheduled_for', table_name='email_queues')
    op.drop_index('ix_email_queues_status', table_name='email_queues')
    op.drop_index('ix_email_queues_priority', table_name='email_queues')
    op.drop_index('ix_email_queues_to_email', table_name='email_queues')
    op.drop_table('email_queues')
