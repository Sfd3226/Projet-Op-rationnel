import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin'; 

interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  role: string;
  enabled: boolean;
  createdAt: string;
  comptes: any[];
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.html',
  styleUrls: ['./admin-users.scss']
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  searchTerm = '';
  selectedRole = 'ALL';
  selectedStatus = 'ALL';

  // Pour la modification du rôle
  editingUserId: number | null = null;
  newRole: string = '';

  // Exposer Math pour l'utiliser dans le template
  Math = Math;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getUsers(this.currentPage, this.pageSize, 'createdAt', 'desc').subscribe({
      next: (response: any) => {
        this.users = response.content;
        this.filteredUsers = [...this.users];
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors du chargement des utilisateurs';
        this.loading = false;
        console.error('Erreur users:', error);
      }
    });
  }

  toggleUserStatus(user: User): void {
    this.adminService.toggleUserStatus(user.id).subscribe({
      next: (updatedUser: any) => {
        user.enabled = updatedUser.enabled;
        this.successMessage = `Statut de ${user.prenom} ${user.nom} ${updatedUser.enabled ? 'activé' : 'désactivé'}`;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors de la modification du statut';
        console.error('Erreur toggle status:', error);
      }
    });
  }

  startEditRole(user: User): void {
    this.editingUserId = user.id;
    this.newRole = user.role;
  }

  saveUserRole(user: User): void {
    if (!this.newRole || this.newRole === user.role) {
      this.cancelEdit();
      return;
    }

    this.adminService.updateUserRole(user.id, this.newRole).subscribe({
      next: (updatedUser: any) => {
        user.role = updatedUser.role;
        this.successMessage = `Rôle de ${user.prenom} ${user.nom} mis à jour`;
        setTimeout(() => this.successMessage = '', 3000);
        this.cancelEdit();
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors de la modification du rôle';
        console.error('Erreur update role:', error);
      }
    });
  }

  cancelEdit(): void {
    this.editingUserId = null;
    this.newRole = '';
  }

  filterUsers(): void {
    this.filteredUsers = this.users.filter(user => {
      const matchesSearch = 
        user.nom.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.prenom.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.telephone.includes(this.searchTerm);
      
      const matchesRole = this.selectedRole === 'ALL' || user.role === this.selectedRole;
      const matchesStatus = this.selectedStatus === 'ALL' || 
        (this.selectedStatus === 'ACTIVE' && user.enabled) ||
        (this.selectedStatus === 'INACTIVE' && !user.enabled);
      
      return matchesSearch && matchesRole && matchesStatus;
    });
  }

  onSearchChange(): void {
    this.filterUsers();
  }

  onRoleChange(): void {
    this.filterUsers();
  }

  onStatusChange(): void {
    this.filterUsers();
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadUsers();
    }
  }

  getPages(): number[] {
    const pages = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, start + 4);
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'badge-danger';
      case 'USER': return 'badge-primary';
      default: return 'badge-secondary';
    }
  }

  getStatusBadgeClass(enabled: boolean): string {
    return enabled ? 'badge-success' : 'badge-secondary';
  }

  getStatusText(enabled: boolean): string {
    return enabled ? 'Actif' : 'Inactif';
  }
}