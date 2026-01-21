import { TestBed } from '@angular/core/testing';
import { DestinationService } from './destination';

describe('Destination', () => {
  let service: DestinationService;  
  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DestinationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
