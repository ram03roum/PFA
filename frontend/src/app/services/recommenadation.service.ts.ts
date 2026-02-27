import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RecommendationService {

  private apiUrl = 'http://localhost:5000';

  constructor(private http: HttpClient) { }

  getRecommendations(token: string, forceRefresh = false): Observable<any> {
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    const params = forceRefresh ? '?refresh=true' : '';
    return this.http.get(`${this.apiUrl}/api/recommendations${params}`, { headers });
  }

  logInteraction(token: string, destinationId: number, action: string): void {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    this.http.post(`${this.apiUrl}/api/interactions`,
      { destination_id: destinationId, action: action },
      { headers }
    ).subscribe();  // fire and forget
  }
}