import { Component, OnInit, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { timeout } from 'rxjs';
import jsPDF from 'jspdf';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-profile.html',
  styleUrls: ['./user-profile.css']
})
export class UserProfileComponent implements OnInit {
  user: any = { name: '', email: '', segment: 'nouveau', member_since: '' };
  stats: any = { total: 0, confirmed: 0, cancelled: 0, pending: 0, total_spent: 0 };
  reservations: any[] = [];
  loading = true;
  error = false;
  visitedCountries: any[] = [];
  private map: any;
  activeTab: string = 'profil';

  segmentConfig: any = {
    'nouveau': { label: 'Nouveau', color: '#6B7280', bg: '#F3F4F6', icon: '★' },
    'Régulier': { label: 'Régulier', color: '#1D9E75', bg: '#E1F5EE', icon: '★★' },
    'VIP': { label: 'VIP', color: '#B45309', bg: '#FAEEDA', icon: '★★★' }
  };

  statusConfig: any = {
    'confirmée': { label: 'Confirmée', class: 'status-confirmed' },
    'annulée': { label: 'Annulée', class: 'status-cancelled' },
    'en attente': { label: 'En attente', class: 'status-pending' },
    'payée': { label: 'Payée', class: 'status-paid' }
  };

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit() {
    if (!isPlatformBrowser(this.platformId)) return;

    const token = localStorage.getItem('access_token');
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

    this.http.get<any>('http://localhost:5000/users/me/dashboard', { headers })
      .pipe(timeout(15000))
      .subscribe({
        next: (data) => {
          this.user = data.user;
          this.stats = data.stats;
          this.reservations = data.reservations;
          this.visitedCountries = data.visited_countries || [];  // ← nouveau
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Erreur chargement profil:', err);
          this.error = true;
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }
  initMapOnTab(): void {
    setTimeout(() => this.initMap(), 150);
  }

  async initMap(): Promise<void> {
    if (!this.visitedCountries.length) return;

    // Import dynamique → seulement dans le navigateur
    const L = await import('leaflet');

    if (this.map) {
      this.map.remove();
    }

    this.map = L.map('travel-map', {
      center: [20, 0],
      zoom: 2
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    const icon = L.divIcon({
      html: '✈️',
      className: 'map-marker',
      iconSize: [30, 30]
    });

    this.visitedCountries.forEach((c: any) => {
      L.marker(c.coords, { icon })
        .addTo(this.map)
        .bindPopup(`<b>🌍 ${c.country}</b><br>${c.destinations.join('<br>')}`);
    });
  }
  getSegment() {
    const seg = this.user?.segment;
    return this.segmentConfig[seg]
      || Object.entries(this.segmentConfig).find(
        ([k]) => k.toLowerCase() === seg?.toLowerCase()
      )?.[1]
      || this.segmentConfig['nouveau'];
  }

  getStatusConfig(status: string) {
    return this.statusConfig[status] || { label: status, class: 'status-pending' };
  }

  getInitials(name: string) {
    return name?.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2) || 'U';
  }
  // facture
  downloadInvoice(r: any): void {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();

    // ── En-tête ──────────────────────────────────────────
    doc.setFillColor(79, 110, 247);
    doc.rect(0, 0, pageWidth, 35, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFontSize(20);
    doc.setFont('helvetica', 'bold');
    doc.text('TravelApp', 14, 20);

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text('Votre agence de voyage', 14, 28);

    // Numéro de facture
    doc.setFontSize(10);
    doc.text(`Facture #${r.id}`, pageWidth - 14, 20, { align: 'right' });
    doc.text(`Émise le ${new Date().toLocaleDateString('fr-FR')}`, pageWidth - 14, 28, { align: 'right' });

    // ── Infos client ─────────────────────────────────────
    doc.setTextColor(0, 0, 0);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('Client', 14, 50);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    doc.text(this.user.name, 14, 58);
    doc.text(this.user.email, 14, 65);

    // ── Séparateur ───────────────────────────────────────
    doc.setDrawColor(220, 220, 220);
    doc.line(14, 72, pageWidth - 14, 72);

    // ── Détails du voyage ─────────────────────────────────
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('Détails du voyage', 14, 82);

    const details = [
      ['Destination', r.destination],
      ['Pays', r.country],
      ['Date d\'arrivée', r.check_in],
      ['Date de départ', r.check_out],
      ['Statut', r.status],
    ];

    doc.setFontSize(11);
    let y = 92;
    details.forEach(([label, value]) => {
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(100, 100, 100);
      doc.text(label + ' :', 14, y);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(0, 0, 0);
      doc.text(value, 80, y);
      y += 10;
    });

    // ── Séparateur ───────────────────────────────────────
    doc.setDrawColor(220, 220, 220);
    doc.line(14, y + 3, pageWidth - 14, y + 3);
    y += 13;

    // ── Montant total ─────────────────────────────────────
    doc.setFillColor(245, 247, 255);
    doc.rect(14, y - 5, pageWidth - 28, 20, 'F');

    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(0, 0, 0);
    doc.text('Total', 20, y + 7);

    doc.setFontSize(16);
    doc.setTextColor(79, 110, 247);
    doc.text(`$${r.total_amount}`, pageWidth - 20, y + 7, { align: 'right' });

    // ── Pied de page ─────────────────────────────────────
    doc.setFontSize(9);
    doc.setTextColor(150, 150, 150);
    doc.setFont('helvetica', 'normal');
    doc.text('Merci pour votre confiance — TravelApp', pageWidth / 2, 280, { align: 'center' });

    // ── Téléchargement ────────────────────────────────────
    doc.save(`facture-${r.destination}-${r.id}.pdf`);
  }
}