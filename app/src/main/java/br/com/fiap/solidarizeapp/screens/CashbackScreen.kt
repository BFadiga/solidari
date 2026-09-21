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
import androidx.compose.ui.text.style.TextOverflow
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
    val imagemUrl: String,
    val destaque: Boolean = false,
    val categoria: String = "",
    val percentualCashback: Double = 0.0
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
    refeicoesServidas: Int = 40,
    aviso: String? = null,
    onAvisoExibido: () -> Unit = {},
    onDoar: (Double, String) -> Unit = { _, _ -> },
    onHistoricoClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    var dialogoAberto by remember { mutableStateOf(false) }
    var categoriaSelecionada by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbarHostState.showSnackbar(aviso)
            onAvisoExibido()
        }
    }

    if (dialogoAberto) {
        DialogoDoacao(
            saldo = usuario.saldo,
            onFechar = { dialogoAberto = false },
            onConfirmar = { valor, categoria ->
                dialogoAberto = false
                onDoar(valor, categoria)
            }
        )
    }

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
                onDoarClick = { dialogoAberto = true },
                onHistoricoClick = onHistoricoClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            ImpactoSection(
                arvoresPlantadas = arvoresPlantadas,
                refeicoesServidas = refeicoesServidas,
                impactos = impactos
            )
            Spacer(modifier = Modifier.height(24.dp))
            ParceirosSection(
                parceiros = parceiros,
                categorias = categorias,
                categoriaSelecionada = categoriaSelecionada,
                onSelecionarCategoria = { categoriaSelecionada = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DialogoDoacao(
    saldo: Double,
    onFechar: () -> Unit,
    onConfirmar: (Double, String) -> Unit
) {
    var valorTexto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("RESTAURACAO_AMBIENTAL") }

    val valor = valorTexto.replace(',', '.').toDoubleOrNull()
    val valido = valor != null && valor > 0 && valor <= saldo

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = White,
        title = {
            Text(text = "Doar crédito", fontWeight = FontWeight.Bold, color = GreenDark)
        },
        text = {
            Column {
                Text(
                    text = "Disponível: R$ " + "%.2f".format(saldo).replace('.', ','),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Valor") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    isError = valorTexto.isNotBlank() && !valido,
                    modifier = Modifier.fillMaxWidth()
                )
                if (valorTexto.isNotBlank() && valor != null && valor > saldo) {
                    Text(
                        text = "Valor acima do seu saldo.",
                        fontSize = 12.sp,
                        color = Color(0xFFB42318)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Destino", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                OpcaoCategoria(
                    titulo = "Restauração ambiental",
                    detalhe = "R$ 10 plantam uma árvore",
                    selecionado = categoria == "RESTAURACAO_AMBIENTAL",
                    onClick = { categoria = "RESTAURACAO_AMBIENTAL" }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OpcaoCategoria(
                    titulo = "Bem-estar comunitário",
                    detalhe = "R$ 5 servem uma refeição",
                    selecionado = categoria == "BEM_ESTAR_COMUNITARIO",
                    onClick = { categoria = "BEM_ESTAR_COMUNITARIO" }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { valor?.let { onConfirmar(it, categoria) } },
                enabled = valido,
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(text = "Confirmar", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onFechar) {
                Text(text = "Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun OpcaoCategoria(
    titulo: String,
    detalhe: String,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selecionado) GreenLight.copy(alpha = 0.18f) else Color(0xFFF2F3F7))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selecionado,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = GreenAccent)
        )
        Column {
            Text(text = titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenDark)
            Text(text = detalhe, fontSize = 12.sp, color = TextSecondary)
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
            text = "Com suas doações você já ajudou a plantar $arvoresPlantadas árvores e a servir $refeicoesServidas refeições.",
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
fun ParceirosSection(
    parceiros: List<Parceiro>,
    categorias: List<String>,
    categoriaSelecionada: String?,
    onSelecionarCategoria: (String?) -> Unit
) {
    val visiveis = if (categoriaSelecionada == null) {
        parceiros
    } else {
        parceiros.filter { it.categoria == categoriaSelecionada }
    }

    Column {
        Text(
            text = "Vantagens dos Parceiros",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Consuma nesses lugares e parte do valor volta como crédito.",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(14.dp))

        CategoriasRow(
            categorias = categorias,
            selecionada = categoriaSelecionada,
            onSelecionar = onSelecionarCategoria
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (visiveis.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum parceiro nesta categoria ainda.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            return@Column
        }

        val destacado = visiveis.firstOrNull { it.destaque } ?: visiveis.first()
        val demais = visiveis.filter { it !== destacado }

        ParceiroCardGrande(parceiro = destacado)

        if (demais.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            demais.chunked(2).forEach { linha ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    linha.forEach { parceiro ->
                        ParceiroCardPequeno(
                            parceiro = parceiro,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Mantém o alinhamento quando a última linha tem um item só.
                    if (linha.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ParceiroCardGrande(parceiro: Parceiro) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            ImagemParceiro(parceiro = parceiro)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, GreenDark.copy(alpha = 0.55f))
                        )
                    )
            )

            SeloCashback(
                percentual = parceiro.percentualCashback,
                badge = parceiro.badge,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }

        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = parceiro.nome,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = parceiro.descricao,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ParceiroCardPequeno(parceiro: Parceiro, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            ImagemParceiro(parceiro = parceiro)

            SeloCashback(
                percentual = parceiro.percentualCashback,
                badge = parceiro.badge,
                compacto = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(
                text = parceiro.nome,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = parceiro.categoria.ifBlank { parceiro.descricao },
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ImagemParceiro(parceiro: Parceiro) {
    if (parceiro.imagemUrl.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GreenLight, GreenDark)))
        )
    } else {
        AsyncImage(
            model = parceiro.imagemUrl,
            contentDescription = parceiro.nome,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SeloCashback(
    percentual: Double,
    badge: String,
    modifier: Modifier = Modifier,
    compacto: Boolean = false
) {
    val texto = if (percentual > 0) {
        "${"%.0f".format(percentual)}% de volta"
    } else {
        badge
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(GreenDark.copy(alpha = 0.88f))
            .padding(horizontal = if (compacto) 8.dp else 11.dp, vertical = if (compacto) 3.dp else 5.dp)
    ) {
        Text(
            text = texto,
            fontSize = if (compacto) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )
    }
}

@Composable
fun CategoriasRow(
    categorias: List<String>,
    selecionada: String?,
    onSelecionar: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipCategoria(
            rotulo = "Todos",
            selecionada = selecionada == null,
            onClick = { onSelecionar(null) }
        )
        categorias.forEach { categoria ->
            ChipCategoria(
                rotulo = categoria,
                selecionada = categoria == selecionada,
                onClick = { onSelecionar(if (categoria == selecionada) null else categoria) }
            )
        }
    }
}

@Composable
private fun ChipCategoria(rotulo: String, selecionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selecionada) GreenDark else White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = rotulo,
            fontSize = 13.sp,
            color = if (selecionada) White else TextSecondary,
            fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal
        )
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
