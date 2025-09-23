import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction';
import { TransactionDTO } from '../../models/transaction.dto';
import { AuthService } from '../../services/auth'; 
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-historique',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './historique.html',
  styleUrls: ['./historique.scss']
})
export class HistoriqueComponent implements OnInit {
  transactions: TransactionDTO[] = [];
  filteredTransactions: TransactionDTO[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  currentFilter: 'all' | 'sent' | 'received' = 'all';

  // Filtres
  searchTerm = '';
  statutFilter = '';
  dateDebut = '';
  dateFin = '';

  // Background Nebula
  particles: any[] = [];
  mouse = { x: 0, y: 0 };
  canvas!: HTMLCanvasElement;
  ctx!: CanvasRenderingContext2D;

  constructor(
    private transactionService: TransactionService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initNebulaBg();
    this.loadHistorique();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  loadHistorique(): void {
    this.loading = true;
    this.errorMessage = '';
    let observable$: Observable<TransactionDTO[]>;

    switch (this.currentFilter) {
      case 'sent':
        observable$ = this.transactionService.getTransactionsEnvoyees();
        break;
      case 'received':
        observable$ = this.transactionService.getTransactionsRecues();
        break;
      default:
        observable$ = this.transactionService.getHistoriqueComplet();
    }

    observable$.subscribe({
      next: (transactions: TransactionDTO[]) => {
        this.transactions = transactions;
        this.applyFilters();
        this.loading = false;
      },
      error: (error: any) => {
        this.errorMessage = 'Erreur lors du chargement de l\'historique';
        this.loading = false;
        console.error(error);
      }
    });
  }

  // ----------------- Télécharger reçu -----------------
  downloadReceipt(transaction: TransactionDTO): void {
    this.errorMessage = '';
    if (transaction.receiptNumero) {
      this.transactionService.downloadReceiptByNumero(transaction.receiptNumero).subscribe({
        next: (blob: Blob) => this.handleDownload(blob, `recu-${transaction.receiptNumero}.pdf`),
        error: () => this.errorMessage = 'Erreur téléchargement reçu'
      });
    } else if (transaction.id) {
      this.transactionService.downloadReceipt(transaction.id).subscribe({
        next: (blob: Blob) => this.handleDownload(blob, `recu-${transaction.id}.pdf`),
        error: () => this.errorMessage = 'Reçu non disponible'
      });
    }
  }

  private handleDownload(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
    this.successMessage = 'Reçu téléchargé !';
    setTimeout(() => this.successMessage = '', 3000);
  }

  // ----------------- Rembourser -----------------
  rembourserTransaction(transaction: TransactionDTO): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!transaction.id) return;

    this.transactionService.rembourserTransaction(transaction.id).subscribe({
      next: (updatedTx: TransactionDTO) => {
        transaction.statut = updatedTx.statut;
        this.successMessage = `Transaction #${transaction.id} remboursée avec succès !`;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        console.error(error);
        this.errorMessage = `Erreur lors du remboursement de la transaction #${transaction.id}`;
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  // ----------------- Filtres -----------------
  applyFilters(): void {
    this.filteredTransactions = this.transactions.filter(tx => {
      const matchesSearch = this.searchTerm === '' || 
        (tx.compteDestinationNumero?.includes(this.searchTerm)) ||
        (tx.compteSourceNumero?.includes(this.searchTerm)) ||
        tx.montant.toString().includes(this.searchTerm);

      const matchesStatut = this.statutFilter === '' || tx.statut === this.statutFilter;

      let matchesDate = true;
      if (this.dateDebut) matchesDate = new Date(tx.dateTransaction) >= new Date(this.dateDebut);
      if (this.dateFin) {
        const dateFin = new Date(this.dateFin);
        dateFin.setHours(23,59,59);
        matchesDate = matchesDate && new Date(tx.dateTransaction) <= dateFin;
      }

      return matchesSearch && matchesStatut && matchesDate;
    });

    this.filteredTransactions.sort((a,b) => new Date(b.dateTransaction).getTime() - new Date(a.dateTransaction).getTime());
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.statutFilter = '';
    this.dateDebut = '';
    this.dateFin = '';
    this.applyFilters();
  }

  setFilter(filter: 'all' | 'sent' | 'received') {
    this.currentFilter = filter;
    this.loadHistorique();
  }

  formatMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'XOF' }).format(montant);
  }

  formatDate(date: Date | string): string {
    return new Date(date).toLocaleDateString('fr-FR', { 
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' 
    });
  }

  // ----------------- Background nébuleuse -----------------
  initNebulaBg() {
    this.canvas = document.getElementById('nebula-bg') as HTMLCanvasElement;
    if (!this.canvas) return;
    this.ctx = this.canvas.getContext('2d')!;
    this.resizeCanvas();

    const colors = ['#2563eb','#06b6d4','#22c55e','#facc15','#f472b6'];
    for(let i=0;i<200;i++){
      this.particles.push({
        x: Math.random()*this.canvas.width,
        y: Math.random()*this.canvas.height,
        radius: Math.random()*2+1,
        dx: (Math.random()-0.5)*0.6,
        dy: (Math.random()-0.5)*0.6,
        color: colors[Math.floor(Math.random()*colors.length)]
      });
    }
    this.animateParticles();
  }

  animateParticles() {
    if (!this.ctx || !this.canvas) return;
    this.ctx.clearRect(0,0,this.canvas.width,this.canvas.height);
    for(const p of this.particles){
      p.x += p.dx; p.y += p.dy;
      if(p.x<0||p.x>this.canvas.width) p.dx*=-1;
      if(p.y<0||p.y>this.canvas.height) p.dy*=-1;

      const offsetX = (this.mouse.x - this.canvas.width/2)*0.02;
      const offsetY = (this.mouse.y - this.canvas.height/2)*0.02;

      this.ctx.beginPath();
      this.ctx.arc(p.x+offsetX,p.y+offsetY,p.radius,0,Math.PI*2);
      const gradient = this.ctx.createRadialGradient(p.x+offsetX,p.y+offsetY,p.radius/2,p.x+offsetX,p.y+offsetY,p.radius);
      gradient.addColorStop(0,p.color);
      gradient.addColorStop(1,'rgba(0,0,0,0)');
      this.ctx.fillStyle = gradient;
      this.ctx.fill();
    }
    requestAnimationFrame(()=>this.animateParticles());
  }

  @HostListener('window:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    this.mouse = { x: event.clientX, y: event.clientY };
  }

  @HostListener('window:resize')
  onResize() {
    this.resizeCanvas();
  }

  resizeCanvas() {
    if (!this.canvas) return;
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;
  }
}
