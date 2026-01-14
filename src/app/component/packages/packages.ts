import { Component, OnInit } from '@angular/core';
import { DataService } from '../../services/data';
import { Package } from '../../models/package.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-packages', 
  standalone: true,  
  imports: [CommonModule],
  templateUrl: './packages.html',
  styleUrls: ['./packages.css']
})
export class PackagesComponent implements OnInit {
  packages: Package[] = [];

  constructor(private dataService: DataService) { }

  ngOnInit(): void {
    this.packages = this.dataService.getPackages();
  }

  getStars(rating: number): any[] {
    return Array(rating).fill(0);
  }

  bookPackage(packageId: number): void {
    alert(`Booking package ${packageId}`);
  }
}