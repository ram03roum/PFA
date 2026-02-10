import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class FavoritesService {

  private apiUrl = 'http://localhost:5000/favorites'; // Votre URL Flask

  // ✅ Stocke les IDs des favoris uniquement
  private favoritesSubject = new BehaviorSubject<Set<number>>(new Set());
  favorites$ = this.favoritesSubject.asObservable();

  constructor(private http: HttpClient) {
    console.log('FavoritesService initialisé');

  }


  //appele la focntion de recuperation des donnes depuis la abse de donnes dans le backned
  getFavorites(token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}` // JWT dans le header
    });
    console.log("get marche");
    return this.http.get(this.apiUrl, { headers });
  }



  //ajouter une destination aux favoris
  addFavorite(destinationId: number, token: string) {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    const body = { destination_id: destinationId };

    return this.http.post(this.apiUrl, body, { headers });
  }


  /**
     * Retire une destination des favoris
     */

  removeFavorite(destinationId: number, token: string) {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete(`${this.apiUrl}/${destinationId}`, { headers });
  }




  // 🔹 Mettre à jour les favoris en mémoire après login
  setFavorites(data: any[]) {
    const ids = new Set<number>(data.map(f => f.id)); // récupère uniquement les ids
    this.favoritesSubject.next(ids);
  }
  /**
   * Bascule l'état favori d'une destination
   */
  toggleFavorite(destinationId: number, token: string): void {
    if (this.isFavorite(destinationId)) {
      this.removeFavorite(destinationId, token);
    } else {
      this.addFavorite(destinationId, token);
    }
  }
  /**
   * Vérifie si une destination est favorite
   */
  isFavorite(destinationId: number): boolean {
    return this.favoritesSubject.value.has(destinationId);
  }
  /**
   * Récupère le nombre de favoris
   */
  getFavoritesCount(): number {
    return this.favoritesSubject.value.size;
  }

  /**
   * Vide tous les favoris
   */
  // --- Supprimer tous les favoris ---
  clearFavorites(token: string): Observable<any> {
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    const url = `${this.apiUrl}/clear`; // endpoint DELETE /favorites/clear
    return this.http.delete(url, { headers });
  }
}
