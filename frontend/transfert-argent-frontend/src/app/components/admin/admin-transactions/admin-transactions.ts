import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../../services/transaction';
import { AdminService } from '../../../services/admin';
import { AuthService } from '../../../services/auth';

interface Transaction {
  id: number;
  montant: number;
  frais: number;
  statut: string;
  dateTransaction: string;
  compteSource: any;
  compteDestination: any;
  type: string;
  numeroReceipt?: string;
}

@Component({
  selector: 'app-admin-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './admin-transactions.html',
  styleUrls: ['./admin-transactions.scss']
})
export class AdminTransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
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
  startDate: string = '';
  endDate: string = '';

  // ✅ Gestion Modal Dépôt / Retrait
  showOperationModal = false;
  operationType: 'DEPOT' | 'RETRAIT' = 'DEPOT';
  operationCompteId: number = 0;
  operationMontant: number = 0;
  operationMotif: string = '';

  Math = Math;

  constructor(
    private adminService: AdminService,
    private transactionService: TransactionService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  // ✅ Charger toutes les transactions
  loadTransactions(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getTransactions(this.currentPage, this.pageSize, 'dateTransaction', 'desc')
      .subscribe({
        next: (response: any) => {
          this.transactions = response.content || [];
          this.totalPages = response.totalPages || 1;
          this.totalElements = response.totalElements || this.transactions.length;
          this.processTransactions();
          this.loading = false;
        },
        error: (error: any) => {
          this.loading = false;
          this.errorMessage = 'Erreur lors du chargement des transactions';
          console.error('Erreur chargement transactions:', error);
        }
      });
  }

  // ✅ Déterminer le type si non défini
  processTransactions(): void {
    this.transactions.forEach(tx => {
      if (!tx.type) {
        if (!tx.compteSource && tx.compteDestination) tx.type = 'DÉPÔT';
        else if (tx.compteSource && !tx.compteDestination) tx.type = 'RETRAIT';
        else if (tx.compteSource && tx.compteDestination) tx.type = 'TRANSFERT';
        else tx.type = 'AUTRE';
      }
    });
    this.filteredTransactions = [...this.transactions];
    this.applyFilters();
  }

  // ✅ Filtrage
  filterTransactions(): void { this.applyFilters(); }

  private applyFilters(): void {
    this.filteredTransactions = this.transactions.filter(tx => {
      const matchesSearch =
        tx.id.toString().includes(this.searchTerm) ||
        (tx.compteSource?.numeroTelephone?.includes(this.searchTerm)) ||
        (tx.compteDestination?.numeroTelephone?.includes(this.searchTerm));

      const matchesStatus = this.selectedStatus === 'ALL' || tx.statut === this.selectedStatus;
      const matchesType = this.selectedType === 'ALL' || tx.type === this.selectedType;

      let matchesDate = true;
      if (this.startDate) matchesDate = new Date(tx.dateTransaction) >= new Date(this.startDate);
      if (this.endDate) {
        const endDate = new Date(this.endDate);
        endDate.setHours(23, 59, 59);
        matchesDate = matchesDate && new Date(tx.dateTransaction) <= endDate;
      }

      return matchesSearch && matchesStatus && matchesType && matchesDate;
    });
  }

  onSearchChange(): void { this.filterTransactions(); }
  onStatusChange(): void { this.filterTransactions(); }
  onTypeChange(): void { this.filterTransactions(); }
  onDateChange(): void { this.filterTransactions(); }

  // ✅ Pagination
  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadTransactions();
    }
  }

  getPages(): number[] {
    const pages = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, start + 4);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  // ✅ Badges
  getStatusBadgeClass(statut: string): string {
    switch (statut) {
      case 'SUCCES': return 'badge-success';
      case 'ECHEC': return 'badge-danger';
      case 'EN_ATTENTE': return 'badge-warning';
      default: return 'badge-secondary';
    }
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'DÉPÔT': return 'badge-info';
      case 'RETRAIT': return 'badge-primary';
      case 'TRANSFERT': return 'badge-success';
      default: return 'badge-secondary';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'XOF' }).format(amount);
  }

  // ✅ Annulation transaction
  annulerTransaction(transactionId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir annuler cette transaction ?')) return;
    this.transactionService.annulerTransaction(transactionId).subscribe({
      next: () => {
        this.successMessage = 'Transaction annulée avec succès';
        this.loadTransactions();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur lors de l\'annulation';
        console.error('Erreur annulation:', error);
      }
    });
  }

  // ✅ Télécharger reçu
  downloadReceipt(transaction: Transaction): void {
    if (!transaction.numeroReceipt) return;
    this.transactionService.downloadReceiptByNumero(transaction.numeroReceipt)
      .subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reçu_${transaction.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      }, error => console.error('Erreur téléchargement reçu:', error));
  }

  // ✅ Modal Dépôt / Retrait
  openOperationModal(type: 'DEPOT' | 'RETRAIT'): void {
    this.operationType = type;
    this.operationCompteId = 0;
    this.operationMontant = 0;
    this.operationMotif = '';
    this.showOperationModal = true;
  }

  closeOperationModal(): void { this.showOperationModal = false; }

  executeOperation(): void {
    if (this.operationMontant <= 0 || this.operationCompteId <= 0) {
      this.errorMessage = 'Veuillez saisir un montant et un compte valides.';
      return;
    }
    const operation = this.operationType === 'DEPOT'
      ? this.adminService.effectuerDepot(this.operationCompteId, this.operationMontant, this.operationMotif)
      : this.adminService.effectuerRetrait(this.operationCompteId, this.operationMontant, this.operationMotif);

    operation.subscribe({
      next: () => {
        this.successMessage = `${this.operationType} effectué avec succès.`;
        this.closeOperationModal();
        this.loadTransactions();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: error => {
        this.errorMessage = `Erreur lors du ${this.operationType.toLowerCase()}`;
        console.error('Erreur operation:', error);
      }
    });
  }
}
