// src/app/components/admin/reservations/reservations.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatusBadgeComponent } from '../shared/status-badge/status-badge';
import { ClientCellComponent } from '../shared/client-cell/client-cell';
import { FilterBarComponent } from '../shared/filter-bar/filter-bar';
import { ReservationsDashboard } from '../../services/reservation-dashboard';

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    StatusBadgeComponent, 
    ClientCellComponent, 
    FilterBarComponent
  ],
  templateUrl: './reservation-dashboard.html',
  styleUrls: ['./reservation-dashboard.css']
})
export class ReservationDashboardComponent implements OnInit {
  reservations: any[] = [];
  filteredReservations: any[] = [];
  
  // Filters
  filters = ['tous', 'confirmée', 'en attente', 'annulée'];
  activeFilter = 'tous';
  searchQuery = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;
  totalItems = 0;
  
  isLoading = true;

  constructor(private reservationsService: ReservationsDashboard) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  /**
   * Charge les réservations depuis le backend
   */

  loadReservations(): void {
    this.isLoading = true;
    
    const status = this.activeFilter === 'tous' ? '' : this.activeFilter;
        // ✅ APPEL AU SERVICE ADMIN POUR RÉCUPÉRER LES RÉSERVATIONS AVEC FILTRES ET PAGINATION
    this.reservationsService.getAll(
      this.currentPage, 
      this.pageSize, 
      this.searchQuery, 
      status
    ).subscribe({
      next: (response) => {
        console.log('✅ Réservations reçues:', response);
        this.reservations = response.data;
        this.filteredReservations = response.data;
        this.totalPages = response.pages;
        this.totalItems = response.total;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement réservations:', err);
        this.isLoading = false;
      }
    });
  }

  onFilterChange(filter: string): void {
    this.activeFilter = filter;
    this.currentPage = 1;
    this.loadReservations();
  }

  onSearch(): void {
    this.currentPage = 1;
    this.loadReservations();
  }

  confirmReservation(reservation: any): void {
    if (confirm(`Confirmer la réservation de ${reservation.client} ?`)) {
      this.reservationsService.updateStatus(reservation.id, 'confirmée').subscribe({
        next: () => {
          this.loadReservations();
        },
        error: (err) => console.error('Erreur confirmation:', err)
      });
    }
  }

  cancelReservation(reservation: any): void {
    if (confirm(`Annuler la réservation de ${reservation.client} ?`)) {
      this.reservationsService.cancel(reservation.id).subscribe({
        next: () => {
          this.loadReservations();
        },
        error: (err) => console.error('Erreur annulation:', err)
      });
    }
  }

  getInitials(name: string): string {
    if (!name) return '';
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadReservations();
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadReservations();
    }
  }
}