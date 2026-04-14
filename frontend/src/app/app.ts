import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './component/header/header.component';
import { HomeComponent } from "./pages/home/home";
import { DestinationDetail } from './pages/destination-detail/destination-detail';
import { ReactiveFormsModule } from '@angular/forms';
import { ReservationFormComponent } from './pages/reservation-form/reservation-form';
import { ReservationDashboardComponent } from './component/reservation-dashboard/reservation-dashboard';
import { UsersComponent } from './component/users-dashboard/users-dashboard';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    HeaderComponent,
    ReservationFormComponent,
    ReservationDashboardComponent,
    UsersComponent,
    DestinationDetail,
    HomeComponent,
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