import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { DataService } from '../../services/data.service';
import { FavoritesService } from '../../services/favorites.service'; // 1. Importe le service
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
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
  // ✅ On déclare la variable pour que le HTML puisse la voir
  // On lui assigne l'observable qui vient du service
  token: string | null = null;


  constructor(
    private dataService: DataService,
    public favoriteService: FavoritesService, // 2. Injecte le service ici
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router, // ✅ ajoute juste cette ligne

  ) {

    // On ne touche au localStorage que si on est côté client (navigateur)
    if (isPlatformBrowser(this.platformId)) {
      this.token = localStorage.getItem('token');
    }
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('access_token'); // Vérifie bien si c'est 'token' ou 'access_token'
      // console.log('Valeur du token récupérée :'); // <--- Ajoute ça ICI

      // console.log('Valeur du token récupérée :', token); // <--- Ajoute ça ICI
      // 1. On s'abonne au flux du service. 
      // Dès que le service change (clic ou chargement), ce code s'exécute TOUT SEUL.
      this.favoriteService.favorites$.subscribe(ids => {
        // On ne fait que lire ici, on ne modifie rien manuellement
        // console.log('Mise à jour visuelle des favoris reçue');
      });

      if (token) {
        // 2. On demande au serveur les favoris
        this.favoriteService.getFavorites(token).subscribe({
          next: (favIds: any[]) => {
            // On envoie les données au service pour qu'il remplisse son BehaviorSubject
            this.favoriteService.setFavorites(favIds);
            console.log('Favoris chargés depuis le serveur :', favIds);
          },
          error: (err) => console.error('Erreur de chargement', err)
        });
      } else {
        // 3. Si pas de token, on vide le service
        this.favoriteService.setFavorites([]);
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

  toggleFavorite(id: number): void {
    const token = localStorage.getItem('access_token') || '';
    // ❌ Non connecté → redirection login
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }
    this.favoriteService.toggleFavorite(id, token);
  }


  // Ta fonction de vérification dans le HTML doit maintenant appeler le SERVICE
  checkIfFavorite(id: number): boolean {
    return this.favoriteService.isFavorite(id);
  }

  trackDestination = (_: number, item: any) => item.id;
}
