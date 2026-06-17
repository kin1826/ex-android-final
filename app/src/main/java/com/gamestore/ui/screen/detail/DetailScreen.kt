package com.gamestore.ui.screen.detail

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.gamestore.model.Game
import com.gamestore.model.UiState
import com.gamestore.ui.theme.*
import com.gamestore.util.toVND
import com.gamestore.viewmodel.DetailViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    vm: DetailViewModel = hiltViewModel(),
) {
    val state   by vm.game.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            delay(1500)
            vm.clearMessage()
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost   = { SnackbarHost(snackbar) },
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                Alignment.Center
            ) {
                CircularProgressIndicator(color = PurpleLt)
            }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("😕", fontSize = 52.sp)
                    Text(s.message, color = TextMuted, fontSize = 14.sp)
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape = RoundedCornerShape(10.dp),
                    ) { Text("Quay lại") }
                }
            }

            is UiState.Success -> {
                val game = s.data
                Box(Modifier.fillMaxSize()) {
                    // Scrollable content
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 100.dp) // space for bottom bar
                    ) {
                        // ── Screenshot Pager ──────────────────────────────
                        ScreenshotPager(game = game)

                        Column(Modifier.padding(16.dp)) {

                            // ── Badges hàng đầu ───────────────────────────
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 10.dp),
                            ) {
                                if (game.isNew) GameBadge("🆕 Mới", Color(0xFF10B981))
                                if (game.isHot) GameBadge("🔥 Hot", Color(0xFFF97316))
                                if (game.hasDiscount) GameBadge("-${game.discountPercent}%", RedColor)
                                GameBadge(game.ageRating, Color(0xFF6B7280))
                            }

                            // ── Tên game ──────────────────────────────────
                            Text(
                                game.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPri,
                                lineHeight = 30.sp,
                            )
                            Spacer(Modifier.height(4.dp))

                            // ── Developer / Publisher ─────────────────────
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 12.dp),
                            ) {
                                if (game.developer.isNotBlank()) {
                                    Column {
                                        Text("Nhà phát triển", fontSize = 10.sp, color = TextMuted)
                                        Text(game.developer, fontSize = 12.sp, color = PurpleLt, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (game.publisher.isNotBlank()) {
                                    Column {
                                        Text("Nhà phát hành", fontSize = 10.sp, color = TextMuted)
                                        Text(game.publisher, fontSize = 12.sp, color = TextPri, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Divider(color = DarkBorder)
                            Spacer(Modifier.height(12.dp))

                            // ── Rating Section (như Steam) ─────────────────
                            RatingSection(game)

                            Spacer(Modifier.height(12.dp))
                            Divider(color = DarkBorder)
                            Spacer(Modifier.height(12.dp))

                            // ── Thông tin game ────────────────────────────
                            GameInfoGrid(game)

                            Spacer(Modifier.height(12.dp))
                            Divider(color = DarkBorder)
                            Spacer(Modifier.height(12.dp))

                            // ── Tags ──────────────────────────────────────
                            if (game.tags.isNotEmpty()) {
                                Text(
                                    "Thẻ phổ biến của người dùng",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                                FlowRow(game.tags)
                                Spacer(Modifier.height(12.dp))
                                Divider(color = DarkBorder)
                                Spacer(Modifier.height(12.dp))
                            }

                            // ── Mô tả ngắn ────────────────────────────────
                            DescriptionSection(game)

                            Spacer(Modifier.height(12.dp))
                            Divider(color = DarkBorder)
                            Spacer(Modifier.height(12.dp))

                            // ── Thông tin hệ thống ────────────────────────
                            SystemRequirements(game)
                        }
                    }

                    // ── TopBar overlay (trên ảnh) ─────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(Color.Black.copy(0.5f), CircleShape)
                                .size(40.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { vm.toggleWishlist() },
                                modifier = Modifier
                                    .background(Color.Black.copy(0.5f), CircleShape)
                                    .size(40.dp),
                            ) {
                                Icon(
                                    if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    null,
                                    tint = if (game.isFavorite) RedColor else Color.White
                                )
                            }
                            IconButton(
                                onClick = onCartClick,
                                modifier = Modifier
                                    .background(Color.Black.copy(0.5f), CircleShape)
                                    .size(40.dp),
                            ) {
                                Icon(Icons.Default.ShoppingCart, null, tint = Color.White)
                            }
                        }
                    }

                    // ── Bottom Bar cố định ────────────────────────────────
                    BottomPurchaseBar(
                        game    = game,
                        onBuy   = { vm.addToCart() },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

// ── Screenshot Pager ─────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenshotPager(game: Game) {
    val screenshots = if (game.screenshotUrls.isNotEmpty())
        game.screenshotUrls
    else
        listOf(game.thumbnailUrl.ifBlank { "https://placehold.co/800x450/1A1A2E/A855F7?text=${game.title}" })

    val pagerState = rememberPagerState { screenshots.size }

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(240.dp),
        ) { page ->
            AsyncImage(
                model = screenshots[page],
                contentDescription = "${game.title} screenshot ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Gradient overlay bên dưới
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkBg)
                    )
                )
        )

        // Dot indicator
        if (screenshots.size > 1) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(screenshots.size) { i ->
                    Box(
                        Modifier
                            .size(
                                width = if (i == pagerState.currentPage) 20.dp else 6.dp,
                                height = 6.dp,
                            )
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i == pagerState.currentPage) PurpleLt
                                else Color.White.copy(0.4f)
                            )
                    )
                }
            }
        }
    }
}

