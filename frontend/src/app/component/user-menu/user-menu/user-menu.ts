import { inject } from '@angular/core';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/AuthService';

@Component({
  selector: 'app-user-menu',
  standalone: true,   // OBLIGATOIRE
  templateUrl: './user-menu.html',
  styleUrls: ['./user-menu.css'],
  imports: [CommonModule]
})
export class UserMenuComponent {
  // On injecte le service d'auth
  AuthService = inject(AuthService);

  isOpen = false;
  isLoggedIn$ = this.AuthService.isAuthenticated();


  toggleMenu() { this.isOpen = !this.isOpen; }

  logout() {
    this.AuthService.logout();
    this.isOpen = false;
    localStorage.removeItem('token'); // Supprime le jeton
    localStorage.removeItem('favorites'); // Supprime l'ancienne liste de favoris
    // this.favorites = new Set(); // Vide la variable dans votre composant
    // this.router.navigate(['/login']);
  }
}