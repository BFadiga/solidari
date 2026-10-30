package br.com.fiap.solidari.backend.repository.jdbc;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.fiap.solidari.backend.dto.FiltroTransacoes;
import br.com.fiap.solidari.backend.dto.ResumoTransacoesResponse;
import br.com.fiap.solidari.backend.dto.TransacaoRelatorioResponse;

/**
 * Consultas do painel que usam Dynamic SQL no banco ({@code pkg_relatorios}).
 *
 * <p>As functions devolvem um {@code SYS_REFCURSOR}; aqui ele e registrado como
 * {@link Types#REF_CURSOR} e lido como um {@link ResultSet} comum.
 */
@Repository
public class RelatorioRepository {

    private final JdbcTemplate jdbcTemplate;

    public RelatorioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TransacaoRelatorioResponse> buscarTransacoes(FiltroTransacoes filtro) {
        try {
            return jdbcTemplate.execute((java.sql.Connection conexao) -> {
                try (CallableStatement cs = conexao.prepareCall(
                        "{? = call pkg_relatorios.fn_buscar_transacoes(?, ?, ?, ?, ?, ?, ?, ?)}")) {
                    cs.registerOutParameter(1, Types.REF_CURSOR);
                    texto(cs, 2, filtro.status());
                    numero(cs, 3, filtro.parceiroId() == null ? null : BigDecimal.valueOf(filtro.parceiroId()));
                    texto(cs, 4, filtro.categoria());
                    data(cs, 5, filtro.dataInicio());
                    data(cs, 6, filtro.dataFim());
                    numero(cs, 7, filtro.valorMinimo());
                    texto(cs, 8, filtro.ordenarPor() == null ? "DATA" : filtro.ordenarPor());
                    cs.setInt(9, filtro.limite() == null ? 100 : filtro.limite());
                    cs.execute();

                    List<TransacaoRelatorioResponse> linhas = new ArrayList<>();
                    try (ResultSet rs = cs.getObject(1, ResultSet.class)) {
                        while (rs.next()) {
                            Timestamp data = rs.getTimestamp("data");
                            linhas.add(new TransacaoRelatorioResponse(
                                    rs.getLong("id"),
                                    rs.getString("usuario"),
                                    rs.getString("parceiro"),
                                    rs.getString("categoria"),
                                    rs.getBigDecimal("valor"),
                                    rs.getBigDecimal("cashback_gerado"),
                                    rs.getString("status"),
                                    data == null ? null : data.toLocalDateTime()
                            ));
                        }
                    }
                    return linhas;
                }
            });
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    public List<ResumoTransacoesResponse> resumo(String dimensao, String status) {
        try {
            return jdbcTemplate.execute((java.sql.Connection conexao) -> {
                try (CallableStatement cs = conexao.prepareCall(
                        "{? = call pkg_relatorios.fn_resumo_transacoes(?, ?)}")) {
                    cs.registerOutParameter(1, Types.REF_CURSOR);
                    texto(cs, 2, dimensao);
                    texto(cs, 3, status);
                    cs.execute();

                    List<ResumoTransacoesResponse> linhas = new ArrayList<>();
                    try (ResultSet rs = cs.getObject(1, ResultSet.class)) {
                        while (rs.next()) {
                            linhas.add(new ResumoTransacoesResponse(
                                    rs.getString("chave"),
                                    rs.getLong("quantidade"),
                                    rs.getBigDecimal("total_valor"),
                                    rs.getBigDecimal("total_cashback")
                            ));
                        }
                    }
                    return linhas;
                }
            });
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    private static void texto(CallableStatement cs, int indice, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            cs.setNull(indice, Types.VARCHAR);
        } else {
            cs.setString(indice, valor.trim());
        }
    }

    private static void numero(CallableStatement cs, int indice, BigDecimal valor) throws SQLException {
        if (valor == null) {
            cs.setNull(indice, Types.NUMERIC);
        } else {
            cs.setBigDecimal(indice, valor);
        }
    }

    private static void data(CallableStatement cs, int indice, LocalDate valor) throws SQLException {
        if (valor == null) {
            cs.setNull(indice, Types.DATE);
        } else {
            cs.setDate(indice, Date.valueOf(valor));
        }
    }
}
