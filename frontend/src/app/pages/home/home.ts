import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../component/header/header.component';
import { Footer } from '../../component/footer/footer';
import { DestinationsPageComponent } from '../destinations-page/destinations-page';
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, Footer, DestinationsPageComponent]
})
export class HomeComponent {
  searchDestination: string = '';
  searchDate: string = '';

  constructor(private router: Router) { }

  exploreNow() {
    this.router.navigate(['/destinations']);
  }

  search() {
    const params: any = {};
    if (this.searchDestination) {
      params.query = this.searchDestination;
    }
    this.router.navigate(['/destinations'], { queryParams: params });
  }
}
