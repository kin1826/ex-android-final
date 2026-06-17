package com.gamestore.ui.screen.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamestore.data.remote.AdminStatsDto
import com.gamestore.data.remote.AdminTopGameDto
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    onManageGames: () -> Unit,
    onManageCategories: () -> Unit,
    onManageUsers: () -> Unit,
    vm: AdminViewModel = hiltViewModel()
) {
    val statsState by vm.stats.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadStats() }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", color = TextPri, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        }
    ) { padding ->
        when (val s = statsState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = PurpleLt) }
            is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = TextMuted) }
            is UiState.Success -> {
                val stats = s.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Thống kê tổng quan
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Người dùng", "${stats.totalUsers}", Icons.Default.People, PurpleLt, Modifier.weight(1f).clickable { onManageUsers() })
                            StatCard("Đơn hàng", "${stats.totalOrders}", Icons.Default.Receipt, Color(0xFF3B82F6), Modifier.weight(1f))
                        }
                    }
                    item {
                        StatCard("Tổng doanh thu", stats.totalRevenue.toVND(), Icons.Default.MonetizationOn, GreenColor, Modifier.fillMaxWidth())
                    }

                    // 2. Các nút chức năng
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminMenuButton("Game", Icons.Default.Games, Purple, onManageGames, Modifier.weight(1f))
                            AdminMenuButton("Thể loại", Icons.Default.Category, Color(0xFFF59E0B), onManageCategories, Modifier.weight(1f))
                            AdminMenuButton("User", Icons.Default.Person, Color(0xFF10B981), onManageUsers, Modifier.weight(1f))
                        }
                    }

                    // 3. Top Game bán chạy
                    item {
                        Text("TOP GAME BÁN CHẠY", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    items(stats.topGames) { game ->
                        TopGameItem(game)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(0.5.dp, DarkBorder)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(color.copy(0.1f), RoundedCornerShape(10.dp)), Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPri)
                Text(label, fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
fun AdminMenuButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkCard,
        border = BorderStroke(0.5.dp, DarkBorder)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = TextPri, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun TopGameItem(game: AdminTopGameDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(0.5.dp, DarkBorder)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = game.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.title, fontWeight = FontWeight.Bold, color = TextPri, fontSize = 14.sp)
                Text(game.genre, color = TextMuted, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${game.salesCount} lượt tải", color = PurpleLt, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(game.price.toVND(), color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}