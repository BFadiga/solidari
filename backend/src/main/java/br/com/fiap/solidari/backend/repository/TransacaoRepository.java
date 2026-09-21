package br.com.fiap.solidari.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.solidari.backend.model.StatusTransacao;
import br.com.fiap.solidari.backend.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @EntityGraph(attributePaths = "parceiro")
    List<Transacao> findByUsuarioIdOrderByDataDesc(Long usuarioId);

    long countByStatus(StatusTransacao status);
}
