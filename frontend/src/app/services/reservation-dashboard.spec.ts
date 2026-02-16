import { TestBed } from '@angular/core/testing';

import { ReservationDashboardTs } from './reservation-dashboard.js';

describe('ReservationDashboardTs', () => {
  let service: ReservationDashboardTs;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReservationDashboardTs);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
