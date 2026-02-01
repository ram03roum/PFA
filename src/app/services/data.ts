import { Injectable } from '@angular/core';
import { Package, Destination, Testimonial, BlogPost } from '../models/package.model';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  constructor() { }

  getPackages(): Package[] {
    return [
      {
        id: 1,
        title: 'Italy',
        price: 499,
        image: 'assets/images/packages/p1.jpg',
        duration: '3 Days 2 nights',
        accommodation: '5 star accommodation',
        transportation: true,
        food: true,
        rating: 5,
        reviews: 254
      },
      {
        id: 2,
        title: 'England',
        price: 1499,
        image: 'assets/images/packages/p2.jpg',
        duration: '6 Days 7 nights',
        accommodation: '5 star accommodation',
        transportation: true,
        food: true,
        rating: 5,
        reviews: 344
      },
      {
        id: 3,
        title: 'France',
        price: 1199,
        image: 'assets/images/packages/p3.jpg',
        duration: '5 Days 6 nights',
        accommodation: '5 star accommodation',
        transportation: true,
        food: true,
        rating: 5,
        reviews: 544
      },
      {
        id: 4,
        title: 'India',
        price: 799,
        image: 'assets/images/packages/p4.jpg',
        duration: '4 Days 5 nights',
        accommodation: '5 star accommodation',
        transportation: true,
        food: true,
        rating: 5,
        reviews: 625
      },
      {
        id: 5,
        title: 'Spain',
        price: 999,
        image: 'assets/images/packages/p5.jpg',
        duration: '4 Days 4 nights',
        accommodation: '5 star accommodation',
        transportation: true,
        food: true,
        rating: 5,
        reviews: 379
      },
      {
        id: 6,
        title: 'Thailand',
        price: 799,
        image: 'assets/images/packages/p6.jpg',
        duration: '5 Days 6 nights',
        accommodation: '5 star accommodation',
        transportation: true,
        food: true,
        rating: 5,
        reviews: 447
      }
    ];
  }

  getDestinations(): Destination[] {
    return [
      { id: 1, name: 'China', image: 'assets/images/gallary/g1.jpg', tours: 20, places: 15 },
      { id: 2, name: 'Venuzuela', image: 'assets/images/gallary/g2.jpg', tours: 12, places: 9 },
      { id: 3, name: 'Brazil', image: 'assets/images/gallary/g3.jpg', tours: 25, places: 10 },
      { id: 4, name: 'Australia', image: 'assets/images/gallary/g4.jpg', tours: 18, places: 9 },
      { id: 5, name: 'Netherlands', image: 'assets/images/gallary/g5.jpg', tours: 14, places: 12 },
      { id: 6, name: 'Turkey', image: 'assets/images/gallary/g6.jpg', tours: 14, places: 6 }
    ];
  }

  getTestimonials(): Testimonial[] {
    return [
      {
        id: 1,
        name: 'Kevin Watson',
        location: 'London, England',
        image: 'assets/images/client/testimonial1.jpg',
        text: 'Lorem ipsum dolor sit amet, contur adip elit, sed do mod incid ut labore et dolore magna aliqua.'
      },
      {
        id: 2,
        name: 'Sarah Johnson',
        location: 'New York, USA',
        image: 'assets/images/client/testimonial2.jpg',
        text: 'Lorem ipsum dolor sit amet, contur adip elit, sed do mod incid ut labore et dolore magna aliqua.'
      },
      {
        id: 3,
        name: 'Michael Brown',
        location: 'Sydney, Australia',
        image: 'assets/images/client/testimonial1.jpg',
        text: 'Lorem ipsum dolor sit amet, contur adip elit, sed do mod incid ut labore et dolore magna aliqua.'
      }
    ];
  }

  getBlogPosts(): BlogPost[] {
    return [
      {
        id: 1,
        title: 'Discover on beautiful weather, Fantastic foods and historical place in Prag',
        date: '15 November 2017',
        image: 'assets/images/blog/b1.jpg',
        excerpt: 'Lorem ipsum dolor sit amet, contur adip elit, sed do mod incid ut labore et dolore magna aliqua.'
      },
      {
        id: 2,
        title: 'Discover on beautiful weather, Fantastic foods and historical place in India',
        date: '15 November 2017',
        image: 'assets/images/blog/b2.jpg',
        excerpt: 'Lorem ipsum dolor sit amet, contur adip elit, sed do mod incid ut labore et dolore magna aliqua.'
      },
      {
        id: 3,
        title: '10 Most Natural place to Discover',
        date: '15 November 2017',
        image: 'assets/images/blog/b3.jpg',
        excerpt: 'Lorem ipsum dolor sit amet, contur adip elit, sed do mod incid ut labore et dolore magna aliqua.'
      }
    ];
  }
}