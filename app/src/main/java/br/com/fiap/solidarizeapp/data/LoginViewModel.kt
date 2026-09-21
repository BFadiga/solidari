package br.com.fiap.solidarizeapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    fun entrar(email: String, senha: String, aoConcluir: () -> Unit) {
        if (email.isBlank() || senha.isBlank()) {
            erro = "Informe e-mail e senha."
            return
        }

        carregando = true
        erro = null

        viewModelScope.launch {
            runCatching {
                ApiClient.api.login(LoginRequest(email.trim(), senha))
            }.onSuccess { resposta ->
                Sessao.token = resposta.token
                Sessao.nome = resposta.usuario.nome
                carregando = false
                aoConcluir()
            }.onFailure { falha ->
                carregando = false
                erro = mensagemDoErro(falha, "Não foi possível entrar agora.")
            }
        }
    }

    fun limparErro() {
        erro = null
    }
}
