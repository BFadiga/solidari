package br.com.fiap.solidari.backend.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.solidari.backend.dto.FiltroTransacoes;
import br.com.fiap.solidari.backend.dto.ImpactoResponse;
import br.com.fiap.solidari.backend.dto.ResumoTransacoesResponse;
import br.com.fiap.solidari.backend.dto.TransacaoRelatorioResponse;
import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.exception.RegraNegocioException;
import br.com.fiap.solidari.backend.model.CategoriaImpacto;

/**
 * Testes de integracao dos packages, triggers e Dynamic SQL contra um Oracle real.
 * Cada teste roda em transacao desfeita ao final (@Transactional).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RotinasPlsqlTest {

    @Autowired
    private ProcedureRepository procedures;

    @Autowired
    private FunctionRepository functions;

    @Autowired
    private RelatorioRepository relatorios;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long usuarioId;
    private Long parceiroId;

    @BeforeEach
    void criarCenario() {
        jdbcTemplate.update("""
                INSERT INTO usuarios (nome, email, senha, saldo, papel)
                VALUES ('Teste PLSQL', 'teste.plsql@solidari.com', 'hash-irrelevante', 100.00, 'USUARIO')
                """);
        usuarioId = jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios WHERE email = 'teste.plsql@solidari.com'", Long.class);

        jdbcTemplate.update("""
                INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback)
                VALUES ('Parceiro Teste', 'desc', 'Teste', '10% CASHBACK', 10.00)
                """);
        parceiroId = jdbcTemplate.queryForObject(
                "SELECT id FROM parceiros WHERE nome = 'Parceiro Teste'", Long.class);
    }

    // -------------------------------------------------------------------------
    // pkg_carteira
    // -------------------------------------------------------------------------

    @Test
    void registrarDoacaoDebitaSaldoEGravaDoacao() {
        Long doacaoId = procedures.registrarDoacao(
                usuarioId, parceiroId, new BigDecimal("40.00"), CategoriaImpacto.RESTAURACAO_AMBIENTAL);

        assertThat(doacaoId).isNotNull();
        assertThat(saldoDoUsuario()).isEqualByComparingTo("60.00");
    }

    @Test
    void registrarDoacaoRecusaValorAcimaDoSaldo() {
        assertThatThrownBy(() -> procedures.registrarDoacao(
                usuarioId, parceiroId, new BigDecimal("500.00"), CategoriaImpacto.RESTAURACAO_AMBIENTAL))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageStartingWith("Saldo insuficiente")
                .hasMessageContaining("R$ 100,00");
    }

    @Test
    void registrarDoacaoRecusaUsuarioInexistente() {
        assertThatThrownBy(() -> procedures.registrarDoacao(
                -1L, null, new BigDecimal("10.00"), CategoriaImpacto.BEM_ESTAR_COMUNITARIO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void impactoConverteDoacoesEmArvoresERefeicoes() {
        procedures.registrarDoacao(usuarioId, null, new BigDecimal("45.00"), CategoriaImpacto.RESTAURACAO_AMBIENTAL);
        procedures.registrarDoacao(usuarioId, null, new BigDecimal("30.00"), CategoriaImpacto.BEM_ESTAR_COMUNITARIO);

        ImpactoResponse impacto = functions.impactoDoUsuario(usuarioId);

        assertThat(impacto.arvoresPlantadas()).isEqualTo(4);
        assertThat(impacto.refeicoesServidas()).isEqualTo(6);
        assertThat(impacto.totalDoado()).isEqualByComparingTo("75.00");
    }

    @Test
    void extratoFormatadoUsaPadraoBrasileiro() {
        procedures.registrarDoacao(usuarioId, parceiroId, new BigDecimal("12.50"), CategoriaImpacto.RESTAURACAO_AMBIENTAL);

        String extrato = functions.extratoFormatado(usuarioId, 10);

        assertThat(extrato)
                .contains("Teste PLSQL")
                .contains("R$ 12,50")
                .contains("Parceiro Teste");
    }

    @Test
    void extratoDeUsuarioSemDoacaoNaoQuebra() {
        assertThat(functions.extratoFormatado(usuarioId, 10))
                .contains("Nenhuma doação registrada");
    }

    @Test
    void moedaBrFormataIndependenteDoLocale() {
        assertThat(functions.moedaBr(new BigDecimal("1234567.89"))).isEqualTo("1.234.567,89");
        assertThat(functions.moedaBr(new BigDecimal("0.50"))).isEqualTo("0,50");
    }

    // -------------------------------------------------------------------------
    // pkg_cashback + triggers
    // -------------------------------------------------------------------------

    @Test
    void processarCashbackCreditaTransacaoPendente() {
        jdbcTemplate.update("""
                INSERT INTO transacoes (usuario_id, parceiro_id, valor, status)
                VALUES (?, ?, 200.00, 'PENDENTE')
                """, usuarioId, parceiroId);

        procedures.processarCashback(500);

        assertThat(saldoDoUsuario()).isEqualByComparingTo("120.00");

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM transacoes WHERE usuario_id = ?", String.class, usuarioId);
        assertThat(status).isEqualTo("PROCESSADA");

        // trg_transacao_status carimba a data de processamento.
        Integer comData = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transacoes WHERE usuario_id = ? AND processada_em IS NOT NULL",
                Integer.class, usuarioId);
        assertThat(comData).isEqualTo(1);
    }

    @Test
    void parceiroInativoMarcaErroSemCreditarEAbreAlerta() {
        jdbcTemplate.update("UPDATE parceiros SET ativo = 0 WHERE id = ?", parceiroId);
        jdbcTemplate.update("""
                INSERT INTO transacoes (usuario_id, parceiro_id, valor, status)
                VALUES (?, ?, 80.00, 'PENDENTE')
                """, usuarioId, parceiroId);

        procedures.processarCashback(500);

        assertThat(saldoDoUsuario()).isEqualByComparingTo("100.00");

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM transacoes WHERE usuario_id = ?", String.class, usuarioId);
        assertThat(status).isEqualTo("ERRO");

        // trg_alerta_transacao_erro abre o alerta na hora.
        Integer alertas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM alertas
                WHERE tipo = 'TRANSACAO_COM_ERRO' AND usuario_id = ? AND severidade = 'CRITICO'
                """, Integer.class, usuarioId);
        assertThat(alertas).isEqualTo(1);
    }

    @Test
    void auditoriaRegistraDebitoECredito() {
        procedures.registrarDoacao(usuarioId, null, new BigDecimal("40.00"), CategoriaImpacto.RESTAURACAO_AMBIENTAL);
        jdbcTemplate.update("UPDATE usuarios SET saldo = saldo + 15 WHERE id = ?", usuarioId);

        List<String> operacoes = jdbcTemplate.queryForList("""
                SELECT operacao || ':' || TO_CHAR(diferenca, 'FM990.00')
                FROM auditoria_saldo WHERE usuario_id = ? ORDER BY id
                """, String.class, usuarioId);

        assertThat(operacoes).containsExactly("DEBITO:-40.00", "CREDITO:15.00");
    }

    @Test
    void auditoriaIgnoraUpdateSemMudancaDeSaldo() {
        jdbcTemplate.update("UPDATE usuarios SET saldo = saldo WHERE id = ?", usuarioId);

        Integer registros = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auditoria_saldo WHERE usuario_id = ?", Integer.class, usuarioId);
        assertThat(registros).isZero();
    }

    // -------------------------------------------------------------------------
    // pkg_monitoramento
    // -------------------------------------------------------------------------

    @Test
    void gerarAlertasNaoDuplicaAlertaAberto() {
        procedures.gerarAlertas();
        int segundaExecucao = procedures.gerarAlertas();

        assertThat(segundaExecucao).isZero();
    }

    // -------------------------------------------------------------------------
    // pkg_relatorios (Dynamic SQL)
    // -------------------------------------------------------------------------

    @Test
    void buscaDinamicaAplicaSoOsFiltrosInformados() {
        jdbcTemplate.update("""
                INSERT INTO transacoes (usuario_id, parceiro_id, valor, status)
                VALUES (?, ?, 55.00, 'PENDENTE')
                """, usuarioId, parceiroId);
        jdbcTemplate.update("""
                INSERT INTO transacoes (usuario_id, parceiro_id, valor, status)
                VALUES (?, ?, 300.00, 'PENDENTE')
                """, usuarioId, parceiroId);

        List<TransacaoRelatorioResponse> todasDoParceiro = relatorios.buscarTransacoes(
                new FiltroTransacoes(null, parceiroId, null, null, null, null, "VALOR", 50));
        assertThat(todasDoParceiro)
                .extracting(t -> t.valor().setScale(2).toPlainString())
                .containsExactly("300.00", "55.00");

        List<TransacaoRelatorioResponse> acimaDe100 = relatorios.buscarTransacoes(
                new FiltroTransacoes("pendente", parceiroId, "teste", null, null, new BigDecimal("100"), null, null));
        assertThat(acimaDe100).hasSize(1);
        assertThat(acimaDe100.get(0).usuario()).isEqualTo("Teste PLSQL");
    }

    @Test
    void buscaDinamicaRecusaOrdenacaoForaDaLista() {
        assertThatThrownBy(() -> relatorios.buscarTransacoes(
                new FiltroTransacoes(null, null, null, null, null, null, "valor; DROP TABLE usuarios", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageStartingWith("Ordenação inválida");
    }

    @Test
    void sqlGeradoUsaBindVariablesENaoConcatenaValores() {
        String sql = jdbcTemplate.queryForObject(
                "SELECT pkg_relatorios.fn_sql_busca(p_status => 'ERRO', p_valor_minimo => 999) FROM dual",
                String.class);

        assertThat(sql)
                .contains("t.status = :status")
                .contains("t.valor >= :valor_minimo")
                .contains(":parceiro_id IS NULL")
                .doesNotContain("999")
                .doesNotContain("'ERRO'");
    }

    @Test
    void resumoAgrupaPelaDimensaoEscolhida() {
        List<ResumoTransacoesResponse> porStatus = relatorios.resumo("STATUS", null);

        assertThat(porStatus).extracting(ResumoTransacoesResponse::chave)
                .isNotEmpty()
                .isSubsetOf("PENDENTE", "PROCESSADA", "ERRO");

        assertThatThrownBy(() -> relatorios.resumo("SENHA", null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageStartingWith("Dimensão inválida");
    }

    private BigDecimal saldoDoUsuario() {
        return jdbcTemplate.queryForObject(
                "SELECT saldo FROM usuarios WHERE id = ?", BigDecimal.class, usuarioId);
    }
}
