import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DestinationService {
  private apiUrl = 'http://127.0.0.1:5000/api/destinations'; // L'URL de ton Flask

  constructor(private http: HttpClient) { }

  getDestinations(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}
