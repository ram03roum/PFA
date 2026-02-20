import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoleBadgeComponent } from './role-badge';

describe('RoleBadgeComponent', () => {
  let component: RoleBadgeComponent;
  let fixture: ComponentFixture<RoleBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoleBadgeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoleBadgeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
