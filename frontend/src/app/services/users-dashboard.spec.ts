import { TestBed } from '@angular/core/testing';

import { UsersDashboard } from './users-dashboard';

describe('UsersDashboard', () => {
  let service: UsersDashboard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UsersDashboard);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
