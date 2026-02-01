import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-destination-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './destination-detail.html',
})
export class DestinationDetail implements OnInit {

  destination: any;

  // Fake data (plus tard → service / API)
  destinations = [
    {
      id: 1,
      name: 'Paris',
      description: 'La ville de l’amour, idéale pour une escapade romantique.',
      price: 899,
      image: 'assets/images/paris.jpg'
    },
    {
      id: 2,
      name: 'Tokyo',
      description: 'Culture, technologie et gastronomie unique.',
      price: 1299,
      image: 'assets/images/tokyo.jpg'
    },
    {
      id: 3,
      name: 'Bali',
      description: 'Nature exotique et plages paradisiaques.',
      price: 1099,
      image: 'assets/images/bali.jpg'
    }
  ];

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.destination = this.destinations.find(d => d.id === id);
  }
}
