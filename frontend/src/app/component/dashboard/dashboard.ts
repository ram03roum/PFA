// src/app/components/admin/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../services/dashboard.service';
import { finalize } from 'rxjs/internal/operators/finalize';
import { title } from 'node:process';
import { NgxChartsModule } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgxChartsModule],
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
  revenueChartData: any[] = [];
  destinationChartData: any[] = [];

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.isLoading = true;
    this.dashboardService.getDashboardData().pipe(
      finalize(() => {
        this.isLoading = false;
        // S'exécutera TOUJOURS (Succès ou Erreur)
        // console.log(this.isLoading)
        })
    ).subscribe({
      next: (data) => {
        console.log('2. DATA REÇUE:', data);
        this.formatKpis(data.kpis);
        this.monthlyStats = data.revenue;
        this.destinationsStats = data.destinations;
        this.recentReservations = data.reservations;
        this.activityLogs = data.logs;
        // 1. KPI Cards
        this.formatKpis(data.kpis);
        
        // 2. Transformer les données pour les graphiques
        this.prepareCharts(data.revenue, data.destinations);

      },

      error: (err) => {
        console.error("Erreur Backend Flask:", err);
        // Le finalize s'occupe déjà du isLoading = false
      }
    });
    // Après le subscribe existant dans ngOnInit()
    this.dashboardService.getSentimentStats().subscribe({
      next: (res) => {
      this.sentimentChartData = res.data.map((item: any) => ({
      name: item.sentiment.charAt(0).toUpperCase() + item.sentiment.slice(1),
      value: item.count
    }));
    },
    error: (err) => console.error('Sentiment error:', err)
});
  }
  formatKpis(kpis: any) {
    this.kpisArray = [
      { title: 'Réservations', value: kpis.totalReservations, icon: '✈️', bg: '#eef2ff' },
      { title: 'Revenus', value: kpis.totalRevenue + ' TND', icon: '💰', bg: '#ecfdf5' },
      { title: 'Clients Fidèles', value: kpis.loyalClients, icon: '⭐', bg: '#f5f3ff' },
      // { title: 'Annulations', value: kpis.cancellation_rate + '%', icon: '📉', bg: '#fef2f2' }
      { title: 'Annulations', value: kpis.cancelRes, icon: '⭐' ,bg: '#fef2f2'}
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

    this.dashboardService.getActivityLogs().subscribe({
      next: (data) => {
        this.activityLogs = data;
      },
      error: (err) => console.error('Erreur logs:', err)
    });

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

  // Transformation cruciale pour ngx-charts
  prepareCharts(revenueRaw: any[], destinationsRaw: any[]) {
    // Transformation des revenus mensuels
    this.revenueChartData = revenueRaw.map(item => ({
      name: item.month, // ex: 'Jan'
      value: item.revenue // le montant
    }));

    // Transformation des destinations
    this.destinationChartData = destinationsRaw.map(item => ({
      name: item.name,
      value: item.value // le pourcentage
    }));
  }

  sentimentChartData: any[] = [];
    sentimentColorScheme :any = {
  domain: ['#22c55e', '#94a3b8', '#ef4444']  // positif, neutre, négatif
};
}