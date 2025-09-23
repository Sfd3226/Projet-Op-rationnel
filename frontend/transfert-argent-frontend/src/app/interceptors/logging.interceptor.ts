// interceptors/logging.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs';

export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  console.log(`Requête vers: ${req.url}`);
  console.log('Headers:', req.headers);
  
  return next(req).pipe(
    tap({
      next: (response) => console.log('Réponse reçue:', response),
      error: (error) => console.error('Erreur de requête:', error)
    })
  );
};