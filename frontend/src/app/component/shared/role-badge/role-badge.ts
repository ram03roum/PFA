// role-badge.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-role-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './role-badge.html',
  styleUrls: ['./role-badge.css']
})
export class RoleBadgeComponent {
  @Input() role: string = '';

  getBadgeClass(): string {
    return this.role.toLowerCase();
  }
}