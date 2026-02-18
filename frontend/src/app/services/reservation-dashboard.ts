// src/app/services/reservations.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Interface pour une réservation (vue admin)
 */
export interface ReservationAdmin {
  id: number;
  user: string;
  destination: string;
  dates: string;
  amount: number;
  status: string;
  payment_status?: string;
  created_at?: string;
}

/**
 * Interface pour la réponse paginée
 */
export interface ReservationsResponse {
  data: ReservationAdmin[];
  total: number;
  page: number;
  pages: number;
}

/**
 * Service pour gérer les réservations côté ADMIN
 * Utilisé uniquement dans le dashboard admin
 */
@Injectable({ providedIn: 'root' })
export class ReservationsDashboard {
  private apiUrl = 'http://127.0.0.1:5000/reservations';

  constructor(private http: HttpClient) {}


  /**
   * Récupère TOUTES les réservations (ADMIN uniquement)
   * @param page - Numéro de page (défaut: 1)
   * @param limit - Nombre d'éléments par page (défaut: 10)
   * @param search - Recherche par nom client ou destination
   * @param status - Filtre par statut ('tous', 'confirmée', 'en attente', 'annulée')
   * @returns Observable avec les réservations paginées
   */

  getAll(page: number = 1, limit: number = 10, search: string = '', status: string = ''): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('limit', limit.toString());
    
    if (search) params = params.set('search', search);
    if (status) params = params.set('status', status);
    
    return this.http.get<ReservationsResponse>(this.apiUrl, { params });
  }

  /**
   * Récupère une réservation spécifique par ID
   * ADMIN peut voir n'importe quelle réservation
   * @param id - ID de la réservation
   */
  getById(id: number): Observable<ReservationAdmin> {
    return this.http.get<ReservationAdmin>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Met à jour le statut d'une réservation
   * @param id - ID de la réservation
   * @param status - Nouveau statut Nouveau statut ('en attente', 'confirmée', 'annulée')
   */
  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/status`, { status });
  }

  /**
   * Annule une réservation (ADMIN)
   * @param id - ID de la réservation à annuler
   */
  cancel(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Confirme une réservation (ADMIN)
   * Raccourci pour updateStatus avec statut 'confirmée'
   * @param id - ID de la réservation
   */
  confirm(id: number): Observable<any> {
    return this.updateStatus(id, 'confirmée');
  }

  /**
   * Refuse une réservation (ADMIN)
   * Raccourci pour updateStatus avec statut 'annulée'
   * @param id - ID de la réservation
   */
  reject(id: number): Observable<any> {
    return this.updateStatus(id, 'annulée');
  }

  /**
   * Statistiques globales des réservations (pour dashboard)
   */
  getStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`);
  }

  /**
   * Export des réservations en CSV (optionnel)
   * @param filters - Filtres à appliquer à l'export
   */
  exportToCSV(filters?: any): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export`, {
      params: filters,
      responseType: 'blob'
    });
  }

  getTotalItems(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/total`);
  } 

}