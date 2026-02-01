import { TestBed } from '@angular/core/testing';

import { ValidationService } from './validation';  // ← Import du service

describe('Validation', () => {
  let service: ValidationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ValidationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
