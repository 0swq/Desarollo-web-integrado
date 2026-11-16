import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageStock } from './manage-stock';

describe('ManageStock', () => {
  let component: ManageStock;
  let fixture: ComponentFixture<ManageStock>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageStock],
    }).compileComponents();

    fixture = TestBed.createComponent(ManageStock);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
