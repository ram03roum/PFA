import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app'; 
import { provideRouter } from '@angular/router';
import { importProvidersFrom } from '@angular/core';
import { Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Home } from './app/pages/home/home';
import { Destinations } from './app/pages/destinations/destinations';
import { DestinationDetail } from './app/pages/destination-detail/destination-detail';

export const routes: Routes = [
  { path: '', component: Home },
  // { path: 'destinations', component: Destinations },
  // { path: 'offres', component: DestinationDetail },
  // { path: '**', redirectTo: '' }
];

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    importProvidersFrom(FormsModule)
  ]
}).catch(err => console.error(err));