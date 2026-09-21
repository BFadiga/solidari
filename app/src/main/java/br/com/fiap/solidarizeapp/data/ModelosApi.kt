package br.com.fiap.solidarizeapp.data

data class LoginRequest(
    val email: String,
    val senha: String
)

data class RegistroRequest(
    val nome: String,
    val email: String,
    val senha: String
)

data class AuthResponse(
    val token: String,
    val tipo: String,
    val usuario: UsuarioResponse
)

data class UsuarioResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val saldo: Double,
    val papel: String
)

data class ParceiroResponse(
    val id: Long,
    val nome: String,
    val descricao: String,
    val categoria: String,
    val badge: String,
    val percentualCashback: Double,
    val imagemUrl: String?,
    val destaque: Boolean,
    val latitude: Double?,
    val longitude: Double?
)

data class ImpactoResponse(
    val arvoresPlantadas: Long,
    val refeicoesServidas: Long,
    val totalDoado: Double
)

data class ExtratoResponse(
    val extrato: String,
    val posicaoRanking: Int
)

data class DoacaoRequest(
    val valor: Double,
    val categoriaImpacto: String,
    val parceiroId: Long?
)

data class DoacaoResponse(
    val id: Long,
    val valor: Double,
    val categoriaImpacto: String,
    val data: String,
    val parceiroNome: String?
)

data class TransacaoRequest(
    val parceiroId: Long,
    val valor: Double
)

data class TransacaoResponse(
    val id: Long,
    val parceiroNome: String,
    val valor: Double,
    val cashbackGerado: Double?,
    val status: String,
    val data: String
)

data class ErroResponse(
    val status: Int?,
    val mensagem: String?
)
