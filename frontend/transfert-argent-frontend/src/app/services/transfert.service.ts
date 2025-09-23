import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';
import { TransfertRequest } from '../models/transfert-request';
import { Transaction } from '../models/transaction';

@Injectable({
  providedIn: 'root'
})
export class TransfertService {
  private baseUrl = 'http://localhost:8080/api/transfert';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // Headers avec le token JWT
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Effectuer un transfert
  effectuerTransfert(transfertRequest: TransfertRequest): Observable<Transaction> {
    return this.http.post<Transaction>(
      this.baseUrl, 
      transfertRequest, 
      { headers: this.getHeaders() }
    );
  }

  // Obtenir l'historique des transactions
  getHistoriqueTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(
      `${this.baseUrl}/historique`,
      { headers: this.getHeaders() }
    );
  }

  // Obtenir les transactions envoyées
  getTransactionsEnvoyees(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(
      `${this.baseUrl}/envoyees`,
      { headers: this.getHeaders() }
    );
  }

  // Obtenir les transactions reçues
  getTransactionsRecues(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(
      `${this.baseUrl}/recues`,
      { headers: this.getHeaders() }
    );
  }
}