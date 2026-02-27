import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { RecommendationService } from './recommenadation.service.ts';


@Injectable({
  providedIn: 'root'
})
export class FavoritesService {

  private apiUrl = 'http://localhost:5000/favorites'; // Votre URL Flask

  // ✅ Stocke les IDs des favoris uniquement
  private favoritesSubject = new BehaviorSubject<Set<number>>(new Set());
  favorites$ = this.favoritesSubject.asObservable();

  constructor(private http: HttpClient,
    private recommendationService: RecommendationService
  ) {
    console.log('FavoritesService initialisé');
    console.log(this.favoritesSubject);
    // ✅ On recharge les favoris du localStorage immédiatement
    const saved = localStorage.getItem('favorites');
    if (saved) {
      const ids = new Set<number>(JSON.parse(saved));
      this.favoritesSubject = new BehaviorSubject<Set<number>>(ids);
    } else {
      this.favoritesSubject = new BehaviorSubject<Set<number>>(new Set());
    }
    this.favorites$ = this.favoritesSubject.asObservable();
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
    // On extrait les IDs. Attention : vérifie si ton backend renvoie [1, 2] 
    // ou [{id: 1}, {id: 2}]. Si c'est des objets, utilise .map(f => f.id)
    const ids = new Set<number>(data.map(f => typeof f === 'number' ? f : f.destination_id || f.id));

    this.favoritesSubject.next(ids);

    // Optionnel : sauvegarde en local pour le hors-ligne
    localStorage.setItem('favorites', JSON.stringify(Array.from(ids)));
  }
  /**
   * Bascule l'état favori d'une destination
   */
  toggleFavorite(destinationId: number, token: string): void {
    const currentFavorites = this.favoritesSubject.value;
    const newFavorites = new Set(currentFavorites);
    const isRemoving = newFavorites.has(destinationId);

    if (isRemoving) {
      newFavorites.delete(destinationId);
    } else {
      newFavorites.add(destinationId);
    }

    this.favoritesSubject.next(newFavorites);

    if (isRemoving) {
      this.removeFavorite(destinationId, token).subscribe({
        next: () => {
          // ← ajoute cette ligne
          this.recommendationService.logInteraction(token, destinationId, 'cancel');
        },
        error: (err) => {
          const rollbackSet = new Set(this.favoritesSubject.value);
          rollbackSet.add(destinationId);
          this.favoritesSubject.next(rollbackSet);
        }
      });
    } else {
      this.addFavorite(destinationId, token).subscribe({
        next: () => {
          // ← ajoute cette ligne
          this.recommendationService.logInteraction(token, destinationId, 'favorite');
        },
        error: (err) => {
          const rollbackSet = new Set(this.favoritesSubject.value);
          rollbackSet.delete(destinationId);
          this.favoritesSubject.next(rollbackSet);
        }
      });
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

  // /**
  //  * Vide tous les favoris
  //  */
  // // --- Supprimer tous les favoris ---
  clearFavorites(): void {
    this.favoritesSubject.next(new Set()); // ✅ vide le Set en mémoire
    localStorage.removeItem('favorites');  // ✅ vide le localStorage
  }
}
