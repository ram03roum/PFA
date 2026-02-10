import { inject, PLATFORM_ID } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

    const platformId = inject(PLATFORM_ID);

    // ✅ 1. Protection SSR / prerender
    if (!isPlatformBrowser(platformId)) {
        return next(req);
    }

    // ✅ 2. Ne pas intercepter la requête de login
    if (req.url.includes('/login')) {
        return next(req);
    }

    // ✅ 3. Récupérer le token
    const token = localStorage.getItem('auth_token');

    // ✅ 4. S'il n'y a pas de token, on laisse passer
    if (!token) {
        return next(req);
    }

    // ✅ 5. Cloner la requête avec le header Authorization
    const cloned = req.clone({
        setHeaders: {
            Authorization: `Bearer ${token}`
        }
    });

    return next(cloned);
};
