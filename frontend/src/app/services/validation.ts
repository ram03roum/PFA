
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor() { }

  // Fonction pour valider l'email
  validateEmail(email: string): string {
    // Si l'email est vide
    if (!email) {
      return 'L\'email est requis';
    }

    // Si l'email ne contient pas @
    if (!email.includes('@')) {
      return 'L\'email doit contenir un @';
    }

    // Vérifier le format complet avec regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailRegex.test(email)) {
      return 'Format d\'email invalide (ex: nom@example.com)';
    }

    // Si tout est OK, retourner une chaîne vide (pas d'erreur)
    return '';
  }
}
