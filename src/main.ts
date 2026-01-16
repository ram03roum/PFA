import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app'; 
import { provideRouter } from '@angular/router';
import { importProvidersFrom } from '@angular/core';
import { Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HomeComponent } from './app/pages/home/home';
import { DestinationsComponent } from './app/pages/destinations-page/destinations-page';
import { DestinationDetail } from './app/pages/destination-detail/destination-detail';
import { PackagePage } from './app/pages/package-page/package-page';
import { PackageDetail } from './app/pages/package-detail/package-detail';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'destinations', component: DestinationsComponent },
  { path: 'destinations/:id', component: DestinationDetail },
  { path: 'packages', component: PackagePage },
  { path: 'packages/:id', component: PackageDetail },
  // { path: '**', redirectTo: '' }
];

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    importProvidersFrom(FormsModule)
  ]
}).catch(err => console.error(err));