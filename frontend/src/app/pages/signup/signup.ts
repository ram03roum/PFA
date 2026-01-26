import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ValidationService } from '../../services/validation';  // ← Import du service
import { AuthService } from '../../services/AuthService';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './signup.html',
  styleUrls: ['./signup.css']
})
export class SignupComponent {
  name: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';
  passwordMismatch: boolean = false;
  emailError: string = '';
  serverError: string = ''; // Pour afficher "Email déjà utilisé"


  constructor(private router: Router,
              private validationService: ValidationService,
              private authService: AuthService,
              ) { }
  validateEmail() {
    // ← Utilise le service
    this.emailError = this.validationService.validateEmail(this.email);
  }

  onSubmit() {
    this.validateEmail();
    if (this.emailError) {
      return;
    }
    const userData = {
            name: this.name,
            email: this.email,
            password: this.password
        };

        this.authService.register(userData).subscribe({
            next: (response) => {
                console.log('Inscription réussie', response);
                alert('Compte créé avec succès ! Connectez-vous.');
                this.router.navigate(['/login']); // Redirection automatique
            },
            error: (err) => {
              console.error('Détails de', err);
    // Affiche le message envoyé par Flask (ex: "Email déjà utilisé")
              const errorMsg = err.error?.message || "Erreur inconnue";
              alert('Erreur inscription : ' + errorMsg);
}
        });
    }
    // Vérifier si les mots de passe correspondent
  //   if (this.password !== this.confirmPassword) {
  //     this.passwordMismatch = true;
  //     return;
  //   } else {
  //     this.passwordMismatch = false;
  //   }

  //   // Logique d'inscription
  //   console.log('Nom:', this.name);
  //   console.log('Email:', this.email);
  //   console.log('Password:', this.password);

  //   //Après l'inscription réussie, rediriger vers login
  //   this.router.navigate(['/home']);
  // }
}
