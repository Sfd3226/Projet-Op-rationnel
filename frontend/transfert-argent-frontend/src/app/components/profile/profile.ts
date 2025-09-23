import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProfileService, UserProfile, PasswordChangeRequest } from '../../services/profile';
import { AuthService } from '../../services/auth';
import { animateValue } from '../dashboard/utils/animateValue';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  loading = false;
  errorMessage = '';
  successMessage = '';
  isEditing = false;
  isChangingPassword = false;
  soldeMasque: boolean = true;

  profileForm: FormGroup;
  passwordForm: FormGroup;

  // Particules pour le background
  particles: any[] = [];
  mouse = { x: 0, y: 0 };
  canvas!: HTMLCanvasElement;
  ctx!: CanvasRenderingContext2D;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.profileForm = this.fb.group({
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\+?[0-9]{8,15}$/)]],
      pays: ['', Validators.required],
      photoProfil: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.loadProfile();
    this.initNebulaBg();
  }
logout(): void {
    this.authService.logout();
  }
  // ✅ AJOUTÉ: Méthode pour masquer/afficher le solde
  toggleSolde(): void {
    this.soldeMasque = !this.soldeMasque;
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMessage = '';

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileForm.patchValue(profile);
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors du chargement du profil';
        this.loading = false;
        console.error('Erreur profil:', error);
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (this.isEditing && this.profile) {
      this.profileForm.patchValue(this.profile);
    }
  }

  togglePasswordChange(): void {
    this.isChangingPassword = !this.isChangingPassword;
    this.passwordForm.reset();
  }

  updateProfile(): void {
    if (this.profileForm.valid) {
      this.loading = true;
      this.profileService.updateProfile(this.profileForm.value).subscribe({
        next: (updatedProfile) => {
          this.profile = updatedProfile;
          this.isEditing = false;
          this.successMessage = 'Profil mis à jour avec succès';
          this.loading = false;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          this.errorMessage = error.error || 'Erreur lors de la mise à jour';
          this.loading = false;
        }
      });
    }
  }

  changePassword(): void {
    if (this.passwordForm.valid) {
      this.loading = true;
      this.profileService.changePassword(this.passwordForm.value).subscribe({
        next: () => {
          this.isChangingPassword = false;
          this.passwordForm.reset();
          this.successMessage = 'Mot de passe modifié avec succès';
          this.loading = false;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          this.errorMessage = error.error || 'Erreur lors du changement de mot de passe';
          this.loading = false;
        }
      });
    }
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
  private passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { mismatch: true };
  }

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
}