import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { UserMenuComponent } from '../user-menu/user-menu/user-menu';
import { AuthService } from '../../services/AuthService';



@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  standalone: true,
  imports: [CommonModule, RouterModule, UserMenuComponent],
  styleUrls: ['./header.css']
})


export class HeaderComponent implements OnInit {
  isMenuCollapsed = true;


  constructor(public auth: AuthService) { }

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
