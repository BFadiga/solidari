package br.com.fiap.solidari.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.solidari.backend.dto.CashbackResponse;
import br.com.fiap.solidari.backend.dto.TransacaoRequest;
import br.com.fiap.solidari.backend.dto.TransacaoResponse;
import br.com.fiap.solidari.backend.model.Usuario;
import br.com.fiap.solidari.backend.service.TransacaoService;
import br.com.fiap.solidari.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transacoes")
@Tag(name = "Transações", description = "Compras em parceiros e apuração de cashback")
public class TransacaoController {

    private final TransacaoService transacaoService;
    private final UsuarioService usuarioService;

    public TransacaoController(TransacaoService transacaoService, UsuarioService usuarioService) {
        this.transacaoService = transacaoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Registra uma compra em parceiro; o cashback é apurado no processamento em lote")
    public ResponseEntity<TransacaoResponse> registrar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransacaoRequest request
    ) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        TransacaoResponse response = TransacaoResponse.de(transacaoService.registrar(usuario, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Compras do usuário autenticado")
    public List<TransacaoResponse> historico(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        return transacaoService.historico(usuario.getId()).stream().map(TransacaoResponse::de).toList();
    }

    @PostMapping("/processar-cashback")
    @Operation(summary = "Executa a procedure sp_processar_cashback e credita as transações pendentes (ADMIN)")
    public CashbackResponse processarCashback() {
        return transacaoService.processarCashback();
    }
}
