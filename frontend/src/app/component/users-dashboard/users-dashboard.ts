// src/app/components/admin/users/users.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// ✅ IMPORTER LES COMPOSANTS
import { StatusBadgeComponent } from '../shared/status-badge/status-badge';
import { ClientCellComponent } from '../shared/client-cell/client-cell';
import { FilterBarComponent } from '../shared/filter-bar/filter-bar';
import { RoleBadgeComponent } from '../shared/role-badge/role-badge';
import { UsersDashboard } from '../../services/users-dashboard';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    // ✅ AJOUTER ICI
    ClientCellComponent,
    FilterBarComponent,
    RoleBadgeComponent,
    StatusBadgeComponent

  ],
  templateUrl: './users-dashboard.html',
  styleUrls: ['./users-dashboard.css']
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  
  filters = ['tous', 'admin', 'agent', 'client'];
  activeFilter = 'tous';
  searchQuery = '';
  
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;
  totalItems = 0;
  
  isLoading = true;

  constructor(private usersService: UsersDashboard) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    const role = this.activeFilter === 'tous' ? '' : this.activeFilter;
    
    this.usersService.getAll(this.currentPage, this.pageSize, this.searchQuery, role)
      .subscribe({
        next: (response) => {
          this.users = response.data;
          this.totalPages = response.pages;
          this.totalItems = response.total;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Erreur:', err);
          this.isLoading = false;
        }
      });
  }

  onFilterChange(filter: string): void {
    this.activeFilter = filter;
    this.currentPage = 1;
    this.loadUsers();
  }

  onSearch(): void {
    this.currentPage = 1;
    this.loadUsers();
  }

  getInitials(name: string): string {
    if (!name) return '';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
  }

  getAvatarColor(role: string): string {
    const colorMap: any = {
      'admin': '#ef4444',
      'agent': '#f59e0b',
      'client': '#6366f1'
    };
    return colorMap[role] || '#6366f1';
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadUsers();
    }
  }
}