import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/AuthService';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { DomSanitizer,SafeHtml} from '@angular/platform-browser';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-profile-menu',
  templateUrl: './profile-menu.html',
  standalone: true,  
  imports: [CommonModule , RouterLink],
  styleUrls: ['./profile-menu.css']
})
export class ProfileMenuComponent implements OnInit {
  open = false;
  user: any = null;
  private authSub!: Subscription;

constructor(private authService: AuthService,private sanitizer: DomSanitizer) {}
  ngOnInit() {
  this.authSub = this.authService.currentUser$.subscribe(userData => {
      this.user = userData;
      console.log("Utilisateur reçu dans le menu:", this.user);
    });
  }

  getSafeIcon(svgString: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(svgString);
  }

  toggleMenu() { this.open = !this.open; }

  logout() {
    this.authService.logout();
    this.open = false;
  }

  ngOnDestroy() {
    // Toujours se désabonner pour éviter les fuites de mémoire
    if (this.authSub) this.authSub.unsubscribe();
  }

  get menuItems() {
    if (this.user) {
      return [
        { label: 'Mon profil', color: '#6366f1', icon: this.sanitizer.bypassSecurityTrustHtml('...') }, // Insérez le SVG ici
        { label: 'Mes réservations', color: '#8b5cf6', icon: '...' },
        { label: 'Favoris', color: '#ec4899', icon: '...' }
      ];
    }
    return [
      { label: 'Se connecter', color: '#6366f1', icon: '...' },
      { label: "S'inscrire", color: '#8b5cf6', icon: '...' }
    ];
  }
}