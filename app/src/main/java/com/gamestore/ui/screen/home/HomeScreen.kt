package com.gamestore.ui.screen.home

import androidx.compose.animation.animateContentSize
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
    // Quan sát dữ liệu trang chủ
    val featured    by vm.featured.collectAsStateWithLifecycle()
    val hotDeals    by vm.hotDeals.collectAsStateWithLifecycle()
    val newReleases by vm.newReleases.collectAsStateWithLifecycle()
    val categories  by vm.categories.collectAsStateWithLifecycle()
    val cartCount   by vm.cartCount.collectAsStateWithLifecycle()

    // Quan sát trạng thái lọc tập trung từ ViewModel
    val filter      by vm.filterState.collectAsStateWithLifecycle()
    val filteredGames      by vm.filteredGames.collectAsStateWithLifecycle()
    val activeFilterCount  by vm.activeFilterCount.collectAsStateWithLifecycle()
    val isSearchingMode    by vm.isSearchingMode.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Trạng thái tạm thời trong BottomSheet (Chỉ cập nhật vào ViewModel khi bấm Áp dụng)
    var tempSortBy by remember(showFilterSheet) { mutableStateOf(filter.sortBy) }
    var tempPriceRange by remember(showFilterSheet) { mutableStateOf(filter.priceRange) }
    var tempOnlyDiscounted by remember(showFilterSheet) { mutableStateOf(filter.onlyDiscounted) }
    var tempGenre by remember(showFilterSheet) { mutableStateOf(filter.genre) }
    var tempPlatform by remember(showFilterSheet) { mutableStateOf(filter.platform) }

    val priceRanges = remember {
        listOf(
            PriceRange("Dưới 100k", 0.0, 100000.0),
            PriceRange("100k - 500k", 100000.0, 500000.0),
            PriceRange("500k - 1tr", 500000.0, 1000000.0),
            PriceRange("Trên 1tr", 1000000.0, null)
        )
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("⚡ GAMESTORE", fontWeight = FontWeight.ExtraBold, color = PurpleLt, fontSize = 20.sp, letterSpacing = 1.sp) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(badge = {
                            if (activeFilterCount > 0) {
                                Badge(containerColor = PurpleLt, contentColor = Color.White) {
                                    Text("$activeFilterCount")
                                }
                            }
                        }) {
                            Icon(Icons.Default.Tune, "Filter", tint = if (activeFilterCount > 0) PurpleLt else TextPri)
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
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Bộ lọc & Sắp xếp", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (activeFilterCount > 0) {
                            TextButton(onClick = { 
                                vm.clearAllFilters()
                                showFilterSheet = false
                            }) {
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
                            "price_asc" to "Giá tăng dần",
                            "price_desc" to "Giá giảm dần"
                        )
                        sortOptions.forEach { (key, label) ->
                            FilterChip(
                                selected = tempSortBy == key,
                                onClick = { tempSortBy = key },
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
                    Text("Thể loại", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = tempGenre == cat.name,
                                onClick = { tempGenre = if (tempGenre == cat.name) null else cat.name },
                                label = { Text(cat.name, fontSize = 13.sp) },
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
                    Text("Nền tảng", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val platforms = listOf("PC", "PlayStation", "Xbox", "Nintendo")
                        platforms.forEach { p ->
                            FilterChip(
                                selected = tempPlatform == p,
                                onClick = { tempPlatform = if (tempPlatform == p) null else p },
                                label = { Text(p, fontSize = 13.sp) },
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
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        priceRanges.forEach { range ->
                            FilterChip(
                                selected = tempPriceRange == range,
                                onClick = {
                                    tempPriceRange = if (tempPriceRange == range) null else range
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
                        selected = tempOnlyDiscounted,
                        onClick = { tempOnlyDiscounted = !tempOnlyDiscounted },
                        label = { Text("Đang giảm giá", fontSize = 13.sp) },
                        leadingIcon = { if(tempOnlyDiscounted) Icon(Icons.Default.Check, null, Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedColor.copy(0.2f),
                            selectedLabelColor = RedColor,
                            containerColor = DarkCard,
                            labelColor = TextPri
                        )
                    )

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { 
                            vm.updateFilters(tempGenre, tempPlatform, tempSortBy, tempPriceRange, tempOnlyDiscounted)
                            showFilterSheet = false 
                        },
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
                    value = filter.search,
                    onValueChange = { vm.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Tìm kiếm game...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                    trailingIcon = {
                        if (filter.search.isNotBlank()) {
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

            // Thanh chọn thể loại nhanh ngoài màn hình chính
            if (categories.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            val isSelected = filter.genre == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { vm.onGenreClick(cat.name) },
                                label = { Text("${cat.iconEmoji} ${cat.name}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurpleLt.copy(0.2f),
                                    selectedLabelColor = PurpleLt,
                                    containerColor = DarkCard,
                                    labelColor = TextMuted
                                ),
                                border = if (isSelected) FilterChipDefaults.filterChipBorder(borderColor = PurpleLt, borderWidth = 1.dp, enabled = true, selected = true) 
                                         else FilterChipDefaults.filterChipBorder(borderColor = DarkBorder, borderWidth = 1.dp, enabled = true, selected = false)
                            )
                        }
                    }
                }
            }

            if (isSearchingMode) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 8.dp).animateContentSize()) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kết quả lọc", color = TextPri, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (activeFilterCount > 0 || filter.search.isNotBlank()) {
                                TextButton(onClick = { vm.clearAllFilters() }) {
                                    Text("Xoá hết", color = RedColor, fontSize = 13.sp)
                                }
                            }
                        }
                        
                        // Hàng Chip hiển thị các bộ lọc đang áp dụng
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Sắp xếp
                            if (filter.sortBy != "newest") {
                                val label = when(filter.sortBy) {
                                    "rating" -> "Đánh giá cao"
                                    "price_asc" -> "Giá tăng dần"
                                    "price_desc" -> "Giá giảm dần"
                                    else -> "Mới nhất"
                                }
                                item { AppliedFilterChip(label) { vm.updateFilters(filter.genre, filter.platform, "newest", filter.priceRange, filter.onlyDiscounted) } }
                            }
                            // Thể loại
                            if (filter.genre != null) {
                                item { AppliedFilterChip(filter.genre!!) { vm.onGenreClick(filter.genre!!) } }
                            }
                            // Nền tảng
                            if (filter.platform != null) {
                                item { AppliedFilterChip(filter.platform!!) { vm.updateFilters(filter.genre, null, filter.sortBy, filter.priceRange, filter.onlyDiscounted) } }
                            }
                            // Khoảng giá
                            if (filter.priceRange != null) {
                                item { AppliedFilterChip(filter.priceRange!!.label) { vm.updateFilters(filter.genre, filter.platform, filter.sortBy, null, filter.onlyDiscounted) } }
                            }
                            // Giảm giá
                            if (filter.onlyDiscounted) {
                                item { AppliedFilterChip("Đang giảm giá") { vm.updateFilters(filter.genre, filter.platform, filter.sortBy, filter.priceRange, false) } }
                            }
                            // Tìm kiếm
                            if (filter.search.isNotBlank()) {
                                item { AppliedFilterChip("\"${filter.search}\"") { vm.setSearchQuery("") } }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
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

/**
 * Component hiển thị Chip lọc đã áp dụng và có nút xóa
 */
@Composable
private fun AppliedFilterChip(text: String, onRemove: () -> Unit) {
    Surface(
        onClick = onRemove,
        shape = RoundedCornerShape(8.dp),
        color = PurpleLt.copy(0.1f),
        border = BorderStroke(1.dp, PurpleLt.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text, color = PurpleLt, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.Close, null, tint = PurpleLt, modifier = Modifier.size(14.dp))
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
            }
            Column(Modifier.padding(10.dp)) {
                Text(game.title, fontWeight = FontWeight.SemiBold, color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                Text(game.genre, color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(game.finalPrice.toVND(), fontWeight = FontWeight.Bold, color = PurpleLt, fontSize = 13.sp)
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
            }
        }
    }
}
