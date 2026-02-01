import { Component } from '@angular/core';
import { PackageComponent } from '../../component/packages/packages';

@Component({
  selector: 'app-package-page',
  standalone: true,
  imports: [PackageComponent],
  templateUrl: './package-page.html',
})
export class PackagePage {}
