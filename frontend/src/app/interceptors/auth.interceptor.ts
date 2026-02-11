import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // 1. On récupère le token que vous avez enregistré lors du login
  const token = localStorage.getItem('access_token');

  // 2. Si le token existe, on "clône" la requête pour lui ajouter le badge d'identité
  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}` // C'est ici que le "Missing Authorization Header" se règle
      }
    });
    console.log('Header Authorization ajouté');
    return next(authReq);

  }
  console.log('Aucun token trouvé, requête sans Authorization');

  // 3. Si pas de token (ex: page login), on laisse passer la requête normale
  return next(req);
};