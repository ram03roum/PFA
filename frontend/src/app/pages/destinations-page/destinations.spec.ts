import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DestinationsPageComponent } from './destinations-page';

describe('DestinationsComponent', () => {
  let component: DestinationsPageComponent;
<<<<<<< HEAD
  let fixture: ComponentFixture<DestinationsPageComponent>; 
=======
  let fixture: ComponentFixture<DestinationsPageComponent>;
>>>>>>> yasmine
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DestinationsPageComponent]
    })
<<<<<<< HEAD
    .compileComponents();
=======
      .compileComponents();
>>>>>>> yasmine

    fixture = TestBed.createComponent(DestinationsPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
