import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

interface UsuarioLogado {
  id: number;
  nome: string;
  email: string;
  saldo: number;
  papel: string;
}

interface AuthResponse {
  token: string;
  tipo: string;
  usuario: UsuarioLogado;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly tokenKey = 'solidari-admin-token';

  constructor(private http: HttpClient) {}

  login(email: string, senha: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, senha })
      .pipe(tap((resposta) => localStorage.setItem(this.tokenKey, resposta.token)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get estaAutenticado(): boolean {
    return !!this.token;
  }
}
