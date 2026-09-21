package br.com.fiap.solidarizeapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.data.CarteiraViewModel
import br.com.fiap.solidarizeapp.data.Sessao
import br.com.fiap.solidarizeapp.navigation.SolidariBottomBar
import br.com.fiap.solidarizeapp.navigation.SolidariDestinations

private val ProfileGreen = Color(0xFF216B52)
private val ProfileGreenLight = Color(0xFF52B788)
private val ProfileBg = Color(0xFFF1F1F5)
private val ProfileSubtitle = Color(0xFF7A7F95)

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: CarteiraViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    val perfil = viewModel.perfil
    val impacto = viewModel.impacto

    Scaffold(
        containerColor = ProfileBg,
        bottomBar = { SolidariBottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ProfileBg)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (viewModel.carregando && perfil == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProfileGreen)
                }
                return@Column
            }

            if (perfil == null) {
                Text(
                    text = viewModel.erro ?: "Entre para ver seu perfil.",
                    fontSize = 14.sp,
                    color = ProfileSubtitle
                )
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ProfileGreen, ProfileGreenLight)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Text(
                        text = perfil.nome,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = perfil.email,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Saldo disponível",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = emReais(perfil.saldo),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CartaoImpacto(
                    valor = "${impacto?.arvoresPlantadas ?: 0}",
                    rotulo = "árvores plantadas",
                    modifier = Modifier.weight(1f)
                )
                CartaoImpacto(
                    valor = "${impacto?.refeicoesServidas ?: 0}",
                    rotulo = "refeições servidas",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Doações recentes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.doacoes.isEmpty()) {
                Text(
                    text = "Você ainda não fez nenhuma doação.",
                    fontSize = 13.sp,
                    color = ProfileSubtitle
                )
            } else {
                viewModel.doacoes.take(8).forEach { doacao ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = doacao.parceiroNome ?: "Doação direta",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (doacao.categoriaImpacto == "RESTAURACAO_AMBIENTAL") {
                                        "Restauração ambiental"
                                    } else {
                                        "Bem-estar comunitário"
                                    },
                                    fontSize = 12.sp,
                                    color = ProfileSubtitle
                                )
                            }
                            Text(
                                text = emReais(doacao.valor),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = {
                    Sessao.encerrar()
                    navController.navigate(SolidariDestinations.LOGIN) {
                        popUpTo(0)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = ProfileSubtitle,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = " Sair da conta", color = ProfileSubtitle, fontSize = 14.sp)
            }
        }
    }
}

private fun emReais(valor: Double): String =
    String.format(Locale("pt", "BR"), "R$ %,.2f", valor)

@Composable
private fun CartaoImpacto(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = valor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileGreen
            )
            Text(text = rotulo, fontSize = 12.sp, color = ProfileSubtitle)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
