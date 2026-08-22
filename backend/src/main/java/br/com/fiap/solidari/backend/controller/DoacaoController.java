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

import br.com.fiap.solidari.backend.dto.DoacaoRequest;
import br.com.fiap.solidari.backend.dto.DoacaoResponse;
import br.com.fiap.solidari.backend.dto.ImpactoResponse;
import br.com.fiap.solidari.backend.model.Usuario;
import br.com.fiap.solidari.backend.service.DoacaoService;
import br.com.fiap.solidari.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doacoes")
@Tag(name = "Doações", description = "Doações de crédito do usuário e impacto gerado")
public class DoacaoController {

    private final DoacaoService doacaoService;
    private final UsuarioService usuarioService;

    public DoacaoController(DoacaoService doacaoService, UsuarioService usuarioService) {
        this.doacaoService = doacaoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Registra uma doação de crédito do usuário autenticado")
    public ResponseEntity<DoacaoResponse> doar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoacaoRequest request
    ) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        DoacaoResponse response = DoacaoResponse.de(doacaoService.doar(usuario, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Histórico de doações do usuário autenticado")
    public List<DoacaoResponse> historico(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        return doacaoService.historico(usuario.getId()).stream().map(DoacaoResponse::de).toList();
    }

    @GetMapping("/impacto")
    @Operation(summary = "Estatísticas de impacto do usuário autenticado (árvores, refeições)")
    public ImpactoResponse impacto(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        return doacaoService.calcularImpacto(usuario.getId());
    }
}
