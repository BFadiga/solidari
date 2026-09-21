package br.com.fiap.solidari.backend.dto;

import java.math.BigDecimal;

public record CashbackResponse(
        int processadas,
        int comErro,
        BigDecimal totalCreditado
) {
}
