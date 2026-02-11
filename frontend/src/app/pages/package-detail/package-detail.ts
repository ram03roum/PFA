import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PackageService } from '../../services/package.service';

@Component({
  selector: 'app-package-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './package-detail.html',
  styleUrls: ['./package-detail.css']
})
export class PackageDetail implements OnInit {

  package: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private packageService: PackageService
  ) { }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.package = this.packageService.getPackageById(id);
  }

  goBack() {
    this.router.navigate(['/packages']);
  }
}
