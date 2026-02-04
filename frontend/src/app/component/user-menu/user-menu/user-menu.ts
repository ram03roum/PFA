import { inject } from '@angular/core';
import { AuthService } from '../../../services/auth';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-menu',
  standalone: true,   // OBLIGATOIRE
  templateUrl: './user-menu.html',
  styleUrls: ['./user-menu.css'],
  imports: [CommonModule]
})
export class UserMenuComponent {
  // On injecte le service d'auth
  auth = inject(AuthService);

  isOpen = false;

  toggleMenu() { this.isOpen = !this.isOpen; }

  logout() {
    this.auth.logout();
    this.isOpen = false;
  }
}