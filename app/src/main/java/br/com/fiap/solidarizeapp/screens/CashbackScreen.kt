package br.com.fiap.solidarizeapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.navigation.SolidariBottomBar
import br.com.fiap.solidarizeapp.ui.theme.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Modelos de dados — futuramente virão do backend
// ---------------------------------------------------------------------------

data class Usuario(
    val nome: String,
    val saldo: Double,
    val avatarUrl: String? = null
)

data class ImpactoItem(
    val icone: String,
    val titulo: String,
    val corFundo: Color
)

data class Parceiro(
    val nome: String,
    val descricao: String,
    val badge: String,
    val imagemUrl: String,       // URL da imagem — futuramente vem do backend
    val destaque: Boolean = false
)

// ---------------------------------------------------------------------------
// Dados de exemplo — substituir por chamadas ao backend futuramente
// ---------------------------------------------------------------------------

val usuarioExemplo = Usuario(
    nome = "Bruno",
    saldo = 428.50
)

val impactosExemplo = listOf(
    ImpactoItem("🌿", "Restauração\nAmbiental", Color(0xFFFDE8D8)),
    ImpactoItem("🤝", "Bem-estar\nComunitário", Color(0xFFD8F0E8))
)

val parceirosExemplo = listOf(
    Parceiro(
        nome = "Green Leaf Cafe",
        descricao = "Refeições sustentáveis e cafés especiais",
        badge = "15% CASHBACK",
        imagemUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&q=80",
        destaque = true
    ),
    Parceiro(
        nome = "Vitality Gym",
        descricao = "Saúde e Bem-estar",
        badge = "10% OFF",
        imagemUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&q=80"
    ),
    Parceiro(
        nome = "Eco Threads",
        descricao = "Moda Consciente",
        badge = "SOLIDARITY+",
        imagemUrl = "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&q=80"
    )
)

val categoriasExemplo = listOf("Comunidade", "Meio Ambiente", "Educação", "Saúde", "Cultura")

// ---------------------------------------------------------------------------
// Tela principal
// ---------------------------------------------------------------------------

@Composable
fun CashbackScreen(
    navController: NavHostController,
    usuario: Usuario = usuarioExemplo,
    impactos: List<ImpactoItem> = impactosExemplo,
    parceiros: List<Parceiro> = parceirosExemplo,
    categorias: List<String> = categoriasExemplo,
    arvoresPlantadas: Int = 12,
    refeicoesServidas: Int = 40
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundStart,
        bottomBar = { SolidariBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundStart, BackgroundEnd)
                    )
                )
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TopBar(nomeUsuario = usuario.nome, avatarUrl = usuario.avatarUrl)
            Spacer(modifier = Modifier.height(16.dp))
            SaldoCard(
                saldo = usuario.saldo,
                onDoarClick = {
                    escopo.launch { snackbarHostState.showSnackbar("Selecione uma causa na Home para doar seu crédito.") }
                },
                onHistoricoClick = {
                    escopo.launch { snackbarHostState.showSnackbar("Histórico completo chega na próxima fase.") }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            ImpactoSection(
                arvoresPlantadas = arvoresPlantadas,
                refeicoesServidas = refeicoesServidas,
                impactos = impactos
            )
            Spacer(modifier = Modifier.height(24.dp))
            ParceirosSection(parceiros = parceiros)
            Spacer(modifier = Modifier.height(16.dp))
            CategoriasRow(categorias = categorias)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// TopBar
// ---------------------------------------------------------------------------

@Composable
fun TopBar(nomeUsuario: String, avatarUrl: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar — se tiver URL usa a imagem, senão mostra ícone padrão
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar de $nomeUsuario",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar de $nomeUsuario",
                        tint = White
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Solidari",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )
        }
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notificações",
            tint = GreenDark,
            modifier = Modifier.size(28.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Card de Saldo
// ---------------------------------------------------------------------------

@Composable
fun SaldoCard(
    saldo: Double,
    onDoarClick: () -> Unit = {},
    onHistoricoClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GreenLight, GreenDark)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "SALDO DISPONÍVEL",
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "R$ ${"%.2f".format(saldo).replace(".", ",")}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BRL",
                    fontSize = 13.sp,
                    color = White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onDoarClick,
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(text = "Doar Crédito", color = GreenDark, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onHistoricoClick,
                    border = BorderStroke(1.dp, White.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(text = "Histórico", color = White)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Seção de Impacto
// ---------------------------------------------------------------------------

@Composable
fun ImpactoSection(
    arvoresPlantadas: Int,
    refeicoesServidas: Int,
    impactos: List<ImpactoItem>
) {
    Column {
        Text(
            text = "Impacto da Solidariedade",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Ao usar nossos parceiros, você ajudou a plantar $arvoresPlantadas árvores e forneceu $refeicoesServidas refeições só este mês.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            impactos.forEach { impacto ->
                ImpactoCard(impacto = impacto, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ImpactoCard(impacto: ImpactoItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(impacto.corFundo)
            .padding(16.dp)
    ) {
        Column {
            Text(text = impacto.icone, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = impacto.titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreenDark,
                lineHeight = 20.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Seção de Parceiros
// ---------------------------------------------------------------------------

@Composable
fun ParceirosSection(parceiros: List<Parceiro>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vantagens dos Parceiros",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Ver tudo",
                fontSize = 13.sp,
                color = GreenAccent,
                modifier = Modifier.clickable { }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Card grande — primeiro parceiro
        parceiros.firstOrNull()?.let { parceiro ->
            ParceiroCardGrande(parceiro = parceiro)
            Spacer(modifier = Modifier.height(6.dp))
            // Nome e descrição fora do card, igual ao design
            Text(
                text = parceiro.nome,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = parceiro.descricao,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Cards pequenos — restantes
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            parceiros.drop(1).forEach { parceiro ->
                ParceiroCardPequeno(
                    parceiro = parceiro,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ParceiroCardGrande(parceiro: Parceiro) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Imagem de fundo
        AsyncImage(
            model = parceiro.imagemUrl,
            contentDescription = parceiro.nome,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay escuro sutil pra legibilidade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GreenDark.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // Badge no topo esquerdo
        Box(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GreenAccent.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = parceiro.badge,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
    }
}

@Composable
fun ParceiroCardPequeno(parceiro: Parceiro, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Imagem de fundo
            AsyncImage(
                model = parceiro.imagemUrl,
                contentDescription = parceiro.nome,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Badge no topo esquerdo
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GreenDark.copy(alpha = 0.85f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = parceiro.badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = parceiro.nome,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = parceiro.descricao,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

// ---------------------------------------------------------------------------
// Chips de categoria
// ---------------------------------------------------------------------------

@Composable
fun CategoriasRow(categorias: List<String>) {
    var categoriaSelecionada by remember { mutableStateOf(categorias.firstOrNull()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categorias.forEach { categoria ->
            val selecionada = categoria == categoriaSelecionada
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (selecionada) GreenLight.copy(alpha = 0.25f) else White
                    )
                    .clickable { categoriaSelecionada = categoria }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = categoria,
                    fontSize = 13.sp,
                    color = if (selecionada) GreenDark else TextSecondary,
                    fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CashbackScreenPreview() {
    CashbackScreen(navController = rememberNavController())
}
