import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    // Vérification si l'utilisateur est connecté
    if (this.authService.isLoggedIn()) {
      return true; // L'utilisateur est connecté, on le laisse passer
    } else {
      // Si l'utilisateur n'est pas connecté, on le redirige vers la page de connexion
      this.router.navigate(['/login']);
      return false; // Empêcher l'accès à la route protégée
    }
  }
}
