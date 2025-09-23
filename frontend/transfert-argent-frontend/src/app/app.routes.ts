import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { LayoutComponent } from './components/layout/layout';
import { DashboardComponent } from './components/dashboard/dashboard';
import { ComptesComponent } from './components/comptes/comptes';
import { TransfertComponent } from './components/transfert/transfert';
import { HistoriqueComponent } from './components/historique/historique';
import { ProfileComponent } from './components/profile/profile';
import { AuthGuard } from './guards/auth-guard';
import { AdminGuard } from './guards/admin-guard';

// Import des composants admin
import { AdminDepotComponent } from './components/admin/admin-depot/admin-depot';
import { AdminRetraitComponent } from './components/admin/admin-retrait/admin-retrait';

import { AdminDashboardComponent } from './components/admin/admin-dashboard/admin-dashboard';
import { AdminUsersComponent } from './components/admin/admin-users/admin-users';
import { AdminTransactionsComponent } from './components/admin/admin-transactions/admin-transactions';
import { AdminComptesComponent } from './components/admin/admin-comptes/admin-comptes'; // ← Import ajouté
import { AdminLayoutComponent } from './components/admin/admin-layout/admin-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'comptes', component: ComptesComponent },
      { path: 'transfert', component: TransfertComponent },
      { path: 'historique', component: HistoriqueComponent },
      { path: 'profile', component: ProfileComponent }
    ]
  },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard, AdminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'transactions', component: AdminTransactionsComponent },
      { path: 'comptes', component: AdminComptesComponent }, // ← Route ajoutée
        { path: 'depot', component: AdminDepotComponent },     // ✅ Dépôt
    { path: 'retrait', component: AdminRetraitComponent }  // ✅ Retrait
    ]
  },
  { path: '**', redirectTo: 'login' }
];