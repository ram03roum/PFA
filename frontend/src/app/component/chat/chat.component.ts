// import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { ChatService } from '../../services/chat.service';
// import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
// import { SafeHtmlPipe } from '../shared/safe-html.pipe';

// @Component({
//   selector: 'app-chat',
//   standalone: true,
//   imports: [CommonModule, FormsModule, SafeHtmlPipe],
//   templateUrl: './chat.component.html',
//   styleUrls: ['./chat.component.css']
// })
// export class ChatComponent implements OnInit {
//   @ViewChild('messagesContainer') messagesContainer!: ElementRef;
// // 2. Créez une variable pour stocker le HTML "sécurisé"
//   htmlSecurise!: SafeHtml;
//   conversations: any[] = [];
//   currentConversation: any = null;
//   messages: any[] = [];
//   newMessage = '';
//   isLoading = false;
//   showNewConvForm = false;
//   newConvTitle = '';
//   newConvTopic = 'general';

//   constructor(private chatService: ChatService ,private sanitizer: DomSanitizer) {}

//   ngOnInit(): void {
//     this.loadConversations();
//   // 4. Transformez le code ici
//     const monCodeBrut = '<h1>Bonjour !</h1><p>Ceci est du HTML.</p>';
//     this.htmlSecurise = this.sanitizer.bypassSecurityTrustHtml(monCodeBrut);
//   }

//   loadConversations(): void {
//     this.chatService.getConversations().subscribe({
//       next: (data) => {
//         this.conversations = data;
//         console.log('✅ Conversations chargées:', data);
//       },
//       error: (err) => {
//         console.error('❌ Erreur chargement conversations:', err);
//         alert('Erreur lors du chargement des conversations');
//       }
//     });
//   }

//   selectConversation(conv: any): void {
//     this.currentConversation = conv;
//     this.messages = [];
    
//     this.chatService.getConversationMessages(conv.id).subscribe({
//       next: (data) => {
//         this.messages = data.messages || [];
//         console.log('✅ Messages chargés:', this.messages);
//         setTimeout(() => this.scrollToBottom(), 100);
//       },
//       error: (err) => {
//         console.error('❌ Erreur chargement messages:', err);
//         alert('Erreur lors du chargement des messages');
//       }
//     });
//   }

//   createNewConversation(): void {
//     if (!this.newConvTitle.trim()) {
//       alert('Veuillez entrer un titre');
//       return;
//     }

//     this.chatService.createConversation({
//       title: this.newConvTitle,
//       topic: this.newConvTopic
//     }).subscribe({
//       next: (conv) => {
//         console.log('✅ Conversation créée:', conv);
//         this.conversations.unshift(conv);
//         this.currentConversation = conv;
//         this.messages = [];
//         this.showNewConvForm = false;
//         this.newConvTitle = '';
//       },
//       error: (err) => {
//         console.error('❌ Erreur création conversation:', err);
//         alert('Erreur lors de la création de la conversation');
//       }
//     });
//   }

//   sendMessage(): void {
//     if (!this.newMessage.trim() || !this.currentConversation) return;

//     this.isLoading = true;
//     const userMessageText = this.newMessage;
//     this.newMessage = '';
    
//     this.chatService.sendMessage({
//       conversation_id: this.currentConversation.id,
//       message: userMessageText
//     }).subscribe({
//       next: (data) => {
//         console.log('✅ Message reçu:', data);
//         this.messages.push(data.user_message);
//         this.messages.push(data.ai_message);
//         this.isLoading = false;
//         setTimeout(() => this.scrollToBottom(), 100);
//       },
//       error: (err) => {
//         console.error('❌ Erreur envoi message:', err);
//         this.newMessage = userMessageText; // Restaurer le message
//         this.isLoading = false;
//         alert('Erreur lors de l\'envoi du message');
//       }
//     });
//   }

//   getSummary(): void {
//     if (!this.currentConversation) return;

//     this.chatService.getSummary(this.currentConversation.id).subscribe({
//       next: (data) => {
//         console.log('✅ Résumé généré:', data);
//         const summary = data.summary;
//         const keyPoints = data.key_points ? data.key_points.join('\n') : 'N/A';
//         alert(`📋 RÉSUMÉ:\n${summary}\n\n🔑 POINTS CLÉS:\n${keyPoints}`);
//       },
//       error: (err) => {
//         console.error('❌ Erreur résumé:', err);
//         alert('Erreur lors de la génération du résumé');
//       }
//     });
//   }

//   closeConversation(): void {
//     if (!this.currentConversation) return;

//     if (confirm('Êtes-vous sûr de vouloir fermer cette conversation ?')) {
//       this.chatService.closeConversation(this.currentConversation.id).subscribe({
//         next: () => {
//           console.log('✅ Conversation fermée');
//           this.loadConversations();
//           this.currentConversation = null;
//           this.messages = [];
//         },
//         error: (err) => {
//           console.error('❌ Erreur fermeture:', err);
//         }
//       });
//     }
//   }

