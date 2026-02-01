import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-contact',
  templateUrl: './contact.html',
  standalone: true,  
  imports: [CommonModule, RouterModule, FormsModule],
  styleUrls: ['./contact.css']
})
export class ContactComponent {
  contactForm = {
    name: '',
    email: '',
    subject: '',
    message: ''
  };

  onSubmit(): void {
    console.log('Form submitted:', this.contactForm);
    alert('Message envoyé avec succès!');
    // Réinitialiser le formulaire
    this.contactForm = { name: '', email: '', subject: '', message: '' };
  }
}