import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-travel-box',
  templateUrl: './travel-box.html',
  standalone: true,
  imports: [CommonModule, 
    FormsModule],
  styleUrls: ['./travel-box.css']
})
export class TravelBoxComponent implements OnInit {
  activeTab = 'tours';
  
  // Données pour les onglets
  searchData = {
    tours: {
      destinationCountry: '',
      destinationLocation: '',
      checkIn: '',
      checkOut: '',
      duration: '5',
      members: '1',
      budget: 1000
    },
    hotels: {
      destinationCountry: '',
      destinationLocation: '',
      checkIn: '',
      checkOut: '',
      duration: '5',
      members: '1'
    },
    flights: {
      tripType: 'round',
      from: '',
      to: '',
      departure: '',
      returnDate: '',
      adults: '1',
      childs: '0',
      flightClass: 'Economy'
    }
  };

  constructor(private router: Router) { }

  ngOnInit(): void {
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  search(): void {
    console.log('Searching with data:', this.searchData[this.activeTab as keyof typeof this.searchData]);
    if (this.activeTab === 'tours') {
      const params: any = {};
      if (this.searchData.tours.destinationCountry) {
        params.country = this.searchData.tours.destinationCountry;
      }
      this.router.navigate(['/destinations'], { queryParams: params });
    } else {
      alert('Search functionality for ' + this.activeTab + ' will be implemented soon!');
    }
  }
}