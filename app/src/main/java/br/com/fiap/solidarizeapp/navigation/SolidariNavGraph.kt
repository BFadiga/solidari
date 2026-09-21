package br.com.fiap.solidarizeapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.data.CarteiraViewModel
import br.com.fiap.solidarizeapp.data.LoginViewModel
import br.com.fiap.solidarizeapp.data.Sessao
import br.com.fiap.solidarizeapp.screens.CashbackScreen
import br.com.fiap.solidarizeapp.screens.HomeScreen
import br.com.fiap.solidarizeapp.screens.LocationScreen
import br.com.fiap.solidarizeapp.screens.LoginScreen
import br.com.fiap.solidarizeapp.screens.OptionsScreen
import br.com.fiap.solidarizeapp.screens.ProfileScreen
import br.com.fiap.solidarizeapp.screens.Parceiro
import br.com.fiap.solidarizeapp.screens.Usuario

// Grafo de navegação do app. Antes cada tela era montada isoladamente
// (a MainActivity só abria a CashbackScreen direto), então os botões de
// navegação existiam visualmente mas não levavam a lugar nenhum.
@Composable
fun SolidariNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (Sessao.autenticado) {
            SolidariDestinations.HOME
        } else {
            SolidariDestinations.LOGIN
        }
    ) {
        composable(SolidariDestinations.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel()

            LoginScreen(
                carregando = loginViewModel.carregando,
                erro = loginViewModel.erro,
                onLoginClick = { email, senha ->
                    loginViewModel.entrar(email, senha) {
                        navController.navigate(SolidariDestinations.HOME) {
                            // Depois de logar não faz sentido voltar pra tela de login.
                            popUpTo(SolidariDestinations.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(SolidariDestinations.HOME) {
            HomeScreen(navController = navController)
        }

        composable(SolidariDestinations.LOCATION) {
            LocationScreen(navController = navController)
        }

        composable(SolidariDestinations.CASHBACK) {
            val carteira: CarteiraViewModel = viewModel()

            LaunchedEffect(Unit) {
                carteira.carregar()
            }

            val perfil = carteira.perfil
            val impacto = carteira.impacto

            CashbackScreen(
                navController = navController,
                usuario = Usuario(
                    nome = perfil?.nome?.substringBefore(' ') ?: (Sessao.nome ?: "Visitante"),
                    saldo = perfil?.saldo ?: 0.0
                ),
                parceiros = carteira.parceiros.map { p ->
                    Parceiro(
                        nome = p.nome,
                        descricao = p.descricao,
                        badge = p.badge,
                        imagemUrl = p.imagemUrl.orEmpty(),
                        destaque = p.destaque,
                        categoria = p.categoria,
                        percentualCashback = p.percentualCashback
                    )
                },
                categorias = carteira.parceiros.map { it.categoria }.distinct(),
                arvoresPlantadas = impacto?.arvoresPlantadas?.toInt() ?: 0,
                refeicoesServidas = impacto?.refeicoesServidas?.toInt() ?: 0,
                aviso = carteira.aviso,
                onAvisoExibido = { carteira.limparAviso() },
                onDoar = { valor, categoria -> carteira.doar(valor, categoria, null) },
                onHistoricoClick = { navController.navigate(SolidariDestinations.PROFILE) }
            )
        }

        composable(SolidariDestinations.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(SolidariDestinations.OPTIONS) {
            OptionsScreen(navController = navController)
        }
    }
}
