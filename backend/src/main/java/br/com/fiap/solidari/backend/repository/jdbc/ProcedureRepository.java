package br.com.fiap.solidari.backend.repository.jdbc;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Types;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.fiap.solidari.backend.model.CategoriaImpacto;

/**
 * Operacoes que alteram dados, executadas pelas procedures dos packages
 * {@code pkg_carteira}, {@code pkg_cashback} e {@code pkg_monitoramento}.
 *
 * <p>As procedures nao fazem COMMIT: a transacao e a do Spring, que envolve a
 * chamada. Por isso a mesma conexao do {@link JdbcTemplate} e usada.
 */
@Repository
public class ProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProcedureRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long registrarDoacao(Long usuarioId, Long parceiroId, BigDecimal valor, CategoriaImpacto categoria) {
        try {
            return jdbcTemplate.execute((java.sql.Connection conexao) -> {
                try (CallableStatement cs = conexao.prepareCall(
                        "{call pkg_carteira.sp_registrar_doacao(?, ?, ?, ?, ?)}")) {
                    cs.setLong(1, usuarioId);
                    if (parceiroId == null) {
                        cs.setNull(2, Types.NUMERIC);
                    } else {
                        cs.setLong(2, parceiroId);
                    }
                    cs.setBigDecimal(3, valor);
                    cs.setString(4, categoria.name());
                    cs.registerOutParameter(5, Types.NUMERIC);
                    cs.execute();
                    return cs.getLong(5);
                }
            });
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    public ResultadoCashback processarCashback(int limite) {
        try {
            return jdbcTemplate.execute((java.sql.Connection conexao) -> {
                try (CallableStatement cs = conexao.prepareCall(
                        "{call pkg_cashback.sp_processar_cashback(?, ?, ?, ?)}")) {
                    cs.setInt(1, limite);
                    cs.registerOutParameter(2, Types.INTEGER);
                    cs.registerOutParameter(3, Types.INTEGER);
                    cs.registerOutParameter(4, Types.NUMERIC);
                    cs.execute();

                    BigDecimal creditado = cs.getBigDecimal(4);
                    return new ResultadoCashback(
                            cs.getInt(2),
                            cs.getInt(3),
                            creditado == null ? BigDecimal.ZERO : creditado
                    );
                }
            });
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    public int gerarAlertas() {
        try {
            return jdbcTemplate.execute((java.sql.Connection conexao) -> {
                try (CallableStatement cs = conexao.prepareCall(
                        "{call pkg_monitoramento.sp_gerar_alertas(?)}")) {
                    cs.registerOutParameter(1, Types.INTEGER);
                    cs.execute();
                    return cs.getInt(1);
                }
            });
        } catch (DataAccessException ex) {
            throw ErroPlsql.traduzir(ex);
        }
    }

    public record ResultadoCashback(int processadas, int comErro, BigDecimal totalCreditado) {
    }
}
