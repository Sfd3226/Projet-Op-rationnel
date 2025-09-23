import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin'; 
import { RouterModule } from '@angular/router';

interface Compte {
  id: number;
  numeroTelephone: string;
  solde: number;
  typeCompte: string;
  dateCreation: string;
  active: boolean;
  user: {
    id: number;
    nom: string;
    prenom: string;
    telephone: string;
  };
}

@Component({
  selector: 'app-admin-comptes',
  standalone: true,
  imports: [CommonModule, FormsModule ,RouterModule],
  templateUrl: './admin-comptes.html',
  styleUrls: ['./admin-comptes.scss']
})
export class AdminComptesComponent implements OnInit {
  comptes: Compte[] = [];
  filteredComptes: Compte[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  searchTerm = '';
  selectedStatus = 'ALL';
  selectedType = 'ALL';

  // Pour les opérations
  showOperationModal = false;
  operationType: 'ACTIVATE' | 'DEACTIVATE' = 'ACTIVATE';
  selectedCompte: Compte | null = null;

  // Exposer Math pour le template
  Math = Math;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadComptes();
  }

  loadComptes(): void {
    this.loading = true;
    this.adminService.getComptes(this.currentPage, this.pageSize).subscribe({
      next: (response: any) => {
        this.comptes = response.content;
        this.filteredComptes = [...this.comptes];
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors du chargement des comptes';
        this.loading = false;
        console.error('Erreur comptes:', error);
      }
    });
  }

  filterComptes(): void {
    this.filteredComptes = this.comptes.filter(compte => {
      const matchesSearch = 
        compte.numeroTelephone.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        compte.user.nom.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        compte.user.prenom.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        compte.typeCompte.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesStatus = this.selectedStatus === 'ALL' || 
        (this.selectedStatus === 'ACTIVE' && compte.active) ||
        (this.selectedStatus === 'INACTIVE' && !compte.active);
      
      const matchesType = this.selectedType === 'ALL' || compte.typeCompte === this.selectedType;
      
      return matchesSearch && matchesStatus && matchesType;
    });
  }

  onSearchChange(): void {
    this.filterComptes();
  }

  onStatusChange(): void {
    this.filterComptes();
  }

  onTypeChange(): void {
    this.filterComptes();
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadComptes();
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

  getStatusBadgeClass(active: boolean): string {
    return active ? 'badge-success' : 'badge-secondary';
  }

  // Nouvelle méthode pour les badges de type
  getTypeBadgeClass(type: string): string {
    switch (type.toLowerCase()) {
      case 'epargne':
        return 'badge-info';
      case 'courant':
        return 'badge-secondary';
      case 'business':
        return 'badge-warn';
      default:
        return 'badge-secondary';
    }
  }

  getStatusText(active: boolean): string {
    return active ? 'Actif' : 'Inactif';
  }

  toggleCompteStatus(compte: Compte): void {
    this.adminService.toggleCompteStatus(compte.id).subscribe({
      next: (updatedCompte: any) => {
        compte.active = updatedCompte.active;
        this.successMessage = `Compte ${compte.numeroTelephone} ${updatedCompte.active ? 'activé' : 'désactivé'}`;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors de la modification du statut';
        console.error('Erreur toggle status:', error);
      }
    });
  }

  confirmToggleStatus(compte: Compte): void {
    this.selectedCompte = compte;
    this.operationType = compte.active ? 'DEACTIVATE' : 'ACTIVATE';
    this.showOperationModal = true;
  }

  executeOperation(): void {
    if (!this.selectedCompte) return;

    this.adminService.toggleCompteStatus(this.selectedCompte.id).subscribe({
      next: (updatedCompte: any) => {
        this.selectedCompte!.active = updatedCompte.active;
        this.successMessage = `Compte ${this.selectedCompte!.numeroTelephone} ${updatedCompte.active ? 'activé' : 'désactivé'}`;
        this.closeOperationModal();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors de la modification du statut';
        this.closeOperationModal();
        console.error('Erreur toggle status:', error);
      }
    });
  }

  closeOperationModal(): void {
    this.showOperationModal = false;
    this.selectedCompte = null;
  }

  viewCompteDetails(compte: Compte): void {
    console.log('Voir détails du compte:', compte);
    alert(`Détails du compte: ${compte.numeroTelephone}\nPropriétaire: ${compte.user.prenom} ${compte.user.nom}\nSolde: ${this.formatCurrency(compte.solde)}`);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'XOF'
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR');
  }
}