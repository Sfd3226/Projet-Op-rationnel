import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

export interface UserInfo {
  id: number;
  firstName: string;
  lastName: string;
  telephone: string;
  email: string;
  photoProfil: string;
  role: string;
}

export interface LoginCredentials {
  telephone: string;
  password: string;
}


export interface RegisterData {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  password: string;
  pays: string;
  numeroPiece: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'jwt-token';
  
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();
  
  private userInfoSubject = new BehaviorSubject<UserInfo | null>(this.getUserInfoFromToken());
  public userInfo$ = this.userInfoSubject.asObservable();

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: any
  ) {}

  login(credentials: LoginCredentials): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.baseUrl}/login`, credentials)
      .pipe(
        tap(response => {
          this.saveToken(response.token);
          this.isLoggedInSubject.next(true);
          this.userInfoSubject.next(this.getUserInfoFromToken());
        })
      );
  }

  // Méthode d'inscription simplifiée : elle accepte directement l'objet FormData
  register(formData: FormData): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.baseUrl}/register`, formData)
      .pipe(
        tap(response => {
          this.saveToken(response.token);
          this.isLoggedInSubject.next(true);
          this.userInfoSubject.next(this.getUserInfoFromToken());
        })
      );
  }

  saveToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.tokenKey, token);
    }
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(this.tokenKey);
    }
    return null;
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.tokenKey);
    }
    this.isLoggedInSubject.next(false);
    this.userInfoSubject.next(null);
  }

  isLoggedIn(): boolean {
    return this.hasToken() && !this.isTokenExpired();
  }

  decodeToken(): any {
    const token = this.getToken();
    if (!token) return null;
    
    try {
      return jwtDecode(token);
    } catch (error) {
      console.error('Erreur lors du décodage du token:', error);
      this.logout();
      return null;
    }
  }

  isTokenExpired(): boolean {
    const decoded = this.decodeToken();
    if (!decoded || !decoded.exp) return true;
    
    return decoded.exp < Math.floor(Date.now() / 1000);
  }

  getUserInfoFromToken(): UserInfo | null {
    const decoded = this.decodeToken();
    if (!decoded) return null;
    
    return {
      id: decoded.id,
      firstName: decoded.firstName || decoded.prenom,
      lastName: decoded.lastName || decoded.nom,
      telephone: decoded.telephone,
      email: decoded.email,
      photoProfil: decoded.photoProfil || '',
      role: decoded.role || 'USER'
    };
  }

  getUserInfo(): UserInfo | null {
    return this.userInfoSubject.value;
  }

  hasRole(role: string): boolean {
    const userInfo = this.getUserInfo();
    return userInfo ? userInfo.role === role : false;
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  refreshUserInfo(): void {
    this.userInfoSubject.next(this.getUserInfoFromToken());
  }

  checkEmail(email: string): Observable<{ exists: boolean }> {
    return this.http.get<{ exists: boolean }>(`${this.baseUrl}/check-email`, {
      params: { email }
    });
  }

  checkTelephone(telephone: string): Observable<{ exists: boolean }> {
    return this.http.get<{ exists: boolean }>(`${this.baseUrl}/check-telephone`, {
      params: { telephone }
    });
  }
}