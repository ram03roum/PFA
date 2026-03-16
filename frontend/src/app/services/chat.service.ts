import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:5000/api/chat';

  constructor(private http: HttpClient) {}

  private getHeaders(): { headers: HttpHeaders } {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      })
    };
  }

  /**
   * Récupère toutes les conversations de l'utilisateur
   */
  getConversations(): Observable<any[]> {
    return this.http.get<any>(`${this.apiUrl}/conversations`, this.getHeaders())
      .pipe(map(res => res.data || []));
  }

  /**
   * Créer une nouvelle conversation
   */
  createConversation(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/conversations`, data, this.getHeaders())
      .pipe(map(res => res.data));
  }

  /**
   * Récupère une conversation avec ses messages
   */
  getConversationMessages(conversationId: number): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/conversations/${conversationId}`, 
      this.getHeaders()
    );
  }

  /**
   * Envoie un message et obtient la réponse IA
   */
  sendMessage(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/message`, data, this.getHeaders());
  }

  /**
   * Génère un résumé de la conversation
   */
  getSummary(conversationId: number): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/summary/${conversationId}`, 
      this.getHeaders()
    ).pipe(map(res => res.data));
  }

  /**
   * Ferme une conversation
   */
  closeConversation(conversationId: number): Observable<any> {
    return this.http.delete<any>(
      `${this.apiUrl}/conversations/${conversationId}`, 
      this.getHeaders()
    );
  }
}
