export interface LoginRequest { telephone: string; password: string; }
export interface RegisterRequest { telephone: string; password: string; nom?: string; }
export interface AuthResponse { token: string; }
