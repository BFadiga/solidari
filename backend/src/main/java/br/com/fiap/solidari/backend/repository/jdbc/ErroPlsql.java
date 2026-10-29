package br.com.fiap.solidari.backend.repository.jdbc;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.exception.RegraNegocioException;

/**
 * Traduz os erros de negocio levantados pelos packages PL/SQL em excecoes da API.
 *
 * <p>Os packages usam {@code RAISE_APPLICATION_ERROR} com codigos da faixa
 * -20000..-20999, declarados uma unica vez em {@code pkg_util}. O driver Oracle
 * devolve esse numero (sem o sinal) em {@link SQLException#getErrorCode()}, entao
 * o Java distingue regra de negocio de falha tecnica sem depender do texto.
 */
final class ErroPlsql {

    /** pkg_util.c_erro_nao_encontrado */
    private static final int NAO_ENCONTRADO = 20002;

    private static final int FAIXA_INICIO = 20000;
    private static final int FAIXA_FIM = 20999;

    private ErroPlsql() {
    }

    static RuntimeException traduzir(DataAccessException ex) {
        SQLException sql = procurarSqlException(ex);

        if (sql == null || sql.getErrorCode() < FAIXA_INICIO || sql.getErrorCode() > FAIXA_FIM) {
            return ex;
        }

        String mensagem = primeiraLinha(sql.getMessage());

        if (sql.getErrorCode() == NAO_ENCONTRADO) {
            return new RecursoNaoEncontradoException(mensagem);
        }
        return new RegraNegocioException(mensagem);
    }

    private static SQLException procurarSqlException(Throwable ex) {
        for (Throwable atual = ex; atual != null; atual = atual.getCause()) {
            if (atual instanceof SQLException sql) {
                return sql;
            }
        }
        return null;
    }

    /**
     * O Oracle devolve "ORA-20001: mensagem" seguido da pilha de chamadas
     * ("ORA-06512: at ..."). Para a API interessa so a mensagem da primeira linha.
     */
    static String primeiraLinha(String mensagem) {
        if (mensagem == null) {
            return "Erro ao executar rotina do banco";
        }
        int quebra = mensagem.indexOf('\n');
        String linha = quebra < 0 ? mensagem.trim() : mensagem.substring(0, quebra).trim();

        return linha.replaceFirst("^ORA-\\d{5}:\\s*", "");
    }
}
