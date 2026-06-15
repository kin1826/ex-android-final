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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamestore.model.Game
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.AdminViewModel
import com.gamestore.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGameListScreen(
    onBack: () -> Unit,
    onEditGame: (Game) -> Unit,
    onAddGame: () -> Unit,
    adminVm: AdminViewModel = hiltViewModel()
) {
    // Lấy toàn bộ game từ AdminViewModel (không lọc)
    val gamesState by adminVm.allGames.collectAsStateWithLifecycle() 
    val adminState by adminVm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    
    var gameToDelete by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(Unit) { adminVm.loadAllGames() }

    LaunchedEffect(adminState) {
        if (adminState is UiState.Success) {
            snackbar.showSnackbar((adminState as UiState.Success).data)
            adminVm.clearState()
            adminVm.loadAllGames() // Load lại danh sách sau khi xóa thành công
        } else if (adminState is UiState.Error) {
            snackbar.showSnackbar((adminState as UiState.Error).message)
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý kho Game", color = TextPri, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPri) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddGame,
                containerColor = Purple,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Thêm Game mới") }
            )
        }
    ) { padding ->
        when (val s = gamesState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = PurpleLt) }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("DANH SÁCH GAME HIỆN TẠI (${s.data.size})", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    items(s.data, key = { it.id }) { game ->
                        AdminGameItem(
                            game = game,
                            onEdit = { onEditGame(game) },
                            onDelete = { gameToDelete = game }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
            is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = TextMuted) }
        }
    }

    // Popup xác nhận xóa
    if (gameToDelete != null) {
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            containerColor = DarkSurf,
            icon = { Icon(Icons.Default.Warning, null, tint = RedColor, modifier = Modifier.size(32.dp)) },
            title = { Text("Xác nhận xóa game?", color = TextPri, fontWeight = FontWeight.Bold) },
            text = { 
                Text("Bạn có chắc chắn muốn xóa game \"${gameToDelete?.title}\"? Hành động này không thể hoàn tác.", color = TextMuted) 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        gameToDelete?.let { adminVm.deleteGame(it.id) {} }
                        gameToDelete = null 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedColor)
                ) { Text("Xóa vĩnh viễn", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) { Text("Hủy", color = TextPri) }
            }
        )
    }
}

@Composable
fun AdminGameItem(game: Game, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(0.5.dp, DarkBorder)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = game.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(game.title, fontWeight = FontWeight.Bold, color = TextPri, maxLines = 1)
                Text(game.genre, fontSize = 12.sp, color = PurpleLt)
                Spacer(Modifier.height(4.dp))
                Text(game.finalPrice.toVND(), color = TextPri, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (game.isOwned) {
                    Text("✓ Đang có người dùng sở hữu", color = GreenColor, fontSize = 10.sp)
                }
            }
            Column {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Sửa", tint = Color.Gray) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteForever, "Xóa", tint = RedColor) }
            }
        }
    }
}