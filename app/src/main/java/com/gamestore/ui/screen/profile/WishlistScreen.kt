package com.gamestore.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamestore.model.UiState
import com.gamestore.ui.screen.home.GameListItem
import com.gamestore.ui.theme.DarkBg
import com.gamestore.ui.theme.DarkSurf
import com.gamestore.ui.theme.TextMuted
import com.gamestore.ui.theme.TextPri
import com.gamestore.viewmodel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onBack: () -> Unit,
    onGameClick: (Int) -> Unit,
    vm: WishlistViewModel = hiltViewModel()
) {
    val state by vm.wishlistState.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Danh sách yêu thích", color = TextPri, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = TextPri)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.loadWishlist() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val s = state) {
                    is UiState.Loading -> if (!isRefreshing) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is UiState.Error -> Text(
                        text = s.message,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("❤️", fontSize = 64.sp)
                                Spacer(Modifier.height(16.dp))
                                Text("Chưa có game nào trong danh sách yêu thích", color = TextMuted)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(s.data, key = { it.id }) { game ->
                                    GameListItem(
                                        game = game,
                                        onClick = { onGameClick(game.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
