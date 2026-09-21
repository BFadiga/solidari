package br.com.fiap.solidarizeapp.data

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // 10.0.2.2 é como o emulador enxerga o localhost da máquina que o hospeda.
    // Em aparelho físico, trocar pelo IP da máquina na rede local.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val autenticacao = okhttp3.Interceptor { cadeia ->
        val token = Sessao.token
        val requisicao = if (token.isNullOrBlank()) {
            cadeia.request()
        } else {
            cadeia.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        cadeia.proceed(requisicao)
    }

    private val cliente = OkHttpClient.Builder()
        .addInterceptor(autenticacao)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: SolidariApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SolidariApi::class.java)
}

fun mensagemDoErro(erro: Throwable, padrao: String): String {
    if (erro is HttpException) {
        if (erro.code() == 401) {
            return "E-mail ou senha incorretos."
        }
        val corpo = erro.response()?.errorBody()?.string()
        if (!corpo.isNullOrBlank()) {
            val mensagem = runCatching {
                Gson().fromJson(corpo, ErroResponse::class.java)?.mensagem
            }.getOrNull()
            if (!mensagem.isNullOrBlank()) {
                return mensagem
            }
        }
        return "Não foi possível concluir (erro ${erro.code()})."
    }
    return "Sem conexão com o servidor. Verifique se a API está no ar."
}