//   private scrollToBottom(): void {
//     try {
//       this.messagesContainer.nativeElement.scrollTop = 
//         this.messagesContainer.nativeElement.scrollHeight;
//     } catch (err) {}
//   }

//   getSenderClass(sender: string): string {
//     return sender === 'user' ? 'user-message' : 'ai-message';
//   }
// }


import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { SafeHtmlPipe } from '../shared/safe-html.pipe';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, SafeHtmlPipe],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  htmlSecurise!: SafeHtml;
  conversations: any[] = [];
  currentConversation: any = null;
  messages: any[] = [];
  newMessage = '';
  isLoading = false;
  showNewConvForm = false;
  newConvTitle = '';
  newConvTopic = 'general';

  constructor(private chatService: ChatService, private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    this.loadConversations();
    const monCodeBrut = '<h1>Bonjour !</h1><p>Ceci est du HTML.</p>';
    this.htmlSecurise = this.sanitizer.bypassSecurityTrustHtml(monCodeBrut);
  }

  loadConversations(): void {
    this.chatService.getConversations().subscribe({
      next: (data) => {
        this.conversations = data;
      },
      error: (err) => {
        console.error('❌ Erreur chargement conversations:', err);
      }
    });
  }

  selectConversation(conv: any): void {
    this.currentConversation = conv;
    this.messages = [];

    this.chatService.getConversationMessages(conv.id).subscribe({
      next: (data) => {
        this.messages = data.messages || [];
        // Double setTimeout : 1er pour le rendu Angular, 2ème pour les images/markdown
        setTimeout(() => this.scrollToBottom(), 0);
        setTimeout(() => this.scrollToBottom(), 300);
      },
      error: (err) => {
        console.error('❌ Erreur chargement messages:', err);
      }
    });
  }

  createNewConversation(): void {
    if (!this.newConvTitle.trim()) {
      alert('Veuillez entrer un titre');
      return;
    }

    this.chatService.createConversation({
      title: this.newConvTitle,
      topic: this.newConvTopic
    }).subscribe({
      next: (conv) => {
        this.conversations.unshift(conv);
        this.currentConversation = conv;
        this.messages = [];
        this.showNewConvForm = false;
        this.newConvTitle = '';
      },
      error: (err) => {
        console.error('❌ Erreur création conversation:', err);
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.currentConversation || this.isLoading) return;

    this.isLoading = true;
    const userMessageText = this.newMessage;
    this.newMessage = '';

    // Scroll immédiat après que l'utilisateur envoie
    setTimeout(() => this.scrollToBottom(), 0);

    this.chatService.sendMessage({
      conversation_id: this.currentConversation.id,
      message: userMessageText
    }).subscribe({
      next: (data) => {
        this.messages.push(data.user_message);
        this.messages.push(data.ai_message);
        this.isLoading = false;
        // Double scroll : rendu DOM puis contenu markdown
        setTimeout(() => this.scrollToBottom(), 0);
        setTimeout(() => this.scrollToBottom(), 300);
      },
      error: (err) => {
        console.error('❌ Erreur envoi message:', err);
        this.newMessage = userMessageText;
        this.isLoading = false;
      }
    });
  }

  // ✅ Entrée textarea : Shift+Enter = nouvelle ligne, Enter seul = envoyer
  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  getSummary(): void {
    if (!this.currentConversation) return;

    this.chatService.getSummary(this.currentConversation.id).subscribe({
      next: (data) => {
        const summary = data.summary;
        const keyPoints = data.key_points ? data.key_points.join('\n') : 'N/A';
        alert(`📋 RÉSUMÉ:\n${summary}\n\n🔑 POINTS CLÉS:\n${keyPoints}`);
      },
      error: (err) => {
        console.error('❌ Erreur résumé:', err);
      }
    });
  }

  closeConversation(): void {
    if (!this.currentConversation) return;

    if (confirm('Êtes-vous sûr de vouloir fermer cette conversation ?')) {
      this.chatService.closeConversation(this.currentConversation.id).subscribe({
        next: () => {
          this.loadConversations();
          this.currentConversation = null;
          this.messages = [];
        },
        error: (err) => {
          console.error('❌ Erreur fermeture:', err);
        }
      });
    }
  }

  // ✅ Auto-resize du textarea selon le contenu
  autoResize(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    textarea.style.height = 'auto';
    const maxHeight = 120;
    textarea.style.height = Math.min(textarea.scrollHeight, maxHeight) + 'px';
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    } catch (err) {}
  }

  getSenderClass(sender: string): string {
    return sender === 'user' ? 'user-message' : 'ai-message';
  }
}