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
  // <<<<<<< HEAD
  //   serverError: string = ''; // Pour afficher "Email déjà utilisé"


  //   constructor(private router: Router,
  //     private validationService: ValidationService,
  //     private authService: AuthService,
  //   ) { }
  passwordEmpty: string = '';
  serverError: string = ''; // ✅ Pour afficher les erreurs du backend (ex: mauvais mdp)



  constructor(private router: Router, private validationService: ValidationService,
    private authService: AuthService) { }
  validateEmail() {
    // ← Utilise le service
    this.emailError = this.validationService.validateEmail(this.email);
  }

  // <<<<<<< HEAD
  //   onSubmit() {
  //     this.validateEmail();
  //     if (this.emailError) {
  //       return;
  //     }
  //     const userData = {
  //       name: this.name,
  //       email: this.email,
  //       password: this.password
  //     };

  //     this.authService.register(this.name, this.email, this.password).subscribe({
  //       next: (response) => {
  //         console.log('Inscription réussie', response);
  //         alert('Compte créé avec succès ! Connectez-vous.');
  //         this.router.navigate(['/login']); // Redirection automatique
  //       },
  //       error: (err) => {
  //         console.error('Détails de', err);
  //         // Affiche le message envoyé par Flask (ex: "Email déjà utilisé")
  //         const errorMsg = err.error?.message || "Erreur inconnue";
  //         alert('Erreur inscription : ' + errorMsg);
  //       }
  //     });
  //   }
  //   // Vérifier si les mots de passe correspondent
  //   //   if (this.password !== this.confirmPassword) {
  //   //     this.passwordMismatch = true;
  //   //     return;
  //   //   } else {
  //   //     this.passwordMismatch = false;
  //   //   }

  //   //   // Logique d'inscription
  //   //   console.log('Nom:', this.name);
  //   //   console.log('Email:', this.email);
  //   //   console.log('Password:', this.password);

  //   //   //Après l'inscription réussie, rediriger vers login
  //   //   this.router.navigate(['/home']);
  //   // }
  // }
  // =======


  onSubmit() {
    this.validateEmail();
    if (this.emailError) return;

    const userData = {
      name: this.name,
      email: this.email,
      password: this.password
    };

    // 1️⃣ INSCRIPTION
    this.authService.register(userData).subscribe({
      next: () => {
        console.log('Inscription réussie');

        // 2️⃣ LOGIN SEULEMENT APRÈS INSCRIPTION
        this.authService.login({
          email: this.email,
          password: this.password
        }).subscribe({
          next: (response) => {
            console.log('Connexion réussie', response);

            // 3️⃣ Redirection
            this.router.navigate(['/']);
          },
          error: (err) => {
            this.serverError =
              err.error?.message || 'Erreur lors de la connexion';
            console.error('Erreur login:', err);
          }
        });
      },
      error: (err) => {
        this.serverError =
          err.error?.message || "Erreur lors de l'inscription";
        console.error('Erreur inscription:', err);
      }
    });
  }

}
