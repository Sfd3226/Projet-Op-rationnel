import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRetrait } from './admin-retrait';

describe('AdminRetrait', () => {
  let component: AdminRetrait;
  let fixture: ComponentFixture<AdminRetrait>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRetrait]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminRetrait);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
