import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { forkJoin, Observable } from 'rxjs';
import { AuthService } from './AuthService';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = 'http://127.0.0.1:5000/dashboard'; // Votre route Flask

  constructor(private http: HttpClient) {}
/**
   * Cette méthode combine tous les appels vers Flask en un seul flux
   */
  getDashboardData(): Observable<any> {
    // On ajoute this.getHeaders() à chaque appel pour que Flask reçoive le token
    const headers = this.getHeaders();
    return forkJoin({
      kpis: this.http.get(`${this.apiUrl}/kpis`, headers),
      revenue: this.http.get(`${this.apiUrl}/revenue-monthly`, headers),
      destinations: this.http.get(`${this.apiUrl}/destinations-stats`, headers),
      reservations: this.http.get(`${this.apiUrl}/recent-reservations`, headers),
      logs: this.http.get(`${this.apiUrl}/activity-logs`, headers)
    });
  }
  // On récupère le token pour l'autorisation (JWT)
  private getHeaders() {
    const token = localStorage.getItem('access_token');// Assurez-vous que c'est le même nom que dans le Login
    return {
      headers: new HttpHeaders().set('Authorization', `Bearer ${token}`)
    };
  }

  getKpis(): Observable<any> {
    return this.http.get(`${this.apiUrl}/kpis`, this.getHeaders());
  }

  getMonthlyRevenue(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/revenue-monthly`, this.getHeaders());
  }

  getActivityLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/activity-logs`, this.getHeaders());
  }
  

}