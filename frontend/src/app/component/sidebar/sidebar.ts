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
    { id: 'dashboard', label: 'Dashboard', icon: '📊', route: '/admin' },
    { id: 'reservations', label: 'Réservations', icon: '✈️', badge: 12, route: '/admin' },
    { id: 'users', label: 'Utilisateurs', icon: '👥', route: '/admin' },
    // { id: 'offers', label: 'Offres & Destinations', icon: '🌍', badge: 3, route: '/admin/offers' },
    { id: 'settings', label: 'Paramètres', icon: '⚙️', route: '/admin' },
  ];


  onToggle(): void {
    this.toggleCollapse.emit();
  }

  onNavClick(itemId: string) {
    const item = this.navItems.find(i => i.id === itemId);
    if (!item) return;

    this.activeSection = itemId;
    this.router.navigate([item.route]);
  }
}