import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../../services/AuthService';

interface NavItem {
  id: string;
  label: string;
  icon: string;
  badge?: number;
  route: string;
  internal?: boolean; // ← AJOUT: pour marquer les sections internes
}

interface User {
  id: number;
  name: string; 
  email: string;
  role: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Input() activeSection = 'admin';
  @Output() sectionChange = new EventEmitter<string>();
  @Output() toggleCollapse = new EventEmitter<void>();

  constructor(private router: Router,
    public authService: AuthService
  ) { }

  navItems: NavItem[] = [
    { id: 'home', label: 'Home', icon: '🏠', route: '/home' },
    { id: 'dashboard', label: 'Dashboard', icon: '📊', route: '/admin', internal: true },
    { id: 'reservations', label: 'Réservations', icon: '✈️', badge: 12, route: '/admin', internal: true },
    //  Parce que ces sections doivent rester dans /admin et ne pas créer de nouvelles routes.
    { id: 'users', label: 'Utilisateurs', icon: '👥', route: '/admin/users', internal: true },
    { id: 'messages', label: 'Messages', icon: '✉️', route: '/admin', internal: true },
    { id: 'settings', label: 'Paramètres', icon: '⚙️', route: '/admin/settings' },
  ];


  onToggle(): void {
    this.toggleCollapse.emit();
  }

  onNavClick(itemId: string) {
    const item = this.navItems.find(i => i.id === itemId);
    if (!item) return;

    this.activeSection = itemId;
    this.sectionChange.emit(itemId);
    
    // Only navigate if it's not an internal section
    if (!item.internal) {
      this.router.navigate([item.route]);
    }
  }

  getInitials(name: string): string {
    if (!name) return '';
    const parts = name.trim().split(' '); 
    return parts.map(p => p[0].toUpperCase()).join(''); 
  }
}

/////////////////////////////////////////////////////////////////////////////////
// Clic sur "Réservations" → onNavClick('reservations')
// sectionChange.emit('reservations') → envoie à admindash.ts
// onSectionChange() dans admindash → met à jour activeSection = 'reservations'
// Dans admindash.html → *ngIf="activeSection === 'reservations'" affiche le composant
// ✅ Pas de navigation d'URL, juste un changement d'affichage!