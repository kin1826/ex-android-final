package com.gamestore.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(onBack: () -> Unit, vm: UserViewModel = hiltViewModel()) {
    var amountText by remember { mutableStateOf("") }
    val state by vm.depositState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            snackbar.showSnackbar("Nạp tiền thành công!")
            vm.clearState()
            onBack()
        } else if (state is UiState.Error) {
            snackbar.showSnackbar((state as UiState.Error).message)
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Nạp tiền vào ví", color = TextPri) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
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

            // Gợi ý các mức nạp
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("50000", "100000", "500000").forEach { valAmt ->
                    Button(
                        onClick = { amountText = valAmt },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        border = if (amountText == valAmt) androidx.compose.foundation.BorderStroke(1.dp, PurpleLt) else null
                    ) { Text(valAmt, fontSize = 12.sp, color = TextPri) }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { 
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) vm.deposit(amt)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = amountText.isNotBlank() && state !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state is UiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Xác nhận nạp", fontWeight = FontWeight.Bold)
            }
        }
    }
}