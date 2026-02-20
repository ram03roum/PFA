import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { DataService } from '../../services/data.service';
import { FavoritesService } from '../../services/favorites.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-favoris',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './favoris.html',
  styleUrls: ['./favoris.css']
})
export class FavorisComponent implements OnInit {
  favoriteDestinations: any[] = []; // ✅ uniquement les favoris
  chargement: boolean = true;

  constructor(
    private dataService: DataService,
    public favoriteService: FavoritesService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('access_token');

      if (!token) {
        this.chargement = false;
        return;
      }

      // 1. Charger toutes les destinations
      this.dataService.getDestinations().subscribe({
        next: (destinations: any[]) => {
          console.log('Destinations chargées', destinations);
          // 2. S'abonner aux IDs favoris du service
          this.favoriteService.favorites$.subscribe(ids => {

            // 3. ✅ Filtrer uniquement les destinations favorites
            this.favoriteDestinations = destinations.filter(d => ids.has(d.id));
            this.chargement = false;
            this.cdr.detectChanges();
          });

          // 4. Charger les favoris depuis le backend
          this.favoriteService.getFavorites(token).subscribe({
            next: (favIds: any) => {
              console.log('Favoris chargés', favIds);
              this.favoriteService.setFavorites(favIds);
            },
            error: (err) => {
              console.error('Erreur chargement favoris', err);
              this.chargement = false;
            }
          });
        },
        error: (err) => {
          console.error('Erreur chargement destinations', err);
          this.chargement = false;
        }
      });
    }
  }

  toggleFavorite(id: number): void {
    const token = localStorage.getItem('access_token') || '';
    this.favoriteService.toggleFavorite(id, token);
    // ✅ Retire immédiatement de la liste affichée
    this.favoriteDestinations = this.favoriteDestinations.filter(d => d.id !== id);
    this.cdr.detectChanges();
  }

  checkIfFavorite(id: number): boolean {
    return this.favoriteService.isFavorite(id);
  }
}