import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-entrar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './entrar.component.html',
  styleUrl: './entrar.component.css'
})
export class EntrarComponent {

  email = '';
  senha = '';
  erro = '';
  enviando = false;

  constructor(private auth: AuthService, private router: Router) {}

  entrar(): void {
    this.erro = '';
    this.enviando = true;

    this.auth.login(this.email.trim(), this.senha).subscribe({
      next: (resposta) => {
        this.enviando = false;
        this.router.navigate([resposta.usuario.papel === 'ADMIN' ? '/admin' : '/carteira']);
      },
      error: (e: HttpErrorResponse) => {
        this.enviando = false;
        this.erro = this.descreverFalha(e);
      }
    });
  }

  preencher(email: string, senha: string): void {
    this.email = email;
    this.senha = senha;
  }

  private descreverFalha(e: HttpErrorResponse): string {
    if (e.status === 0) {
      return 'Não foi possível falar com o servidor. Verifique se a API está no ar.';
    }
    if (e.status === 401) {
      return 'E-mail ou senha incorretos.';
    }
    if (e.status === 400) {
      return 'Informe um e-mail válido e a senha.';
    }
    return `Não foi possível entrar agora (erro ${e.status}).`;
  }
}
