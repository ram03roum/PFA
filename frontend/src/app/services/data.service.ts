import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DataService {
  private apiUrl = 'http://127.0.0.1:5000/destinations';

  constructor(
    private http: HttpClient

  ) { }

  getDestinations(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
  // data.service.ts
  getDestinationById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(response => {
        console.log("Brut reçu de Flask:", response); // Pour vérifier ce qui sort du serveur

        // Si c'est un tableau, on prend le premier élément
        if (Array.isArray(response) && response.length > 0) {
          return response[0];
        }

        // Si c'est déjà un objet, on le renvoie tel quel
        if (response && typeof response === 'object' && !Array.isArray(response)) {
          return response;
        }

        return null; // Si rien ne correspond
      })
    );
  }
}