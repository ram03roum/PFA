import { Component, OnInit, ChangeDetectorRef, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DataService } from '../../services/data.service';


// import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-destinations-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './destinations-page.html',
})



export class DestinationsComponent implements OnInit {
  listDestinations: any[] = [];
  chargement: boolean = false;

  currentPage: number = 1;
  itemsPerPage: number = 15;

  constructor(private router: Router, private cdr: ChangeDetectorRef, private dataService: DataService, @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  goToDetail(id: number) {
    this.router.navigate(['/destinations', id]);
  }

  ngOnInit() {
    this.dataService.getDestinations().subscribe({
      next: (data: any[]) => {
        console.log('Data reçue dans le composant:', data); // <--- Vérifie le format ici

        // On s'assure que listDestinations est bien mise à jour
        this.listDestinations = [...data]; // L'opérateur spread (...) force une nouvelle référence
        this.chargement = false;

        // On force la détection
        this.cdr.markForCheck();
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Erreur :', err);
        this.chargement = false;
        this.cdr.detectChanges();

        console.log('Données chargées avec succès:', this.listDestinations.length);
      }
    });
  }

  // Calculer les destinations à afficher pour la page actuelle
  get paginatedDestinations() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.listDestinations.slice(startIndex, startIndex + this.itemsPerPage);
  }

  // Calculer le nombre total de pages
  get totalPages() {
    return Math.ceil(this.listDestinations.length / this.itemsPerPage);
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      window.scrollTo({ top: 0, behavior: 'smooth' }); // Remonte en haut de page
    }
  }

}