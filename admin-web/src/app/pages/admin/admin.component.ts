import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Parceiro } from '../../models/parceiro.model';
import { AuthService } from '../../services/auth.service';
import { ParceiroService } from '../../services/parceiro.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {

  parceiros: Parceiro[] = [];
  carregando = false;
  mensagem = '';
  erro = false;

  email = '';
  senha = '';

  novoParceiro: Parceiro = this.parceiroVazio();

  constructor(private auth: AuthService, private parceiroService: ParceiroService) {}

  ngOnInit(): void {
    this.carregarParceiros();
  }

  get autenticado(): boolean {
    return this.auth.estaAutenticado;
  }

  entrar(): void {
    this.auth.login(this.email, this.senha).subscribe({
      next: () => {
        this.exibirMensagem('Login realizado com sucesso.', false);
        this.senha = '';
      },
      error: () => this.exibirMensagem('E-mail ou senha inválidos.', true)
    });
  }

  sair(): void {
    this.auth.logout();
  }

  carregarParceiros(): void {
    this.carregando = true;
    this.parceiroService.listar().subscribe({
      next: (lista) => {
        this.parceiros = lista;
        this.carregando = false;
      },
      error: () => {
        this.exibirMensagem('Não foi possível carregar os parceiros.', true);
        this.carregando = false;
      }
    });
  }

  criarParceiro(): void {
    this.parceiroService.criar(this.novoParceiro).subscribe({
      next: () => {
        this.exibirMensagem('Parceiro criado com sucesso.', false);
        this.novoParceiro = this.parceiroVazio();
        this.carregarParceiros();
      },
      error: () => this.exibirMensagem('Erro ao criar parceiro. Faça login como administrador.', true)
    });
  }

  removerParceiro(id?: number): void {
    if (!id) {
      return;
    }
    this.parceiroService.remover(id).subscribe({
      next: () => {
        this.exibirMensagem('Parceiro removido.', false);
        this.carregarParceiros();
      },
      error: () => this.exibirMensagem('Erro ao remover parceiro. Faça login como administrador.', true)
    });
  }

  private exibirMensagem(texto: string, ehErro: boolean): void {
    this.mensagem = texto;
    this.erro = ehErro;
  }

  private parceiroVazio(): Parceiro {
    return { nome: '', descricao: '', categoria: '', badge: '', destaque: false };
  }
}
