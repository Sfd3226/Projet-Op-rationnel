import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Historique } from '../models/historique';

@Injectable({
  providedIn: 'root'
})
export class HistoriqueService {
  private apiUrl = 'http://localhost:8080/api/historique';

  constructor(private http: HttpClient) {}

  

  // Récupérer l’historique des transferts
  getHistorique(): Observable<Historique[]> {
    return this.http.get<Historique[]>(this.apiUrl);
  }
}
