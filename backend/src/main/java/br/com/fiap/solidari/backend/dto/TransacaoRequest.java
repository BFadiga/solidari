package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransacaoRequest(

        @NotNull(message = "Parceiro é obrigatório")
        Long parceiroId,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal valor
) {
}
