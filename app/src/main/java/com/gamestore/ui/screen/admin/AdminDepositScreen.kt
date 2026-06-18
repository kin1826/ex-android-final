package com.gamestore.ui.screen.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.data.remote.DepositDto
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDepositScreen(onBack: () -> Unit, vm: UserViewModel = hiltViewModel()) {
    val depositsState by vm.adminDeposits.collectAsStateWithLifecycle()
    val actionState by vm.adminActionState.collectAsStateWithLifecycle()
    var selectedRequest by remember { mutableStateOf<DepositDto?>(null) }

    LaunchedEffect(Unit) { vm.loadAdminDeposits() }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Duyệt nạp tiền", color = TextPri, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPri)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = depositsState) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = PurpleLt)
                is UiState.Error -> Text(s.message, color = TextMuted, modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    val pendingList = s.data.filter { it.status == "PENDING" }
                    if (pendingList.isEmpty()) {
                        Text("Không có yêu cầu nào đang chờ", color = TextMuted, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingList, key = { it.id }) { deposit ->
                                DepositRequestItem(deposit) { selectedRequest = deposit }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Duyệt
    if (selectedRequest != null) {
        ApproveDepositDialog(
            deposit = selectedRequest!!,
            onDismiss = { selectedRequest = null },
            onConfirm = { action, note ->
                vm.updateDepositStatus(selectedRequest!!.id, action, note)
                selectedRequest = null
            }
        )
    }
}

@Composable
fun DepositRequestItem(deposit: DepositDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(0.5.dp, DarkBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(deposit.userName ?: "User #${deposit.userId}", fontWeight = FontWeight.Bold, color = TextPri)
                Text(deposit.userEmail ?: "", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.height(4.dp))
                Text("Nội dung: ${deposit.memo}", fontSize = 13.sp, color = PurpleLt, fontWeight = FontWeight.Medium)
            }
            Text(deposit.amount.toVND(), fontWeight = FontWeight.ExtraBold, color = GreenColor, fontSize = 16.sp)
        }
    }
}

@Composable
fun ApproveDepositDialog(deposit: DepositDto, onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }
    
    var showRejectNote by remember { mutableStateOf(false) }
    var adminNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurf,
        title = { Text(if (showRejectNote) "Lý do từ chối" else "Duyệt nạp tiền", color = TextPri) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!showRejectNote) {
                    Text("Người nạp: ${deposit.userName}", color = TextPri)
                    Text("Số tiền: ${deposit.amount.toVND()}", color = GreenColor, fontWeight = FontWeight.Bold)
                    Text("Nội dung chuyển khoản: ${deposit.memo}", color = PurpleLt)
                    
                    HorizontalDivider(color = DarkBorder)
                    
                    Text("Vui lòng kiểm tra kỹ tài khoản ngân hàng:", fontSize = 12.sp, color = TextMuted)
                    
                    CheckRow("Đã nhận đúng ${deposit.amount.toVND()}", check1) { check1 = it }
                    CheckRow("Người gửi khớp tên ${deposit.userName}", check2) { check2 = it }
                    CheckRow("Nội dung chuyển khoản khớp mã ${deposit.memo}", check3) { check3 = it }
                } else {
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        placeholder = { Text("Nhập lý do từ chối (ví dụ: Sai nội dung)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPri,
                            unfocusedTextColor = TextPri,
                            focusedBorderColor = RedColor
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (!showRejectNote) {
                Button(
                    onClick = { onConfirm("approve", null) },
                    enabled = check1 && check2 && check3,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenColor)
                ) { Text("Duyệt ngay") }
            } else {
                Button(
                    onClick = { onConfirm("reject", adminNote) },
                    colors = ButtonDefaults.buttonColors(containerColor = RedColor)
                ) { Text("Xác nhận từ chối") }
            }
        },
        dismissButton = {
            if (!showRejectNote) {
                TextButton(onClick = { showRejectNote = true }) {
                    Text("Từ chối", color = RedColor)
                }
            } else {
                TextButton(onClick = { showRejectNote = false }) {
                    Text("Quay lại", color = TextMuted)
                }
            }
        }
    )
}

@Composable
fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onCheckedChange(!checked) }) {
        Checkbox(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = PurpleLt)
        )
        Text(label, color = TextPri, fontSize = 13.sp)
    }
}
