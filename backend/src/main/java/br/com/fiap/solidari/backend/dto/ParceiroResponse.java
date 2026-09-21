package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;

import br.com.fiap.solidari.backend.model.Parceiro;

public record ParceiroResponse(
        Long id,
        String nome,
        String descricao,
        String categoria,
        String badge,
        BigDecimal percentualCashback,
        String imagemUrl,
        Boolean destaque,
        Double latitude,
        Double longitude
) {
    public static ParceiroResponse de(Parceiro parceiro) {
        return new ParceiroResponse(
                parceiro.getId(),
                parceiro.getNome(),
                parceiro.getDescricao(),
                parceiro.getCategoria(),
                parceiro.getBadge(),
                parceiro.getPercentualCashback(),
                parceiro.getImagemUrl(),
                parceiro.getDestaque(),
                parceiro.getLatitude(),
                parceiro.getLongitude()
        );
    }
}
