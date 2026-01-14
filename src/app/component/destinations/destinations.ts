import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-destinations',
  templateUrl: './destinations.html',
  imports: [CommonModule],
  styleUrls: ['./destinations.css']
})
export class DestinationsComponent implements OnInit {
  destinations = [
    {
      id: 1,
      name: 'Paris, France',
      description: 'La ville lumière vous attend',
      image: 'https://images.unsplash.com/photo-1499856871958-5b9627545d1a',
      price: 599
    },
    {
      id: 2,
      name: 'Tokyo, Japon',
      description: 'Culture et modernité',
      image: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf',
      price: 1299
    },
    // Ajoutez plus de destinations...
  ];

  constructor(private router: Router) { }

  ngOnInit(): void { }

  goToDetail(id: number): void {
    this.router.navigate(['/destinations', id]);
  }
}