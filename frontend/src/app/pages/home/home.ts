import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../component/header/header.component';
import { Footer } from '../../component/footer/footer';
import { DestinationsPageComponent } from '../destinations-page/destinations-page';
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
  standalone: true,
  imports: [CommonModule, HeaderComponent, Footer, DestinationsPageComponent]
})
export class HomeComponent {

  constructor(private router: Router) {}

  exploreNow() {
    this.router.navigate(['/destinations']);
  }
}
