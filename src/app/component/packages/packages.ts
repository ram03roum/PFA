import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-package',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './packages.html',
})
export class PackageComponent {

  packages = [
    {
      id: 1,
      title: 'Paris Getaway',
      description: 'Un séjour romantique à Paris',
      price: 999,
      image: 'assets/images/paris-package.jpg'
    },
    {
      id: 2,
      title: 'Tokyo Experience',
      description: 'Découvrez la culture japonaise',
      price: 1399,
      image: 'assets/images/tokyo-package.jpg'
    },
    {
      id: 3,
      title: 'Bali Escape',
      description: 'Détente et plages paradisiaques',
      price: 1199,
      image: 'assets/images/bali-package.jpg'
    }
  ];

  constructor(private router: Router) {}

  goToDetail(id: number) {
    this.router.navigate(['/packages', id]);
  }
}
