// src/app/components/admin/layout/admin-layout.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from '../../component/sidebar/sidebar';
import { NavbarComponent } from '../../component/navbar/navbar';
import { DashboardComponent } from '../../component/dashboard/dashboard';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, NavbarComponent,DashboardComponent],
  templateUrl: './admindash.html',
  styleUrls: ['./admindash.css']
})
export class AdminLayoutComponent {
  sidebarCollapsed = false;
  activeSection = 'dashboard';
  pageTitle = 'Dashboard';
  pageIcon = '📊';

  navItems: any = {
    dashboard: { title: 'Dashboard', icon: '📊' },
    reservations: { title: 'Réservations', icon: '✈️' },
    users: { title: 'Utilisateurs', icon: '👥' },
    // offers: { title: 'Offres & Destinations', icon: '🌍' },
    // documents: { title: 'Documents', icon: '📄' },
    // support: { title: 'Support', icon: '💬' },
    settings: { title: 'Paramètres', icon: '⚙️' },
  };

  onSectionChange(section: string): void {
    this.activeSection = section;
    const item = this.navItems[section];
    if (item) {
      this.pageTitle = item.title;
      this.pageIcon = item.icon;
    }
  }

  onToggleCollapse(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}