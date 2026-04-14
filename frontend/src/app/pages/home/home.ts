import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderComponent } from '../../component/header/header.component';
import { Footer } from '../../component/footer/footer';
import { DestinationsPageComponent } from '../destinations-page/destinations-page';
import { DataService } from '../../services/data.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, Footer, DestinationsPageComponent]
})
export class HomeComponent implements OnInit, OnDestroy {

  searchQuery: string = '';

  // Removed searchSubject and searchSub - no more real-time search

  constructor(
    private router: Router,
    private dataService: DataService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    // No more search subscription - search only on button click
  }

  ngOnDestroy(): void {
    // No subscription to unsubscribe
  }

  onSearchSubmit(): void {
    if (!this.searchQuery.trim()) return;
    this.router.navigate(['/destinations'], {
      queryParams: { country: this.searchQuery.trim() }
    });
  }
}
