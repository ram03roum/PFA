import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ValidationService } from '../../services/validation';  // ← Import du service


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
  passwordEmpty: string = '';


  constructor(private router: Router, private validationService: ValidationService
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

    // Vérifier si les mots de passe correspondent
    if (this.password !== this.confirmPassword) {
      this.passwordMismatch = true;
      return;
    } else {
      this.passwordMismatch = false;
    }


    // Logique d'inscription
    console.log('Nom:', this.name);
    console.log('Email:', this.email);
    console.log('Password:', this.password);

    //Après l'inscription réussie, rediriger vers login
    this.router.navigate(['/home']);
  }
}
