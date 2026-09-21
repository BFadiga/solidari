package br.com.fiap.solidarizeapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CarteiraViewModel : ViewModel() {

    var perfil by mutableStateOf<UsuarioResponse?>(null)
        private set

    var impacto by mutableStateOf<ImpactoResponse?>(null)
        private set

    var parceiros by mutableStateOf<List<ParceiroResponse>>(emptyList())
        private set

    var doacoes by mutableStateOf<List<DoacaoResponse>>(emptyList())
        private set

    var extrato by mutableStateOf<ExtratoResponse?>(null)
        private set

    var transacoes by mutableStateOf<List<TransacaoResponse>>(emptyList())
        private set

    var carregando by mutableStateOf(false)
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    var aviso by mutableStateOf<String?>(null)
        private set

    fun carregar() {
        carregando = true
        erro = null

        viewModelScope.launch {
            runCatching {
                parceiros = ApiClient.api.parceiros()
                if (Sessao.autenticado) {
                    perfil = ApiClient.api.perfil()
                    impacto = ApiClient.api.impacto()
                    doacoes = ApiClient.api.doacoes()
                    extrato = ApiClient.api.extrato(10)
                    transacoes = ApiClient.api.transacoes()
                }
            }.onFailure { falha ->
                erro = mensagemDoErro(falha, "Não foi possível carregar seus dados.")
            }
            carregando = false
        }
    }

    fun doar(valor: Double, categoria: String, parceiroId: Long?) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.doar(DoacaoRequest(valor, categoria, parceiroId))
            }.onSuccess {
                aviso = "Doação confirmada. Obrigado por contribuir!"
                carregar()
            }.onFailure { falha ->
                aviso = mensagemDoErro(falha, "Não foi possível concluir a doação.")
            }
        }
    }

    fun registrarCompra(parceiroId: Long, valor: Double) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.registrarCompra(TransacaoRequest(parceiroId, valor))
            }.onSuccess {
                aviso = "Compra registrada. O cashback entra assim que for apurado."
                carregar()
            }.onFailure { falha ->
                aviso = mensagemDoErro(falha, "Não foi possível registrar a compra.")
            }
        }
    }

    fun limparAviso() {
        aviso = null
    }
}
