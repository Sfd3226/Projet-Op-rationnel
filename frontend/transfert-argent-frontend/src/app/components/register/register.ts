import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { animateValue } from '../dashboard/utils/animateValue'; 

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrls: ['./register.scss']
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  loading = false;
  message = '';
  photoProfilFile?: File;
  cniFile?: File;
  
  // Background Nebula
  particles: any[] = [];
  mouse = { x: 0, y: 0 };
  canvas!: HTMLCanvasElement;
  ctx!: CanvasRenderingContext2D;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      pays: ['', Validators.required],
      numeroPiece: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.initNebulaBg();
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    return password && confirmPassword && password.value === confirmPassword.value ? 
      null : { mismatch: true };
  }

  onPhotoProfilChange(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.photoProfilFile = event.target.files[0];
    }
  }

  onCniChange(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.cniFile = event.target.files[0];
    }
  }

  submit() {
    if (this.registerForm.invalid) {
      this.loading = false;
      return;
    }
    this.loading = true;

    // Crée un objet FormData pour envoyer les données et les fichiers ensemble
    const formData = new FormData();

    // Crée un objet pour les données d'inscription sans les fichiers
    const registerData = {
      nom: this.registerForm.get('nom')?.value,
      prenom: this.registerForm.get('prenom')?.value,
      email: this.registerForm.get('email')?.value,
      telephone: this.registerForm.get('telephone')?.value,
      password: this.registerForm.get('password')?.value,
      pays: this.registerForm.get('pays')?.value,
      numeroPiece: this.registerForm.get('numeroPiece')?.value,
    };
    
   
    formData.append('registerRequest', new Blob([JSON.stringify(registerData)], { type: 'application/json' }));

    // Ajoute les fichiers s'ils existent
    if (this.photoProfilFile) {
      formData.append('photoProfil', this.photoProfilFile, this.photoProfilFile.name);
    }
    if (this.cniFile) {
      formData.append('photoPiece', this.cniFile, this.cniFile.name);
    }

    this.authService.register(formData).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: any) => {
        this.loading = false;
        this.message = err?.error?.message || 'Erreur lors de l\'inscription';
      }
    });
  }

  get nom() { return this.registerForm.get('nom'); }
  get prenom() { return this.registerForm.get('prenom'); }
  get email() { return this.registerForm.get('email'); }
  get telephone() { return this.registerForm.get('telephone'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
  get pays() { return this.registerForm.get('pays'); }
  get numeroPiece() { return this.registerForm.get('numeroPiece'); }

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