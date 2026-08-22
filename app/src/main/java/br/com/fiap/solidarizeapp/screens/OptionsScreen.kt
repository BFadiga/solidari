package br.com.fiap.solidarizeapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.navigation.SolidariBottomBar

private val OptionsGreen = Color(0xFF216B52)
private val OptionsBg = Color(0xFFF1F1F5)
private val OptionsSubtitle = Color(0xFF7A7F95)

// Assim como o Perfil, essa tela só existe pra fechar a navegação entre
// as quatro abas. Configurações de conta e créditos dos desenvolvedores
// entram numa próxima iteração.
@Composable
fun OptionsScreen(navController: NavHostController) {
    Scaffold(
        containerColor = OptionsBg,
        bottomBar = { SolidariBottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OptionsBg)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Construction,
                contentDescription = null,
                tint = OptionsGreen,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Opções em construção",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OptionsGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Configurações da conta e créditos dos desenvolvedores chegam na próxima fase.",
                fontSize = 13.sp,
                color = OptionsSubtitle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OptionsScreenPreview() {
    OptionsScreen(navController = rememberNavController())
}
