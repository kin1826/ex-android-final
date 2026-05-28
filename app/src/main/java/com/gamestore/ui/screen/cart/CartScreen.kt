package com.gamestore.ui.screen.cart

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamestore.model.*
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onOrderSuccess: (Int) -> Unit,
    vm: CartViewModel = hiltViewModel(),
) {
    val cart        by vm.cart.collectAsStateWithLifecycle()
    val orderResult by vm.orderResult.collectAsStateWithLifecycle()
    val message     by vm.message.collectAsStateWithLifecycle()
    var showDialog  by remember { mutableStateOf(false) }
    val snackbar    = remember { SnackbarHostState() }

    LaunchedEffect(orderResult) {
        if (orderResult is UiState.Success) {
            val order = (orderResult as UiState.Success).data
            vm.clearOrderResult()
            onOrderSuccess(order.id)
        }
    }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost   = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("🛒 Giỏ hàng (${cart.count})", color = TextPri, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf),
            )
        }
    ) { padding ->
        if (cart.isEmpty) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🛒", fontSize = 64.sp)
                    Text("Giỏ hàng trống", fontSize = 18.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(10.dp)) { Text("Khám phá game") }
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(cart.items, key = { it.id }) { item ->
                        CartItemRow(item, { vm.increaseQty(item.game.id) }, { vm.decreaseQty(item.game.id) }, { vm.removeItem(item.id) })
                    }
                }
                Card(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), colors = CardDefaults.cardColors(containerColor = DarkSurf), border = BorderStroke(0.5.dp, DarkBorder)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Tạm tính", color = TextMuted); Text(cart.subtotal.toVND(), color = TextPri) }
                        Spacer(Modifier.height(6.dp)); HorizontalDivider(color = DarkBorder); Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tổng cộng", fontWeight = FontWeight.Bold, color = TextPri, fontSize = 16.sp)
                            Text(cart.total.toVND(), fontWeight = FontWeight.Bold, color = PurpleLt, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = orderResult !is UiState.Loading, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(12.dp)) {
                            if (orderResult is UiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else { Icon(Icons.Default.Payment, null); Spacer(Modifier.width(8.dp)); Text("Thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor   = DarkSurf,
            title = { Text("Chọn thanh toán", color = TextPri, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("WALLET" to "💰 Ví GameStore", "MOMO" to "🟣 MoMo", "ZALOPAY" to "🔵 ZaloPay", "BANK_TRANSFER" to "🏦 Chuyển khoản").forEach { (key, label) ->
                        Card(onClick = { showDialog = false; vm.placeOrder(key) }, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = DarkCard), border = BorderStroke(0.5.dp, DarkBorder)) {
                            Text(label, color = TextPri, fontSize = 15.sp, modifier = Modifier.fillMaxWidth().padding(14.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Hủy", color = TextMuted) } },
        )
    }
}

@Composable
fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard), border = BorderStroke(0.5.dp, DarkBorder)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = item.game.thumbnailUrl.ifBlank { "https://placehold.co/70x70/1A1A2E/white?text=🎮" }, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.game.title, fontWeight = FontWeight.SemiBold, color = TextPri, maxLines = 2)
                Text(item.game.finalPrice.toVND(), color = PurpleLt, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = PurpleLt, modifier = Modifier.size(16.dp)) }
                    Text("${item.quantity}", color = TextPri, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = PurpleLt, modifier = Modifier.size(16.dp)) }
                }
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.DeleteOutline, null, tint = RedColor) }
        }
    }
}