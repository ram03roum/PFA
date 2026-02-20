import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './component/header/header.component';
import { Footer } from './component/footer/footer';
import { HomeComponent } from "./pages/home/home";
import { About } from "./pages/about/about";
import { DestinationDetail } from './pages/destination-detail/destination-detail';
import { ReactiveFormsModule } from '@angular/forms';
import { ReservationFormComponent } from './pages/reservation-form/reservation-form';
import { ReservationDashboardComponent } from './component/reservation-dashboard/reservation-dashboard';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    HeaderComponent,
    ReservationFormComponent,
    ReservationDashboardComponent,
    Footer,
    DestinationDetail,
    HomeComponent,
    About,
    ReactiveFormsModule
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  title = 'voyages-luxe-app';
  showBackToTop = false;

  ngOnInit() {
    window.addEventListener('scroll', () => {
      this.showBackToTop = window.pageYOffset > 300;
    });
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}