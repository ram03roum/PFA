// client-cell.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-client-cell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './client-cell.html',
  styleUrls: ['./client-cell.css']
})
export class ClientCellComponent {
  @Input() name: string = '';
  @Input() initials: string = '';
  @Input() color: string = '#6366f1';

  getGradient(): string {
    return `linear-gradient(135deg, ${this.color}, ${this.color}dd)`;
  }
}