// // src/app/components/admin/users/users.component.ts
// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';

// // ✅ IMPORTER LES COMPOSANTS
// import { StatusBadgeComponent } from '../shared/status-badge/status-badge';
// import { ClientCellComponent } from '../shared/client-cell/client-cell';
// import { FilterBarComponent } from '../shared/filter-bar/filter-bar';
// import { RoleBadgeComponent } from '../shared/role-badge/role-badge';
// import { UsersDashboard } from '../../services/users-dashboard';

// @Component({
//   selector: 'app-users',
//   standalone: true,
//   imports: [
//     CommonModule,
//     FormsModule,
//     // ✅ AJOUTER ICI
//     ClientCellComponent,
//     FilterBarComponent,
//     RoleBadgeComponent,
//     StatusBadgeComponent

//   ],
//   templateUrl: './users-dashboard.html',
//   styleUrls: ['./users-dashboard.css']
// })
// export class UsersComponent implements OnInit {
//   users: any[] = [];
  
//   filters = ['tous', 'admin', 'agent', 'client'];
//   activeFilter = 'tous';
//   searchQuery = '';
  
//   currentPage = 1;
//   pageSize = 10;
//   totalPages = 1;
//   totalItems = 0;
  
//   isLoading = true;

//   constructor(private usersService: UsersDashboard) {}

//   ngOnInit(): void {
//     this.loadUsers();
//   }

//   loadUsers(): void {
//     this.isLoading = true;
//     const role = this.activeFilter === 'tous' ? '' : this.activeFilter;
    
//     this.usersService.getAll(this.currentPage, this.pageSize, this.searchQuery, role)
//       .subscribe({
//         next: (response) => {
//           this.users = response.data;
//           this.totalPages = response.pages;
//           this.totalItems = response.total;
//           this.isLoading = false;
//         },
//         error: (err) => {
//           console.error('Erreur:', err);
//           this.isLoading = false;
//         }
//       });
//   }

//   onFilterChange(filter: string): void {
//     this.activeFilter = filter;
//     this.currentPage = 1;
//     this.loadUsers();
//   }

//   onSearch(): void {
//     this.currentPage = 1;
//     this.loadUsers();
//   }

//   getInitials(name: string): string {
//     if (!name) return '';
//     return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
//   }

//   getAvatarColor(role: string): string {
//     const colorMap: any = {
//       'admin': '#ef4444',
//       'agent': '#f59e0b',
//       'client': '#6366f1'
//     };
//     return colorMap[role] || '#6366f1';
//   }

//   nextPage(): void {
//     if (this.currentPage < this.totalPages) {
//       this.currentPage++;
//       this.loadUsers();
//     }
//   }

//   prevPage(): void {
//     if (this.currentPage > 1) {
//       this.currentPage--;
//       this.loadUsers();
//     }
//   }
// }


////////////////////////////////////hatha lcode le9dim li ken 3andi

// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { HttpClient } from '@angular/common/http';
// import { StatusBadgeComponent } from '../shared/status-badge/status-badge';
// import { ClientCellComponent } from '../shared/client-cell/client-cell';
// import { FilterBarComponent } from '../shared/filter-bar/filter-bar';
// import { RoleBadgeComponent } from '../shared/role-badge/role-badge';
// import { UsersDashboard } from '../../services/users-dashboard';
// import { ScoreFilterPipe } from '../../pipes/score-filter.pipe';
// @Component({
//   selector: 'app-users',
//   standalone: true,
//   imports: [
//     CommonModule, FormsModule, ScoreFilterPipe,
//     ClientCellComponent, FilterBarComponent,
//     RoleBadgeComponent, StatusBadgeComponent
//   ],
//   templateUrl: './users-dashboard.html',
//   styleUrls: ['./users-dashboard.css']
// })
// export class UsersComponent implements OnInit {
//   users: any[] = [];
//   scoringData: any[] = [];
//   filteredScoringData: any[] = [];

//   filters = ['tous', 'admin','client'];
//   activeFilter = 'tous';
//   segmentFilter = 'tous';  // ← filtre par segment (VIP, Régulier, Nouveau, Inactif)
//   searchQuery = '';

//   currentPage = 1;
//   pageSize = 10;
//   totalPages = 1;
//   totalItems = 0;

//   isLoading = true;
//   showScoring = false;  // ← toggle entre vue normale et vue scoring

//   private apiUrl = 'http://localhost:5000';

//   constructor(
//     private usersService: UsersDashboard,
//     private http: HttpClient
//   ) {}

//   ngOnInit(): void {
//     this.loadUsers();
//     this.loadScoring();
//   }

