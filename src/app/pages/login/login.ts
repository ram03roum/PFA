import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ValidationService } from '../../services/validation';  // ← Import du service



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

    constructor(private validationService: ValidationService, private router: Router) { }

    validateEmail() {
        // ← Appel de la fonction du service
        this.emailError = this.validationService.validateEmail(this.email);
    }
    validatePassword() {  // ← Nouvelle méthode
        if (this.password === '') {
            this.passwordError = 'Le mot de passe est obligatoire';
        } else {
            this.passwordError = '';
        }
    }
    onSubmit() {
        this.validateEmail();
        this.validatePassword();

        if (this.emailError) {
            return; // Bloque si erreur
        }
        // Vérifier si les mots de passe correspondent
        if (this.passwordError) {
            return;
        }
        console.log('Email:', this.email);
        console.log('Password:', this.password);


        this.router.navigate(['/home']);

    }
}