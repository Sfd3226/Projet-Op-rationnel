import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../../services/transaction';
import { AdminService } from '../../../services/admin';

interface Transaction {
  id: number;
  montant: number;
  frais: number;
  statut: string;
  dateTransaction: string;
  compteSource?: { numeroTelephone: string };
  compteDestination?: { numeroTelephone: string };
  type?: string;
  numeroReceipt?: string;
}

@Component({
  selector: 'app-admin-retrait',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-retrait.html',
  styleUrls: ['./admin-retrait.scss']
})
export class AdminRetraitComponent implements OnInit {
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

  // Formulaire Retrait
  operationCompteId = 0;
  operationMontant = 0;
  operationMotif = '';

  constructor(
    private adminService: AdminService,
    private transactionService: TransactionService
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getTransactions(this.currentPage, this.pageSize, 'dateTransaction', 'desc')
      .subscribe({
        next: (response: any) => {
          this.transactions = (response.content || [])
            .map((tx: Transaction) => {
              // Assigner le type si absent
              if (!tx.type) {
                if (tx.compteSource && !tx.compteDestination) tx.type = 'RETRAIT';
                else if (!tx.compteSource && tx.compteDestination) tx.type = 'DÉPÔT';
                else if (tx.compteSource && tx.compteDestination) tx.type = 'TRANSFERT';
                else tx.type = 'AUTRE';
              }
              return tx;
            })
            .filter((tx: Transaction) => tx.type?.toUpperCase() === 'RETRAIT');

          this.totalPages = response.totalPages || 1;
          this.totalElements = response.totalElements || this.transactions.length;
          this.filteredTransactions = [...this.transactions];
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = 'Erreur lors du chargement des retraits';
          console.error('Erreur chargement retraits:', error);
        }
      });
  }

  // Filtrage simple
  onSearchChange(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredTransactions = this.transactions.filter((tx: Transaction) =>
      tx.id.toString().includes(term) ||
      tx.compteSource?.numeroTelephone?.toLowerCase().includes(term) ||
      tx.compteDestination?.numeroTelephone?.toLowerCase().includes(term)
    );
  }

  // Pagination
  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadTransactions();
    }
  }

  getPages(): number[] {
    const pages: number[] = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, start + 4);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'XOF' }).format(amount);
  }

  getStatusBadgeClass(statut: string): string {
    switch (statut) {
      case 'SUCCES': return 'badge-success';
      case 'ECHEC': return 'badge-danger';
      case 'EN_ATTENTE': return 'badge-warning';
      default: return 'badge-secondary';
    }
  }

  annulerTransaction(transactionId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir annuler cette transaction ?')) return;
    this.transactionService.annulerTransaction(transactionId).subscribe({
      next: () => {
        this.successMessage = 'Retrait annulé avec succès';
        this.loadTransactions();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur lors de l\'annulation';
        console.error('Erreur annulation:', error);
      }
    });
  }

  downloadReceipt(numeroReceipt: string | undefined, txId: number): void {
    if (!numeroReceipt) return;
    this.transactionService.downloadReceiptByNumero(numeroReceipt)
      .subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reçu_${txId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      }, error => console.error('Erreur téléchargement reçu:', error));
  }

  // Formulaire Retrait
  effectuerRetrait(): void {
    if (this.operationCompteId <= 0 || this.operationMontant <= 0) {
      this.errorMessage = 'Veuillez saisir un compte et un montant valides';
      return;
    }
    this.adminService.effectuerRetrait(this.operationCompteId, this.operationMontant, this.operationMotif)
      .subscribe({
        next: () => {
          this.successMessage = 'Retrait effectué avec succès';
          this.operationCompteId = 0;
          this.operationMontant = 0;
          this.operationMotif = '';
          this.loadTransactions();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          this.errorMessage = 'Erreur lors du retrait';
          console.error('Erreur retrait:', error);
        }
      });
  }
}
