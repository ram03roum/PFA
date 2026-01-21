import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PackageDetail } from './package-detail';

describe('PackageDetail', () => {
  let component: PackageDetail;
  let fixture: ComponentFixture<PackageDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PackageDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PackageDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
