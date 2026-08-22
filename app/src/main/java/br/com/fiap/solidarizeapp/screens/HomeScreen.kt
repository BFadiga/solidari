package br.com.fiap.solidarizeapp.screens


import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import br.com.fiap.solidarizeapp.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.navigation.SolidariBottomBar
import kotlinx.coroutines.launch

private val BgColor = Color(0xFFF4F4F8)
private val GreenMain = Color(0xFF95D5B2)
private val GreenLight = Color(0xFFB5EAD7)
private val Peach = Color(0xFFFFDAC1)
private val Purple = Color(0xFFC7CEEA)
private val DarkText = Color(0xFF1C2033)
private val GrayText = Color(0xFF6F7380)
private val DarkGreenButton = Color(0xFF2F6D50)
private val SoftCard = Color(0xFFF8F8FB)

@Composable
fun HomeScreen(navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    fun avisarAjuda(mensagem: String) {
        escopo.launch { snackbarHostState.showSnackbar(mensagem) }
    }

    Scaffold(
        containerColor = BgColor,
        bottomBar = { SolidariBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TopHeader()

            Spacer(modifier = Modifier.height(24.dp))

            TitleSection()

            Spacer(modifier = Modifier.height(22.dp))

            FinanceCard(onDoarClick = { avisarAjuda("Obrigado! Redirecionando para a doação financeira...") })

            Spacer(modifier = Modifier.height(16.dp))

            SmallDonationCards()

            Spacer(modifier = Modifier.height(28.dp))

            UrgentHeader()

            Spacer(modifier = Modifier.height(14.dp))

            EmergencyKitchenCard(onAjudarClick = { avisarAjuda("Obrigado por ajudar a Cozinha Comunitária Metro!") })

            Spacer(modifier = Modifier.height(18.dp))

            OpenBooksCard(onEntregarClick = { avisarAjuda("Show! Vamos indicar o ponto de entrega mais próximo.") })

            Spacer(modifier = Modifier.height(26.dp))

            ImpactSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👨🏻‍💼", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Solidari",
                color = Color(0xFF216C4C),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notificações",
            tint = Color(0xFF216C4C),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun TitleSection() {
    Text(
        text = "Como gostaria de\najudar hoje?",
        fontSize = 30.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Cada pequeno gesto gera uma onda\nde mudança em nossa comunidade.",
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = GrayText
    )
}

@Composable
fun FinanceCard(onDoarClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GreenLight, GreenMain)
                    )
                )
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    contentDescription = "Financeiro",
                    tint = Color(0xFF216C4C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Financeiro",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Apoio direto para projetos verificados",
                fontSize = 14.sp,
                color = Color(0xFF46504B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDoarClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenButton)
            ) {
                Text(
                    text = "Doar Fundos",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SmallDonationCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SmallCard(
            modifier = Modifier.weight(1f),
            title = "Alimentos",
            subtitle = "Combate à fome\nlocal",
            bgColor = Color(0xFFF1EFF8),
            circleColor = Peach,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Fastfood,
                    contentDescription = "Alimentos",
                    tint = DarkText,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        SmallCard(
            modifier = Modifier.weight(1f),
            title = "Roupas",
            subtitle = "Compartilhe calor\ne dignidade",
            bgColor = Color(0xFFF0F0FA),
            circleColor = GreenLight,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Checkroom,
                    contentDescription = "Roupas",
                    tint = DarkText,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
fun SmallCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    bgColor: Color,
    circleColor: Color,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = GrayText
            )
        }
    }
}

@Composable
fun UrgentHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Causas Urgentes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Text(
            text = "Ver Tudo ↗",
            fontSize = 13.sp,
            color = Color(0xFF216C4C),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmergencyKitchenCard(onAjudarClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SoftCard)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.kitchen),
                contentDescription = "Cozinha comunitária",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Peach)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUXÍLIO DE EMERGÊNCIA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF845B43)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "faltam 2 dias",
                        fontSize = 12.sp,
                        color = GrayText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cozinha Comunitária Metro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Nosso aquecedor principal quebrou.\nPrecisamos de fundos urgentes para...",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = GrayText
                )

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = GreenMain,
                    trackColor = Color(0xFFE5E5EA)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "78% da meta de $5.000 atingida",
                        fontSize = 11.sp,
                        color = GrayText
                    )

                    Text(
                        text = "Ajudar Agora",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF216C4C),
                        modifier = Modifier.clickable { onAjudarClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun OpenBooksCard(onEntregarClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SoftCard)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.books),
                contentDescription = "Fundação Open Books",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GreenLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EDUCAÇÃO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2A6A51)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "ONG Verificada",
                        fontSize = 12.sp,
                        color = GrayText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Fundação Open Books",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Coletando livros didáticos e materiais\nescolares usados para o próximo semestr...",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = GrayText
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFE7E8FA))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Distância",
                                tint = DarkText,
                                modifier = Modifier.size(15.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "2.4 milhas de distância",
                                fontSize = 12.sp,
                                color = DarkText
                            )
                        }
                    }

                    Text(
                        text = "Entregar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF216C4C),
                        modifier = Modifier.clickable { onEntregarClick() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImpactSection() {
    Text(
        text = "Navegar por Impacto",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(16.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ImpactChip("Comunidade", Peach)
        ImpactChip("Meio Ambiente", Color(0xFFDDE1FF))
        ImpactChip("Animais", Color(0xFFDDE1FF))
        ImpactChip("Saúde", Color(0xFFDDE1FF))
        ImpactChip("Alívio de Crises", Color(0xFFDDE1FF))
    }
}

@Composable
fun ImpactChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = DarkText
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}