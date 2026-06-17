package com.gamestore.ui.screen.order

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.model.*
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.OrderViewModel

@Composable
fun OrderSuccessScreen(
    orderId: Int,
    onGoHome: () -> Unit,
    onViewOrders: () -> Unit,
) {
    Scaffold(containerColor = DarkBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape    = RoundedCornerShape(50.dp),
                color    = GreenColor.copy(alpha = 0.15f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, null, tint = GreenColor, modifier = Modifier.size(60.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Đặt hàng thành công!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPri)
            Spacer(Modifier.height(8.dp))
            Text("Mã đơn: #$orderId", fontSize = 14.sp, color = TextMuted)
            Text("Cảm ơn bạn đã mua tại GameStore!", fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(40.dp))
            Button(
                onClick  = onViewOrders,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Purple),
                shape    = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Receipt, null)
                Spacer(Modifier.width(8.dp))
                Text("Xem đơn hàng", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick  = onGoHome,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border   = BorderStroke(1.dp, DarkBorder),
                shape    = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Home, null, tint = TextMuted)
                Spacer(Modifier.width(8.dp))
                Text("Về trang chủ", color = TextMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    vm: OrderViewModel = hiltViewModel(),
) {
    val state by vm.orders.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đơn hàng", color = TextPri, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPri)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf),
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Box(Modifier.fillMaxSize()) {
                when (val s = state) {
                    is UiState.Loading -> if (!isRefreshing) Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = PurpleLt)
                    }
                    is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😕", fontSize = 48.sp)
                            Text(s.message, color = TextMuted)
                        }
                    }
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text("📦", fontSize = 56.sp)
                                    Text("Chưa có đơn hàng", color = TextMuted, fontSize = 16.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(s.data, key = { it.id }) { order -> OrderCard(order) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val statusColor = when (order.status) {
        "COMPLETED"  -> GreenColor
        "CANCELLED"  -> RedColor
        "PROCESSING" -> AmberColor
        else         -> AmberColor
    }
    val statusLabel = when (order.status) {
        "COMPLETED"  -> "Hoàn thành"
        "CANCELLED"  -> "Đã hủy"
        "PROCESSING" -> "Đang xử lý"
        else         -> "Chờ xử lý"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkCard),
        border   = BorderStroke(0.5.dp, DarkBorder),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Đơn #${order.id}", fontWeight = FontWeight.Bold, color = TextPri, fontSize = 15.sp)
                Surface(color = statusColor.copy(0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(Modifier.height(10.dp))
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), Arrangement.SpaceBetween) {
                    Text("• ${item.gameTitle}  x${item.quantity}", color = TextMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text((item.price * item.quantity).toVND(), color = TextPri, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text(order.createdAt.take(10), color = TextMuted, fontSize = 11.sp)
                    Text(order.paymentMethod, color = TextMuted, fontSize = 11.sp)
                }
                Text(order.total.toVND(), fontWeight = FontWeight.Bold, color = PurpleLt, fontSize = 16.sp)
            }
        }
    }
}