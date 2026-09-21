package br.com.fiap.solidari.backend.repository.jdbc;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

import br.com.fiap.solidari.backend.exception.RecursoNaoEncontradoException;
import br.com.fiap.solidari.backend.exception.RegraNegocioException;

final class ErroPlpgsql {

    private static final String NAO_ENCONTRADO = "SL002";

    private ErroPlpgsql() {
    }

    static RuntimeException traduzir(DataAccessException ex) {
        SQLException sql = procurarSqlException(ex);

        if (sql == null || sql.getSQLState() == null || !sql.getSQLState().startsWith("SL")) {
            return ex;
        }

        String mensagem = primeiraLinha(sql.getMessage());

        if (NAO_ENCONTRADO.equals(sql.getSQLState())) {
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

    private static String primeiraLinha(String mensagem) {
        if (mensagem == null) {
            return "Erro ao executar rotina do banco";
        }
        int quebra = mensagem.indexOf('\n');
        String linha = quebra < 0 ? mensagem.trim() : mensagem.substring(0, quebra).trim();

        return linha.startsWith("ERROR: ") ? linha.substring("ERROR: ".length()) : linha;
    }
}
