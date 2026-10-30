package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros opcionais da busca de transacoes. Campo nulo = filtro nao aplicado;
 * o package monta o SQL so com os filtros presentes.
 */
public record FiltroTransacoes(
        String status,
        Long parceiroId,
        String categoria,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorMinimo,
        String ordenarPor,
        Integer limite
) {
}
