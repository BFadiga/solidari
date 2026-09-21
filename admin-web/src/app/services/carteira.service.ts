import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Doacao, Extrato, Impacto, Transacao, Usuario } from '../models/parceiro.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CarteiraService {

  constructor(private http: HttpClient, private auth: AuthService) {}

  perfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${environment.apiUrl}/usuarios/me`, { headers: this.headers() });
  }

  impacto(): Observable<Impacto> {
    return this.http.get<Impacto>(`${environment.apiUrl}/doacoes/impacto`, { headers: this.headers() });
  }

  extrato(limite = 10): Observable<Extrato> {
    return this.http.get<Extrato>(`${environment.apiUrl}/doacoes/extrato?limite=${limite}`, { headers: this.headers() });
  }

  doacoes(): Observable<Doacao[]> {
    return this.http.get<Doacao[]>(`${environment.apiUrl}/doacoes/me`, { headers: this.headers() });
  }

  doar(valor: number, categoriaImpacto: string, parceiroId: number | null): Observable<Doacao> {
    return this.http.post<Doacao>(`${environment.apiUrl}/doacoes`,
      { valor, categoriaImpacto, parceiroId }, { headers: this.headers() });
  }

  transacoes(): Observable<Transacao[]> {
    return this.http.get<Transacao[]>(`${environment.apiUrl}/transacoes/me`, { headers: this.headers() });
  }

  registrarCompra(parceiroId: number, valor: number): Observable<Transacao> {
    return this.http.post<Transacao>(`${environment.apiUrl}/transacoes`,
      { parceiroId, valor }, { headers: this.headers() });
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.token}` });
  }
}
