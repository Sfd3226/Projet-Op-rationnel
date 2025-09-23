import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TransfertService } from '../../services/transfert.service';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-transfert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './transfert.html',
  styleUrls: ['./transfert.scss']
})
export class TransfertComponent implements OnInit {
  transfertForm: FormGroup;
  loading = false;
  message = '';
  frais: number = 0;
  total: number = 0;

  // Background Nebula
  particles: any[] = [];
  mouse = { x: 0, y: 0 };
  canvas!: HTMLCanvasElement;
  ctx!: CanvasRenderingContext2D;

  constructor(
    private fb: FormBuilder,
    private transfertService: TransfertService,
    private authService: AuthService,
    private router: Router
  ) {
    this.transfertForm = this.fb.group({
      telephoneDestinataire: ['', [Validators.required, Validators.pattern(/^[0-9]+$/)]],
      montant: ['', [Validators.required, Validators.min(100), Validators.max(1000000)]]
    });

    this.transfertForm.get('montant')?.valueChanges.subscribe(montant => {
      this.calculerFraisEtTotal(montant);
    });
  }

  ngOnInit(): void {
    this.initNebulaBg();
  }

  calculerFraisEtTotal(montant: number) {
    if (montant && !isNaN(montant)) {
      this.frais = montant * 0.01;
      this.total = montant + this.frais;
    } else {
      this.frais = 0;
      this.total = 0;
    }
  }

  submit() {
    if (this.transfertForm.invalid) {
      this.message = 'Veuillez corriger les erreurs du formulaire.';
      return;
    }

    this.loading = true;
    this.message = '';

    const transfertData = {
      telephoneDestinataire: this.transfertForm.get('telephoneDestinataire')?.value,
      montant: this.transfertForm.get('montant')?.value
    };

    this.transfertService.effectuerTransfert(transfertData).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.message = 'Transfert effectué avec succès ! ✅';

        setTimeout(() => {
          this.transfertForm.reset();
          this.frais = 0;
          this.total = 0;
          this.message = '';
        }, 3000);
      },
      error: (error) => {
        this.loading = false;
        this.message = error.error?.message || 'Erreur lors du transfert';
        console.error('Erreur transfert:', error);
      }
    });
  }

  get telephoneDestinataire() {
    return this.transfertForm.get('telephoneDestinataire');
  }

  get montant() {
    return this.transfertForm.get('montant');
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
}