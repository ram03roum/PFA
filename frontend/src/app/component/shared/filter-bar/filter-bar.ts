// filter-bar.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-filter-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './filter-bar.html',
  styleUrls: ['./filter-bar.css']
})
export class FilterBarComponent {
  @Input() filters: string[] = [];
  @Input() activeFilter: string = '';
  @Output() filterChange = new EventEmitter<string>();

  onFilterChange(filter: string): void {
    this.filterChange.emit(filter);
  }
}