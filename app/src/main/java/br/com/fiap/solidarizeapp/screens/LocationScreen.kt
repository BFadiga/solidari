package br.com.fiap.solidarizeapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.solidarizeapp.R
import br.com.fiap.solidarizeapp.navigation.SolidariBottomBar

private val SolidariGreen = Color(0xFF95D5B2)
private val SolidariMint = Color(0xFFB5EAD7)
private val SolidariPeach = Color(0xFFFFDAC1)

private val DarkGreen = Color(0xFF216B52)
private val DarkText = Color(0xFF1F2743)
private val SubtleText = Color(0xFF7A7F95)
private val SearchText = Color(0xFF7E8195)
private val HeaderBg = Color(0xFFF1F1F5)
private val CardWhite = Color(0xFFF7F7F7)
private val MarkerBrown = Color(0xFF8A6B56)
private val OverlayMap = Color(0x4434484A)

@Composable
fun LocationScreen(navController: NavHostController) {
    var selectedFilter by remember { mutableStateOf("Todos os Pontos") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection()
            MapSection(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }

        BottomCardAndNav(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .padding(top = 24.dp, start = 22.dp, end = 22.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(3.dp, SolidariMint)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color(0xFF8A5A3E),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Solidari",
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = DarkGreen,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun MapSection(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_map),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.02f
                    scaleY = 1.02f
                    translationY = 8f
                },
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayMap)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SearchBar()

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChipButton(
                    text = "Todos os Pontos",
                    selected = selectedFilter == "Todos os Pontos",
                    selectedColor = DarkGreen,
                    onClick = { onFilterSelected("Todos os Pontos") }
                )

                FilterChipButton(
                    text = "Bancos de Alimentos",
                    selected = selectedFilter == "Bancos de Alimentos",
                    selectedColor = SolidariPeach,
                    onClick = { onFilterSelected("Bancos de Alimentos") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                MarkerWithLabel(
                    icon = Icons.Default.MedicalServices,
                    circleColor = SolidariMint,
                    label = "Centro de Ajuda\nde Saúde",
                    labelTextColor = DarkGreen,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = 4.dp)
                )

                MarkerWithLabel(
                    icon = Icons.Default.Favorite,
                    circleColor = DarkGreen,
                    label = "Cozinha Comunitária",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-12).dp)
                )

                MarkerWithLabel(
                    icon = Icons.Default.LocalMall,
                    circleColor = MarkerBrown,
                    label = "Parceiro: EcoStore",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 118.dp, y = 172.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        shape = RoundedCornerShape(42.dp),
        color = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = SearchText,
                modifier = Modifier.size(33.dp)
            )

            Spacer(modifier = Modifier.width(18.dp))

            Text(
                text = "Buscar pontos de doação...",
                color = SearchText,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color(0xFFD8D8E0))
            )

            Spacer(modifier = Modifier.width(18.dp))

            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = DarkGreen,
                modifier = Modifier.size(29.dp)
            )
        }
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) selectedColor else CardWhite,
        modifier = Modifier.height(46.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected && selectedColor == DarkGreen) Color.White else DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MarkerWithLabel(
    icon: ImageVector,
    circleColor: Color,
    label: String,
    modifier: Modifier = Modifier,
    labelTextColor: Color = DarkGreen
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = circleColor,
            border = BorderStroke(5.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                color = labelTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BottomCardAndNav(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-4).dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(7.dp)
                        .height(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkGreen)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SolidariGreen
                        ) {
                            Text(
                                text = "ATIVO\nAGORA",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                color = DarkGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "• A 0.4 milhas de\ndistância",
                            color = SubtleText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Banco de Alimentos\nComunitário",
                        color = DarkText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Aceitando produtos secos e frescos...",
                        color = SubtleText,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = DarkGreen
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        SolidariBottomBar(navController)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LocationScreenPreview() {
    LocationScreen(navController = rememberNavController())
}