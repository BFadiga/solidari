export interface Parceiro {
  id?: number;
  nome: string;
  descricao: string;
  categoria: string;
  badge: string;
  percentualCashback: number;
  imagemUrl?: string;
  destaque: boolean;
  latitude?: number;
  longitude?: number;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  saldo: number;
  papel: 'USUARIO' | 'ADMIN';
}

export interface Impacto {
  arvoresPlantadas: number;
  refeicoesServidas: number;
  totalDoado: number;
}

export interface Extrato {
  extrato: string;
  posicaoRanking: number;
}

export interface Doacao {
  id: number;
  valor: number;
  categoriaImpacto: string;
  data: string;
  parceiroNome: string | null;
}

export interface Transacao {
  id: number;
  parceiroNome: string;
  valor: number;
  cashbackGerado: number | null;
  status: 'PENDENTE' | 'PROCESSADA' | 'ERRO';
  data: string;
}

export interface ResultadoCashback {
  processadas: number;
  comErro: number;
  totalCreditado: number;
}

export interface Alerta {
  id: number;
  tipo: string;
  severidade: 'INFO' | 'ATENCAO' | 'CRITICO';
  descricao: string;
  criadoEm: string;
}
