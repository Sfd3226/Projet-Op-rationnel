import { Component, HostListener, OnInit, AfterViewInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class LoginComponent implements OnInit, AfterViewInit {
  loginForm: FormGroup;
  loading = false;
  message = '';

  // Background Nebula
  particles: any[] = [];
  mouse = { x: 0, y: 0 };
  canvas!: HTMLCanvasElement;
  ctx!: CanvasRenderingContext2D;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.loginForm = this.fb.group({
      telephone: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.initNebulaBg();
    }
  }

  submit() {
    if (this.loginForm.invalid) return;
    this.loading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.authService.saveToken(res.token);

        const userInfo = this.authService.getUserInfo();

        if (userInfo && userInfo.role === 'ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err: any) => {
        this.loading = false;
        this.message = err?.error?.message || 'Erreur lors de la connexion';
      }
    });
  }

  get telephone() {
    return this.loginForm.get('telephone');
  }

  get password() {
    return this.loginForm.get('password');
  }

  // ------------------ Background nébuleuse ------------------
  initNebulaBg() {
    this.canvas = document.getElementById('nebula-bg') as HTMLCanvasElement;
    if (!this.canvas) return;

    this.ctx = this.canvas.getContext('2d')!;
    this.resizeCanvas();

    const colors = ['#2563eb','#06b6d4','#22c55e','#facc15','#f472b6'];
    for (let i = 0; i < 200; i++) {
      this.particles.push({
        x: Math.random() * this.canvas.width,
        y: Math.random() * this.canvas.height,
        radius: Math.random() * 2 + 1,
        dx: (Math.random() - 0.5) * 0.6,
        dy: (Math.random() - 0.5) * 0.6,
        color: colors[Math.floor(Math.random() * colors.length)]
      });
    }
    this.animateParticles();
  }

  animateParticles() {
    if (!this.ctx || !this.canvas) return;

    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    for (const p of this.particles) {
      p.x += p.dx;
      p.y += p.dy;

      if (p.x < 0 || p.x > this.canvas.width) p.dx *= -1;
      if (p.y < 0 || p.y > this.canvas.height) p.dy *= -1;

      let offsetX = (this.mouse.x - this.canvas.width / 2) * 0.02;
      let offsetY = (this.mouse.y - this.canvas.height / 2) * 0.02;

      this.ctx.beginPath();
      this.ctx.arc(p.x + offsetX, p.y + offsetY, p.radius, 0, Math.PI * 2);
      const gradient = this.ctx.createRadialGradient(
        p.x + offsetX,
        p.y + offsetY,
        p.radius / 2,
        p.x + offsetX,
        p.y + offsetY,
        p.radius
      );
      gradient.addColorStop(0, p.color);
      gradient.addColorStop(1, 'rgba(0,0,0,0)');
      this.ctx.fillStyle = gradient;
      this.ctx.fill();
    }
    requestAnimationFrame(() => this.animateParticles());
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
