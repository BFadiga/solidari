package br.com.fiap.solidari.backend.dto;

import java.time.LocalDateTime;

import br.com.fiap.solidari.backend.model.Alerta;
import br.com.fiap.solidari.backend.model.SeveridadeAlerta;

public record AlertaResponse(
        Long id,
        String tipo,
        SeveridadeAlerta severidade,
        String descricao,
        LocalDateTime criadoEm
) {
    public static AlertaResponse de(Alerta alerta) {
        return new AlertaResponse(
                alerta.getId(),
                alerta.getTipo(),
                alerta.getSeveridade(),
                alerta.getDescricao(),
                alerta.getCriadoEm()
        );
    }
}
