import { TestBed } from '@angular/core/testing';

import { RecommenadationServiceTs } from './recommenadation.service.ts';

describe('RecommenadationServiceTs', () => {
  let service: RecommenadationServiceTs;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RecommenadationServiceTs);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
