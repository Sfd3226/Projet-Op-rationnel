import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminComptes } from './admin-comptes';

describe('AdminComptes', () => {
  let component: AdminComptes;
  let fixture: ComponentFixture<AdminComptes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminComptes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminComptes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
