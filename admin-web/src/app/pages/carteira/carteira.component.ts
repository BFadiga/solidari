import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Doacao, Extrato, Impacto, Parceiro, Transacao, Usuario } from '../../models/parceiro.model';
import { AuthService } from '../../services/auth.service';
import { CarteiraService } from '../../services/carteira.service';
import { ParceiroService } from '../../services/parceiro.service';

@Component({
  selector: 'app-carteira',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './carteira.component.html',
  styleUrl: './carteira.component.css'
})
export class CarteiraComponent implements OnInit {

  perfil: Usuario | null = null;
  impacto: Impacto | null = null;
  extrato: Extrato | null = null;
  doacoes: Doacao[] = [];
  transacoes: Transacao[] = [];
  parceiros: Parceiro[] = [];

  compraParceiroId: number | null = null;
  compraValor: number | null = null;

  doacaoValor: number | null = null;
  doacaoCategoria = 'RESTAURACAO_AMBIENTAL';
  doacaoParceiroId: number | null = null;

  mensagem = '';
  erro = false;

  constructor(
    public auth: AuthService,
    private carteira: CarteiraService,
    private parceiroService: ParceiroService
  ) {}

  ngOnInit(): void {
    this.parceiroService.listar().subscribe((lista) => this.parceiros = lista);
    if (this.auth.estaAutenticado) {
      this.carregar();
    }
  }

  carregar(): void {
    this.carteira.perfil().subscribe({
      next: (u) => {
        this.perfil = u;
        this.auth.atualizarUsuario(u);
      },
      error: () => this.avisar('Sua sessão expirou. Entre novamente.', true)
    });
    this.carteira.impacto().subscribe((i) => this.impacto = i);
    this.carteira.extrato(10).subscribe((e) => this.extrato = e);
    this.carteira.doacoes().subscribe((d) => this.doacoes = d);
    this.carteira.transacoes().subscribe((t) => this.transacoes = t);
  }

  registrarCompra(): void {
    if (!this.compraParceiroId || !this.compraValor) {
      return;
    }
    this.carteira.registrarCompra(this.compraParceiroId, this.compraValor).subscribe({
      next: () => {
        this.avisar('Compra registrada. O cashback entra na carteira assim que for apurado.', false);
        this.compraValor = null;
        this.carregar();
      },
      error: (e) => this.avisar(this.mensagemDoErro(e, 'Não foi possível registrar a compra.'), true)
    });
  }

  doar(): void {
    if (!this.doacaoValor) {
      return;
    }
    this.carteira.doar(this.doacaoValor, this.doacaoCategoria, this.doacaoParceiroId).subscribe({
      next: () => {
        this.avisar('Doação confirmada. Obrigado por contribuir!', false);
        this.doacaoValor = null;
        this.carregar();
      },
      error: (e) => this.avisar(this.mensagemDoErro(e, 'Não foi possível concluir a doação.'), true)
    });
  }

  cashbackPrevisto(): number | null {
    const parceiro = this.parceiros.find((p) => p.id === this.compraParceiroId);
    if (!parceiro || !this.compraValor) {
      return null;
    }
    return this.compraValor * parceiro.percentualCashback / 100;
  }

  private avisar(texto: string, ehErro: boolean): void {
    this.mensagem = texto;
    this.erro = ehErro;
  }

  private mensagemDoErro(e: any, padrao: string): string {
    return e?.error?.mensagem ?? padrao;
  }
}
