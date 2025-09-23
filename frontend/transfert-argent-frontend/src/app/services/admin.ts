import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private baseUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  // Méthodes pour les utilisateurs
  getUsers(page: number = 0, size: number = 10, sortBy: string = 'createdAt', direction: string = 'desc'): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.get(`${this.baseUrl}/users`, { params });
  }

  // Méthode avec logging détaillé
  getTransactions(page: number = 0, size: number = 10, sortBy: string = 'dateTransaction', direction: string = 'desc'): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);

    const url = `${this.baseUrl}/transactions`;
    console.log('🔄 AdminService - Requesting transactions from:', url);
    console.log('📋 Parameters:', { page, size, sortBy, direction });

    return this.http.get(url, { params }).pipe(
      tap(response => {
        console.log('✅ AdminService - Transactions response:', response);
      }),
      catchError(error => {
        console.error('❌ AdminService - Transactions error:', error);
        console.error('❌ Error status:', error.status);
        console.error('❌ Error message:', error.message);
        console.error('❌ Error URL:', error.url);
        return throwError(() => error);
      })
    );
  }

  toggleUserStatus(userId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/users/${userId}/status`, {});
  }

  updateUserRole(userId: number, newRole: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/users/${userId}/role`, {}, {
      params: { newRole }
    });
  }

  // Méthodes pour les comptes
  getComptes(page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get(`${this.baseUrl}/comptes`, { params });
  }

  toggleCompteStatus(compteId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/comptes/${compteId}/status`, {});
  }

  // Méthodes pour les statistiques
  getStatistics(): Observable<any> {
    console.log('🔄 AdminService - Requesting statistics');
    return this.http.get(`${this.baseUrl}/statistics`).pipe(
      tap(response => console.log('✅ Statistics response:', response)),
      catchError(error => {
        console.error('❌ Statistics error:', error);
        return throwError(() => error);
      })
    );
  }

  getTransactionStats(start: string, end: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/transactions/stats`, {
      params: { start, end }
    });
  }

  effectuerDepot(compteId: number, montant: number, motif: string = ''): Observable<any> {
    return this.http.post(`${this.baseUrl}/comptes/${compteId}/depot`, {}, {
      params: { montant: montant.toString(), motif }
    });
  }

  effectuerRetrait(compteId: number, montant: number, motif: string = ''): Observable<any> {
    return this.http.post(`${this.baseUrl}/comptes/${compteId}/retrait`, {}, {
      params: { montant: montant.toString(), motif }
    });
  }

  // Méthode de test pour vérifier la connexion
  testAdminEndpoint(): Observable<any> {
    console.log('🔄 Testing admin endpoint...');
    return this.http.get(`${this.baseUrl}/test`).pipe(
      tap(response => console.log('✅ Test endpoint response:', response)),
      catchError(error => {
        console.error('❌ Test endpoint error:', error);
        return throwError(() => error);
      })
    );
  }
}