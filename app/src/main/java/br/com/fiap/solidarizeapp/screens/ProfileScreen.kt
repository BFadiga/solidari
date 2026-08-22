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

private val ProfileGreen = Color(0xFF216B52)
private val ProfileBg = Color(0xFFF1F1F5)
private val ProfileSubtitle = Color(0xFF7A7F95)

// Tela de Perfil ainda não implementada de verdade: por enquanto só
// confirma que a navegação chega até aqui. Histórico de doações e score
// de impacto ficam para a próxima entrega.
@Composable
fun ProfileScreen(navController: NavHostController) {
    Scaffold(
        containerColor = ProfileBg,
        bottomBar = { SolidariBottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ProfileBg)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Construction,
                contentDescription = null,
                tint = ProfileGreen,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Perfil em construção",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Histórico de doações e score de impacto chegam na próxima fase.",
                fontSize = 13.sp,
                color = ProfileSubtitle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
