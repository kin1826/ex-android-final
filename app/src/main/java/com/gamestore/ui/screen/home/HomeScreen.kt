package com.gamestore.ui.screen.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamestore.model.*
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val featured    by vm.featured.collectAsStateWithLifecycle()
    val hotDeals    by vm.hotDeals.collectAsStateWithLifecycle()
    val newReleases by vm.newReleases.collectAsStateWithLifecycle()
    val categories  by vm.categories.collectAsStateWithLifecycle()
    val selectedGenre by vm.selectedGenre.collectAsStateWithLifecycle()
    val cartCount   by vm.cartCount.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val searchText by vm.searchText.collectAsStateWithLifecycle()
    val searchResult by vm.searchResult.collectAsStateWithLifecycle()

    val isSearching = searchText.isNotBlank()

    // Tự động làm mới khi mở Trang chủ
    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "⚡ GAMESTORE",
                        fontWeight = FontWeight.ExtraBold,
                        color = PurpleLt,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                },
                actions = {
                    BadgedBox(badge = {
                        if (cartCount > 0) Badge(containerColor = RedColor) { Text("$cartCount") }
                    }) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, null, tint = TextPri)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf),
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ================= SEARCH BOX =================
                item {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { vm.onSearchChange(it) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (isSearching) {
                                IconButton(onClick = { vm.onSearchChange("") }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        },
                        placeholder = { Text("Tìm game...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleLt,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                }

                if (isSearching) {
                    // ================= SEARCH MODE =================
                    when (val res = searchResult) {
                        is UiState.Loading -> item {
                            Box(Modifier.fillMaxWidth().height(150.dp), Alignment.Center) {
                                CircularProgressIndicator(color = PurpleLt)
                            }
                        }
                        is UiState.Error -> item {
                            Text(res.message, color = TextMuted, modifier = Modifier.padding(16.dp))
                        }
                        is UiState.Success -> {
                            if (res.data.isEmpty()) {
                                item {
                                    Text("Không tìm thấy game phù hợp", color = TextMuted, modifier = Modifier.padding(16.dp))
                                }
                            } else {
                                items(res.data, key = { it.id }) { game ->
                                    GameListItem(game, { onGameClick(game.id) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                                }
                            }
                        }
                    }
                } else {
                    // ================= NORMAL MODE =================
                    if (categories.isNotEmpty()) {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(categories, key = { it.id }) { cat ->
                                    FilterChip(
                                        selected = selectedGenre == cat.name,
                                        onClick = { vm.onGenreClick(cat.name) },
                                        label = { Text("${cat.iconEmoji} ${cat.name}", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurpleLt,
                                            selectedLabelColor = Color.White,
                                            containerColor = DarkCard,
                                            labelColor = TextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }

                    item { SectionTitle("⭐ Nổi bật") }
                    item { GameRow(featured, onGameClick) }

                    item { SectionTitle("🔥 Giảm giá hot") }
                    item { GameRow(hotDeals, onGameClick) }

                    item { SectionTitle("🆕 Mới phát hành") }
                    when (val s = newReleases) {
                        is UiState.Success -> items(s.data, key = { it.id }) { game ->
                            GameListItem(game, { onGameClick(game.id) }, Modifier.padding(horizontal = 16.dp, vertical = 3.dp))
                        }
                        is UiState.Loading -> item {
                            Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                                CircularProgressIndicator(color = PurpleLt, modifier = Modifier.size(30.dp))
                            }
                        }
                        is UiState.Error -> item {
                            Text(s.message, color = TextMuted, modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, color = TextPri, fontSize = 16.sp,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
}

@Composable
fun GameRow(state: UiState<List<Game>>, onClick: (Int) -> Unit) {
    when (state) {
        is UiState.Loading -> LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(4) { SkeletonCard() } }
        is UiState.Success -> LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.data, key = { it.id }) { game -> GameCard(game) { onClick(game.id) } }
        }
        is UiState.Error -> Box(Modifier.fillMaxWidth().height(150.dp), Alignment.Center) { Text(state.message, color = TextMuted) }
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        onClick  = onClick,
        modifier = Modifier.width(160.dp).height(240.dp), // Tăng chiều cao để thoải mái hơn
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkCard),
        border   = BorderStroke(0.5.dp, DarkBorder),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box {
                AsyncImage(
                    model              = game.thumbnailUrl.ifBlank { "https://placehold.co/200x120/1A1A2E/A855F7?text=${game.title}" },
                    contentDescription = game.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxWidth().height(110.dp),
                )
                if (game.hasDiscount) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(5.dp),
                        color    = RedColor,
                        shape    = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            "-${game.discountPercent}%",
                            color      = Color.White,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                }
                if (game.isHot) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(5.dp),
                        color    = Color(0xFFF97316),
                        shape    = RoundedCornerShape(4.dp),
                    ) {
                        Text("🔥 Hot", fontSize = 9.sp, color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
            }
            Column(Modifier.padding(10.dp).weight(1f)) {
                Text(
                    game.title,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPri,
                    maxLines   = 2, // Cho phép hiển thị 2 dòng tiêu đề
                    overflow   = TextOverflow.Ellipsis,
                    fontSize   = 13.sp,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(game.genre, color = TextMuted, fontSize = 11.sp, maxLines = 1)
                
                Spacer(Modifier.weight(1f))
                
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically,
                ) {
                    Column {
                        if (game.hasDiscount) {
                            Text(
                                game.originalPrice.toVND(),
                                fontSize       = 9.sp,
                                color          = TextMuted,
                                textDecoration = TextDecoration.LineThrough,
                            )
                        }
                        Text(
                            game.finalPrice.toVND(),
                            fontWeight = FontWeight.Bold,
                            color      = PurpleLt,
                            fontSize   = 13.sp,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = AmberColor, modifier = Modifier.size(12.dp))
                        Text("%.1f".format(game.rating), fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun GameListItem(game: Game, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard), border = BorderStroke(0.5.dp, DarkBorder)) {
        Row(Modifier.padding(10.dp).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = game.thumbnailUrl.ifBlank { "https://placehold.co/70x70/1A1A2E/white?text=🎮" },
                contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(game.title, fontWeight = FontWeight.SemiBold, color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(game.genre, color = TextMuted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = AmberColor, modifier = Modifier.size(12.dp))
                    Text(" %.1f".format(game.rating), fontSize = 11.sp, color = TextMuted)
                }
            }
            Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                if (game.hasDiscount) Text(game.originalPrice.toVND(), fontSize = 10.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough)
                Text(game.finalPrice.toVND(), fontWeight = FontWeight.Bold, color = PurpleLt)
            }
        }
    }
}

@Composable
fun SkeletonCard() {
    Card(Modifier.width(155.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column {
            Box(Modifier.fillMaxWidth().height(105.dp).background(DarkBorder))
            Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.fillMaxWidth(0.8f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(DarkBorder))
                Box(Modifier.fillMaxWidth(0.5f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(DarkBorder))
                Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(DarkBorder))
            }
        }
    }
}
