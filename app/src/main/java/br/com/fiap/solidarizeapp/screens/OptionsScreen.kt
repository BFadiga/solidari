package br.com.fiap.solidarizeapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.data.CarteiraViewModel
import br.com.fiap.solidarizeapp.data.ParceiroResponse
import br.com.fiap.solidarizeapp.data.Sessao
import br.com.fiap.solidarizeapp.navigation.SolidariBottomBar
import br.com.fiap.solidarizeapp.navigation.SolidariDestinations
import br.com.fiap.solidarizeapp.ui.theme.GreenAccent
import br.com.fiap.solidarizeapp.ui.theme.GreenDark
import br.com.fiap.solidarizeapp.ui.theme.GreenLight
import br.com.fiap.solidarizeapp.ui.theme.TextPrimary
import br.com.fiap.solidarizeapp.ui.theme.TextSecondary
import br.com.fiap.solidarizeapp.ui.theme.White

private val OptionsBg = Color(0xFFF1F1F5)

@Composable
fun OptionsScreen(
    navController: NavHostController,
    viewModel: CarteiraViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var compraAberta by remember { mutableStateOf(false) }
    var extratoAberto by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.carregar() }

    LaunchedEffect(viewModel.aviso) {
        viewModel.aviso?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparAviso()
        }
    }

    if (compraAberta) {
        DialogoCompra(
            parceiros = viewModel.parceiros,
            onFechar = { compraAberta = false },
            onConfirmar = { parceiroId, valor ->
                compraAberta = false
                viewModel.registrarCompra(parceiroId, valor)
            }
        )
    }

    Scaffold(
        containerColor = OptionsBg,
        bottomBar = { SolidariBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OptionsBg)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Conta e ajustes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            val perfil = viewModel.perfil
            if (perfil != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(GreenDark, GreenLight)))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = perfil.nome.take(1).uppercase(),
                            color = White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = perfil.nome,
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = perfil.email,
                            color = White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "R$ " + "%.2f".format(perfil.saldo).replace('.', ','),
                        color = White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = "CASHBACK",
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            ItemAcao(
                icone = Icons.Default.ShoppingBag,
                titulo = "Registrar compra em parceiro",
                detalhe = "Informe o que consumiu e acompanhe o crédito",
                onClick = { compraAberta = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ItemAcao(
                icone = Icons.Default.ReceiptLong,
                titulo = "Extrato de doações",
                detalhe = if (viewModel.extrato?.posicaoRanking?.takeIf { it > 0 } != null) {
                    "Você é o ${viewModel.extrato?.posicaoRanking}º maior doador"
                } else {
                    "Suas doações mais recentes"
                },
                expandido = extratoAberto,
                onClick = { extratoAberto = !extratoAberto }
            )

            if (extratoAberto) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(White)
                        .padding(14.dp)
                ) {
                    Text(
                        text = viewModel.extrato?.extrato ?: "Carregando...",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MINHAS COMPRAS",
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.transacoes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(White)
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Nenhuma compra registrada ainda.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                viewModel.transacoes.take(6).forEach { transacao ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(White)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transacao.parceiroNome,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "R$ " + "%.2f".format(transacao.valor).replace('.', ','),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        SituacaoCompra(status = transacao.status, cashback = transacao.cashbackGerado)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SOBRE",
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Solidari",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cada R$ 10 doados em restauração ambiental plantam uma árvore. " +
                        "Cada R$ 5 em bem-estar comunitário servem uma refeição.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        Sessao.encerrar()
                        navController.navigate(SolidariDestinations.LOGIN) { popUpTo(0) }
                    }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFB42318),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sair da conta",
                    color = Color(0xFFB42318),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ItemAcao(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    detalhe: String,
    expandido: Boolean? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(GreenLight.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icone, contentDescription = null, tint = GreenAccent)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = detalhe, fontSize = 12.sp, color = TextSecondary)
        }
        if (expandido != null) {
            Icon(
                imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun SituacaoCompra(status: String, cashback: Double?) {
    val (texto, cor, fundo) = when (status) {
        "PROCESSADA" -> Triple(
            "+ R$ " + "%.2f".format(cashback ?: 0.0).replace('.', ','),
            GreenDark,
            GreenLight.copy(alpha = 0.22f)
        )
        "PENDENTE" -> Triple("Aguardando", Color(0xFF7A5B00), Color(0xFFFFF3CD))
        else -> Triple("Com problema", Color(0xFFB42318), Color(0xFFFDECEA))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cor)
    }
}

@Composable
private fun DialogoCompra(
    parceiros: List<ParceiroResponse>,
    onFechar: () -> Unit,
    onConfirmar: (Long, Double) -> Unit
) {
    var parceiroId by remember { mutableStateOf<Long?>(null) }
    var valorTexto by remember { mutableStateOf("") }

    val parceiro = parceiros.firstOrNull { it.id == parceiroId }
    val valor = valorTexto.replace(',', '.').toDoubleOrNull()
    val valido = parceiro != null && valor != null && valor > 0
    val previsto = if (valido) valor!! * parceiro!!.percentualCashback / 100 else null

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = White,
        title = { Text(text = "Registrar compra", fontWeight = FontWeight.Bold, color = GreenDark) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = "Onde você comprou", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                parceiros.forEach { p ->
                    val selecionado = p.id == parceiroId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selecionado) GreenLight.copy(alpha = 0.2f) else Color(0xFFF2F3F7))
                            .clickable { parceiroId = p.id }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = p.nome,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${"%.0f".format(p.percentualCashback)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Valor da compra") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (previsto != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Você receberá cerca de R$ " + "%.2f".format(previsto).replace('.', ',') +
                            " assim que o cashback for apurado.",
                        fontSize = 12.sp,
                        color = GreenAccent,
                        lineHeight = 17.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (valido) onConfirmar(parceiroId!!, valor!!) },
                enabled = valido,
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(text = "Registrar", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onFechar) {
                Text(text = "Cancelar", color = TextSecondary)
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OptionsScreenPreview() {
    OptionsScreen(navController = rememberNavController())
}
