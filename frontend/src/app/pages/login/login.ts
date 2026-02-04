import { Component, inject } from '@angular/core'; // Ajout de inject
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ValidationService } from '../../services/validation';
import { AuthService } from '../../services/auth'; // ✅ Importe ton AuthService

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './login.html',
    styleUrls: ['./login.css']
})
export class LoginComponent {
    email: string = '';
    password: string = '';
    emailError: string = '';
    passwordError: string = '';
    serverError: string = ''; // ✅ Pour afficher les erreurs du backend (ex: mauvais mdp)

    // Utilisation de inject() pour la modernité ou conservation du constructeur
    constructor(
        private validationService: ValidationService,
        private router: Router,
        private authService: AuthService // ✅ Injecte le service ici
    ) { }

    validateEmail() {
        this.emailError = this.validationService.validateEmail(this.email);
    }

    validatePassword() {
        this.passwordError = this.password === '' ? 'Le mot de passe est obligatoire' : '';
    }

    onSubmit() {
        this.validateEmail();
        this.validatePassword();
        this.serverError = ''; // On réinitialise l'erreur serveur

        if (this.emailError || this.passwordError) {
            console.log("les informations ne sont pas compatibles")
            return;
        }

        // --- CONNEXION RÉELLE AU BACKEND ---
        this.authService.login({ email: this.email, password: this.password }).subscribe({
            next: (response) => {
                console.log('Connexion réussie !', response);
                // On redirige vers la racine (ou /home)
                // Le Header changera automatiquement car il observe le Signal dans AuthService
                this.router.navigate(['/']);
            },
            error: (err) => {
                // On récupère le message d'erreur envoyé par Flask (ex: "Email ou mot de passe incorrect")
                this.serverError = err.error?.message || 'Une erreur est survenue lors de la connexion';
                console.error('Erreur login:', err);
            }
        });
    }
}