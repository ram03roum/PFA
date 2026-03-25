import { Component } from '@angular/core';
import { AiService } from '../../services/ai.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ai-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-assistant.html',
  styleUrls: ['./ai-assistant.css']
})
export class AiAssistantComponent {
  customerName = '';
  subject = '';
  context = '';
  emailResult = '';
  conversation = '';
  summaryResult = '';
  followupResult = '';
  loadingEmail = false;
  loadingSummary = false;
  loadingFollowup = false;
  errorMessage = '';

  constructor(private aiService: AiService) {}

  generateEmail() {
    this.errorMessage = '';
    this.emailResult = '';
    this.loadingEmail = true;

    this.aiService.generateEmail({
      customer_name: this.customerName || 'client',
      subject: this.subject || 'proposition de voyage',
      context: this.context || 'Présenter une offre adaptée'
    }).subscribe({
      next: data => {
        this.emailResult = data.email || 'Aucun contenu généré';
        this.loadingEmail = false;
      },
      error: err => {
        this.errorMessage = 'Erreur génération email : ' + (err?.error?.error ?? err.message ?? err);
        this.loadingEmail = false;
      }
    });
  }

  summarizeConversation() {
    this.errorMessage = '';
    this.summaryResult = '';
    this.loadingSummary = true;
    this.aiService.summarize(this.conversation || '').subscribe({
      next: data => {
        this.summaryResult = data.summary || 'Aucun résumé';
        this.loadingSummary = false;
      },
      error: err => {
        this.errorMessage = 'Erreur résumé : ' + (err?.error?.error ?? err.message ?? err);
        this.loadingSummary = false;
      }
    });
  }

  generateFollowup() {
    this.errorMessage = '';
    this.followupResult = '';
    this.loadingFollowup = true;
    this.aiService.followup(this.conversation || '').subscribe({
      next: data => {
        this.followupResult = data.followup || 'Aucune suggestion';
        this.loadingFollowup = false;
      },
      error: err => {
        this.errorMessage = 'Erreur relance : ' + (err?.error?.error ?? err.message ?? err);
        this.loadingFollowup = false;
      }
    });
  }
}
