import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router); // Pour rediriger l'utilisateur
  const token = localStorage.getItem('access_token');

  let authReq = req;

  // 1. Ajout du token si présent
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // 2. Gestion de la réponse et des erreurs (Le "Nettoyage")
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si le code est 401 (Unauthorized), le token est probablement expiré ou invalide
      if (error.status === 401) {
        console.warn('Session expirée ou invalide. Nettoyage...');

        localStorage.removeItem('access_token'); // On supprime le token mort
        router.navigate(['/login']); // On renvoie l'utilisateur à la connexion
      }

      // On laisse l'erreur remonter pour que le composant puisse aussi l'afficher si besoin
      return throwError(() => error);
    })
  );
};