import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PackagesComponent } from '../../component/packages/packages';
import { HeroComponent } from '../../component/hero/hero';
import { TravelBoxComponent } from '../../component/travel-box/travel-box'; // Ne pas oublier !
import { Services } from '../../component/services/services';
import { SpecialOffer } from '../../component/special-offer/special-offer';
import { Testimonials } from '../../component/testimonials/testimonials';

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
    Testimonials
  ],
  template: `
    <app-hero></app-hero>
    <div class="container position-relative" style="margin-top: -50px; z-index: 5;">
      <app-travel-box></app-travel-box>
    </div>
    <app-services></app-services>
    <app-special-offer></app-special-offer>
    <app-packages></app-packages>
    <app-testimonials></app-testimonials>
  `
})
export class Home {} // On exporte la classe ici, proprement.