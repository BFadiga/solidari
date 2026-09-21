import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Parceiro } from '../models/parceiro.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ParceiroService {

  private readonly url = `${environment.apiUrl}/parceiros`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  listar(): Observable<Parceiro[]> {
    return this.http.get<Parceiro[]>(this.url);
  }

  criar(parceiro: Parceiro): Observable<Parceiro> {
    return this.http.post<Parceiro>(this.url, parceiro, { headers: this.headersAutenticadas() });
  }

  atualizar(id: number, parceiro: Parceiro): Observable<Parceiro> {
    return this.http.put<Parceiro>(`${this.url}/${id}`, parceiro, { headers: this.headersAutenticadas() });
  }

  remover(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`, { headers: this.headersAutenticadas() });
  }

  private headersAutenticadas(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.token}` });
  }
}
