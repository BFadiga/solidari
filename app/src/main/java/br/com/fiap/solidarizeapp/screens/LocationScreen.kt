package br.com.fiap.solidarizeapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.R
import br.com.fiap.solidarizeapp.data.CarteiraViewModel
import br.com.fiap.solidarizeapp.data.ParceiroResponse
import br.com.fiap.solidarizeapp.navigation.SolidariBottomBar
import kotlin.math.abs

private val SolidariMint = Color(0xFFB5EAD7)
private val DarkGreen = Color(0xFF216B52)
private val DarkText = Color(0xFF1F2743)
private val SubtleText = Color(0xFF7A7F95)
private val HeaderBg = Color(0xFFF1F1F5)
private val OverlayMap = Color(0x5534484A)

@Composable
fun LocationScreen(
    navController: NavHostController,
    viewModel: CarteiraViewModel = viewModel()
) {
    var busca by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var selecionado by remember { mutableStateOf<ParceiroResponse?>(null) }

    LaunchedEffect(Unit) { viewModel.carregar() }

    val categorias = remember(viewModel.parceiros) {
        viewModel.parceiros.map { it.categoria }.distinct().sorted()
    }

    val visiveis = viewModel.parceiros.filter { parceiro ->
        val casaCategoria = categoria == null || parceiro.categoria == categoria
        val casaBusca = busca.isBlank() || parceiro.nome.contains(busca, ignoreCase = true)
        casaCategoria && casaBusca
    }

    Scaffold(
        containerColor = HeaderBg,
        bottomBar = { SolidariBottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBg)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "Onde usar seu Solidari",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "${visiveis.size} de ${viewModel.parceiros.size} parceiros perto de você",
                    fontSize = 13.sp,
                    color = SubtleText
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                MapaComParceiros(
                    parceiros = visiveis,
                    selecionado = selecionado,
                    onSelecionar = { selecionado = it }
                )

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))

                    BarraBusca(
                        valor = busca,
                        onValor = { busca = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChipMapa(
                            texto = "Todos",
                            selecionado = categoria == null,
                            onClick = { categoria = null }
                        )
                        categorias.forEach { cat ->
                            ChipMapa(
                                texto = cat,
                                selecionado = cat == categoria,
                                onClick = { categoria = if (cat == categoria) null else cat }
                            )
                        }
                    }
                }

                selecionado?.let { parceiro ->
                    CartaoParceiroSelecionado(
                        parceiro = parceiro,
                        onFechar = { selecionado = null },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }

                if (visiveis.isEmpty() && !viewModel.carregando) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Nenhum parceiro encontrado.",
                            fontSize = 13.sp,
                            color = SubtleText
                        )
                    }
                }
            }
        }
    }
}

/**
 * Posiciona cada parceiro no mapa a partir da sua latitude e longitude reais,
 * normalizadas para a área visível. Não é um mapa navegável — é a planta de
 * fundo com os pontos nas posições relativas corretas entre si.
 */
@Composable
private fun MapaComParceiros(
    parceiros: List<ParceiroResponse>,
    selecionado: ParceiroResponse?,
    onSelecionar: (ParceiroResponse) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_map),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayMap)
        )

        val comCoordenada = parceiros.filter { it.latitude != null && it.longitude != null }
        if (comCoordenada.isEmpty()) return@Box

        val latitudes = comCoordenada.mapNotNull { it.latitude }
        val longitudes = comCoordenada.mapNotNull { it.longitude }
        val latMin = latitudes.min()
        val latMax = latitudes.max()
        val lonMin = longitudes.min()
        val lonMax = longitudes.max()

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val larguraUtil = maxWidth - 120.dp
            val alturaUtil = maxHeight - 260.dp

            comCoordenada.forEach { parceiro ->
                val fracaoX = if (abs(lonMax - lonMin) < 1e-9) 0.5
                else (parceiro.longitude!! - lonMin) / (lonMax - lonMin)
                val fracaoY = if (abs(latMax - latMin) < 1e-9) 0.5
                else (latMax - parceiro.latitude!!) / (latMax - latMin)

                MarcadorParceiro(
                    parceiro = parceiro,
                    destacado = parceiro.id == selecionado?.id,
                    onClick = { onSelecionar(parceiro) },
                    modifier = Modifier.offset(
                        x = 60.dp + larguraUtil * fracaoX.toFloat(),
                        y = 150.dp + alturaUtil * fracaoY.toFloat()
                    )
                )
            }
        }
    }
}

@Composable
private fun MarcadorParceiro(
    parceiro: ParceiroResponse,
    destacado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(if (destacado) 46.dp else 38.dp)
            .clip(CircleShape)
            .background(if (destacado) DarkGreen else SolidariMint)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalMall,
            contentDescription = parceiro.nome,
            tint = if (destacado) Color.White else DarkGreen,
            modifier = Modifier.size(if (destacado) 22.dp else 18.dp)
        )
    }
}

@Composable
private fun BarraBusca(valor: String, onValor: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = SubtleText,
                modifier = Modifier.size(20.dp)
            )
            TextField(
                value = valor,
                onValueChange = onValor,
                placeholder = { Text("Buscar parceiro...", fontSize = 14.sp, color = SubtleText) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            if (valor.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Limpar busca",
                    tint = SubtleText,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onValor("") }
                )
            }
        }
    }
}

@Composable
private fun ChipMapa(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selecionado) DarkGreen else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = texto,
            fontSize = 13.sp,
            color = if (selecionado) Color.White else SubtleText,
            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CartaoParceiroSelecionado(
    parceiro: ParceiroResponse,
    onFechar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGreen)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parceiro.nome,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = parceiro.descricao,
                    fontSize = 12.sp,
                    color = SubtleText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(50.dp), color = SolidariMint) {
                        Text(
                            text = "${"%.0f".format(parceiro.percentualCashback)}% de volta",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = parceiro.categoria, fontSize = 11.sp, color = SubtleText)
                }
            }
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Fechar",
                tint = SubtleText,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onFechar)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LocationScreenPreview() {
    LocationScreen(navController = rememberNavController())
}
