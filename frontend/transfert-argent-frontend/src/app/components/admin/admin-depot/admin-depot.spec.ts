import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminDepot } from './admin-depot';

describe('AdminDepot', () => {
  let component: AdminDepot;
  let fixture: ComponentFixture<AdminDepot>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDepot]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminDepot);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
