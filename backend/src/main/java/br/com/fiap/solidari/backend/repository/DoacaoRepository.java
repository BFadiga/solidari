package br.com.fiap.solidari.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.solidari.backend.model.Doacao;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    @EntityGraph(attributePaths = "parceiro")
    List<Doacao> findByUsuarioIdOrderByDataDesc(Long usuarioId);

    @Override
    @EntityGraph(attributePaths = "parceiro")
    Optional<Doacao> findById(Long id);

}