//   loadUsers(): void {
//     this.isLoading = true;
//     const role = this.activeFilter === 'tous' ? '' : this.activeFilter;
//     this.usersService.getAll(this.currentPage, this.pageSize, this.searchQuery, role)
//       .subscribe({
//         next: (response) => {
//           this.users = response.data;
//           this.totalPages = response.pages;
//           this.totalItems = response.total;
//           this.isLoading = false;
//         },
//         error: () => this.isLoading = false
//       });
//   }

//   loadScoring(): void {
//     this.usersService.getScoring().subscribe({
//       next: (res) => {
//         this.scoringData = res.data;
//         this.applySegmentFilter();
//       },
//       error: (err) => console.error('Scoring error:', err)
//     });
//   }

//   applySegmentFilter(): void {
//     if (this.segmentFilter === 'tous') {
//       this.filteredScoringData = this.scoringData;
//     } else {
//       this.filteredScoringData = this.scoringData.filter(
//         user => user.segment === this.segmentFilter
//       );
//     }
//   }

//   onSegmentFilterChange(segment: string): void {
//     this.segmentFilter = segment;
//     this.applySegmentFilter();
//   }

//   getSegmentClass(segment: string): string {
//     const map: any = {
//       'VIP':      'segment-vip',
//       'Régulier': 'segment-regulier',
//       'Nouveau':  'segment-nouveau',
//       'Inactif':  'segment-inactif',
//     };
//     return map[segment] || 'segment-inactif';
//   }

//   getSegmentIcon(segment: string): string {
//     const map: any = {
//       'VIP':      '🥇',
//       'Régulier': '🥈',
//       'Nouveau':  '🥉',
//       'Inactif':  '⚪',
//     };
//     return map[segment] || '⚪';
//   }

//   onFilterChange(filter: string): void {
//     this.activeFilter = filter;
//     this.currentPage = 1;
//     this.loadUsers();
//   }

//   onSearch(): void {
//     this.currentPage = 1;
//     this.loadUsers();
//   }

//   getInitials(name: string): string {
//     if (!name) return '';
//     return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
//   }

//   getAvatarColor(role: string): string {
//     const colorMap: any = {
//       'admin':  '#ef4444',
//       'agent':  '#f59e0b',
//       'client': '#6366f1'
//     };
//     return colorMap[role] || '#6366f1';
//   }

//   viewUser(user: any): void {
//     console.log('Voir utilisateur', user);
//     alert(`Voir utilisateur : ${user.name} (${user.email})`);
//   }

//   editUser(user: any): void {
//     console.log('Modifier utilisateur', user);
//     alert(`Modifier utilisateur : ${user.name} (${user.email})`);
//   }

//   toggleUserStatus(user: any): void {
//     const newStatus = user.status === 'actif' ? 'inactif' : 'actif';
//     this.usersService.updateStatus(user.id, newStatus).subscribe({
//       next: (updated) => {
//         user.status = updated.status;
//       },
//       error: (err) => {
//         console.error('Erreur mise à jour statut utilisateur :', err);
//         alert('Impossible de modifier le statut de l\'utilisateur.');
//       }
//     });
//   }

//   nextPage(): void {
//     if (this.currentPage < this.totalPages) { this.currentPage++; this.loadUsers(); }
//   }

//   prevPage(): void {
//     if (this.currentPage > 1) { this.currentPage--; this.loadUsers(); }
//   }
// }


////////////////hatha lcode jdid li 3andi
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { StatusBadgeComponent } from '../shared/status-badge/status-badge';
import { ClientCellComponent } from '../shared/client-cell/client-cell';
import { FilterBarComponent } from '../shared/filter-bar/filter-bar';
import { RoleBadgeComponent } from '../shared/role-badge/role-badge';
import { UsersDashboard } from '../../services/users-dashboard';
import { ScoreFilterPipe } from '../../pipes/score-filter.pipe';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ScoreFilterPipe,
    ClientCellComponent, FilterBarComponent,
    RoleBadgeComponent, StatusBadgeComponent
  ],
  templateUrl: './users-dashboard.html',
  styleUrls: ['./users-dashboard.css']
})
export class UsersComponent implements OnInit {
  users: any[]              = [];
  scoringData: any[]        = [];
  filteredScoringData: any[] = [];

  filters      = ['tous', 'admin', 'client'];
  activeFilter = 'tous';
  segmentFilter = 'tous';
  searchQuery   = '';

  currentPage = 1;
  pageSize    = 10;
  totalPages  = 1;
  totalItems  = 0;

  isLoading  = true;
  showScoring = false;

  // ✅ NOUVEAU — CRM Profile
  selectedUser:     any     = null;
  clientProfile:    any     = null;
  isLoadingProfile: boolean = false;
  showCrmPanel:     boolean = false;
  expandedSummaries:  boolean[] = [];  // contrôle l'affichage du résumé détaillé

  private apiUrl = 'http://localhost:5000';

  constructor(
    private usersService: UsersDashboard,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadScoring();
  }

