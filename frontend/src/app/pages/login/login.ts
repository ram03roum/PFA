import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
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

    constructor(private validationService: ValidationService) { }

    validateEmail() {
        // ← Appel de la fonction du service
        this.emailError = this.validationService.validateEmail(this.email);
    }
    onSubmit() {
        this.validateEmail();

        if (this.emailError) {
            return; // Bloque si erreur
        }

        console.log('Email:', this.email);
        console.log('Password:', this.password);
    }
}