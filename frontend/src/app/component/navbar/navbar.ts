// // src/app/components/admin/navbar/navbar.component.ts
// import { Component, Input } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { AuthService } from '../../services/AuthService';
// @Component({
//   selector: 'app-navbar',
//   standalone: true,
//   imports: [CommonModule, FormsModule],
//   templateUrl: './navbar.html',
//   styleUrls: ['./navbar.css']
// })
// export class NavbarComponent {
//   @Input() pageTitle = 'Dashboard';
//   @Input() pageIcon = '📊';

//   searchQuery = '';

//   constructor(public authService: AuthService) { }

//   onSearch(): void {
//     console.log('Recherche:', this.searchQuery);
//   }

//   onLogout(): void {
//     if (confirm('Voulez-vous vraiment vous déconnecter ?')) {
//       this.authService.logout();
//     }
//   }
  
//   getInitials(name: string): string {
//     if (!name) return '';
//     const parts = name.trim().split(' '); 
//     return parts.map(p => p[0].toUpperCase()).join(''); 
//   }
  
// }

import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/AuthService';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  @Input() pageTitle = 'Dashboard';
  @Input() pageIcon = '📊';

  searchQuery = '';
  unreadCount = 0;
  showDropdown = false;
  messages: any[] = [];

  private apiUrl = 'http://localhost:5000/api';
  private pollSub!: Subscription;

  constructor(public authService: AuthService, private http: HttpClient) {}

  ngOnInit() {
    this.fetchCount();
    // Polling toutes les 30 secondes pour chaque 30 seconde appel a flask 
    this.pollSub = interval(30000).pipe(
      switchMap(() => this.http.get<any>(`${this.apiUrl}/admin/notifications/count`))
    ).subscribe(res => this.unreadCount = res.unread_count);
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  fetchCount() {
    this.http.get<any>(`${this.apiUrl}/admin/notifications/count`)
      .subscribe(res => this.unreadCount = res.unread_count);
  }

  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
    if (this.showDropdown) {
      this.http.get<any>(`${this.apiUrl}/admin/messages`)
        .subscribe(res => this.messages = res.messages.slice(0, 5));
    }
  }

  markAllRead() {
    this.http.put(`${this.apiUrl}/admin/messages/read-all`, {})
      .subscribe(() => {
        this.unreadCount = 0;
        this.messages.forEach(m => m.is_read = true);
      });
  }

  getCategoryBadge(category: string): string {
    const badges: any = {
      'urgent':       '🔴 Urgent',
      'reclamation':  '🟠 Réclamation',
      'demande_devis':'🟢 Devis',
      'info':         '🔵 Info'
    };
    return badges[category] || '🔵 Info';
  }

  closeDropdown() {
    this.showDropdown = false;
  }

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