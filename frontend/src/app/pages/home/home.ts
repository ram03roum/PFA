import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../component/header/header.component';
import { DestinationsPageComponent } from '../destinations-page/destinations-page';
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
  standalone: true,
  imports: [CommonModule, HeaderComponent,FormsModule, DestinationsPageComponent]
})
export class HomeComponent {
  searchDestination: string = '';
  searchDate: string = '';
  constructor(private router: Router) { }
  
  onSearch() {
    // On navigue vers la page destinations en passant les paramètres dans l'URL
    this.router.navigate(['/destinations'], { 
      queryParams: { 
        location: this.searchDestination, 
        date: this.searchDate 
      } 
    });
  }
  exploreNow() {
    this.router.navigate(['/destinations']);
  }
}
