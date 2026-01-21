import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class PackageService {

  packages = [
    {
      id: 1,
      title: 'Paris Getaway',
      description: 'Discover the beauty of Paris in 5 days',
      price: 1200,
      image: 'assets/images/paris.jpg'
    },
    {
      id: 2,
      title: 'Rome Adventure',
      description: 'Explore Rome and its history',
      price: 950,
      image: 'assets/images/rome.jpg'
    }
  ];

  getPackageById(id: number) {
    return this.packages.find(p => p.id === id);
  }
}