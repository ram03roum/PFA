import { Injectable, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })



export class AuthService {


  // signal pour réactivité
  currentUser = signal<any>(null);
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);

  // L'URL de ton backend Alwaysdata
  private apiUrl = 'http://127.0.0.1:5000';

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      const savedUser = localStorage.getItem('user');
      if (savedUser) this.currentUser.set(JSON.parse(savedUser));
    }
  }

  login(credentials: { email: string; password: string }) {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials, this.httpOptions).pipe(
      tap(response => {
        // response ressemble maintenant à { user: {id: 1, name: '...'} }
        if (response && response.user) {
          this.currentUser.set(response.user);
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('user', JSON.stringify(response.user));
          }
        }
      })
    );
  }

  logout() {
    this.currentUser.set(null);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('user');
    }
  }


  register(userData: { name: string; email: string; password: string }) {
    return this.http.post<any>(`${this.apiUrl}/register`, userData, this.httpOptions).pipe(
      tap(response => {
        if (response && response.user) {
          this.currentUser.set(response.user);
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('user', JSON.stringify(response.user));
          }
        }
      })
    );
  }


}
