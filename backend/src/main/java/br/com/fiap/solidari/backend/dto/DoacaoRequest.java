package br.com.fiap.solidari.backend.dto;

import br.com.fiap.solidari.backend.model.CategoriaImpacto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DoacaoRequest(

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        Double valor,

        @NotNull(message = "Categoria de impacto é obrigatória")
        CategoriaImpacto categoriaImpacto,

        Long parceiroId
) {
}
