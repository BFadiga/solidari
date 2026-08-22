package br.com.fiap.solidari.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.fiap.solidari.backend.dto.ParceiroRequest;
import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.model.Parceiro;
import br.com.fiap.solidari.backend.repository.ParceiroRepository;

@Service
public class ParceiroService {

    private final ParceiroRepository parceiroRepository;

    public ParceiroService(ParceiroRepository parceiroRepository) {
        this.parceiroRepository = parceiroRepository;
    }

    public List<Parceiro> listar(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return parceiroRepository.findAll();
        }
        return parceiroRepository.findByCategoriaIgnoreCase(categoria);
    }

    public Parceiro buscarPorId(Long id) {
        return parceiroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parceiro não encontrado: id " + id));
    }

    public Parceiro criar(ParceiroRequest request) {
        Parceiro parceiro = new Parceiro();
        preencher(parceiro, request);
        return parceiroRepository.save(parceiro);
    }

    public Parceiro atualizar(Long id, ParceiroRequest request) {
        Parceiro parceiro = buscarPorId(id);
        preencher(parceiro, request);
        return parceiroRepository.save(parceiro);
    }

    public void remover(Long id) {
        Parceiro parceiro = buscarPorId(id);
        parceiroRepository.delete(parceiro);
    }

    private void preencher(Parceiro parceiro, ParceiroRequest request) {
        parceiro.setNome(request.nome());
        parceiro.setDescricao(request.descricao());
        parceiro.setCategoria(request.categoria());
        parceiro.setBadge(request.badge());
        parceiro.setImagemUrl(request.imagemUrl());
        parceiro.setDestaque(request.destaque());
        parceiro.setLatitude(request.latitude());
        parceiro.setLongitude(request.longitude());
    }
}
