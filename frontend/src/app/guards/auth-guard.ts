import { isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { CanActivate, Router } from '@angular/router';


@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object

  ) { }

  canActivate(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('access_token');
      if (token) {
        return true;
      }
      this.router.navigate(['/login']);
      return false;
    }

    // ✅ Côté serveur (SSR) → on laisse passer, le navigateur vérifiera
    return true;
  }

}

