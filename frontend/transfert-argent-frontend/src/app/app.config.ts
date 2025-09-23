// src/app/app.config.ts

import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  provideHttpClient,
  withInterceptors, // On utilise withInterceptors pour les fonctions
  withFetch // On ajoute ceci pour corriger l'avertissement de performance
} from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth-interceptor'; // On importe l'intercepteur fonctionnel

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // Configuration moderne pour HttpClient
    provideHttpClient(
      withInterceptors([authInterceptor]),
      withFetch()
    )
  ]
};