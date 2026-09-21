package br.com.fiap.solidari.backend.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.solidari.backend.dto.ImpactoResponse;
import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.exception.RegraNegocioException;
import br.com.fiap.solidari.backend.model.CategoriaImpacto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RotinasPlpgsqlTest {

    @Autowired
    private ProcedureRepository procedures;

    @Autowired
    private FunctionRepository functions;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long usuarioId;
    private Long parceiroId;

    @BeforeEach
    void criarCenario() {
        usuarioId = jdbcTemplate.queryForObject("""
                INSERT INTO usuarios (nome, email, senha, saldo, papel)
                VALUES ('Teste PLpgSQL', 'teste.plpgsql@solidari.com', 'hash-irrelevante', 100.00, 'USUARIO')
                RETURNING id
                """, Long.class);

        parceiroId = jdbcTemplate.queryForObject("""
                INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback)
                VALUES ('Parceiro Teste', 'desc', 'Teste', '10% CASHBACK', 10.00)
                RETURNING id
                """, Long.class);
    }

    @Test
    void registrarDoacaoDebitaSaldoEGravaDoacao() {
        Long doacaoId = procedures.registrarDoacao(
                usuarioId, parceiroId, new BigDecimal("40.00"), CategoriaImpacto.RESTAURACAO_AMBIENTAL);

        assertThat(doacaoId).isNotNull();

        BigDecimal saldo = jdbcTemplate.queryForObject(
                "SELECT saldo FROM usuarios WHERE id = ?", BigDecimal.class, usuarioId);
        assertThat(saldo).isEqualByComparingTo("60.00");
    }

    @Test
    void registrarDoacaoRecusaValorAcimaDoSaldo() {
        assertThatThrownBy(() -> procedures.registrarDoacao(
                usuarioId, parceiroId, new BigDecimal("500.00"), CategoriaImpacto.RESTAURACAO_AMBIENTAL))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");

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
    void processarCashbackCreditaTransacaoPendente() {
        jdbcTemplate.update("""
                INSERT INTO transacoes (usuario_id, parceiro_id, valor, status)
                VALUES (?, ?, 200.00, 'PENDENTE')
                """, usuarioId, parceiroId);

        procedures.processarCashback(500);

        BigDecimal saldo = jdbcTemplate.queryForObject(
                "SELECT saldo FROM usuarios WHERE id = ?", BigDecimal.class, usuarioId);
        assertThat(saldo).isEqualByComparingTo("120.00");

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM transacoes WHERE usuario_id = ?", String.class, usuarioId);
        assertThat(status).isEqualTo("PROCESSADA");
    }

    @Test
    void extratoFormatadoUsaPadraoBrasileiro() {
        procedures.registrarDoacao(usuarioId, parceiroId, new BigDecimal("12.50"), CategoriaImpacto.RESTAURACAO_AMBIENTAL);

        String extrato = functions.extratoFormatado(usuarioId, 10);

        assertThat(extrato)
                .contains("Teste PLpgSQL")
                .contains("R$ 12,50")
                .contains("Parceiro Teste");
    }

    @Test
    void extratoDeUsuarioSemDoacaoNaoQuebra() {
        assertThat(functions.extratoFormatado(usuarioId, 10))
                .contains("Nenhuma doacao registrada");
    }

    @Test
    void moedaBrFormataIndependenteDoLocale() {
        assertThat(functions.moedaBr(new BigDecimal("1234567.89"))).isEqualTo("1.234.567,89");
        assertThat(functions.moedaBr(new BigDecimal("0.50"))).isEqualTo("0,50");
    }
}
