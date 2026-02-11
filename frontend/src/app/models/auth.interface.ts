// Ce fichier définit la "forme" des données
// TypeScript va vérifier qu'on respecte ces formes

export interface User {
    id: number;           // ID unique de l'utilisateur
    name: string;         // Son nom
    email: string;        // Son email
    role: string;         // Son rôle : 'admin' ou 'client'
    phone?: string;       // Le ? signifie "optionnel"
    status?: string;      // actif/inactif/suspendu
}

// Ce qu'on envoie à Flask pour se connecter
export interface LoginCredentials {
    email: string;
    password: string;
}

// Ce que Flask nous renvoie après un login réussi
export interface LoginResponse {
    message: string;      // "Login successful"
    token: string;        // Le JWT token
    user: User;          // Les infos de l'utilisateur
}

// Ce qu'on envoie à Flask pour s'inscrire
export interface RegisterData {
    name: string;
    email: string;
    password: string;
    phone?: string;
}

// Ce que Flask renvoie après inscription
export interface RegisterResponse {
    message: string;
    user: User;
}