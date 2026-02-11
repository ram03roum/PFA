// src/app/components/admin/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../services/dashboard.service';
import { finalize } from 'rxjs/internal/operators/finalize';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  kpis: any = {};
  kpisArray: any[] = [];
  monthlyStats: any[] = [];
  destinationsStats: any[] = [];
  activityLogs: any[] = [];
  recentReservations: any[] = [];
  isLoading = true;

  constructor(private dashboardService: DashboardService

  ) { }

  // ngOnInit(): void {
  //   this.loadDashboardData();
  // }
  ngOnInit(): void {
    // console.log('1. DEBUT - isLoading =', this.isLoading);
    this.isLoading = true;
    this.dashboardService.getDashboardData().pipe(
      finalize(() => {
        // console.log(this.isLoading)
        this.isLoading = false;
        // S'exécutera TOUJOURS (Succès ou Erreur)
        // console.log(this.isLoading)
      })
    ).subscribe({
      next: (data) => {
        console.log('2. DATA REÇUE:', data);
        // console.log(data);
        // On transforme les données Flask pour le format de votre HTML
        this.formatKpis(data.kpis);
        this.monthlyStats = data.revenue;
        this.destinationsStats = data.destinations;
        this.recentReservations = data.reservations;
        this.activityLogs = data.logs;

      },
      error: (err) => {
        console.error("Erreur Backend Flask:", err);
        // Le finalize s'occupe déjà du isLoading = false
      }
    });
  }
  formatKpis(kpis: any) {
    this.kpisArray = [
      { title: 'Réservations', value: kpis.totalReservations, icon: '✈️', bg: '#eef2ff' },
      { title: 'Revenus', value: kpis.totalRevenue + ' TND', icon: '💰', bg: '#ecfdf5' },
      { title: 'Clients Fidèles', value: kpis.loyalClients, icon: '⭐', bg: '#f5f3ff' },
      { title: 'Annulations', value: kpis.cancellationRate + '%', icon: '📉', bg: '#fef2f2' }
    ];
  }
  loadDashboardData(): void {
    this.dashboardService.getKpis().subscribe({
      next: (data) => {
        this.kpis = data;
        this.prepareKPIsArray();
      },
      error: (err) => console.error('Erreur KPIs:', err)
    });

    this.dashboardService.getMonthlyRevenue().subscribe({
      next: (data) => {
        this.monthlyStats = data;
      },
      error: (err) => console.error('Erreur stats:', err)
    });

    // this.dashboardService.getDestinationsStats().subscribe({
    //   next: (data) => {
    //     this.destinationsStats = data;
    //     this.isLoading = false;
    //   },
    //   error: (err) => console.error('Erreur destinations:', err)
    // });

    this.dashboardService.getActivityLogs().subscribe({
      next: (data) => {
        this.activityLogs = data;
      },
      error: (err) => console.error('Erreur logs:', err)
    });

    // this.dashboardService.getRecentReservations().subscribe({
    //   next: (data) => {
    //     this.recentReservations = data;
    //   },
    //   error: (err) => console.error('Erreur réservations:', err)
    // });
  }

  prepareKPIsArray(): void {
    this.kpisArray = [
      { title: 'Réservations', value: this.kpis.totalReservations || 0, icon: '✈️' },
      { title: 'Clients Actifs', value: this.kpis.activeClients || 0, icon: '👥', bg: '#ecfdf5' },
      { title: 'En Attente', value: this.kpis.pendingReservations || 0, icon: '⏳', bg: '#fffbeb' },
      { title: 'Confirmées', value: this.kpis.confirmedReservations || 0, icon: '✓', bg: '#f5f3ff' },
      { title: 'Clients Fidèles', value: this.kpis.loyalClients || 0, icon: '⭐', bg: '#fef2f2' },
      { title: 'Top Destination', value: this.kpis.topDestination || 'N/A', icon: '🌍', bg: '#ecfeff' },
    ];
  }

  getStatusClass(status: string): string {
    const map: any = {
      'confirmée': 'confirmed',
      'en attente': 'pending',
      'annulée': 'cancelled'
    };
    return map[status] || '';
  }
}