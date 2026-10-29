package br.com.fiap.solidari.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.solidari.backend.dto.FiltroTransacoes;
import br.com.fiap.solidari.backend.dto.ResumoTransacoesResponse;
import br.com.fiap.solidari.backend.dto.TransacaoRelatorioResponse;
import br.com.fiap.solidari.backend.repository.jdbc.RelatorioRepository;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional(readOnly = true)
    public List<TransacaoRelatorioResponse> buscarTransacoes(FiltroTransacoes filtro) {
        return relatorioRepository.buscarTransacoes(filtro);
    }

    @Transactional(readOnly = true)
    public List<ResumoTransacoesResponse> resumo(String dimensao, String status) {
        return relatorioRepository.resumo(dimensao, status);
    }
}
