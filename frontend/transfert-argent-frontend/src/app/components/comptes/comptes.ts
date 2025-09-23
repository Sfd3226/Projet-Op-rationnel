import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileService, UserProfile } from '../../services/profile';
import { AuthService } from '../../services/auth';
import { animateValue } from '../dashboard/utils/animateValue'; 
import { RouterModule } from '@angular/router';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule , RouterModule],
  templateUrl: './comptes.html',
  styleUrls: ['./comptes.scss']
})
export class ComptesComponent implements OnInit {
  profile: UserProfile | null = null;
  loading = false;
  errorMessage = '';
  soldeMasque: boolean = true;
  animatedSolde: number = 0;

  // Particules pour le background
  particles: any[] = [];
  mouse = { x: 0, y: 0 };
  canvas!: HTMLCanvasElement;
  ctx!: CanvasRenderingContext2D;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.initNebulaBg();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.errorMessage = '';

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.loading = false;
        
        // Animer le solde total
        if (this.getTotalSolde() > 0) {
          animateValue(0, this.getTotalSolde(), 1500, val => this.animatedSolde = val);
        }
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors du chargement du dashboard';
        this.loading = false;
        console.error('Erreur dashboard:', error);
      }
    });
  }

  // ✅ Méthode pour masquer/afficher le solde
  toggleSolde(): void {
    this.soldeMasque = !this.soldeMasque;
  }

  logout(): void {
    this.authService.logout();
  }

  // ------------------ Background nébuleuse ------------------
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

      let offsetX = (this.mouse.x - this.canvas.width/2)*0.02;
      let offsetY = (this.mouse.y - this.canvas.height/2)*0.02;

      this.ctx.beginPath();
      this.ctx.arc(p.x+offsetX, p.y+offsetY, p.radius, 0, Math.PI*2);
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

  // ------------------ Utilitaires ------------------
  getTotalSolde(): number {
    if (!this.profile?.comptes) return 0;
    return this.profile.comptes.reduce((total, compte) => total + compte.solde, 0);
  }

  formatSolde(solde: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'XOF'
    }).format(solde);
  }

  getPhotoProfilUrl(): string {
    if (!this.profile?.photoProfil) return '/assets/default-avatar.png';
    return `http://localhost:8080/api/files/${this.profile.photoProfil}`;
  }

  getFirstName(): string {
    return this.profile?.prenom || 'Utilisateur';
  }

  getLastName(): string {
    return this.profile?.nom || '';
  }

  // ✅ Statistiques pour le dashboard
  getNombreComptes(): number {
    return this.profile?.comptes?.length || 0;
  }

  getDernierCompte(): string {
    if (!this.profile?.comptes || this.profile.comptes.length === 0) return 'Aucun';
    return this.profile.comptes[0].typeCompte;
  }
  floatingCircles: HTMLDivElement[] = [];

ngAfterViewInit(): void {
  // Après l'initialisation de la vue
  this.initNebulaBg();
  this.createFloatingCircles();
}

createFloatingCircles(): void {
  const numCircles = 8;
  const width = window.innerWidth;
  const height = window.innerHeight;

  for (let i = 0; i < numCircles; i++) {
    const circle = document.createElement('div');
    circle.className = 'floating-circle';
    const size = Math.random() * 180 + 100;
    circle.style.width = `${size}px`;
    circle.style.height = `${size}px`;
    circle.style.left = `${Math.random() * width}px`;
    circle.style.top = `${Math.random() * height}px`;
    document.body.appendChild(circle);
    this.floatingCircles.push(circle);
  }
}

}