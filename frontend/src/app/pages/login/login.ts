import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ValidationService } from '../../services/validation';  // ← Import du service
import { AuthService } from '../../services/AuthService'; // Vérifie le chemin
import { Router } from '@angular/router';


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
    loginError: string = ''; // Pour afficher "Email ou MDP incorrect"

    constructor(private validationService: ValidationService,
                private authService: AuthService, // Injecte ton service API
                private router: Router             // Injecte le routeur
    ) { }

    validateEmail() {
        // ← Appel de la fonction du service
        this.emailError = this.validationService.validateEmail(this.email);
    }
    onSubmit() {
        this.validateEmail();
if (this.emailError) return;

        // Préparation des données pour Flask
        const credentials = { email: this.email, password: this.password };

        // APPEL AU BACKEND
        this.authService.login(credentials).subscribe({
            next: (response) => {
                console.log('Connexion réussie !', response);
                alert('Connexion réussie !');
                // On redirige SEULEMENT si Flask a dit OK
                this.router.navigate(['/home']);
            },
            error: (err) => {
                console.error('Erreur login', err);
                alert('Erreur login : Email ou mot de passe incorrect.')
                this.loginError = "Email ou mot de passe incorrect.";
            }
        });
    }
}