package com.gamestore.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.model.Game
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGameEditScreen(
    game: Game? = null,
    onBack: () -> Unit,
    vm: AdminViewModel = hiltViewModel()
) {
    val isEdit = game != null
    var title by remember { mutableStateOf(game?.title ?: "") }
    var description by remember { mutableStateOf(game?.description ?: "") }
    var genre by remember { mutableStateOf(game?.genre ?: "") }
    var developer by remember { mutableStateOf(game?.developer ?: "") }
    var price by remember { mutableStateOf(game?.price?.toLong()?.toString() ?: "0") }
    var originalPrice by remember { mutableStateOf(game?.originalPrice?.toLong()?.toString() ?: "0") }
    var discountPercent by remember { mutableStateOf(game?.discountPercent?.toString() ?: "0") }
    var thumbnailUrl by remember { mutableStateOf(game?.thumbnailUrl ?: "") }
    
    var publisher by remember { mutableStateOf(game?.publisher ?: "") }
    var releaseDate by remember { mutableStateOf(game?.releaseDate ?: "") }
    var platforms by remember { mutableStateOf(game?.platforms ?: "PC (Windows)") }
    var downloadSize by remember { mutableStateOf(game?.downloadSize ?: "") }
    
    var isFeatured by remember { mutableStateOf(game?.isFeatured ?: false) }
    var isHot by remember { mutableStateOf(game?.isHot ?: false) }
    var isNew by remember { mutableStateOf(game?.isNew ?: false) }

    val adminState by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val categoriesState by vm.allCategories.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadAllCategories() }

    LaunchedEffect(adminState) {
        if (adminState is UiState.Success) {
            onBack()
        } else if (adminState is UiState.Error) {
            snackbar.showSnackbar((adminState as UiState.Error).message)
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Chỉnh sửa Game" else "Thêm Game mới", color = TextPri) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val updatedGame = (game ?: Game()).copy(
                        title = title,
                        description = description,
                        genre = genre,
                        developer = developer,
                        price = price.toDoubleOrNull() ?: 0.0,
                        originalPrice = originalPrice.toDoubleOrNull() ?: 0.0,
                        discountPercent = discountPercent.toIntOrNull() ?: 0,
                        publisher = publisher,
                        releaseDate = releaseDate,
                        platforms = platforms,
                        downloadSize = downloadSize,
                        thumbnailUrl = thumbnailUrl,
                        isFeatured = isFeatured,
                        isHot = isHot,
                        isNew = isNew
                    )
                    vm.saveGame(updatedGame, isEdit) { onBack() }
                },
                containerColor = Purple,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Save, null) },
                text = { Text("Lưu Game") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề game") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả ngắn") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
            )

            // Chọn thể loại
            val categories = (categoriesState as? UiState.Success)?.data ?: emptyList()
            var showGenreMenu by remember { mutableStateOf(false) }
            
            Box {
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Thể loại") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showGenreMenu = true }) {
                            Icon(Icons.Default.ArrowBack, null, modifier = Modifier.rotateIcon(-90f))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
                )
                DropdownMenu(
                    expanded = showGenreMenu,
                    onDismissRequest = { showGenreMenu = false },
                    modifier = Modifier.fillMaxWidth(0.9f).background(DarkSurf)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name, color = TextPri) },
                            onClick = {
                                genre = cat.name
                                showGenreMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = developer,
                onValueChange = { developer = it },
                label = { Text("Nhà phát triển") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
            )

            OutlinedTextField(
                value = publisher,
                onValueChange = { publisher = it },
                label = { Text("Nhà xuất bản") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nhập ngày tháng tự động thêm dấu -
                OutlinedTextField(
                    value = releaseDate,
                    onValueChange = { input ->
                        val clean = input.replace("-", "")
                        var formatted = ""
                        for (i in clean.indices) {
                            formatted += clean[i]
                            if ((i == 3 || i == 5) && i < clean.length - 1) {
                                formatted += "-"
                            }
                        }
                        if (formatted.length <= 10) releaseDate = formatted
                    },
                    label = { Text("Ngày phát hành (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, null, tint = PurpleLt)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
                )

                OutlinedTextField(
                    value = downloadSize,
                    onValueChange = { downloadSize = it },
                    label = { Text("Dung lượng") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("VD: 50 GB") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
                )
            }

            OutlinedTextField(
                value = platforms,
                onValueChange = { platforms = it },
                label = { Text("Nền tảng") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = originalPrice,
                    onValueChange = { originalPrice = it },
                    label = { Text("Giá gốc") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
                )
                OutlinedTextField(
                    value = discountPercent,
                    onValueChange = { discountPercent = it },
                    label = { Text("% Giảm") },
                    modifier = Modifier.weight(0.5f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
                )
            }

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá bán cuối (Tự tính)") },
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged { 
                        if (it.isFocused) {
                            val orig = originalPrice.toDoubleOrNull() ?: 0.0
                            val disc = discountPercent.toDoubleOrNull() ?: 0.0
                            val calculated = orig * (1 - disc / 100)
                            price = calculated.toLong().toString()
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
            )

            OutlinedTextField(
                value = thumbnailUrl,
                onValueChange = { thumbnailUrl = it },
                label = { Text("Link ảnh bìa (Thumbnail)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleLt)
            )

            Text("Tùy chọn hiển thị", color = PurpleLt, fontWeight = FontWeight.Bold)
            
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isFeatured = !isFeatured }) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Text("Game nổi bật (Featured)", color = TextPri)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isHot = !isHot }) {
                    Checkbox(checked = isHot, onCheckedChange = { isHot = it })
                    Text("Game hot/Khuyến mãi lớn", color = TextPri)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isNew = !isNew }) {
                    Checkbox(checked = isNew, onCheckedChange = { isNew = it })
                    Text("Game mới phát hành", color = TextPri)
                }
            }
            
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        releaseDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

// Helper để xoay icon
@Composable
fun Modifier.rotateIcon(degrees: Float) = this.rotate(degrees)

