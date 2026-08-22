package br.com.fiap.solidarizeapp.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination

private val NavGreenDark = Color(0xFF216B52)
private val NavGreenMint = Color(0xFFB5EAD7)
private val NavSubtleText = Color(0xFF7A7F95)

private data class BottomBarItem(
    val rota: String,
    val label: String,
    val icone: ImageVector
)

private val bottomBarItems = listOf(
    BottomBarItem(SolidariDestinations.LOCATION, "Localização", Icons.Default.Map),
    BottomBarItem(SolidariDestinations.CASHBACK, "Cashback", Icons.Default.AttachMoney),
    BottomBarItem(SolidariDestinations.PROFILE, "Perfil", Icons.Default.Person),
    BottomBarItem(SolidariDestinations.OPTIONS, "Opções", Icons.Default.Settings)
)

// Bottom bar única, usada por Localização, Cashback, Perfil e Opções.
// Antes cada tela tinha a sua própria cópia dessa barra (com pequenas
// diferenças de cor e sem navegação de verdade); centralizando aqui,
// uma mudança de rota ou de ícone passa a valer pro app inteiro.
@Composable
fun SolidariBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = Color(0xFFE8ECEE),
        tonalElevation = 0.dp
    ) {
        bottomBarItems.forEach { item ->
            val selecionado = rotaAtual == item.rota

            NavigationBarItem(
                selected = selecionado,
                onClick = {
                    if (!selecionado) {
                        navController.navigate(item.rota) {
                            // Evita empilhar várias cópias da mesma tela ao
                            // alternar entre as abas de baixo repetidamente.
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icone, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavGreenDark,
                    selectedTextColor = NavGreenDark,
                    indicatorColor = NavGreenMint,
                    unselectedIconColor = NavSubtleText,
                    unselectedTextColor = NavSubtleText
                )
            )
        }
    }
}
