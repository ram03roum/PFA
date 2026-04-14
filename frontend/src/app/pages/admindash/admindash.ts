// // src/app/components/admin/layout/admin-layout.component.ts
// import { Component } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { RouterModule } from '@angular/router';
// import { SidebarComponent } from '../../component/sidebar/sidebar';
// import { NavbarComponent } from '../../component/navbar/navbar';
// import { DashboardComponent } from '../../component/dashboard/dashboard';
// import { ReservationDashboardComponent } from '../../component/reservation-dashboard/reservation-dashboard';
// import { UsersComponent } from '../../component/users-dashboard/users-dashboard';
// @Component({
//   selector: 'app-admin-layout',
//   standalone: true,
//   imports: [CommonModule, RouterModule, SidebarComponent, NavbarComponent, DashboardComponent, UsersComponent ,ReservationDashboardComponent],
//   templateUrl: './admindash.html',
//   styleUrls: ['./admindash.css']
// })
// export class AdminLayoutComponent {
//   sidebarCollapsed = false;
//   activeSection = 'dashboard';
//   pageTitle = 'Dashboard';
//   pageIcon = '📊';

//   navItems: any = {
//     dashboard: { title: 'Dashboard', icon: '📊' },
//     reservations: { title: 'Reservations', icon: '✈️' },
//     users: { title: 'Utilisateurs', icon: '👥' },
//     // offers: { title: 'Offres & Destinations', icon: '🌍' },
//     // documents: { title: 'Documents', icon: '📄' },
//     // support: { title: 'Support', icon: '💬' },
//     settings: { title: 'Paramètres', icon: '⚙️' },
//   };

//   onSectionChange(section: string): void {
//     this.activeSection = section;
//     const item = this.navItems[section];
//     if (item) {
//       this.pageTitle = item.title;
//       this.pageIcon = item.icon;
//     }
//   }

//   onToggleCollapse(): void {
//     this.sidebarCollapsed = !this.sidebarCollapsed;
//   }
// }

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { SidebarComponent } from '../../component/sidebar/sidebar';
import { NavbarComponent } from '../../component/navbar/navbar';
import { DashboardComponent } from '../../component/dashboard/dashboard';
import { ReservationDashboardComponent } from '../../component/reservation-dashboard/reservation-dashboard';
import { UsersComponent } from '../../component/users-dashboard/users-dashboard';
import { MessagesComponent } from '../../component/messages/messages';  // ← ajouter

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    SidebarComponent, NavbarComponent,
    DashboardComponent, UsersComponent,
    ReservationDashboardComponent,
    MessagesComponent  // ← ajouter
  ],
  templateUrl: './admindash.html',
  styleUrls: ['./admindash.css']
})
export class AdminLayoutComponent implements OnInit {
  sidebarCollapsed = false;
  activeSection = 'dashboard';
  pageTitle = 'Dashboard';
  pageIcon = '📊';
  unreadCount = 0;

  private apiUrl = 'http://localhost:5000/api';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchUnreadCount();
  }

  fetchUnreadCount() {
    this.http.get<any>(`${this.apiUrl}/admin/notifications/count`)
      .subscribe(res => {
        this.unreadCount = res.unread_count;
        // Met à jour le badge dans la sidebar
        const msgItem = this.navItemsConfig['messages'];
        if (msgItem) msgItem.badge = this.unreadCount;
      });
  }

  navItemsConfig: any = {
    dashboard:    { title: 'Dashboard',    icon: '📊' },
    reservations: { title: 'Réservations', icon: '✈️' },
    users:        { title: 'Utilisateurs', icon: '👥' },
    messages:     { title: 'Messages',     icon: '✉️' },  // ← ajouter
    settings:     { title: 'Paramètres',   icon: '⚙️' },
  };

  onSectionChange(section: string): void {
    this.activeSection = section;
    const item = this.navItemsConfig[section];
    if (item) {
      this.pageTitle = item.title;
      this.pageIcon = item.icon;
    }
    // Quand l'admin ouvre Messages, on rafraîchit le compteur
    if (section === 'messages') {
      this.fetchUnreadCount();
    }
  }

  onToggleCollapse(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}