import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/AuthService';
import { CommonModule } from '@angular/common';
import { timeout, finalize } from 'rxjs';


@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, ReactiveFormsModule],
    templateUrl: './login.html',
    styleUrls: ['./login.css']
})
export class LoginComponent {
    loginForm: FormGroup;
    errorMessage = '';
    isLoading = false;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        // Créer le formulaire
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required]]
        });
    }

    onSubmit(): void {
        console.log("entrée");
        // Si le formulaire est invalide, on arrête
        if (this.loginForm.invalid) {
            console.log("form invalid");
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';

        // On récupère les valeurs du formulaire
        const credentials = this.loginForm.value;

        // On appelle le service
        this.authService.login(credentials)
            .pipe(
                timeout(15000),
                finalize(() => { this.isLoading = false; })
            )
            .subscribe({
                // ✅ Succès
                next: (response) => {
                    // console.log('Composant Login OK:', response);

                    // 🎯 ICI on décide de la redirection selon le rôle
                    if (this.authService.isAdmin()) {
                        // console.log("admin");
                        this.router.navigate(['/admin']);
                    } else {
                        // console.log("user");
                        this.router.navigate(['/home']);
                    }
                },
                // ❌ Erreur
                error: (error) => {
                    console.error('Login failed:', error);
                    this.errorMessage = error.error?.error || 'Email ou mot de passe incorrect';
                }
            });
    }
    get email() {
        return this.loginForm.get('email');
    }
    get password() {
        return this.loginForm.get('password');
    }


}