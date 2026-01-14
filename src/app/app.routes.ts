import { Routes } from '@angular/router';
// Importer les pages
import { Home } from './pages/home/home';
import { ContactComponent} from './pages/contact/contact';
import { About } from './pages/about/about';
import { PackageDetail } from './pages/package-detail/package-detail';
import { BlogPage } from './pages/blog-page/blog-page';
import { PackagePage } from './pages/package-page/package-page';
import { Destinations } from './pages/destinations/destinations';
import { DestinationDetail } from './pages/destination-detail/destination-detail';
export const routes: Routes = [
  { path: '', component: Home },
  { path: 'destinations', component: Destinations },
  { path: 'destinations/:id', component: DestinationDetail },
  { path: 'packages', component: PackagePage },
  { path: 'packages/:id', component: PackageDetail },
  { path: 'about', component: About },
  { path: 'contact', component: ContactComponent },
  { path: 'blog', component: BlogPage },
  { path: '**', redirectTo: '' } // Redirection pour routes non trouvées
];