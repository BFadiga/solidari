package br.com.fiap.solidari.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.solidari.backend.dto.AlertaResponse;
import br.com.fiap.solidari.backend.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Alertas operacionais gerados pelo banco")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    @Operation(summary = "Lista os alertas em aberto (ADMIN)")
    public List<AlertaResponse> listar() {
        return alertaService.pendentes().stream().map(AlertaResponse::de).toList();
    }

    @PostMapping("/gerar")
    @Operation(summary = "Executa a procedure sp_gerar_alertas e devolve quantos foram criados (ADMIN)")
    public Map<String, Integer> gerar() {
        return Map.of("gerados", alertaService.gerar());
    }
}
