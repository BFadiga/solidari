package br.com.fiap.solidari.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.solidari.backend.dto.CashbackResponse;
import br.com.fiap.solidari.backend.dto.TransacaoRequest;
import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.exception.RegraNegocioException;
import br.com.fiap.solidari.backend.model.Parceiro;
import br.com.fiap.solidari.backend.model.Transacao;
import br.com.fiap.solidari.backend.model.Usuario;
import br.com.fiap.solidari.backend.repository.ParceiroRepository;
import br.com.fiap.solidari.backend.repository.TransacaoRepository;
import br.com.fiap.solidari.backend.repository.jdbc.ProcedureRepository;

@Service
public class TransacaoService {

    private static final int LIMITE_LOTE = 500;

    private final TransacaoRepository transacaoRepository;
    private final ParceiroRepository parceiroRepository;
    private final ProcedureRepository procedures;

    public TransacaoService(
            TransacaoRepository transacaoRepository,
            ParceiroRepository parceiroRepository,
            ProcedureRepository procedures
    ) {
        this.transacaoRepository = transacaoRepository;
        this.parceiroRepository = parceiroRepository;
        this.procedures = procedures;
    }

    @Transactional
    public Transacao registrar(Usuario usuario, TransacaoRequest request) {
        Parceiro parceiro = parceiroRepository.findById(request.parceiroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Parceiro não encontrado: id " + request.parceiroId()));

        if (Boolean.FALSE.equals(parceiro.getAtivo())) {
            throw new RegraNegocioException("Parceiro " + parceiro.getNome() + " está inativo");
        }

        Transacao transacao = new Transacao();
        transacao.setUsuario(usuario);
        transacao.setParceiro(parceiro);
        transacao.setValor(request.valor());

        return transacaoRepository.save(transacao);
    }

    public List<Transacao> historico(Long usuarioId) {
        return transacaoRepository.findByUsuarioIdOrderByDataDesc(usuarioId);
    }

    public CashbackResponse processarCashback() {
        ProcedureRepository.ResultadoCashback resultado = procedures.processarCashback(LIMITE_LOTE);
        return new CashbackResponse(
                resultado.processadas(),
                resultado.comErro(),
                resultado.totalCreditado()
        );
    }
}
