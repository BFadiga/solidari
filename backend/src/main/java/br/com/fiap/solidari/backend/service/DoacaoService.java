package br.com.fiap.solidari.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.solidari.backend.dto.DoacaoRequest;
import br.com.fiap.solidari.backend.dto.ImpactoResponse;
import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.exception.RegraNegocioException;
import br.com.fiap.solidari.backend.model.CategoriaImpacto;
import br.com.fiap.solidari.backend.model.Doacao;
import br.com.fiap.solidari.backend.model.Parceiro;
import br.com.fiap.solidari.backend.model.Usuario;
import br.com.fiap.solidari.backend.repository.DoacaoRepository;
import br.com.fiap.solidari.backend.repository.ParceiroRepository;
import br.com.fiap.solidari.backend.repository.UsuarioRepository;

@Service
public class DoacaoService {

    private static final double VALOR_POR_ARVORE = 10.0;
    private static final double VALOR_POR_REFEICAO = 5.0;

    private final DoacaoRepository doacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParceiroRepository parceiroRepository;

    public DoacaoService(
            DoacaoRepository doacaoRepository,
            UsuarioRepository usuarioRepository,
            ParceiroRepository parceiroRepository
    ) {
        this.doacaoRepository = doacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.parceiroRepository = parceiroRepository;
    }

    @Transactional
    public Doacao doar(Usuario usuario, DoacaoRequest request) {
        if (usuario.getSaldo() < request.valor()) {
            throw new RegraNegocioException("Saldo insuficiente para esta doação");
        }

        Parceiro parceiro = null;
        if (request.parceiroId() != null) {
            parceiro = parceiroRepository.findById(request.parceiroId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Parceiro não encontrado: id " + request.parceiroId()));
        }

        usuario.setSaldo(usuario.getSaldo() - request.valor());
        usuarioRepository.save(usuario);

        Doacao doacao = new Doacao();
        doacao.setUsuario(usuario);
        doacao.setParceiro(parceiro);
        doacao.setValor(request.valor());
        doacao.setCategoriaImpacto(request.categoriaImpacto());

        return doacaoRepository.save(doacao);
    }

    public List<Doacao> historico(Long usuarioId) {
        return doacaoRepository.findByUsuarioIdOrderByDataDesc(usuarioId);
    }

    public ImpactoResponse calcularImpacto(Long usuarioId) {
        double totalAmbiental = doacaoRepository.somarValorPorCategoria(
                usuarioId, CategoriaImpacto.RESTAURACAO_AMBIENTAL);
        double totalComunitario = doacaoRepository.somarValorPorCategoria(
                usuarioId, CategoriaImpacto.BEM_ESTAR_COMUNITARIO);

        long arvoresPlantadas = (long) (totalAmbiental / VALOR_POR_ARVORE);
        long refeicoesServidas = (long) (totalComunitario / VALOR_POR_REFEICAO);

        return new ImpactoResponse(arvoresPlantadas, refeicoesServidas, totalAmbiental + totalComunitario);
    }
}
