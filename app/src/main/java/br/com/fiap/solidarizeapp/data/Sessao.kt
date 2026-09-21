package br.com.fiap.solidarizeapp.data

import android.content.Context
import android.content.SharedPreferences

object Sessao {

    private const val ARQUIVO = "solidari.sessao"
    private const val CHAVE_TOKEN = "token"
    private const val CHAVE_NOME = "nome"

    private var preferencias: SharedPreferences? = null

    fun iniciar(contexto: Context) {
        if (preferencias == null) {
            preferencias = contexto.applicationContext
                .getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)
        }
    }

    var token: String?
        get() = preferencias?.getString(CHAVE_TOKEN, null)
        set(valor) {
            preferencias?.edit()?.putString(CHAVE_TOKEN, valor)?.apply()
        }

    var nome: String?
        get() = preferencias?.getString(CHAVE_NOME, null)
        set(valor) {
            preferencias?.edit()?.putString(CHAVE_NOME, valor)?.apply()
        }

    val autenticado: Boolean
        get() = !token.isNullOrBlank()

    fun encerrar() {
        preferencias?.edit()?.clear()?.apply()
    }
}
