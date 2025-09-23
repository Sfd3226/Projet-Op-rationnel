import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { AuthService } from './auth';
import { TransactionDTO } from '../models/transaction.dto'; 

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private baseUrl = 'http://localhost:8080/api/transactions';
  private receiptsUrl = 'http://localhost:8080/api/receipts';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    if (!token) throw new Error('Utilisateur non authentifié');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Erreur inconnue';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 0: errorMessage = 'Impossible de se connecter au serveur'; break;
        case 401: errorMessage = 'Session expirée, veuillez vous reconnecter'; break;
        case 403: errorMessage = 'Accès non autorisé'; break;
        case 404: errorMessage = 'Ressource non trouvée'; break;
        case 500: errorMessage = 'Erreur interne du serveur'; break;
        default: errorMessage = error.error?.message || `Erreur ${error.status}: ${error.message}`;
      }
    }
    console.error('Erreur TransactionService:', error);
    return throwError(() => new Error(errorMessage));
  }

  // ----------------- Rembourser -----------------
  rembourserTransaction(id: number): Observable<TransactionDTO> {
    return this.http.post<TransactionDTO>(
      `${this.baseUrl}/${id}/rembourser`,
      {},
      { headers: this.getHeaders() }
    ).pipe(catchError(error => this.handleError(error)));
  }

  // ----------------- Télécharger reçu -----------------
  downloadReceiptByNumero(numero: string): Observable<Blob> {
    return this.http.get(`${this.receiptsUrl}/${numero}/download`, {
      headers: this.getHeaders(),
      responseType: 'blob'
    }).pipe(catchError(error => this.handleError(error)));
  }

  downloadReceipt(transactionId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${transactionId}/receipt`, {
      headers: this.getHeaders(),
      responseType: 'blob'
    }).pipe(catchError(error => this.handleError(error)));
  }

  // ----------------- Annuler transaction -----------------
  annulerTransaction(transactionId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/admin/${transactionId}/annuler`, {}, { headers: this.getHeaders() })
      .pipe(catchError(error => this.handleError(error)));
  }

  // ----------------- Historique -----------------
  getHistoriqueComplet(): Observable<TransactionDTO[]> {
    return this.http.get<TransactionDTO[]>(`${this.baseUrl}/historique`, { headers: this.getHeaders() })
      .pipe(catchError(error => this.handleError(error)));
  }

  getTransactionsEnvoyees(): Observable<TransactionDTO[]> {
    return this.http.get<TransactionDTO[]>(`${this.baseUrl}/envoyees`, { headers: this.getHeaders() })
      .pipe(catchError(error => this.handleError(error)));
  }

  getTransactionsRecues(): Observable<TransactionDTO[]> {
    return this.http.get<TransactionDTO[]>(`${this.baseUrl}/recues`, { headers: this.getHeaders() })
      .pipe(catchError(error => this.handleError(error)));
  }

  getTransactionById(id: number): Observable<TransactionDTO> {
    return this.http.get<TransactionDTO>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() })
      .pipe(catchError(error => this.handleError(error)));
  }

  // ----------------- Filtre par date -----------------
  getTransactionsByDateRange(startDate: Date, endDate: Date): Observable<TransactionDTO[]> {
    const params = {
      start: startDate.toISOString().split('T')[0],
      end: endDate.toISOString().split('T')[0]
    };
    return this.http.get<TransactionDTO[]>(`${this.baseUrl}/periode`, { headers: this.getHeaders(), params })
      .pipe(catchError(error => this.handleError(error)));
  }

  // ----------------- Statistiques (optionnel) -----------------
  getStatistiques(): Observable<any> {
    return this.http.get(`${this.baseUrl}/statistiques`, { headers: this.getHeaders() })
      .pipe(catchError(error => this.handleError(error)));
  }
}
