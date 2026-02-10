import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ValidationService } from '../../services/validation';  // ← Import du service
import { AuthService } from '../../services/AuthService';
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
    role: string = ''; // Pour stocker le rôle de l'utilisateur après connexion
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
        this.authService.login(this.email, this.password).subscribe({
            next: (response) => {
                console.log('Connexion réussie !', response);
                // Flask renvoie généralement le token sous le nom 'access_token' ou 'token'
            // Vérifiez bien le nom dans votre console Python (Flask)
                if (response.access_token) {
                localStorage.setItem('access_token', response.access_token);
                }
                alert('Connexion réussie !');
                // On redirige SEULEMENT si Flask a dit OK
                // this.router.navigate(['/home']);
            },
            error: (err) => {
                console.error('Erreur login', err);
                alert('Erreur login : Email ou mot de passe incorrect.')
                this.loginError = "Email ou mot de passe incorrect.";
            }
        });
    }
}