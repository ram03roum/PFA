import { Component, OnInit } from '@angular/core';
import { DataService } from '../../services/data.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router'; // 1. Importe ceci
import { ChangeDetectorRef } from '@angular/core'; // 2. Importez ChangeDetectorRef
@Component({
  selector: 'app-destinations-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './destinations-page.html',
  styleUrls: ['./destinations-page.css'],
})
export class DestinationsPageComponent implements OnInit {
  destinations: any[] = [];
  chargement: boolean = true;

   // 🔹 Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;

  constructor(private dataService: DataService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
  this.chargerDestinations();
}
// 🔹 Charger les destinations
  chargerDestinations(): void {
    this.chargement = true;

    this.dataService.getDestinations().subscribe({
      next: (data) => {
        console.log('Données reçues :', data);
        this.destinations = data;

        // reset pagination
        this.currentPage = 1;

        setTimeout(() => {
          this.chargement = false;
          this.cdr.detectChanges();
        }, 0);
      },
      error: (err) => {
        console.error('Erreur chargement destinations :', err);
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ==========================
  // 🔹 PAGINATION
  // ==========================

  get paginatedDestinations(): any[] {
  const startIndex = (this.currentPage - 1) * this.itemsPerPage;
  const endIndex = startIndex + this.itemsPerPage;

  return this.destinations.slice(startIndex, endIndex);
}

// Pour optimiser le rendu avec *ngFor
  trackDestination = (_: number, item: any) => item;

  get totalPages(): number {
    return Math.ceil(this.destinations.length / this.itemsPerPage);
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

}
