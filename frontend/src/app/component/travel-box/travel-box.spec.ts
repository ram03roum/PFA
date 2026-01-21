import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TravelBoxComponent } from './travel-box';

describe('TravelBox', () => {
  let component: TravelBoxComponent;
  let fixture: ComponentFixture<TravelBoxComponent>;  
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TravelBoxComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TravelBoxComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
