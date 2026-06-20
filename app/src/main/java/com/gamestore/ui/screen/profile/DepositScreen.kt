package com.gamestore.ui.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamestore.data.remote.DepositDto
import com.gamestore.data.remote.DepositResponse
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(onBack: () -> Unit, vm: UserViewModel = hiltViewModel()) {
    var amountText by remember { mutableStateOf("") }
    val requestState by vm.depositRequestState.collectAsStateWithLifecycle()
    val myDeposits by vm.myDeposits.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Nạp tiền vào ví", color = TextPri) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                actions = {
                    IconButton(onClick = { 
                        vm.loadMyDeposits()
                        showHistory = true 
                    }) {
                        Icon(Icons.Default.History, "Lịch sử", tint = PurpleLt)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (requestState is UiState.Success) {
                val data = (requestState as UiState.Success<DepositResponse>).data
                DepositInfoView(data) {
                    clipboard.setText(AnnotatedString(it))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("💰", fontSize = 64.sp)
                    Text("Nhập số tiền muốn nạp", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                        label = { Text("Số tiền (VNĐ)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = PurpleLt) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleLt, unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPri, unfocusedTextColor = TextPri
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("50000", "100000", "500000").forEach { valAmt ->
                            Button(
                                onClick = { amountText = valAmt },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                                border = if (amountText == valAmt) BorderStroke(1.dp, PurpleLt) else null
                            ) { Text(valAmt, fontSize = 12.sp, color = TextPri) }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { 
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt >= 10000) vm.requestDeposit(amt)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = amountText.isNotBlank() && requestState !is UiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (requestState is UiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Tiếp tục", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showHistory) {
        ModalBottomSheet(
            onDismissRequest = { showHistory = false },
            containerColor = DarkSurf,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
        ) {
            Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    "Lịch sử nạp tiền", 
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPri,
                    fontWeight = FontWeight.Bold
                )
                
                when (val s = myDeposits) {
                    is UiState.Loading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                        CircularProgressIndicator(color = PurpleLt)
                    }
                    is UiState.Error -> Text(s.message, color = RedColor, modifier = Modifier.padding(16.dp))
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            Text("Chưa có giao dịch nào", color = TextMuted, modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally))
                        } else {
                            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(s.data, key = { it.id }) { item ->
                                    HistoryItem(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(item: DepositDto) {
    val statusColor = when (item.status) {
        "APPROVED" -> GreenColor
        "REJECTED" -> RedColor
        else -> AmberColor
    }
    val statusText = when (item.status) {
        "APPROVED" -> "Thành công"
        "REJECTED" -> "Bị từ chối"
        else -> "Đang chờ"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(0.5.dp, DarkBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.amount.toVND(), fontWeight = FontWeight.Bold, color = TextPri)
                val dateStr = if (item.createdAt.length >= 10) item.createdAt.take(10) else item.createdAt
                Text(dateStr, fontSize = 11.sp, color = TextMuted)
                Text("Mã: ${item.memo}", fontSize = 10.sp, color = TextMuted)
            }
            Surface(
                color = statusColor.copy(0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    statusText, 
                    color = statusColor, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DepositInfoView(data: DepositResponse, onCopy: (String) -> Unit) {
    // Định dạng: https://img.vietqr.io/image/{BANK}-{ACCOUNT}-{TEMPLATE}.png?amount={AMOUNT}&addInfo={CONTENT}
    val qrUrl = "https://img.vietqr.io/image/${data.bankInfo.bank_name}-${data.bankInfo.account_number}-compact2.png?amount=${data.amount.toInt()}&addInfo=${data.memo}"
    
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("QUÉT MÃ ĐỂ THANH TOÁN", fontWeight = FontWeight.ExtraBold, color = TextPri, fontSize = 18.sp)
        
        Card(
            shape = RoundedCornerShape(16.dp), 
            border = BorderStroke(2.dp, PurpleLt),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            AsyncImage(
                model = qrUrl,
                contentDescription = "VietQR",
                modifier = Modifier.size(280.dp).background(Color.White).padding(12.dp)
            )
        }
        
        Text("Chủ TK: ${data.bankInfo.account_name}", fontWeight = FontWeight.Bold, color = TextPri)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Purple.copy(0.1f)),
            border = BorderStroke(1.dp, PurpleLt),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NỘI DUNG CHUYỂN KHOẢN", fontSize = 11.sp, color = PurpleLt, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.memo, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPri)
                    IconButton(onClick = { onCopy(data.memo) }) {
                        Icon(Icons.Default.ContentCopy, null, tint = PurpleLt)
                    }
                }
            }
        }

        Button(
            onClick = { /* User has transferred */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenColor)
        ) {
            Text("Tôi đã chuyển khoản thành công", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, onCopy: ((String) -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextMuted, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = TextPri, fontWeight = FontWeight.SemiBold)
            if (onCopy != null) {
                IconButton(onClick = { onCopy(value) }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.ContentCopy, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
