package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.solidari.backend.model.StatusTransacao;
import br.com.fiap.solidari.backend.model.Transacao;

public record TransacaoResponse(
        Long id,
        String parceiroNome,
        BigDecimal valor,
        BigDecimal cashbackGerado,
        StatusTransacao status,
        LocalDateTime data
) {
    public static TransacaoResponse de(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getParceiro().getNome(),
                transacao.getValor(),
                transacao.getCashbackGerado(),
                transacao.getStatus(),
                transacao.getData()
        );
    }
}
