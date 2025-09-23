import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recu } from '../models/recu';

@Injectable({
  providedIn: 'root'
})
export class ReceiptService {
  private apiUrl = 'http://localhost:8080/api/receipts';

  constructor(private http: HttpClient) {}

  // Générer un reçu pour une transaction
  getRecu(transactionId: number): Observable<Recu> {
    return this.http.get<Recu>(`${this.apiUrl}/${transactionId}`);
  }
}
