package br.com.fiap.solidari.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.solidari.backend.dto.DoacaoRequest;
import br.com.fiap.solidari.backend.dto.ImpactoResponse;
import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.model.Doacao;
import br.com.fiap.solidari.backend.model.Usuario;
import br.com.fiap.solidari.backend.repository.DoacaoRepository;
import br.com.fiap.solidari.backend.repository.jdbc.FunctionRepository;
import br.com.fiap.solidari.backend.repository.jdbc.ProcedureRepository;

@Service
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final ProcedureRepository procedures;
    private final FunctionRepository functions;

    public DoacaoService(
            DoacaoRepository doacaoRepository,
            ProcedureRepository procedures,
            FunctionRepository functions
    ) {
        this.doacaoRepository = doacaoRepository;
        this.procedures = procedures;
        this.functions = functions;
    }

    @Transactional
    public Doacao doar(Usuario usuario, DoacaoRequest request) {
        Long doacaoId = procedures.registrarDoacao(
                usuario.getId(),
                request.parceiroId(),
                request.valor(),
                request.categoriaImpacto()
        );

        return doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Doacao " + doacaoId + " nao encontrada apos o registro"));
    }

    public List<Doacao> historico(Long usuarioId) {
        return doacaoRepository.findByUsuarioIdOrderByDataDesc(usuarioId);
    }

    public ImpactoResponse calcularImpacto(Long usuarioId) {
        return functions.impactoDoUsuario(usuarioId);
    }

    public String extrato(Long usuarioId, int limite) {
        return functions.extratoFormatado(usuarioId, limite);
    }

    public int ranking(Long usuarioId) {
        return functions.rankingDoador(usuarioId);
    }
}
