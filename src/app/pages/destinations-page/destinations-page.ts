import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-destinations-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './destinations-page.html',
})
export class DestinationsComponent {

  destinations = [
    {
      id: 1,
      name: 'Paris',
      description: 'La ville de l’amour et de la lumière.',
      price: 899,
      image: 'assets/images/paris.jpg'
    },
    {
      id: 2,
      name: 'Tokyo',
      description: 'Un mélange unique de tradition et de modernité.',
      price: 1299,
      image: 'assets/images/tokyo.jpg'
    },
    {
      id: 3,
      name: 'Bali',
      description: 'Plages paradisiaques et nature exotique.',
      price: 1099,
      image: 'assets/images/bali.jpg'
    }
  ];

  constructor(private router: Router) {}

  goToDetail(id: number) {
    this.router.navigate(['/destinations', id]);
  }
}
