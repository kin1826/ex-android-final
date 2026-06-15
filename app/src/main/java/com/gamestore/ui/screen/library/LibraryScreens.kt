package com.gamestore.ui.screen.library

import androidx.compose.foundation.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamestore.model.Game
import com.gamestore.ui.theme.*
import com.gamestore.viewmodel.LibraryViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextFieldDefaults

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
                        "🎮 Thư viện",
                        color = TextPri,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPri)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurf)
            )
        }
    ) { padding ->

        when (state) {

            is com.gamestore.model.UiState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PurpleLt)
                }
            }

            is com.gamestore.model.UiState.Error -> {
                val msg = (state as com.gamestore.model.UiState.Error).message
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(msg, color = TextMuted)
                }
            }

            is com.gamestore.model.UiState.Success -> {

                val games = (state as com.gamestore.model.UiState.Success<List<Game>>).data

                val filtered = games.filter {
                    it.title.contains(searchText, ignoreCase = true)
                }

                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    item {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },

                            placeholder = {
                                Text("Tìm game...")
                            },

                            singleLine = true,
                            maxLines = 1,

                            shape = RoundedCornerShape(12.dp),

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),

                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFB026FF),
                                unfocusedBorderColor = Color(0xFFB026FF),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(filtered, key = { it.id }) { game ->

                        Card(
                            onClick = { onGameClick(game.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = BorderStroke(0.5.dp, DarkBorder)
                        ) {

                            Row(
                                Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                AsyncImage(
                                    model = game.thumbnailUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(game.title, color = TextPri, fontWeight = FontWeight.Bold)
                                    Text(game.genre, color = TextMuted, fontSize = 12.sp)

                                    Row {
                                        Icon(
                                            Icons.Default.SportsEsports,
                                            null,
                                            tint = PurpleLt
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Đã sở hữu", color = PurpleLt)
                                    }
                                }

                                Button(onClick = { onGameClick(game.id) }) {
                                    Text("Chơi")
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}