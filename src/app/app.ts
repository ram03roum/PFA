import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common'; // Import indispensable
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './component/header/header.component';
import { Footer } from './component/footer/footer';
// import { LoginComponent } from './pages/login/login'; // N'oublie pas ton Login !
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    HeaderComponent,
    Footer,
    // LoginComponent // Ajoute ton composant Login ici
    // Note : On enlève HomeComponent, About, etc. car c'est le Router qui va les charger
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit {
  title = 'voyages-luxe-app';
  showBackToTop = false;

  // On injecte l'ID de la plateforme (Browser ou Server)
  private platformId = inject(PLATFORM_ID);

  ngOnInit() {
    // ON VÉRIFIE SI ON EST DANS LE NAVIGATEUR
    if (isPlatformBrowser(this.platformId)) {
      window.addEventListener('scroll', () => {
        this.showBackToTop = window.pageYOffset > 300;
      });
    }
  }

  scrollToTop() {
    if (isPlatformBrowser(this.platformId)) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}