import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AiService {
  private apiUrl = 'http://127.0.0.1:5000/ai';

  constructor(private http: HttpClient) {}

  generateEmail(payload: { customer_name: string; subject: string; context: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/email`, payload);
  }

  summarize(conversation: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/summary`, { conversation });
  }

  followup(conversation: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/followup`, { conversation });
  }
}
