import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { DataService } from '../../services/data.service';
import { FavoritesService } from '../../services/favorites.service'; // 1. Importe le service
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-destinations-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './destinations-page.html',
  styleUrls: ['./destinations-page.css'],
})
export class DestinationsPageComponent implements OnInit {
  destinations: any[] = [];
  chargement: boolean = true;
  currentPage: number = 1;
  itemsPerPage: number = 20;
  favorites = new Set<number>();

  token: string | null = null;
  // if(!this.token) {
  //   console.error("Utilisateur non connecté !");
  //   return;
  // }

  constructor(
    private dataService: DataService,
    private favoriteService: FavoritesService, // 2. Injecte le service ici
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // On ne touche au localStorage que si on est côté client (navigateur)
    if (isPlatformBrowser(this.platformId)) {
      this.token = localStorage.getItem('token');
    }
  }

  ngOnInit(): void {
    // On vérifie si on est sur le navigateur pour éviter les erreurs SSR
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('token');

      if (token) {
        // On récupère les favoris de l'utilisateur actuel via le backend
        this.favoriteService.getFavorites(token).subscribe({
          next: (favIds: number[]) => {
            // On remplace les anciens favoris par ceux de l'ID utilisateur connecté
            this.favorites = new Set(favIds);
            this.saveToLocalStorage();
          },
          error: (err) => console.error('Erreur de chargement des favoris', err)
        });
      } else {
        // Si pas de token, on s'assure que la liste est vide pour le nouvel utilisateur
        this.favorites = new Set();
        localStorage.removeItem('favorites');
      }

      this.chargerDestinations();
    }
  }

  chargerDestinations(): void {
    this.chargement = true;
    this.dataService.getDestinations().subscribe({
      next: (data) => {
        this.destinations = data;
        this.currentPage = 1;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement destinations :', err);
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  // --- PAGINATION ---
  get paginatedDestinations(): any[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.destinations.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages(): number {
    return Math.ceil(this.destinations.length / this.itemsPerPage);
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  // --- GESTION DES FAVORIS (Lien avec le Backend) ---
  // if(token:) {
  //   console.error("Utilisateur non connecté !");
  //   return;
  // }

  toggleFavorite(destinationId: number): void {

    if (typeof window !== 'undefined' && window.localStorage) {
      const token = localStorage.getItem('token') || '';


      if (this.favorites.has(destinationId)) {
        // Cas : Retrait du favori
        this.favoriteService.removeFavorite(destinationId, token).subscribe({
          next: () => {
            this.favorites.delete(destinationId);
            console.log(`Retiré de la BDD : ${destinationId}`);
          },
          error: (err) => console.error('Erreur suppression BDD', err)
        });
      } else {
        // Cas : Ajout du favori
        this.favoriteService.addFavorite(destinationId, token).subscribe({
          next: (response) => {
            this.favorites.add(destinationId);
            this.saveToLocalStorage();
            console.log('Ajouté à la BDD !', response);
          },
          error: (err) => console.error('Erreur ajout BDD', err)
        });
      }
    }
  }

  private saveToLocalStorage(): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('favorites', JSON.stringify(Array.from(this.favorites)));
    }
  }

  checkIfFavorite(destinationId: number): boolean {
    return this.favorites.has(destinationId);
  }

  trackDestination = (_: number, item: any) => item.id;
}