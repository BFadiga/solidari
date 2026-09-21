import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Alerta, ResultadoCashback } from '../models/parceiro.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class RotinasService {

  constructor(private http: HttpClient, private auth: AuthService) {}

  processarCashback(): Observable<ResultadoCashback> {
    return this.http.post<ResultadoCashback>(`${environment.apiUrl}/transacoes/processar-cashback`,
      {}, { headers: this.headers() });
  }

  gerarAlertas(): Observable<{ gerados: number }> {
    return this.http.post<{ gerados: number }>(`${environment.apiUrl}/alertas/gerar`,
      {}, { headers: this.headers() });
  }

  listarAlertas(): Observable<Alerta[]> {
    return this.http.get<Alerta[]>(`${environment.apiUrl}/alertas`, { headers: this.headers() });
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.token}` });
  }
}
