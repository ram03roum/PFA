// services/reservation.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Reservation {
  id?: number;
  destination_id: number;
  check_in: string;
  check_out: string;
  total_amount: number;
  notes?: string;
  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = 'http://localhost:5000/client'; // Assurez-vous que c'est la bonne URL pour votre backend Flask

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('access_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  createReservation(reservation: Reservation): Observable<any> {
    return this.http.post(`${this.apiUrl}/reservations`, reservation, { headers: this.getHeaders() });
  }
  addActivityLog(action: string, entityType: string, entityId: number, details: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/logs`, {
      action, 
      entity_type: entityType,
      entity_id: entityId,
      details
    }, { headers: this.getHeaders() });
  }
  getReservations(): Observable<any> {
    return this.http.get(`${this.apiUrl}/reservations`, { headers: this.getHeaders() });
  }

  getReservation(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/reservations/${id}`, { headers: this.getHeaders() });
  }

  updateReservation(id: number, reservation: Partial<Reservation>): Observable<any> {
    return this.http.put(`${this.apiUrl}/reservations/${id}`, reservation, { headers: this.getHeaders() });
  }

  deleteReservation(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/reservations/${id}/cancel`, { headers: this.getHeaders() });
  }

  calculatePrice(destinationId: number, checkIn: string, checkOut: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/calculate-price`, {
      destination_id: destinationId,
      check_in: checkIn,
      check_out: checkOut
    }, { headers: this.getHeaders() });
  }
}