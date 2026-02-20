// status-badge.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status-badge.html',  // ← Pointeur vers HTML
  styleUrls: ['./status-badge.css']    // ← Pointeur vers CSS
})
export class StatusBadgeComponent {
  @Input() status: string = '';

  private statusMap: any = {
    'confirmée': { class: 'confirmed', icon: '✓' },
    'en attente': { class: 'pending', icon: '⏳' },
    'annulée': { class: 'cancelled', icon: '✕' },
    'actif': { class: 'actif', icon: '●' },
    'inactif': { class: 'inactif', icon: '○' },
    'suspendu': { class: 'suspendu', icon: '⊘' },
    'validé': { class: 'validé', icon: '✓' },
    'en cours': { class: 'en-cours', icon: '◐' },
    'ouvert': { class: 'ouvert', icon: '○' },
    'résolu': { class: 'résolu', icon: '✓' },
    'à valider': { class: 'à-valider', icon: '◐' },
  };

  getBadgeClass(): string {
    return this.statusMap[this.status]?.class || '';
  }

  getIcon(): string {
    return this.statusMap[this.status]?.icon || '?';
  }
}