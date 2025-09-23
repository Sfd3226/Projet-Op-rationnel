import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Compte } from '../models/compte';
import { AuthService } from './auth';

@Injectable({ providedIn: 'root' })
export class CompteService {
  private baseUrl = 'http://localhost:8080/api/comptes';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getComptes(): Observable<Compte[]> {
    const userInfo = this.authService.getUserInfo();
    if (!userInfo) throw new Error("Utilisateur non connecté");
    return this.http.get<Compte[]>(`${this.baseUrl}/user/${userInfo.id}`);
  }

  depot(compteId: number, montant: number): Observable<Compte> {
    return this.http.post<Compte>(`${this.baseUrl}/depot?montant=${montant}`, {});
  }

  retrait(compteId: number, montant: number): Observable<Compte> {
    return this.http.post<Compte>(`${this.baseUrl}/retrait?montant=${montant}`, {});
  }
}
