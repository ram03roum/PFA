import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { ContactComponent } from './pages/contact/contact';
import { About } from './pages/about/about';
import { PackageDetail } from './pages/package-detail/package-detail';
import { BlogPage } from './pages/blog-page/blog-page';
import { PackagePage } from './pages/package-page/package-page';
import { DestinationsPageComponent } from './pages/destinations-page/destinations-page';
import { DestinationDetail } from './pages/destination-detail/destination-detail';
import { LoginComponent } from './pages/login/login';
import { SignupComponent } from './pages/signup/signup';
import { DestinationsComponent } from './component/destinations/destinations';


export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'destinations', component: DestinationsPageComponent },
  { path: 'destinations/:id', component: DestinationDetail },
  { path: 'packages', component: PackagePage },
  { path: 'packages/:id', component: PackageDetail },
  { path: 'about', component: About },
  { path: 'contact', component: ContactComponent },
  { path: 'blog', component: BlogPage },
  { path: '**', redirectTo: '' }
];