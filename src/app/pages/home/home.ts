import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PackagesComponent } from '../../component/packages/packages';
import { HeroComponent } from '../../component/hero/hero';
import { TravelBoxComponent } from '../../component/travel-box/travel-box'; // Ne pas oublier !
import { Services } from '../../component/services/services';
import { SpecialOffer } from '../../component/special-offer/special-offer';
import { Testimonials } from '../../component/testimonials/testimonials';
import { HeaderComponent } from "../../component/header/header.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    PackagesComponent,
    HeroComponent,
    TravelBoxComponent,
    Services,
    SpecialOffer,
    Testimonials,
    HeaderComponent
],
  templateUrl: './home.html',
  styleUrls: [`./home.css`]
})
export class Home {} // On exporte la classe ici, proprement.