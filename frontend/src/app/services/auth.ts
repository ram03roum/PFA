import { Injectable, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })


export class AuthService {

  // signal pour réactivité kif valeur tetbadel l'affichage yetbadel wahdou
  currentUser = signal<any>(null);
  private http = inject(HttpClient);
  //bch naarfou fel brower w ela fel ssr khtr fama des methodes mtaa browser kima localStorage 
  private platformId = inject(PLATFORM_ID);

  // Observable ysajel akher valeur bch naarfou user connecté w ela le 
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  isLoggedIn$ = this.isLoggedInSubject.asObservable();


  // L'URL de ton backend Alwaysdata
  private apiUrl = 'http://127.0.0.1:5000';

  //headers nestaamlouh lel JSON w JWT Token 
  private httpOptions = {
    headers: new HttpHeaders({
      //les donnes sont json
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
      tap((response: any) => {
        // On vérifie si le backend a envoyé le access_token
        if (response.access_token) {
          // ON RANGE LE BADGE DANS LE TIROIR (localStorage)
          localStorage.setItem('auth_token', response.access_token);
          this.isLoggedInSubject.next(true);
          this.currentUser.set(response.user);

          console.log('Token sauvegardé !');
        }
      })
    );
  }

  logout() {
    this.currentUser.set(null);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('user');
    }
    this.isLoggedInSubject.next(false);
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
