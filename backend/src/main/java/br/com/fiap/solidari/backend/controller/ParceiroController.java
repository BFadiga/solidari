package br.com.fiap.solidari.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.solidari.backend.dto.ParceiroRequest;
import br.com.fiap.solidari.backend.dto.ParceiroResponse;
import br.com.fiap.solidari.backend.service.ParceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parceiros")
@Tag(name = "Parceiros", description = "Parceiros e vantagens de cashback do Solidari")
public class ParceiroController {

    private final ParceiroService parceiroService;

    public ParceiroController(ParceiroService parceiroService) {
        this.parceiroService = parceiroService;
    }

    @GetMapping
    @Operation(summary = "Lista os parceiros, opcionalmente filtrando por categoria")
    public List<ParceiroResponse> listar(@RequestParam(required = false) String categoria) {
        return parceiroService.listar(categoria).stream().map(ParceiroResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um parceiro pelo id")
    public ParceiroResponse buscar(@PathVariable Long id) {
        return ParceiroResponse.de(parceiroService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria um novo parceiro (somente ADMIN)")
    public ResponseEntity<ParceiroResponse> criar(@Valid @RequestBody ParceiroRequest request) {
        ParceiroResponse response = ParceiroResponse.de(parceiroService.criar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um parceiro existente (somente ADMIN)")
    public ParceiroResponse atualizar(@PathVariable Long id, @Valid @RequestBody ParceiroRequest request) {
        return ParceiroResponse.de(parceiroService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um parceiro (somente ADMIN)")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        parceiroService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
