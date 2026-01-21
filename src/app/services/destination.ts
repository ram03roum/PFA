import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Définition du type Destination
export interface Destination {
  id: number;
  name: string;
  description: string;
  price: number;
  image: string;
  highlights?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class DestinationService {

  private apiUrl = 'http://127.0.0.1:5000/destinations'; // URL de ton backend Flask

  constructor(private http: HttpClient) { }

  getDestinations(): Observable<Destination[]> {
    return this.http.get<Destination[]>(this.apiUrl);
  }
}
