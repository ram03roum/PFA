import { inject } from '@angular/core';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/AuthService';
import { Router, RouterModule } from '@angular/router';
import { FavoritesService } from '../../../services/favorites.service';

@Component({
  selector: 'app-user-menu',
  standalone: true,   // OBLIGATOIRE
  templateUrl: './user-menu.html',
  styleUrls: ['./user-menu.css'],
  imports: [CommonModule, RouterModule]
})
export class UserMenuComponent {
  // On injecte le service d'auth
  AuthService = inject(AuthService);

  isOpen = false;
  isLoggedIn$ = this.AuthService.isAuthenticated();

  constructor(
    private favoritesService: FavoritesService,
    private router: Router
  ) { }
  toggleMenu() { this.isOpen = !this.isOpen; }

  logout() {
    this.AuthService.logout();
    this.isOpen = false;
    localStorage.removeItem('token'); // Supprime le jeton
    localStorage.removeItem('favorites'); // Supprime l'ancienne liste de favoris
    // ✅ Vider le BehaviorSubject du FavoritesService
    this.favoritesService.clearFavorites();

    this.router.navigate(['/login']);
  }
}