import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UsersDashboard {
  private apiUrl = 'http://127.0.0.1:5000/admin/users';

  constructor(private http: HttpClient) {}

  getAll(page: number = 1, limit: number = 10, search: string = '', role: string = ''): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('limit', limit.toString());
    
    if (search) params = params.set('search', search);
    if (role) params = params.set('role', role);

    const token = localStorage.getItem('access_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get(this.apiUrl, { params, headers });
  }

  updateStatus(id: number, status: string): Observable<any> {
    const token = localStorage.getItem('access_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put(`${this.apiUrl}/${id}/status`, { status }, { headers });
  }

  updateRole(id: number, role: string): Observable<any> {
    const token = localStorage.getItem('access_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put(`${this.apiUrl}/${id}/role`, { role }, { headers });
  }

  getScoring(): Observable<any> {
  return this.http.get(`${this.apiUrl}/scoring`
  //   , 
  //   {
  //   headers: this.getHeaders()
  // }
);
}

}