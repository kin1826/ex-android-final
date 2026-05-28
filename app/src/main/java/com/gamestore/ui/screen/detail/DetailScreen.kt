package com.gamestore.ui.screen.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.DetailViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    vm: DetailViewModel = hiltViewModel(),
) {
    val state   by vm.game.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); delay(1500); vm.clearMessage() }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost   = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết game", color = TextPri) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                actions = { IconButton(onClick = onCartClick) { Icon(Icons.Default.ShoppingCart, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf),
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = PurpleLt) }
            is UiState.Error   -> Box(Modifier.fillMaxSize(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("😕", fontSize = 48.sp); Text(s.message, color = TextMuted) } }
            is UiState.Success -> {
                val game = s.data
                Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                    AsyncImage(
                        model = game.thumbnailUrl.ifBlank { "https://placehold.co/400x220/1A1A2E/A855F7?text=${game.title}" },
                        contentDescription = game.title, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp))

                    Column(Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (game.isHot) Badge(containerColor = Color(0xFFF97316)) { Text("🔥 Hot") }
                            if (game.isNew) Badge(containerColor = GreenColor) { Text("🆕 Mới") }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(game.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPri)
                        Text(game.developer, fontSize = 13.sp, color = TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { i -> Icon(Icons.Default.Star, null, tint = if (i < game.rating.toInt()) AmberColor else DarkBorder, modifier = Modifier.size(16.dp)) }
                            Spacer(Modifier.width(6.dp))
                            Text("%.1f  (${game.reviewCount} đánh giá)".format(game.rating), color = TextMuted, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Surface(color = DarkCard, shape = RoundedCornerShape(6.dp), border = BorderStroke(0.5.dp, DarkBorder)) {
                            Text(game.genre, color = PurpleLt, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Mô tả", fontWeight = FontWeight.Bold, color = TextPri, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(game.description.ifBlank { "Chưa có mô tả." }, color = TextMuted, lineHeight = 22.sp, fontSize = 14.sp)
                        Spacer(Modifier.height(24.dp))
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard), border = BorderStroke(0.5.dp, DarkBorder)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Column {
                                    if (game.hasDiscount) {
                                        Text(game.originalPrice.toVND(), fontSize = 12.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough)
                                        Text("Tiết kiệm ${(game.originalPrice - game.finalPrice).toVND()}", fontSize = 11.sp, color = GreenColor)
                                    }
                                    Text(game.finalPrice.toVND(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PurpleLt)
                                }
                                Button(onClick = { vm.addToCart() }, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(50.dp)) {
                                    Icon(Icons.Default.AddShoppingCart, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Thêm giỏ", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}