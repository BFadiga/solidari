package br.com.fiap.solidari.backend.repository.jdbc;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Types;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.fiap.solidari.backend.dto.ImpactoResponse;

/**
 * Leituras calculadas no banco: chama as functions e procedures de consulta
 * dos packages {@code pkg_carteira} e {@code pkg_util}.
 */
@Repository
public class FunctionRepository {

    private final JdbcTemplate jdbcTemplate;

    public FunctionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ImpactoResponse impactoDoUsuario(Long usuarioId) {
        try {
            return jdbcTemplate.execute((java.sql.Connection conexao) -> {
                try (CallableStatement cs = conexao.prepareCall(
                        "{call pkg_carteira.sp_impacto_usuario(?, ?, ?, ?)}")) {
                    cs.setLong(1, usuarioId);
                    cs.registerOutParameter(2, Types.NUMERIC);
                    cs.registerOutParameter(3, Types.NUMERIC);
                    cs.registerOutParameter(4, Types.NUMERIC);
                    cs.execute();

                    BigDecimal total = cs.getBigDecimal(4);
                    return new ImpactoResponse(
                            cs.getLong(2),
                            cs.getLong(3),
                            total == null ? BigDecimal.ZERO : total
                    );
                }
            });
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    public String extratoFormatado(Long usuarioId, int limite) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT pkg_carteira.fn_extrato_formatado(?, ?) FROM dual",
                    String.class, usuarioId, limite);
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    public int rankingDoador(Long usuarioId) {
        try {
            Integer posicao = jdbcTemplate.queryForObject(
                    "SELECT pkg_carteira.fn_ranking_doador(?) FROM dual", Integer.class, usuarioId);
            return posicao == null ? 0 : posicao;
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    public String moedaBr(BigDecimal valor) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT pkg_util.fn_moeda_br(?) FROM dual", String.class, valor);
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }
}
