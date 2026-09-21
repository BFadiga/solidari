package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.solidari.backend.model.CategoriaImpacto;
import br.com.fiap.solidari.backend.model.Doacao;

public record DoacaoResponse(
        Long id,
        BigDecimal valor,
        CategoriaImpacto categoriaImpacto,
        LocalDateTime data,
        String parceiroNome
) {
    public static DoacaoResponse de(Doacao doacao) {
        return new DoacaoResponse(
                doacao.getId(),
                doacao.getValor(),
                doacao.getCategoriaImpacto(),
                doacao.getData(),
                doacao.getParceiro() != null ? doacao.getParceiro().getNome() : null
        );
    }
}
