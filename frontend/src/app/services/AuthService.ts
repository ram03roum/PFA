import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User, LoginCredentials, LoginResponse, RegisterData, RegisterResponse } from '../models/auth.interface';

// @Injectable signifie "ce service peut être injecté partout"
// providedIn: 'root' = une seule instance dans toute l'app (singleton)
@Injectable({
    providedIn: 'root'
})
export class AuthService {

    // 1️⃣ CONFIGURATION
    // L'URL de votre backend Flask
    private apiUrl = 'http://127.0.0.1:5000';

    // 2️⃣ LE CERVEAU (BehaviorSubject)
    // PRIVÉ = personne d'autre ne peut le modifier directement
    // null au départ car on ne sait pas encore si quelqu'un est connecté
    // Créer la station de radio: celui qui parle et decide et actionne et change et garde en mem la derniere info et envoie les infos comme master
    private currentUserSubject = new BehaviorSubject<User | null>(null);
    // Un seul BehaviorSubject peut envoyer les mêmes infos à plusieurs Observables 
    // (ou plutôt, plusieurs composants qui s'abonnent à l'Observable).


    // 3️⃣ LA RADIO (Observable)
    // PUBLIC = les composants peuvent s'abonner
    // On expose juste en lecture, pas en écriture
    public currentUser$ = this.currentUserSubject.asObservable();

    // 2. ON AJOUTE LE SIGNAL (Initialisé à null)
    // C'est lui que tu utiliseras dans ton nouveau HTML avec @if
    private currentUserSignal = signal<User | null>(null);

    // Ton signal public (lecture seule)
    public readonly currentUser = this.currentUserSignal.asReadonly();

    // 4️⃣ INJECTION DES DÉPENDANCES
    // Angular va automatiquement nous fournir HttpClient
    constructor(private http: HttpClient) {
        // 5️⃣ AU DÉMARRAGE : On vérifie si un utilisateur était connecté
        this.loadUserFromStorage();
    }

    // 🔄 MÉTHODE PRIVÉE : Charger l'utilisateur depuis localStorage
    private loadUserFromStorage(): void {
        // On essaie de récupérer le token
        const token = localStorage.getItem('access_token');
        // On essaie de récupérer l'utilisateur (stocké en JSON)
        const userJson = localStorage.getItem('current_user');

        // Si on a les deux, on restaure la session
        if (token && userJson) {
            try {
                // JSON.parse transforme le texte en objet JavaScript
                const user = JSON.parse(userJson) as User;
                // On dit au BehaviorSubject : "Hey, cet utilisateur est connecté"
                this.currentUserSubject.next(user);
                this.currentUserSignal.set(user); // <--- Ajoute ça !
            } catch (error) {
                // Si le JSON est corrompu, on nettoie
                console.error('Erreur lors du chargement de l\'utilisateur:', error);
                this.clearStorage();
            }
        }
    }

    // 🔐 MÉTHODE PRINCIPALE : LOGIN
    login(credentials: LoginCredentials): Observable<LoginResponse> {
        // On envoie une requête POST à Flask
        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
            .pipe(
                // tap() = faire une action de côté SANS modifier la réponse
                tap((response: LoginResponse) => {
                    // ✅ Flask a répondu avec succès

                    // 1. On stocke le token dans localStorage
                    localStorage.setItem('access_token', response.token);

                    // 2. On stocke aussi l'utilisateur (optionnel mais pratique)
                    // JSON.stringify transforme l'objet en texte
                    localStorage.setItem('current_user', JSON.stringify(response.user));


                    // Tout afficher
                    console.log('Token:', localStorage.getItem('access_token'));
                    console.log('User:', localStorage.getItem('current_user'));

                    // 3. On notifie TOUS les composants abonnés
                    this.currentUserSubject.next(response.user);
                    this.currentUserSignal.set(response.user); // <--- Ajoute ça !
                    console.log('✅ Login réussi:', response.user.name);
                })
            );
        // IMPORTANT : On ne fait PAS subscribe() ici !
        // C'est le COMPOSANT qui va subscribe et décider quoi faire
    }

    // 📝 MÉTHODE : REGISTER
    register(data: RegisterData): Observable<RegisterResponse> {
        return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, data)
            .pipe(
                tap((response: RegisterResponse) => {
                    // Après inscription, on peut automatiquement "connecter" l'utilisateur
                    // Mais il faudrait que Flask renvoie aussi un token
                    // Pour l'instant, on stocke juste l'user
                    localStorage.setItem('current_user', JSON.stringify(response.user));
                    this.currentUserSubject.next(response.user);

                    // Tout afficher
                    console.log('Token:', localStorage.getItem('access_token'));
                    console.log('User:', localStorage.getItem('current_user'));

                    console.log('✅ Inscription réussie:', response.user.name);
                })
            );
    }

    // 🚪 MÉTHODE : LOGOUT
    logout(): void {
        // 1. On vide localStorage
        this.clearStorage();

        // 2. ON PRÉVIENT LE SIGNAL (C'est l'étape manquante !)
        // Cela va déclencher instantanément le @else dans ton HTML
        this.currentUserSignal.set(null);

        // 3. On prévient aussi le Subject (pour les anciens composants)
        this.currentUserSubject.next(null);

        console.log('👋 Déconnexion');
    }

    // 🧹 MÉTHODE PRIVÉE : Nettoyer le localStorage
    private clearStorage(): void {
        localStorage.removeItem('access_token');
        localStorage.removeItem('current_user');
    }

    // 🔍 MÉTHODES UTILITAIRES

    // Est-ce qu'un utilisateur est connecté ?
    isAuthenticated(): boolean {
        // Vérifie juste si le token existe
        return !!localStorage.getItem('access_token');
        // Le !! transforme en boolean : si token existe = true, sinon = false
    }

    // Récupérer le token (pour l'intercepteur)
    getToken(): string | null {
        return localStorage.getItem('access_token');
    }

    // Récupérer l'utilisateur actuel (de manière synchrone)
    getCurrentUser(): User | null {
        return this.currentUserSubject.value;
        // .value donne la valeur actuelle du BehaviorSubject
    }

    // 3. EXPOSER LE SIGNAL EN LECTURE SEULE
    // public currentUser = this.currentUserSignal.asReadonly();
    // Ou plus simple, une fonction getter de signal :

    // get currentUserSignal() {
    //     return this.currentUserSignal.asReadonly();
    // }

    // Récupérer le rôle de l'utilisateur
    getRole(): string {
        // console.log("getuser");
        const user = this.getCurrentUser();
        return user ? user.role : '';
        // Si user existe → renvoie son rôle, sinon ''
    }

    // Est-ce que l'utilisateur est admin ?
    isAdmin(): boolean {
        // console.log(this.getRole() === 'admin');
        return this.getRole() === 'admin';
    }

    // Est-ce que l'utilisateur est client ?
    isClient(): boolean {
        return this.getRole() === 'client';
    }

    // Vérifier si l'utilisateur a un rôle spécifique
    hasRole(role: string): boolean {
        return this.getRole() === role;
    }
}