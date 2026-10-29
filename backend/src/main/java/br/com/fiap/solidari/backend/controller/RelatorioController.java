package br.com.fiap.solidari.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.solidari.backend.dto.FiltroTransacoes;
import br.com.fiap.solidari.backend.dto.ResumoTransacoesResponse;
import br.com.fiap.solidari.backend.dto.TransacaoRelatorioResponse;
import br.com.fiap.solidari.backend.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Consultas flexíveis do painel, montadas com Dynamic SQL (pkg_relatorios)")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/transacoes")
    @Operation(summary = "Busca transações com filtros opcionais e ordenação escolhida (ADMIN)",
            description = "ordenarPor: DATA, VALOR, CASHBACK, PARCEIRO ou USUARIO. "
                    + "Datas no formato AAAA-MM-DD; a data final é inclusiva.")
    public List<TransacaoRelatorioResponse> buscarTransacoes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long parceiroId,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) BigDecimal valorMinimo,
            @RequestParam(required = false, defaultValue = "DATA") String ordenarPor,
            @RequestParam(required = false, defaultValue = "100") Integer limite
    ) {
        FiltroTransacoes filtro = new FiltroTransacoes(
                status, parceiroId, categoria, dataInicio, dataFim, valorMinimo, ordenarPor, limite);
        return relatorioService.buscarTransacoes(filtro);
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo das transações agrupado pela dimensão escolhida (ADMIN)",
            description = "dimensao: PARCEIRO, CATEGORIA, STATUS ou MES")
    public List<ResumoTransacoesResponse> resumo(
            @RequestParam(defaultValue = "PARCEIRO") String dimensao,
            @RequestParam(required = false) String status
    ) {
        return relatorioService.resumo(dimensao, status);
    }
}
