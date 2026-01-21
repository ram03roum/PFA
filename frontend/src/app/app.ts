import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './component/header/header.component';
import { Footer } from './component/footer/footer';
import { HomeComponent } from "./pages/home/home";
import { About } from "./pages/about/about";
import { HeroComponent } from "./component/hero/hero";
import { DestinationDetail } from './pages/destination-detail/destination-detail';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    HeaderComponent,
    Footer,
    DestinationDetail,
    HomeComponent,
    About,
    HeroComponent
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