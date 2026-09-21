package br.com.fiap.solidari.backend.repository.jdbc;

import java.math.BigDecimal;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.fiap.solidari.backend.dto.ImpactoResponse;

@Repository
public class FunctionRepository {

    private final JdbcTemplate jdbcTemplate;

    public FunctionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ImpactoResponse impactoDoUsuario(Long usuarioId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT arvores_plantadas, refeicoes_servidas, total_doado FROM fn_impacto_usuario(?)",
                    (rs, linha) -> new ImpactoResponse(
                            rs.getLong("arvores_plantadas"),
                            rs.getLong("refeicoes_servidas"),
                            rs.getBigDecimal("total_doado")
                    ),
                    usuarioId
            );
        } catch (DataAccessException ex) {
            throw ErroPlpgsql.traduzir(ex);
        }
    }

    public String extratoFormatado(Long usuarioId, int limite) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT fn_extrato_formatado(?, ?)", String.class, usuarioId, limite);
        } catch (DataAccessException ex) {
            throw ErroPlpgsql.traduzir(ex);
        }
    }

    public int rankingDoador(Long usuarioId) {
        try {
            Integer posicao = jdbcTemplate.queryForObject(
                    "SELECT fn_ranking_doador(?)", Integer.class, usuarioId);
            return posicao == null ? 0 : posicao;
        } catch (DataAccessException ex) {
            throw ErroPlpgsql.traduzir(ex);
        }
    }

    public String moedaBr(BigDecimal valor) {
        try {
            return jdbcTemplate.queryForObject("SELECT fn_moeda_br(?)", String.class, valor);
        } catch (DataAccessException ex) {
            throw ErroPlpgsql.traduzir(ex);
        }
    }
}
