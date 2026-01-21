import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DataService {
  private apiUrl = 'http://127.0.0.1:5000/destinations';

  constructor(private http: HttpClient) {}

  getDestinations(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}