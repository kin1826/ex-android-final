package com.gamestore.ui.screen.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.model.UiState
import com.gamestore.model.User
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserScreen(
    onBack: () -> Unit,
    vm: AdminViewModel = hiltViewModel()
) {
    val usersState by vm.allUsers.collectAsStateWithLifecycle()
    val adminState by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    
    var searchQuery by remember { mutableStateOf("") }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var showWalletDialog by remember { mutableStateOf<User?>(null) }
    var userToConfirmAction by remember { mutableStateOf<Pair<User, String>?>(null) }

    LaunchedEffect(Unit) { vm.loadAllUsers() }

    LaunchedEffect(adminState) {
        if (adminState is UiState.Success) {
            snackbar.showSnackbar((adminState as UiState.Success).data)
            vm.clearState()
        } else if (adminState is UiState.Error) {
            snackbar.showSnackbar((adminState as UiState.Error).message)
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Người dùng", color = TextPri, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Thanh tìm kiếm
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    vm.loadAllUsers(it)
                },
                placeholder = { Text("Tìm theo tên, email...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PurpleLt) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurf,
                    unfocusedContainerColor = DarkSurf,
                    focusedBorderColor = PurpleLt
                ),
                shape = RoundedCornerShape(12.dp)
            )

            when (val s = usersState) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = PurpleLt) }
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.data, key = { it.id }) { user ->
                            AdminUserItem(
                                user = user,
                                onAction = { action ->
                                    when(action) {
                                        "wallet" -> showWalletDialog = user
                                        "status" -> vm.updateUserStatus(user.id, isAdmin = !user.isAdmin)
                                        "lock"   -> userToConfirmAction = user to if(user.isActive) "lock" else "unlock"
                                        "reset"  -> userToConfirmAction = user to "reset"
                                    }
                                }
                            )
                        }
                    }
                }
                is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = TextMuted) }
            }
        }
    }

    // Dialog nạp tiền
    if (showWalletDialog != null) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showWalletDialog = null },
            containerColor = DarkSurf,
            title = { Text("Cập nhật ví: ${showWalletDialog?.username}", color = TextPri) },
            text = {
                Column {
                    Text("Số dư hiện tại: ${showWalletDialog?.walletBalance?.toVND()}", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Số tiền cộng thêm") },
                        placeholder = { Text("VD: 50000 hoặc -20000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val valAmount = amount.toDoubleOrNull() ?: 0.0
                    showWalletDialog?.let { vm.updateWallet(it.id, valAmount) }
                    showWalletDialog = null
                }) { Text("Cập nhật") }
            },
            dismissButton = { TextButton(onClick = { showWalletDialog = null }) { Text("Hủy") } }
        )
    }

    // Dialog xác nhận Reset Password / Khóa
    if (userToConfirmAction != null) {
        val (user, type) = userToConfirmAction!!
        val title = when(type) {
            "reset" -> "Reset mật khẩu?"
            "lock" -> "Khóa tài khoản?"
            else -> "Mở khóa tài khoản?"
        }
        val msg = when(type) {
            "reset" -> "Mật khẩu sẽ được đưa về \"123456\". Bạn chắc chứ?"
            "lock" -> "Người dùng này sẽ không thể đăng nhập. Tiếp tục?"
            else -> "Người dùng sẽ có thể đăng nhập lại bình thường. Tiếp tục?"
        }
        
        AlertDialog(
            onDismissRequest = { userToConfirmAction = null },
            containerColor = DarkSurf,
            title = { Text(title, color = TextPri) },
            text = { Text(msg, color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        when(type) {
                            "reset" -> vm.resetPassword(user.id)
                            "lock" -> vm.updateUserStatus(user.id, isActive = false)
                            "unlock" -> vm.updateUserStatus(user.id, isActive = true)
                        }
                        userToConfirmAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if(type == "unlock") GreenColor else RedColor)
                ) { Text("Xác nhận") }
            },
            dismissButton = { TextButton(onClick = { userToConfirmAction = null }) { Text("Hủy") } }
        )
    }
}

@Composable
fun AdminUserItem(user: User, onAction: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(0.5.dp, if(user.isAdmin) PurpleLt.copy(0.5f) else DarkBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar giả
                Box(Modifier.size(44.dp).background(Purple.copy(0.2f), CircleShape), Alignment.Center) {
                    Text(user.username.take(1).uppercase(), color = PurpleLt, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.displayName, fontWeight = FontWeight.Bold, color = TextPri)
                        if (user.isAdmin) {
                            Surface(Modifier.padding(start = 8.dp), color = PurpleLt.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                                Text("ADMIN", color = PurpleLt, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Black)
                            }
                        }
                        if (!user.isActive) {
                            Surface(Modifier.padding(start = 8.dp), color = RedColor.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                                Text("LOCKED", color = RedColor, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Text(user.email, color = TextMuted, fontSize = 12.sp)
                }
                Text(user.walletBalance.toVND(), color = GreenColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Divider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = DarkBorder)
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                UserActionButton("Ví", Icons.Default.AccountBalanceWallet, Color(0xFFF59E0B)) { onAction("wallet") }
                UserActionButton(if(user.isAdmin) "Hạ quyền" else "Nâng quyền", Icons.Default.AdminPanelSettings, PurpleLt) { onAction("status") }
                UserActionButton("Reset Pass", Icons.Default.LockReset, Color(0xFF3B82F6)) { onAction("reset") }
                UserActionButton(if(user.isActive) "Khóa" else "Mở khóa", if(user.isActive) Icons.Default.Block else Icons.Default.LockOpen, RedColor) { onAction("lock") }
            }
        }
    }
}

@Composable
fun UserActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        Modifier.clickable { onClick() }.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Text(label, color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
