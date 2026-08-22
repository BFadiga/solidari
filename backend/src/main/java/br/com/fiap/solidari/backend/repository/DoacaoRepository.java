package br.com.fiap.solidari.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.solidari.backend.model.CategoriaImpacto;
import br.com.fiap.solidari.backend.model.Doacao;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    List<Doacao> findByUsuarioIdOrderByDataDesc(Long usuarioId);

    @Query("select coalesce(sum(d.valor), 0) from Doacao d "
            + "where d.usuario.id = :usuarioId and d.categoriaImpacto = :categoria")
    double somarValorPorCategoria(@Param("usuarioId") Long usuarioId, @Param("categoria") CategoriaImpacto categoria);
}
