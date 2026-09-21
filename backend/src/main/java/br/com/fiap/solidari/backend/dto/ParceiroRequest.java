package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParceiroRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotBlank(message = "Categoria é obrigatória")
        String categoria,

        @NotBlank(message = "Badge é obrigatório")
        String badge,

        @NotNull(message = "Percentual de cashback é obrigatório")
        @DecimalMin(value = "0.0", message = "Percentual não pode ser negativo")
        @DecimalMax(value = "100.0", message = "Percentual não pode passar de 100")
        BigDecimal percentualCashback,

        String imagemUrl,

        @NotNull(message = "Destaque é obrigatório")
        Boolean destaque,

        Double latitude,

        Double longitude
) {
}
