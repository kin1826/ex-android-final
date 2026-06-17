package com.gamestore.ui.screen.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
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
import com.gamestore.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onGameClick: (Int) -> Unit,
    onBack: () -> Unit,
    vm: LibraryViewModel = hiltViewModel()
) {

    val state by vm.games.collectAsStateWithLifecycle()

    var searchText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎮 Thư viện",
                        color = TextPri,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = TextPri
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurf
                )
            )
        }
    ) { padding ->

        when (val currentState = state) {

            is UiState.Loading -> {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PurpleLt
                    )
                }
            }

            is UiState.Error -> {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentState.message,
                        color = TextMuted
                    )
                }
            }

            is UiState.Success -> {

                val games = currentState.data

                val filteredGames = games.filter {
                    it.title.contains(
                        searchText,
                        ignoreCase = true
                    )
                }

                if (games.isEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            Text(
                                text = "Chưa có game nào",
                                color = TextPri,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Hãy mua game để thêm vào thư viện",
                                color = TextMuted
                            )
                        }
                    }

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        item {

                            OutlinedTextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                placeholder = {
                                    Text("Tìm game...")
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFB026FF),
                                    unfocusedBorderColor = Color(0xFFB026FF),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )
                        }

                        items(
                            filteredGames,
                            key = { it.id }
                        ) { game ->

                            Card(
                                onClick = {
                                    onGameClick(game.id)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = DarkCard
                                ),
                                border = BorderStroke(
                                    0.5.dp,
                                    DarkBorder
                                )
                            ) {

                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    AsyncImage(
                                        model = game.thumbnailUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(
                                                RoundedCornerShape(8.dp)
                                            )
                                    )

                                    Spacer(
                                        modifier = Modifier.width(12.dp)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = game.title,
                                            color = TextPri,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = game.genre,
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )

                                        Spacer(
                                            modifier = Modifier.height(4.dp)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                tint = PurpleLt
                                            )

                                            Spacer(
                                                modifier = Modifier.width(4.dp)
                                            )

                                            Text(
                                                text = "Đã sở hữu",
                                                color = PurpleLt
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onGameClick(game.id)
                                        }
                                    ) {
                                        Text("Chơi")
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(
                                modifier = Modifier.height(80.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}