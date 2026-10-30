package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha da busca dinamica de transacoes (pkg_relatorios.fn_buscar_transacoes). */
public record TransacaoRelatorioResponse(
        Long id,
        String usuario,
        String parceiro,
        String categoria,
        BigDecimal valor,
        BigDecimal cashbackGerado,
        String status,
        LocalDateTime data
) {
}
