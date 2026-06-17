package com.gamestore.ui.screen.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
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
import com.gamestore.viewmodel.PriceRange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val cartCount   by vm.cartCount.collectAsStateWithLifecycle()

    val filteredGames    by vm.filteredGames.collectAsStateWithLifecycle()
    val selectedPrice    by vm.selectedPriceRange.collectAsStateWithLifecycle()
    val sortBy           by vm.sortBy.collectAsStateWithLifecycle()
    val onlyDiscounted   by vm.onlyDiscounted.collectAsStateWithLifecycle()
    val selectedCategory by vm.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery      by vm.searchQuery.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val priceRanges = remember {
        listOf(
            PriceRange("0 - 100k", 0.0, 100000.0),
            PriceRange("100k - 500k", 100000.0, 500000.0),
            PriceRange("500k - 1tr", 500000.0, 1000000.0),
            PriceRange("> 1tr", 1000000.0, null)
        )
    }

    val isFiltering = vm.isFiltering()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("⚡ GAMESTORE", fontWeight = FontWeight.ExtraBold, color = PurpleLt, fontSize = 20.sp, letterSpacing = 1.sp) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = if (isFiltering) PurpleLt else TextPri
                            )
                            if (isFiltering) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(PurpleLt, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .border(1.2.dp, DarkSurf, CircleShape)
                                )
                            }
                        }
                    }
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
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = DarkSurf,
                dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Bộ lọc & Sắp xếp", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (isFiltering) {
                            TextButton(onClick = { vm.clearAllFilters() }) {
                                Text("Thiết lập lại", color = RedColor, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    Text("Sắp xếp theo", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sortOptions = listOf(
                            "newest" to "Mới nhất",
                            "rating" to "Đánh giá cao",
                            "price_asc" to "Giá thấp -> cao",
                            "price_desc" to "Giá cao -> thấp"
                        )
                        sortOptions.forEach { (key, label) ->
                            FilterChip(
                                selected = sortBy == key,
                                onClick = { vm.setSortBy(key) },
                                label = { Text(label, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurpleLt.copy(0.2f),
                                    selectedLabelColor = PurpleLt,
                                    containerColor = DarkCard,
                                    labelColor = TextPri
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Khoảng giá", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        priceRanges.forEach { range ->
                            FilterChip(
                                selected = selectedPrice == range,
                                onClick = {
                                    if (selectedPrice == range) vm.setPriceRange(null)
                                    else vm.setPriceRange(range)
                                },
                                label = { Text(range.label, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurpleLt.copy(0.2f),
                                    selectedLabelColor = PurpleLt,
                                    containerColor = DarkCard,
                                    labelColor = TextPri
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Ưu đãi", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    FilterChip(
                        selected = onlyDiscounted,
                        onClick = { vm.toggleOnlyDiscounted() },
                        label = { Text("Đang giảm giá", fontSize = 13.sp) },
                        leadingIcon = { if(onlyDiscounted) Icon(Icons.Default.Check, null, Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedColor.copy(0.2f),
                            selectedLabelColor = RedColor,
                            containerColor = DarkCard,
                            labelColor = TextPri
                        )
                    )

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleLt),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Áp dụng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vm.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Tìm kiếm game...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { vm.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, null, tint = TextMuted)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleLt,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri
                    ),
                    singleLine = true
                )
            }

            if (categories.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { vm.setCategory(cat.name) },
                                label = { Text("${cat.iconEmoji} ${cat.name}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurpleLt.copy(0.2f),
                                    selectedLabelColor = PurpleLt,
                                    containerColor = DarkCard,
                                    labelColor = TextMuted
                                ),
                                border = if (isSelected) FilterChipDefaults.filterChipBorder(
                                    borderColor = PurpleLt, 
                                    borderWidth = 1.dp, 
                                    enabled = true, 
                                    selected = true
                                ) else null
                            )
                        }
                    }
                }
            }

            if (isFiltering) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Kết quả lọc", color = TextPri, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            val filterDesc = buildString {
                                val sorts = mapOf("rating" to "Đánh giá cao", "price_asc" to "Giá thấp", "price_desc" to "Giá cao")
                                sorts[sortBy]?.let { append(it) }
                                
                                if (onlyDiscounted) {
                                    if (isNotEmpty()) append(" • ")
                                    append("Giảm giá")
                                }
                                if (selectedCategory != null) {
                                    if (isNotEmpty()) append(" • ")
                                    append(selectedCategory)
                                }
                                if (selectedPrice != null) {
                                    if (isNotEmpty()) append(" • ")
                                    append(selectedPrice!!.label)
                                }
                                if (searchQuery.isNotBlank()) {
                                    if (isNotEmpty()) append(" • ")
                                    append("\"$searchQuery\"")
                                }
                            }
                            if (filterDesc.isNotEmpty()) {
                                Text(filterDesc, color = PurpleLt, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        IconButton(onClick = { vm.clearAllFilters() }) {
                            Icon(Icons.Default.Close, null, tint = RedColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                when (val s = filteredGames) {
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            item { 
                                Column(Modifier.fillMaxWidth().padding(top = 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = TextMuted)
                                    Spacer(Modifier.height(16.dp))
                                    Text("Không tìm thấy game nào phù hợp", color = TextMuted)
                                }
                            }
                        } else {
                            items(s.data, key = { it.id }) { game ->
                                GameListItem(game, { onGameClick(game.id) }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                            }
                        }
                    }
                    is UiState.Loading -> item { Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator(color = PurpleLt) } }
                    is UiState.Error -> item { Text(s.message, color = RedColor, modifier = Modifier.padding(16.dp)) }
                }
            } else {
                item { SectionTitle("⭐ Nổi bật") }
                item { GameRow(featured, onGameClick) }
                item { SectionTitle("🔥 Giảm giá hot") }
                item { GameRow(hotDeals, onGameClick) }
                item { SectionTitle("🆕 Mới phát hành") }

                when (val s = newReleases) {
                    is UiState.Success -> items(s.data, key = { it.id }) { game ->
                        GameListItem(game, { onGameClick(game.id) }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                    is UiState.Loading -> item { Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) { CircularProgressIndicator(color = PurpleLt, modifier = Modifier.size(24.dp)) } }
                    is UiState.Error   -> item { Text(s.message, color = TextMuted, modifier = Modifier.padding(16.dp)) }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, fontWeight = FontWeight.Bold, color = TextPri, fontSize = 17.sp,
        modifier = modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
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
        modifier = Modifier.width(160.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkCard),
        border   = BorderStroke(0.5.dp, DarkBorder),
    ) {
        Column {
            Box {
                AsyncImage(
                    model              = game.thumbnailUrl.ifBlank { "https://placehold.co/200x120/1A1A2E/A855F7?text=${game.title}" },
                    contentDescription = game.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxWidth().height(110.dp),
                )
                if (game.hasDiscount) {
                    Surface(modifier = Modifier.align(Alignment.TopEnd).padding(5.dp), color = RedColor, shape = RoundedCornerShape(4.dp)) {
                        Text("-${game.discountPercent}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
                if (game.isHot) {
                    Surface(modifier = Modifier.align(Alignment.TopStart).padding(5.dp), color = Color(0xFFF97316), shape = RoundedCornerShape(4.dp)) {
                        Text("🔥 Hot", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(game.title, fontWeight = FontWeight.SemiBold, color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                Text(game.genre, color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        if (game.hasDiscount) Text(game.originalPrice.toVND(), fontSize = 9.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough)
                        Text(game.finalPrice.toVND(), fontWeight = FontWeight.Bold, color = PurpleLt, fontSize = 13.sp)
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
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = game.thumbnailUrl.ifBlank { "https://placehold.co/70x70/1A1A2E/white?text=🎮" },
                contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.title, fontWeight = FontWeight.SemiBold, color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(game.genre, color = TextMuted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = AmberColor, modifier = Modifier.size(14.dp))
                    Text(" %.1f".format(game.rating), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPri)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (game.hasDiscount) Text(game.originalPrice.toVND(), fontSize = 10.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough)
                Text(game.finalPrice.toVND(), fontWeight = FontWeight.Bold, color = PurpleLt, fontSize = 15.sp)
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
