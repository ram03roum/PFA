import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Destimonials } from './testimonials';

describe('Destimonials', () => {
  let component: Destimonials;
  let fixture: ComponentFixture<Destimonials>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Destimonials]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Destimonials);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
