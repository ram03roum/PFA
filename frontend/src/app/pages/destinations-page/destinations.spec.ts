import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DestinationsPageComponent } from './destinations-page';

describe('DestinationsComponent', () => {
  let component: DestinationsPageComponent;
  let fixture: ComponentFixture<DestinationsPageComponent>; 
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DestinationsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DestinationsPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
