import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:5000/api';

  constructor(private http: HttpClient) {}

  // ── En-têtes avec JWT ────────────────────────────────────
  private getHeaders(): any {
    const token = localStorage.getItem('access_token');
    return {
      headers: new HttpHeaders().set('Authorization', `Bearer ${token}`)
    };
  }

  // ───────────────────────────────────────────────────────
  // 📋 MESSAGES
  // ───────────────────────────────────────────────────────

  /**
   * Récupère tous les messages pour l'admin
   */
  getMessages(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/messages`, this.getHeaders());
  }

  /**
   * Récupère les messages prioritisés
   */
  getPrioritizedMessages(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/messages/prioritized`, this.getHeaders());
  }

  /**
   * Marque un message comme lu
   */
  markAsRead(messageId: number): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/admin/messages/${messageId}/read`,
      {},
      this.getHeaders()
    );
  }

  /**
   * Marque tous les messages comme lus
   */
  markAllAsRead(): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/admin/messages/read-all`,
      {},
      this.getHeaders()
    );
  }

  /**
   * Envoie une réponse à un client
   */
  sendReply(messageId: number, reply: string): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/admin/messages/${messageId}/reply`,
      { reply },
      this.getHeaders()
    );
  }

  // ───────────────────────────────────────────────────────
  // 👤 CLIENT PROFILE (CRM)
  // ───────────────────────────────────────────────────────

  /**
   * Récupère le profil CRM complet d'un client
   * Inclut : historique réservations, sentiments, segments, churn risk
   */
  getClientProfile(email: string): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/admin/client-profile/${encodeURIComponent(email)}`,
      this.getHeaders()
    );
  }
}
