package br.com.fiap.solidarizeapp.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SolidariApi {

    @POST("api/auth/login")
    suspend fun login(@Body corpo: LoginRequest): AuthResponse

    @POST("api/auth/registrar")
    suspend fun registrar(@Body corpo: RegistroRequest): AuthResponse

    @GET("api/usuarios/me")
    suspend fun perfil(): UsuarioResponse

    @GET("api/parceiros")
    suspend fun parceiros(@Query("categoria") categoria: String? = null): List<ParceiroResponse>

    @GET("api/doacoes/impacto")
    suspend fun impacto(): ImpactoResponse

    @GET("api/doacoes/extrato")
    suspend fun extrato(@Query("limite") limite: Int = 10): ExtratoResponse

    @GET("api/doacoes/me")
    suspend fun doacoes(): List<DoacaoResponse>

    @POST("api/doacoes")
    suspend fun doar(@Body corpo: DoacaoRequest): DoacaoResponse

    @GET("api/transacoes/me")
    suspend fun transacoes(): List<TransacaoResponse>

    @POST("api/transacoes")
    suspend fun registrarCompra(@Body corpo: TransacaoRequest): TransacaoResponse
}
