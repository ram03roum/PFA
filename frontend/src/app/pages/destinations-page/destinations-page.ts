import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { DataService } from '../../services/data.service';
import { FavoritesService } from '../../services/favorites.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { RecommendationService } from '../../services/recommenadation.service.ts';

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
  isConnected: boolean = false;
  isPersonalized: boolean = false;  // true si destinations personnalisées
  recommendationSource: string = '';     // 'hybrid', 'cache', 'popular'

  constructor(
    private dataService: DataService,
    public favoriteService: FavoritesService,
    private recommendationService: RecommendationService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router,
  ) {
    if (isPlatformBrowser(this.platformId)) {
      this.token = localStorage.getItem('access_token');
      this.isConnected = !!this.token;
    }
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {

      this.favoriteService.favorites$.subscribe(ids => { });

      if (this.token) {
        // ── User connecté ─────────────────────────────────────
        this.favoriteService.getFavorites(this.token).subscribe({
          next: (favIds: any[]) => this.favoriteService.setFavorites(favIds),
          error: (err) => console.error('Erreur favoris', err)
        });

        // Charger destinations personnalisées
        this.chargerRecommandations();

      } else {
        // ── User non connecté ─────────────────────────────────
        this.chargerDestinations();
      }
    }
  }

  // ── Destinations personnalisées (user connecté) ─────────────
  chargerRecommandations(): void {
    this.chargement = true;
    this.recommendationService.getRecommendations(this.token!).subscribe({
      next: (result) => {
        this.destinations = result.data;
        this.isPersonalized = true;
        this.recommendationSource = result.source;
        this.currentPage = 1;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur recommandations:', err);
        // Fallback → toutes les destinations
        this.chargerDestinations();
      }
    });
  }

  // ── Toutes les destinations (user non connecté) ─────────────
  chargerDestinations(): void {
    this.chargement = true;
    this.isPersonalized = false;
    this.dataService.getDestinations().subscribe({
      next: (data) => {
        this.destinations = data;
        this.currentPage = 1;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur destinations:', err);
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── Rafraîchir les recommandations ──────────────────────────
  rafraichirRecommandations(): void {
    this.chargement = true;
    this.recommendationService.getRecommendations(this.token!, true).subscribe({
      next: (result) => {
        this.destinations = result.data;
        this.recommendationSource = result.source;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => this.chargement = false
    });
  }

  // ── Pagination ───────────────────────────────────────────────
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

  // ── Favoris ──────────────────────────────────────────────────
  toggleFavorite(id: number): void {
    const token = localStorage.getItem('access_token') || '';
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }
    this.favoriteService.toggleFavorite(id, token);

    // Log interaction pour le moteur IA
    this.recommendationService.logInteraction(token, id, 'favorite');
  }

  checkIfFavorite(id: number): boolean {
    return this.favoriteService.isFavorite(id);
  }

  trackDestination = (_: number, item: any) => item.id;
}