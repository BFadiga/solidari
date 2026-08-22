package br.com.fiap.solidari.backend.dto;

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

        String imagemUrl,

        @NotNull(message = "Destaque é obrigatório")
        Boolean destaque,

        Double latitude,

        Double longitude
) {
}
