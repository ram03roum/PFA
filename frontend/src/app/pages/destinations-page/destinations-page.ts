import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DestinationService } from '../../services/destination';
// import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-destinations-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './destinations-page.html',
})
export class DestinationsComponent implements OnInit {
  listDestinations: any[] = [];
  constructor(private router: Router, private destService: DestinationService, private cdr: ChangeDetectorRef) { }

  goToDetail(id: number) {
    this.router.navigate(['/destinations', id]);
  }

  ngOnInit() {
    this.destService.getDestinations().subscribe({
      next: (data) => {
        this.listDestinations = data;
        // console.log()
        this.cdr.detectChanges();
        // console.log('Données reçues :', data); // <--- C'est ICI qu'on l'affiche
      },
      error: (err) => {
        // console.error('Erreur de connexion au Backend :', err);
      }
    });
  }
}
