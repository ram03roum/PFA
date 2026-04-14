import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient , HttpHeaders} from '@angular/common/http';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.html',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  styleUrls: ['./contact.css']
})
export class ContactComponent {

  private apiUrl = 'http://localhost:5000/api';

  // Type de contact sélectionné
  contactType: 'general' | 'reservation' | 'destination' = 'general';

  // contactForm = {
  //   name: '',
  //   email: '',
  //   phone: '',
  //   subject: '',
  //   message: ''
  // };

  // isLoading = false;
  // successMessage = '';
  // errorMessage = '';

  // constructor(private http: HttpClient) {}

  // onSubmit(): void {
  //   if (!this.contactForm.name || !this.contactForm.email || !this.contactForm.message) {
  //     this.errorMessage = 'Veuillez remplir tous les champs obligatoires.';
  //     return;
  //   }

  //   this.isLoading = true;
  //   this.successMessage = '';
  //   this.errorMessage = '';

  //   this.http.post(`${this.apiUrl}/contact`, this.contactForm).subscribe({
  //     next: () => {
  //       this.isLoading = false;
  //       this.successMessage = '✅ Message envoyé ! Vous recevrez un email de confirmation.';
  //       this.contactForm = { name: '', email: '', phone: '', subject: '', message: '' };
  //     },
  //     error: () => {
  //       this.isLoading = false;
  //       this.errorMessage = '❌ Une erreur est survenue. Veuillez réessayer.';
  //     }
  //   });
  // }

  // Données
  destinations:  any[] = [];
  reservations:  any[] = [];
  isLoggedIn   = false;

  // Loaders
  isLoadingDests  = false;
  isLoadingResas  = false;
  isLoading       = false;

  // Messages
  successMessage = '';
  errorMessage   = '';

  contactForm = {
    name:             '',
    email:            '',
    phone:            '',
    subject:          '',
    message:          '',
    contact_type:     'general',
    destination_id:   null as number | null,
    destination_name: '',
    reservation_id:   null as number | null,
  };

  selectedReservation: any = null;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    // Vérifie si connecté
    const token = localStorage.getItem('access_token');
    this.isLoggedIn = !!token;

    // Pré-remplir nom/email si connecté
    const user = localStorage.getItem('user');
    if (user) {
      const u = JSON.parse(user);
      this.contactForm.name  = u.name  || '';
      this.contactForm.email = u.email || '';
    }
  }

  // ── Sélection du type ────────────────────────────────────
  selectType(type: 'general' | 'reservation' | 'destination') {
    this.contactType          = type;
    this.contactForm.contact_type = type;
    this.selectedReservation  = null;
    this.contactForm.reservation_id   = null;
    this.contactForm.destination_id   = null;
    this.contactForm.destination_name = '';

    console.log(`📋 Type de contact sélectionné : ${type}`);

    if (type === 'destination' && this.destinations.length === 0) {
      this.loadDestinations();
    }
    if (type === 'reservation' && this.reservations.length === 0) {
      this.loadReservations();
    }
  }

  // ── Chargement destinations ──────────────────────────────
  loadDestinations() {
    this.isLoadingDests = true;
    this.http.get<any>(`${this.apiUrl}/contact/destinations`).subscribe({
      next: res => {
        this.destinations   = res.destinations;
        this.isLoadingDests = false;
        console.log(`✅ ${this.destinations.length} destination(s) chargée(s)`);
      },
      error: () => this.isLoadingDests = false
    });
  }

  // ── Chargement réservations du client ────────────────────
  loadReservations() {
    this.isLoadingResas = true;
    const token   = localStorage.getItem('access_token');
    const headers = token
      ? new HttpHeaders().set('Authorization', `Bearer ${token}`)
      : new HttpHeaders();

    this.http.get<any>(`${this.apiUrl}/contact/my-reservations`,
      { headers }).subscribe({
      next: res => {
        this.reservations   = res.reservations;
        this.isLoadingResas = false;
        console.log(`✅ ${this.reservations.length} réservation(s) chargée(s)`);
      },
      error: () => this.isLoadingResas = false
    });
  }

  // ── Sélection d'une réservation ──────────────────────────
  selectReservation(resa: any) {
    this.selectedReservation        = resa;
    this.contactForm.reservation_id = resa.id;
    this.contactForm.subject        = `Concernant ma réservation #${resa.id} — ${resa.destination}`;
    console.log(`✅ Réservation sélectionnée : #${resa.id} — ${resa.destination}`);
  }

  // ── Sélection d'une destination ──────────────────────────
  onDestinationChange() {
    const selected = this.destinations.find(
      d => d.id == this.contactForm.destination_id
    );
    if (selected) {
      this.contactForm.destination_name = selected.name;
      this.contactForm.subject = `Question sur ${selected.name}`;
      console.log(`📍 Destination sélectionnée : ${selected.name}`);
    }
  }

  // ── Soumission ───────────────────────────────────────────
  onSubmit() {
    if (!this.contactForm.name || !this.contactForm.email || !this.contactForm.message) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires.';
      return;
    }
    if (this.contactType === 'reservation' && !this.contactForm.reservation_id) {
      this.errorMessage = 'Veuillez sélectionner une réservation.';
      return;
    }

    this.isLoading      = true;
    this.successMessage = '';
    this.errorMessage   = '';

    console.log('📤 Envoi formulaire :', this.contactForm);

    this.http.post(`${this.apiUrl}/contact`, this.contactForm).subscribe({
      next: () => {
        this.isLoading      = false;
        this.successMessage = '✅ Message envoyé ! Vous recevrez un email de confirmation.';
        this.resetForm();
      },
      error: () => {
        this.isLoading    = false;
        this.errorMessage = '❌ Une erreur est survenue. Veuillez réessayer.';
      }
    });
  }

  resetForm() {
    const name  = this.contactForm.name;
    const email = this.contactForm.email;
    this.contactForm = {
      name, email,
      phone: '', subject: '', message: '',
      contact_type: 'general',
      destination_id: null, destination_name: '',
      reservation_id: null,
    };
    this.contactType         = 'general';
    this.selectedReservation = null;
  }

  getStatusClass(status: string): string {
    const map: any = {
      'confirmée':  'status-confirmed',
      'en attente': 'status-pending',
      'annulée':    'status-cancelled',
      'payée':      'status-paid',
    };
    return map[status] || 'status-pending';
  }

  getStatusIcon(status: string): string {
    const map: any = {
      'confirmée':  '✅',
      'en attente': '⏳',
      'annulée':    '❌',
      'payée':      '💳',
    };
    return map[status] || '⏳';
  }
}