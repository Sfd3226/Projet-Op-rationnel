import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin';

interface Transaction {
  id: number;
  montant: number;
  statut: string;
  dateTransaction: string;
  compteDestination: any;
}

@Component({
  selector: 'app-admin-depot',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './admin-depot.html',
  styleUrls: ['./admin-depot.scss']
})
export class AdminDepotComponent implements OnInit {
  compteId: number = 0;
  montant: number = 0;
  motif: string = '';
  loading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  transactions: Transaction[] = [];
  currentPage: number = 0;
  pageSize: number = 10;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  // Formulaire Dépôt
  effectuerDepot(): void {
    if (this.compteId <= 0 || this.montant <= 0) {
      this.errorMessage = 'Veuillez saisir un compte et un montant valides.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.effectuerDepot(this.compteId, this.montant, this.motif)
      .subscribe({
        next: () => {
          this.successMessage = 'Dépôt effectué avec succès.';
          this.compteId = 0;
          this.montant = 0;
          this.motif = '';
          this.loadTransactions();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors du dépôt.';
          console.error('Erreur dépôt:', err);
          this.loading = false;
        },
        complete: () => {
          this.loading = false;
        }
      });
  }

  // Charger l'historique des dépôts
  loadTransactions(): void {
    this.loading = true;
    this.adminService.getTransactions(this.currentPage, this.pageSize, 'dateTransaction', 'desc')
      .subscribe({
        next: (response: any) => {
          // Filtrer uniquement les dépôts
          this.transactions = (response.content || []).filter((tx: Transaction) => 
            !tx.compteDestination ? false : tx.compteDestination && !tx.compteDestination.numeroSource
          );
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors du chargement des dépôts.';
          console.error('Erreur chargement dépôts:', err);
          this.loading = false;
        }
      });
  }

  // Formatage monétaire XOF
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'XOF' }).format(amount);
  }
}
