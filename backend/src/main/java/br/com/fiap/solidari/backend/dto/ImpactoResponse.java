package br.com.fiap.solidari.backend.dto;

public record ImpactoResponse(
        long arvoresPlantadas,
        long refeicoesServidas,
        double totalDoado
) {
}
