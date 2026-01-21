import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef } from '@angular/core'; // 1. Ajoutez cet import
import { DataService } from '../../services/data.service';
@Component({
  selector: 'app-destinations',
  standalone: true,
  imports: [CommonModule,],
  templateUrl: './destinations.component.html',
  styleUrls: ['./destinations.component.css']
})
export class DestinationsComponent implements OnInit {

  destinations: any[] = [];

  constructor(private dataService: DataService,
    private cdr: ChangeDetectorRef
  ) { }

// Dans ton fichier destinations.ts ou destinations-page.ts
ngOnInit(): void {
  this.dataService.getDestinations().subscribe({
    next: (data: any) => {
      console.log("Données reçues de Flask :", data);

      // Sécurité : on s'assure que 'data' est bien le tableau
      if (Array.isArray(data)) {
        this.destinations = data;
      } else if (data && data.destinations) {
        // Au cas où Flask envoie { destinations: [...] }
        this.destinations = data.destinations;
      }

      console.log("Nombre de destinations chargées :", this.destinations.length);
    },
    error: (err) => {
      console.error("Erreur Backend :", err);
    }
  });
}

  goToDetail(id: number) {
    window.location.href = `/destination/${id}`;
  }

}
