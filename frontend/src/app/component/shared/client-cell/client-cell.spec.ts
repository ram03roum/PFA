import { ComponentFixture, TestBed } from '@angular/core/testing';

import {ClientCellComponent} from './client-cell';

describe('ClientCellComponent', () => {
  let component: ClientCellComponent;
  let fixture: ComponentFixture<ClientCellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientCellComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientCellComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
