import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationDashboard } from './reservation-dashboard';

describe('ReservationDashboard', () => {
  let component: ReservationDashboard;
  let fixture: ComponentFixture<ReservationDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReservationDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReservationDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
