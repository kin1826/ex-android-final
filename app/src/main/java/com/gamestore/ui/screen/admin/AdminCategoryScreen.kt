package com.gamestore.ui.screen.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryScreen(onBack: () -> Unit, vm: AdminViewModel = hiltViewModel()) {
    val categoriesState by vm.allCategories.collectAsStateWithLifecycle()
    val adminState by vm.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<com.gamestore.data.remote.CategoryDto?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.loadAllCategories() }

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
                title = { Text("Quản lý thể loại", color = TextPri) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Purple) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        when (val s = categoriesState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = PurpleLt) }
            is UiState.Success -> {
                LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.data) { cat ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = BorderStroke(0.5.dp, DarkBorder)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.iconEmoji, fontSize = 24.sp)
                                Spacer(Modifier.width(16.dp))
                                Text(cat.name, color = TextPri, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                IconButton(onClick = { categoryToDelete = cat }) {
                                    Icon(Icons.Default.Delete, null, tint = RedColor)
                                }
                            }
                        }
                    }
                }
            }
            is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = TextMuted) }
        }
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = DarkSurf,
            title = { Text("Xác nhận xóa", color = TextPri) },
            text = { Text("Bạn có chắc chắn muốn xóa thể loại \"${categoryToDelete?.name}\" không?", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let { vm.deleteCategory(it.id) }
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedColor)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Hủy", color = TextPri)
                }
            }
        )
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("🎮") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurf,
            title = { Text("Thêm thể loại", color = TextPri) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên thể loại") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt))
                    OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("Icon (Emoji)") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt))
                }
            },
            confirmButton = {
                Button(onClick = { vm.addCategory(name, icon); showAddDialog = false }, enabled = name.isNotBlank()) { Text("Thêm") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Hủy") } }
        )
    }
}