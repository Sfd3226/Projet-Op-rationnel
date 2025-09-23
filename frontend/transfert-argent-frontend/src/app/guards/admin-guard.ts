// guards/admin.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const userInfo = this.authService.getUserInfo();
    
    // Vérifiez si l'utilisateur est admin 
    const isAdmin = userInfo && userInfo.role === 'ADMIN'; 
    
    if (this.authService.isLoggedIn() && isAdmin) {
      return true;
    } else {
      this.router.navigate(['/dashboard']);
      return false;
    }
  }
}