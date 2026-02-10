import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProfileMenuComponent } from '../profile-menu/profile-menu';
import { AuthService } from '../../services/AuthService';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  standalone: true,  
  imports: [CommonModule, RouterModule, ProfileMenuComponent], 
  styleUrls: ['./header.css']
})

export class HeaderComponent implements OnInit {
  isMenuCollapsed = true;

  constructor(public authService: AuthService) { 
  }

  ngOnInit(): void {
  }

  toggleMenu(): void {
    this.isMenuCollapsed = !this.isMenuCollapsed;
    
  }

  scrollToSection(sectionId: string): void {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      this.isMenuCollapsed = true; // Fermer le menu mobile après clic
    }
  }

  bookNow(): void {
    alert('Booking functionality will be implemented soon!');
  }
}