// ── Rating Section ───────────────────────────────────────────
@Composable
fun RatingSection(game: Game) {
    val ratingColor = Color(game.ratingColor)

    Column {
        Text("ĐÁNH GIÁ NGƯỜI DÙNG", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // % tích cực (Steam style)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${game.positivePct}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ratingColor,
                )
                Text(
                    game.ratingLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ratingColor,
                )
            }

            // Divider dọc
            Box(Modifier.width(1.dp).height(60.dp).background(DarkBorder))

            // Chi tiết
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Thanh progress
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ThumbUp, null, tint = ratingColor, modifier = Modifier.size(14.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DarkBorder)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(game.positivePct / 100f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(ratingColor)
                        )
                    }
                    Text("${game.positivePct}%", fontSize = 11.sp, color = TextMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ThumbDown, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DarkBorder)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((100 - game.positivePct) / 100f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TextMuted)
                        )
                    }
                    Text("${100 - game.positivePct}%", fontSize = 11.sp, color = TextMuted)
                }

                // Rating sao + số lượng
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { i ->
                        Icon(
                            if (i < game.rating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                            null,
                            tint = AmberColor,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        "%.1f  •  %,d đánh giá".format(game.rating, game.reviewCount),
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}

// ── Game Info Grid ───────────────────────────────────────────
@Composable
fun GameInfoGrid(game: Game) {
    Text("THÔNG TIN", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
    Spacer(Modifier.height(10.dp))

    val infos = buildList {
        if (game.releaseDate.isNotBlank()) add("📅  Ngày phát hành" to game.releaseDate)
        if (game.developer.isNotBlank())  add("🛠  Nhà phát triển" to game.developer)
        if (game.publisher.isNotBlank())  add("🏢  Nhà phát hành" to game.publisher)
        if (game.platforms.isNotBlank())  add("🖥  Nền tảng" to game.platforms)
        if (game.genre.isNotBlank())      add("🎮  Thể loại" to game.genre)
        if (game.ageRating.isNotBlank())  add("🔞  Độ tuổi" to game.ageRating)
        if (game.downloadSize.isNotBlank()) add("💾  Dung lượng" to game.downloadSize)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        infos.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(1f))
                Text(
                    value,
                    fontSize = 12.sp,
                    color = TextPri,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Tags (Flow) ──────────────────────────────────────────────
@Composable
fun FlowRow(tags: List<String>) {
    // Simple flow wrap với Row + wrapping
    var rowTags = tags.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rowTags.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowItems.forEach { tag ->
                    Surface(
                        color  = DarkCard,
                        shape  = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, DarkBorder),
                    ) {
                        Text(
                            tag,
                            fontSize = 11.sp,
                            color    = TextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── Mô tả có expand/collapse ─────────────────────────────────
@Composable
fun DescriptionSection(game: Game) {
    var expanded by remember { mutableStateOf(false) }
    val longDesc = game.longDesc.ifBlank { game.description }
    val shortDesc = game.description

    Text("GIỚI THIỆU", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
    Spacer(Modifier.height(8.dp))

    if (expanded || longDesc == shortDesc) {
        Text(longDesc, color = TextMuted, fontSize = 14.sp, lineHeight = 22.sp)
    } else {
        Text(
            shortDesc,
            color = TextMuted,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (longDesc != shortDesc && longDesc.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = { expanded = !expanded },
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                if (expanded) "Thu gọn ▲" else "Đọc thêm ▼",
                color    = PurpleLt,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── System Requirements ──────────────────────────────────────
@Composable
fun SystemRequirements(game: Game) {
    Text("CẤU HÌNH HỆ THỐNG", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
    Spacer(Modifier.height(10.dp))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Tối thiểu
        Card(
            modifier = Modifier.weight(1f),
            shape    = RoundedCornerShape(10.dp),
            colors   = CardDefaults.cardColors(containerColor = DarkCard),
            border   = BorderStroke(0.5.dp, DarkBorder),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TỐI THIỂU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleLt)
                RequirementRow("OS", "Windows 10 64-bit")
                RequirementRow("CPU", "Core i5-8600K")
                RequirementRow("RAM", "12 GB")
                RequirementRow("GPU", "GTX 1060 6GB")
                RequirementRow("HDD", game.downloadSize.ifBlank { "50 GB" })
            }
        }

        // Khuyến nghị
        Card(
            modifier = Modifier.weight(1f),
            shape    = RoundedCornerShape(10.dp),
            colors   = CardDefaults.cardColors(containerColor = DarkCard),
            border   = BorderStroke(0.5.dp, PurpleLt.copy(0.3f)),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("KHUYẾN NGHỊ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleLt)
                RequirementRow("OS", "Windows 11 64-bit")
                RequirementRow("CPU", "Core i7-12700K")
                RequirementRow("RAM", "16 GB")
                RequirementRow("GPU", "RTX 3070 8GB")
                RequirementRow("HDD", game.downloadSize.ifBlank { "50 GB" } + " SSD")
            }
        }
    }
}

@Composable
fun RequirementRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label + ":", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 10.sp, color = TextPri)
    }
}

// ── Badge ────────────────────────────────────────────────────
@Composable
fun GameBadge(text: String, color: Color) {
    Surface(
        color  = color.copy(alpha = 0.15f),
        shape  = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, color.copy(0.4f)),
    ) {
        Text(
            text,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = color,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

// ── Bottom Purchase Bar ──────────────────────────────────────
@Composable
fun BottomPurchaseBar(
    game: Game,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier  = modifier.fillMaxWidth(),
        color     = DarkSurf,
        tonalElevation = 8.dp,
        border    = BorderStroke(width = 0.5.dp, color = DarkBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Giá
            Column(modifier = Modifier.weight(1f)) {
                if (!game.isOwned) {
                    if (game.hasDiscount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(color = Color(0xFF4ADE80).copy(0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text("-${game.discountPercent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text(game.originalPrice.toVND(), fontSize = 12.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough)
                        }
                    }
                    Text(game.finalPrice.toVND(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PurpleLt)
                } else {
                    Text("GAME ĐÃ MUA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenColor, letterSpacing = 1.sp)
                    Text("Trong thư viện", fontSize = 14.sp, color = TextMuted)
                }
            }

            // Xử lý nút Tải / Chơi / Mua
            var downloadProgress by remember { mutableStateOf(0f) }
            var isDownloading by remember { mutableStateOf(false) }
            var isInstalled by remember { mutableStateOf(false) }

            LaunchedEffect(isDownloading) {
                if (isDownloading) {
                    while (downloadProgress < 1f) {
                        delay(50)
                        downloadProgress += 0.02f
                    }
                    isDownloading = false
                    isInstalled = true
                }
            }

            if (game.isOwned) {
                if (isInstalled) {
                    Button(onClick = { /* Mở game */ }, colors = ButtonDefaults.buttonColors(containerColor = GreenColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(50.dp)) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Chơi ngay", fontWeight = FontWeight.Bold)
                    }
                } else if (isDownloading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(140.dp)) {
                        LinearProgressIndicator(progress = { downloadProgress }, color = PurpleLt, trackColor = DarkBorder, modifier = Modifier.fillMaxWidth().clip(CircleShape))
                        Text("${(downloadProgress * 100).toInt()}%", color = TextMuted, fontSize = 11.sp)
                    }
                } else {
                    Button(onClick = { isDownloading = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(50.dp)) {
                        Icon(Icons.Default.Download, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Tải về", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(onClick = onBuy, modifier = Modifier.height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm vào giỏ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}