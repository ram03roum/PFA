import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../component/header/header.component';
import { Footer } from '../../component/footer/footer';
import { DestinationsComponent } from '../destinations-page/destinations-page';
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
  standalone: true,
  imports: [CommonModule, HeaderComponent, Footer, DestinationsComponent]
})
export class HomeComponent {

  constructor(private router: Router) {}

  exploreNow() {
    this.router.navigate(['/destinations']);
  }
}
