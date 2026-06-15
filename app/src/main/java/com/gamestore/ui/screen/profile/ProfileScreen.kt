package com.gamestore.ui.screen.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    onOrderHistoryClick: () -> Unit,
    onDepositClick: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
) {
    val isLoggedIn  by vm.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by vm.currentUser.collectAsStateWithLifecycle()

    // Mỗi khi màn hình này hiện lên (ví dụ sau khi popBackStack từ Login), ta sẽ refresh dữ liệu
    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title  = { Text("Tài khoản", color = TextPri, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (!isLoggedIn || currentUser == null) {
                // Chưa đăng nhập (giữ nguyên)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("👤", fontSize = 64.sp)
                    Text("Chưa đăng nhập", fontSize = 18.sp, color = TextPri, fontWeight = FontWeight.Bold)
                    Text("Đăng nhập để xem lịch sử mua hàng", color = TextMuted, fontSize = 13.sp)
                    Button(
                        onClick  = onLoginClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape    = RoundedCornerShape(12.dp),
                    ) { Text("Đăng nhập / Đăng ký", fontWeight = FontWeight.SemiBold) }
                }
            } else {
                val user = currentUser!!

                // Avatar + info (giữ nguyên)
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape    = CircleShape,
                        color    = Purple.copy(alpha = 0.2f),
                        border   = BorderStroke(2.dp, PurpleLt),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                user.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                                fontSize   = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color      = PurpleLt,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user.displayName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPri)
                    Text(user.email, fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color  = Purple.copy(alpha = 0.15f),
                        shape  = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, PurpleLt.copy(alpha = 0.3f)),
                    ) {
                        Text(
                            "🏅 ${user.membershipLevel}",
                            color      = PurpleLt,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        )
                    }
                }

                // Stats - BIẾN CARD SỐ DƯ THÀNH NÚT BẤM
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard("💰 Số dư ví", user.walletBalance.toVND(), Modifier.weight(1f), onClick = onDepositClick)
                    StatCard("⭐ Điểm", "${user.points} điểm", Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    "Quản lý tài khoản",
                    color      = TextMuted,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                MenuRow(Icons.Default.Receipt,     "Lịch sử đơn hàng",  onOrderHistoryClick)
                MenuRow(Icons.Default.Favorite,    "Yêu thích",          {})
                MenuRow(Icons.Default.Games,       "Game đã mua",        {})
                // THÊM NÚT NẠP TIỀN Ở ĐÂY
                MenuRow(Icons.Default.AddCard,     "Nạp tiền vào ví",    onDepositClick)

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))

                MenuRow(
                    icon    = Icons.AutoMirrored.Filled.Logout,
                    label   = "Đăng xuất",
                    onClick = { vm.logout() },
                    color   = RedColor,
                )
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(
        modifier = modifier,
        onClick  = { onClick?.invoke() },
        enabled  = onClick != null,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkCard),
        border   = BorderStroke(0.5.dp, DarkBorder),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = PurpleLt, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = TextPri,
) {
    Surface(
        onClick  = onClick,
        color    = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, color = color, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}