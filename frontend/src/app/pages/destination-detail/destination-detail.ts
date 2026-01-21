import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';
import { DataService } from '../../services/data.service';
@Component({
  selector: 'app-destination-detail',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './destination-detail.html',
})
export class DestinationDetail implements OnInit {

 loading: boolean = true; // Pour afficher un état de chargement
  destination: any = null;
  constructor(private route: ActivatedRoute,
    private http: HttpClient ,// Injectez le service HttpClient
    private cdr: ChangeDetectorRef, // Injecte le détecteur de changement
    private dataService: DataService
  ) {}

  ngOnInit(): void {
    // Écoute les changements d'ID dans l'URL
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadDetail(id);
      }
    });
  }

  loadDetail(id: string): void {
    this.loading = true;
     this.destination = null;// Réinitialise la destination avant de charger une nouvelle
    this.dataService.getDestinationById(id).subscribe({
      next: (data) => {
        // Comme ton Flask renvoie Array(1), on prend le premier élément
        this.destination = Array.isArray(data) ? data[0] : data;
        console.log("Données traitées pour affichage :", this.destination);
        this.loading = false;
        this.cdr.detectChanges();
        // setTimeout(() => {
        //   this.loading = false;
        //   this.cdr.detectChanges();
        // }, 0);
        console.log("Données reçues via DataService :", this.destination);
      },
      error: (err) => {
        console.error("Erreur via DataService :", err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}