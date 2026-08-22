export interface Parceiro {
  id?: number;
  nome: string;
  descricao: string;
  categoria: string;
  badge: string;
  imagemUrl?: string;
  destaque: boolean;
  latitude?: number;
  longitude?: number;
}
