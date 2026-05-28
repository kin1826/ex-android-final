package com.gamestore.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onSuccess: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val state  by vm.state.collectAsStateWithLifecycle()
    var isLogin by remember { mutableStateOf(true) }

    LaunchedEffect(state) { if (state is UiState.Success) { vm.clearState(); onSuccess() } }

    Scaffold(containerColor = DarkBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Text("⚡", fontSize = 56.sp)
            Text("GAMESTORE", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = PurpleLt, letterSpacing = 3.sp)
            Text(if (isLogin) "Đăng nhập để tiếp tục" else "Tạo tài khoản mới", color = TextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(40.dp))
            Row(Modifier.fillMaxWidth().background(DarkCard, RoundedCornerShape(12.dp)).padding(4.dp)) {
                listOf("Đăng nhập" to true, "Đăng ký" to false).forEach { (label, flag) ->
                    Button(onClick = { isLogin = flag; vm.clearState() }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isLogin == flag) Purple else Color.Transparent, contentColor = if (isLogin == flag) Color.White else TextMuted),
                        shape = RoundedCornerShape(10.dp), elevation = ButtonDefaults.buttonElevation(0.dp)) { Text(label, fontWeight = FontWeight.SemiBold) }
                }
            }
            Spacer(Modifier.height(28.dp))
            if (isLogin) LoginForm(state is UiState.Loading, (state as? UiState.Error)?.message) { e, p -> vm.login(e, p) }
            else RegisterForm(state is UiState.Loading, (state as? UiState.Error)?.message) { u, e, p, n, ph -> vm.register(u, e, p, n, ph) }
        }
    }
}

@Composable
fun LoginForm(isLoading: Boolean, error: String?, onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    var show  by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AppTextField(email, { email = it }, "Email", Icons.Default.Email, KeyboardType.Email)
        AppTextField(pass,  { pass  = it }, "Mật khẩu", Icons.Default.Lock, isPassword = true, showPassword = show, onTogglePass = { show = !show })
        if (error != null) Text("⚠ $error", color = RedColor, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(RedColor.copy(0.1f), RoundedCornerShape(8.dp)).padding(12.dp))
        Button(onClick = { onLogin(email.trim(), pass) }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = email.isNotBlank() && pass.isNotBlank() && !isLoading, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(12.dp)) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun RegisterForm(isLoading: Boolean, error: String?, onRegister: (String, String, String, String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var name     by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var pass     by remember { mutableStateOf("") }
    var show     by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AppTextField(username, { username = it }, "Tên đăng nhập",  Icons.Default.Person)
        AppTextField(name,     { name     = it }, "Họ và tên",      Icons.Default.Badge)
        AppTextField(email,    { email    = it }, "Email",           Icons.Default.Email, KeyboardType.Email)
        AppTextField(phone,    { phone    = it }, "Số điện thoại",  Icons.Default.Phone, KeyboardType.Phone)
        AppTextField(pass,     { pass     = it }, "Mật khẩu",       Icons.Default.Lock, isPassword = true, showPassword = show, onTogglePass = { show = !show })
        if (error != null) Text("⚠ $error", color = RedColor, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(RedColor.copy(0.1f), RoundedCornerShape(8.dp)).padding(12.dp))
        Button(onClick = { onRegister(username.trim(), email.trim(), pass, name.trim(), phone.trim()) }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = username.isNotBlank() && email.isNotBlank() && pass.isNotBlank() && !isLoading, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(12.dp)) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Tạo tài khoản", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun AppTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text, isPassword: Boolean = false, showPassword: Boolean = false, onTogglePass: (() -> Unit)? = null) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label, color = TextMuted) },
        leadingIcon = { Icon(icon, null, tint = TextMuted) },
        trailingIcon = if (isPassword) { { IconButton(onClick = { onTogglePass?.invoke() }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextMuted) } } } else null,
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt, unfocusedBorderColor = DarkBorder, focusedTextColor = TextPri, unfocusedTextColor = TextPri, cursorColor = PurpleLt, focusedContainerColor = DarkCard, unfocusedContainerColor = DarkCard),
        shape = RoundedCornerShape(10.dp))
}