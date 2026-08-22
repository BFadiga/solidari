package br.com.fiap.solidarizeapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.screens.CashbackScreen
import br.com.fiap.solidarizeapp.screens.HomeScreen
import br.com.fiap.solidarizeapp.screens.LocationScreen
import br.com.fiap.solidarizeapp.screens.LoginScreen
import br.com.fiap.solidarizeapp.screens.OptionsScreen
import br.com.fiap.solidarizeapp.screens.ProfileScreen

// Grafo de navegação do app. Antes cada tela era montada isoladamente
// (a MainActivity só abria a CashbackScreen direto), então os botões de
// navegação existiam visualmente mas não levavam a lugar nenhum.
@Composable
fun SolidariNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SolidariDestinations.LOGIN
    ) {
        composable(SolidariDestinations.LOGIN) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(SolidariDestinations.HOME) {
                        // Depois de logar não faz sentido voltar pra tela de login com o botão "voltar".
                        popUpTo(SolidariDestinations.LOGIN) { inclusive = true }
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
            CashbackScreen(navController = navController)
        }

        composable(SolidariDestinations.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(SolidariDestinations.OPTIONS) {
            OptionsScreen(navController = navController)
        }
    }
}
