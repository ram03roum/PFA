import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DestinationService } from '../../services/destination';

describe('Destinations', () => {
  let component: DestinationService;
  let fixture: ComponentFixture<DestinationService>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DestinationService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DestinationService);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
