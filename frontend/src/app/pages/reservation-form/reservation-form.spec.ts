import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationFormComponent } from './reservation-form';

describe('ReservationFormComponent', () => {
  let component: ReservationFormComponent;
  let fixture: ComponentFixture<ReservationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReservationFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReservationFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
