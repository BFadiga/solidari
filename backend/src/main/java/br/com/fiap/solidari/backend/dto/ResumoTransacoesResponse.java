package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;

/** Linha do resumo agregado (pkg_relatorios.fn_resumo_transacoes). */
public record ResumoTransacoesResponse(
        String chave,
        long quantidade,
        BigDecimal totalValor,
        BigDecimal totalCashback
) {
}
