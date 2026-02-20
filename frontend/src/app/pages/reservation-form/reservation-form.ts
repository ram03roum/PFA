import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { DataService } from '../../services/data.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reservation-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reservation-form.html',
  styleUrl: './reservation-form.css',
})
export class ReservationFormComponent implements OnInit {
  // Initialisation avec un objet vide pour éviter les erreurs "read properties of null"
  public destination: any= {}; 
  public loading: boolean = true;
  public error: string = '';
  public success: string = '';
  public calculatingPrice: boolean = false;

  reservationForm: FormGroup;
  destinationId: number | null = null;
  numberOfNights: number = 0;
  pricePerNight: number = 0;
  totalAmount: number = 0;

  minDate: string;
  minCheckOut: string = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private reservationService: ReservationService,
    private dataService: DataService,
    private cdr: ChangeDetectorRef
  ) {
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];

    this.reservationForm = this.fb.group({
      check_in: ['', Validators.required],
      check_out: ['', Validators.required],
      notes: ['']
    });
  }

  ngOnInit(): void {
    // 1. Récupérer l'ID et charger les données
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');  
      if (id) {
        this.destinationId = parseInt(id); // TRÈS IMPORTANT : Stocker l'ID ici
        this.loadDestinationDetails(this.destinationId);
      } else {
        this.error = "Aucune destination sélectionnée.";
        this.loading = false;
      }
    });

    // 2. Surveiller les changements de dates pour le calcul
    this.reservationForm.valueChanges.subscribe(() => {
      this.calculatePrice();
    });

    // 3. Mise à jour dynamique de la date de départ min
    this.reservationForm.get('check_in')?.valueChanges.subscribe(value => {
      if (value) {
        const checkInDate = new Date(value);
        checkInDate.setDate(checkInDate.getDate() + 1);
        this.minCheckOut = checkInDate.toISOString().split('T')[0];
      }
    });
  }

  loadDestinationDetails(id: number): void {
    this.loading = true;
    this.dataService.getDestinationById(id).subscribe({
      next: (data) => {
        // Gestion si l'API renvoie un tableau ou un objet
        this.destination = Array.isArray(data) ? data[0] : data;
        
        if (this.destination) {
          // this.pricePerNight = this.destination.price_per_night || 0;
        } else {
          this.error = "Destination introuvable dans la base de données.";
        }
        
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Erreur API:", err);
        this.error = 'Impossible de charger les détails de la destination.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  calculatePrice(): void {
    const checkIn = this.reservationForm.get('check_in')?.value;
    const checkOut = this.reservationForm.get('check_out')?.value;

    // On ne lance le calcul que si on a les deux dates ET l'ID de destination
    if (checkIn && checkOut && this.destinationId) {
      const d1 = new Date(checkIn);
      const d2 = new Date(checkOut);

      if (d2 <= d1) {
        this.numberOfNights = 0;
        this.totalAmount = 0;
        return;
      }

      this.calculatingPrice = true;
      this.reservationService.calculatePrice(this.destinationId, checkIn, checkOut).subscribe({
        next: (data) => {
          this.numberOfNights = data.nights;
          this.totalAmount = data.total_amount;
          this.calculatingPrice = false;
        },
        error: (err) => {
          console.error('Erreur calcul prix:', err);
          this.calculatingPrice = false;
        }
      });
    }
  }

  onSubmit(): void {
    if (this.reservationForm.invalid || !this.destinationId) return;

    this.loading = true;
    const reservationData = {
      user_id: this.destination.user_id, // Assurez-vous que l'ID utilisateur est correctement assigné
      destination_id: this.destinationId,
      check_in: this.reservationForm.value.check_in,
      check_out: this.reservationForm.value.check_out,
      total_amount: this.totalAmount,
      notes: this.reservationForm.value.notes 
    };

    this.reservationService.createReservation(reservationData).subscribe({
      next: (response) => {
        this.reservationService.addActivityLog('Création d\'une réservation', 'reservation', response.reservation.id, 'Réservation créée par l\'utilisateur').subscribe();
        this.success = 'Réservation confirmée ! Redirection en cours...';
        setTimeout(() => this.router.navigate(['/mes-reservations']), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.error || 'Une erreur est survenue lors de la réservation.';
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/destinations']);
  }

  getNightsText(): string {
    return this.numberOfNights > 1 ? `${this.numberOfNights} nuits` : `${this.numberOfNights} nuit`;
  }
}