  loadUsers(): void {
    this.isLoading = true;
    const role = this.activeFilter === 'tous' ? '' : this.activeFilter;
    this.usersService.getAll(this.currentPage, this.pageSize, this.searchQuery, role)
      .subscribe({
        next: (response) => {
          this.users      = response.data;
          this.totalPages = response.pages;
          this.totalItems = response.total;
          this.isLoading  = false;
        },
        error: () => this.isLoading = false
      });
  }

  loadScoring(): void {
    this.usersService.getScoring().subscribe({
      next: (res) => {
        this.scoringData = res.data;
        this.applySegmentFilter();
      },
      error: (err) => console.error('Scoring error:', err)
    });
  }

  applySegmentFilter(): void {
    this.filteredScoringData = this.segmentFilter === 'tous'
      ? this.scoringData
      : this.scoringData.filter(u => u.segment === this.segmentFilter);
  }

  onSegmentFilterChange(segment: string): void {
    this.segmentFilter = segment;
    this.applySegmentFilter();
  }

  // ✅ NOUVEAU — Ouvrir le profil CRM d'un client
  openCrmProfile(user: any): void {
    this.selectedUser  = user;
    this.showCrmPanel  = true;
    this.clientProfile = null;
    this.expandedSummaries = [];
    this.isLoadingProfile = true;

    const token   = localStorage.getItem('token');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

    this.http.get<any>(
      `${this.apiUrl}/admin/users/${user.id}/crm-profile`,
      { headers }
    ).subscribe({
      next: (data) => {
        this.clientProfile    = data;
        this.isLoadingProfile = false;
      },
      error: (err) => {
        console.error('❌ Erreur profil CRM:', err);
        this.isLoadingProfile = false;
      }
    });
  }
  
  toggleSummary(index: number): void {
    this.expandedSummaries[index] = !this.expandedSummaries[index];
  }
 
  // ✅ NOUVEAU — Fermer le panneau CRM
  closeCrmPanel(): void {
    this.showCrmPanel  = false;
    this.selectedUser  = null;
    this.clientProfile = null;
    this.expandedSummaries  = [];
  }

  // ✅ NOUVEAU — Classes CSS dynamiques
  getChurnClass(risk: string): string {
    const map: Record<string, string> = {
      'Critique': 'churn-critique',
      'Élevé':    'churn-eleve',
      'Moyen':    'churn-moyen',
      'Faible':   'churn-faible',
    };
    return map[risk] || 'churn-faible';
  }

  getCategoryClass(cat: string): string {
    const map: Record<string, string> = {
      'urgent':        'badge-urgent',
      'reclamation':   'badge-reclamation',
      'demande_devis': 'badge-devis',
      'info':          'badge-info',
    };
    return map[cat] || 'badge-info';
  }

  getCategoryLabel(cat: string): string {
    const map: Record<string, string> = {
      'urgent':        '🔴 Urgent',
      'reclamation':   '🟠 Réclamation',
      'demande_devis': '🟢 Devis',
      'info':          '🔵 Info',
    };
    return map[cat] || cat;
  }

  getSegmentClass(segment: string): string {
    const map: Record<string, string> = {
      'VIP':      'segment-vip',
      'Régulier': 'segment-regulier',
      'Nouveau':  'segment-nouveau',
      'Inactif':  'segment-inactif',
    };
    return map[segment] || 'segment-inactif';
  }

  getSegmentIcon(segment: string): string {
    const map: Record<string, string> = {
      'VIP':      '🥇',
      'Régulier': '🥈',
      'Nouveau':  '🥉',
      'Inactif':  '⚪',
    };
    return map[segment] || '⚪';
  }

  // Méthodes existantes conservées
  onFilterChange(filter: string): void {
    this.activeFilter = filter;
    this.currentPage  = 1;
    this.loadUsers();
  }

  onSearch(): void {
    this.currentPage = 1;
    this.loadUsers();
  }

  getInitials(name: string): string {
    if (!name) return '';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
  }

  getAvatarColor(role: string): string {
    const colorMap: Record<string, string> = {
      'admin':  '#ef4444',
      'agent':  '#f59e0b',
      'client': '#6366f1'
    };
    return colorMap[role] || '#6366f1';
  }

  toggleUserStatus(user: any): void {
    const newStatus = user.status === 'actif' ? 'inactif' : 'actif';
    this.usersService.updateStatus(user.id, newStatus).subscribe({
      next: (updated) => { user.status = updated.status; },
      error: (err) => console.error('Erreur statut:', err)
    });
  }

  viewUser(user: any):  void { console.log('Voir', user); }
  editUser(user: any):  void { console.log('Modifier', user); }

  nextPage(): void {
    if (this.currentPage < this.totalPages) { this.currentPage++; this.loadUsers(); }
  }

  prevPage(): void {
    if (this.currentPage > 1) { this.currentPage--; this.loadUsers(); }
  }
}