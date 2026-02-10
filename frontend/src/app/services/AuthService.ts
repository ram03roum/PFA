import { Injectable } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { Observable , BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // 1. L'URL de ton backend Flask
  private apiUrl = 'http://127.0.0.1:5000';
  // 2. Le "Cerveau" (BehaviorSubject) : Il garde en mémoire l'utilisateur actuel.
  // On commence à 'null' car au chargement, on ne sait pas encore s'il est là.
  private currentUserSubject = new BehaviorSubject<any>(null);
  // 3. La "Radio" (Observable) : Les composants (navbar, etc.) s'abonnent à ceci pour être avertis.
  public currentUser$ = this.currentUserSubject.asObservable();
  // 4. Configuration des headers (Optionnel mais recommandé pour Flask)
  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient, 
    private router: Router //pour la redirection 
  ) {
    // Au démarrage, charge le token depuis localStorage
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    // Si on trouve un utilisateur, on l'envoie dans BehaviorSubject
    if (token && user) {
      this.currentUserSubject.next(JSON.parse(user));
    }
  }

  register(name: string, email: string, password: string): Observable<any> {
    // On passe this.httpOptions pour dire à Flask "C'est du JSON"
    return this.http.post(`${this.apiUrl}/register`, { name, email, password }, this.httpOptions);
  }

login(email: string, password: string): Observable<any> {
    // ✅ CORRIGER les backticks
    return this.http.post(`${this.apiUrl}/login`, { email, password ,role: this.getUserRole() }, this.httpOptions).pipe(
      tap((response: any) => {
        console.log("Réponse Flask:", response);
        
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        localStorage.setItem('role', response.user.role); // Stocker le rôle
        
        this.currentUserSubject.next(response.user);
        
        // ✅ AJOUTER redirection selon le rôle
        if (response.user.role === 'admin') {
          this.router.navigate(['/admin']);
          
        } else {
          this.router.navigate(['/home']);
        }
      })
    );

  }
  
  // verifier silest connecte ou non 
  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUserRole(): string {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user).role : '';
    //dans le localstorage on a stocker lutilisateur sous forme de texte donc on doit le parser pour le transformer en json objet javascript
  }

  logout(): void {
    localStorage.clear(); // Vide tout le localStorage
    this.currentUserSubject.next(null); // Informe l'app que l'utilisateur est parti
  }

  isAdmin(): boolean {
    const role = localStorage.getItem('role'); // Ou récupéré depuis votre store/token
    return role === 'admin';
  }
}