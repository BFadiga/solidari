import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Alerta, Parceiro, ResultadoCashback } from '../../models/parceiro.model';
import { AuthService } from '../../services/auth.service';
import { ParceiroService } from '../../services/parceiro.service';
import { RotinasService } from '../../services/rotinas.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {

  parceiros: Parceiro[] = [];
  alertas: Alerta[] = [];
  carregando = false;
  mensagem = '';
  erro = false;

  ultimoCashback: ResultadoCashback | null = null;
  ultimosAlertasGerados: number | null = null;

  novoParceiro: Parceiro = this.parceiroVazio();
  emEdicao: Parceiro | null = null;

  constructor(
    public auth: AuthService,
    private parceiroService: ParceiroService,
    private rotinas: RotinasService
  ) {}

  ngOnInit(): void {
    this.carregarParceiros();
    if (this.auth.ehAdmin) {
      this.carregarAlertas();
    }
  }

  carregarParceiros(): void {
    this.carregando = true;
    this.parceiroService.listar().subscribe({
      next: (lista) => {
        this.parceiros = lista;
        this.carregando = false;
      },
      error: () => {
        this.avisar('Não foi possível carregar os parceiros.', true);
        this.carregando = false;
      }
    });
  }

  carregarAlertas(): void {
    this.rotinas.listarAlertas().subscribe({
      next: (lista) => this.alertas = lista,
      error: () => this.alertas = []
    });
  }

  criarParceiro(): void {
    this.parceiroService.criar(this.novoParceiro).subscribe({
      next: () => {
        this.avisar('Parceiro cadastrado.', false);
        this.novoParceiro = this.parceiroVazio();
        this.carregarParceiros();
      },
      error: (e) => this.avisar(this.mensagemDoErro(e, 'Não foi possível cadastrar o parceiro.'), true)
    });
  }

  editar(parceiro: Parceiro): void {
    this.emEdicao = { ...parceiro };
  }

  cancelarEdicao(): void {
    this.emEdicao = null;
  }

  salvarEdicao(): void {
    if (!this.emEdicao?.id) {
      return;
    }
    this.parceiroService.atualizar(this.emEdicao.id, this.emEdicao).subscribe({
      next: () => {
        this.avisar('Parceiro atualizado.', false);
        this.emEdicao = null;
        this.carregarParceiros();
      },
      error: (e) => this.avisar(this.mensagemDoErro(e, 'Não foi possível salvar as alterações.'), true)
    });
  }

  removerParceiro(id?: number): void {
    if (!id) {
      return;
    }
    this.parceiroService.remover(id).subscribe({
      next: () => {
        this.avisar('Parceiro removido.', false);
        this.carregarParceiros();
      },
      error: (e) => this.avisar(this.mensagemDoErro(e, 'Não foi possível remover o parceiro.'), true)
    });
  }

  processarCashback(): void {
    this.rotinas.processarCashback().subscribe({
      next: (resultado) => {
        this.ultimoCashback = resultado;
        this.avisar(`Apuração concluída: ${resultado.processadas} compra(s) creditada(s).`, false);
      },
      error: (e) => this.avisar(this.mensagemDoErro(e, 'Não foi possível apurar o cashback.'), true)
    });
  }

  gerarAlertas(): void {
    this.rotinas.gerarAlertas().subscribe({
      next: (resultado) => {
        this.ultimosAlertasGerados = resultado.gerados;
        this.avisar(resultado.gerados === 0
          ? 'Nenhuma pendência encontrada.'
          : `Verificação concluída: ${resultado.gerados} pendência(s) encontrada(s).`, false);
        this.carregarAlertas();
      },
      error: (e) => this.avisar(this.mensagemDoErro(e, 'Não foi possível verificar as pendências.'), true)
    });
  }

  private avisar(texto: string, ehErro: boolean): void {
    this.mensagem = texto;
    this.erro = ehErro;
  }

  private mensagemDoErro(e: any, padrao: string): string {
    if (e?.status === 403) {
      return 'Esta ação exige uma conta de administrador.';
    }
    return e?.error?.mensagem ?? padrao;
  }

  private parceiroVazio(): Parceiro {
    return {
      nome: '',
      descricao: '',
      categoria: '',
      badge: '',
      percentualCashback: 10,
      destaque: false
    };
  }
}
