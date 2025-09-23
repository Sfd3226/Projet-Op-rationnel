import { TestBed } from '@angular/core/testing';

import { Receipt } from './receipt';

describe('Receipt', () => {
  let service: Receipt;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Receipt);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
