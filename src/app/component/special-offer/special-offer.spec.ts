import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SpecialOffer } from './special-offer';

describe('SpecialOffer', () => {
  let component: SpecialOffer;
  let fixture: ComponentFixture<SpecialOffer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SpecialOffer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SpecialOffer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
