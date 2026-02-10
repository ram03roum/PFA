import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FavoritesService } from '../../services/favorites.service';
import { DataService } from '../../services/data.service';

@Component({
  selector: 'app-destinations',
  standalone: true,
  templateUrl: './destinations.html',
  imports: [CommonModule],
  styleUrls: ['./destinations.css']
})
export class DestinationsComponent implements OnInit {

  destinations: any[] = [];
  favorites = new Set<number>();

  constructor(
    private router: Router,
    private favoriteService: FavoritesService,
    private dataService: DataService
  ) { }

  ngOnInit(): void {
    // Charger les destinations
    this.dataService.getDestinations().subscribe({
      next: (data: any) => {
        if (Array.isArray(data)) {
          this.destinations = data;
        } else if (data && data.destinations) {
          this.destinations = data.destinations;
        }
      },
      error: (err) => console.error("Erreur Backend :", err)
    });

    // Charger les favoris sauvegardés (si vous avez un service)
    // this.favorites = this.favoriteService.getFavorites();
  }

  goToDetail(id: number) {
    this.router.navigate(['/destination', id]);
  }

  toggleFavorite(destinationId: number) {
    // if (this.favorites.has(destinationId)) {
    //   this.favorites.delete(destinationId);
    // } else {
    //   this.favorites.add(destinationId);
    // }

    // Sauvegarder dans le service
    // this.favoriteService.updateFavorites(Array.from(this.favorites));


    const userId = 1; // Testez avec un ID fixe pour l'instant



  }

  checkIfFavorite(destinationId: number): boolean {
    return this.favorites.has(destinationId);
  }
}