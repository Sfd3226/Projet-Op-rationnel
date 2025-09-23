import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth'; 

export interface UserProfile {
  prenom: string;
  nom: string;
  email: string;
  telephone: string;
  pays: string;
  photoProfil: string;
  comptes: CompteInfo[];
}

export interface CompteInfo {
  id: number;
  solde: number;
  typeCompte: string;
  numeroTelephone: string;
  dateCreation: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private baseUrl = 'http://localhost:8080/api/users';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/profile`, {
      headers: this.getHeaders()
    });
  }

  updateProfile(profile: Partial<UserProfile>): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.baseUrl}/profile`, profile, {
      headers: this.getHeaders()
    });
  }

  changePassword(passwordData: PasswordChangeRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/password`, passwordData, {
      headers: this.getHeaders()
    });
  }

  checkEmailExists(email: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/check-email?email=${email}`);
  }

  checkTelephoneExists(telephone: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/check-telephone?telephone=${telephone}`);
  }
}