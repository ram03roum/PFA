// src/app/components/admin/navbar/navbar.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/AuthService';
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent {
  @Input() pageTitle = 'Dashboard';
  @Input() pageIcon = '📊';

  searchQuery = '';

  constructor(public authService: AuthService) { }

  onSearch(): void {
    console.log('Recherche:', this.searchQuery);
  }

  onLogout(): void {
    if (confirm('Voulez-vous vraiment vous déconnecter ?')) {
      this.authService.logout();
    }
  }
  
  getInitials(name: string): string {
    if (!name) return '';
    const parts = name.trim().split(' '); 
    return parts.map(p => p[0].toUpperCase()).join(''); 
  }
  
}