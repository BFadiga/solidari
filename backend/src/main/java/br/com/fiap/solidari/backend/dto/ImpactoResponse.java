package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;

public record ImpactoResponse(
        long arvoresPlantadas,
        long refeicoesServidas,
        BigDecimal totalDoado
) {
}
