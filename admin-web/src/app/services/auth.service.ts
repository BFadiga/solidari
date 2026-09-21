import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Usuario } from '../models/parceiro.model';

interface AuthResponse {
  token: string;
  tipo: string;
  usuario: Usuario;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly tokenKey = 'solidari-admin-token';
  private readonly usuarioKey = 'solidari-admin-usuario';

  readonly usuario = signal<Usuario | null>(this.lerUsuarioSalvo());

  constructor(private http: HttpClient) {}

  login(email: string, senha: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, senha })
      .pipe(tap((resposta) => {
        localStorage.setItem(this.tokenKey, resposta.token);
        localStorage.setItem(this.usuarioKey, JSON.stringify(resposta.usuario));
        this.usuario.set(resposta.usuario);
      }));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.usuarioKey);
    this.usuario.set(null);
  }

  atualizarUsuario(usuario: Usuario): void {
    localStorage.setItem(this.usuarioKey, JSON.stringify(usuario));
    this.usuario.set(usuario);
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get estaAutenticado(): boolean {
    return !!this.token;
  }

  get ehAdmin(): boolean {
    return this.usuario()?.papel === 'ADMIN';
  }

  private lerUsuarioSalvo(): Usuario | null {
    const bruto = localStorage.getItem(this.usuarioKey);
    return bruto ? JSON.parse(bruto) as Usuario : null;
  }
}
