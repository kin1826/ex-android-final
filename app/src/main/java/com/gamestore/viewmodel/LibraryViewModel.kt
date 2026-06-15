package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.local.LibraryDao
import com.gamestore.model.Game
import com.gamestore.model.UiState
import com.gamestore.data.remote.toGame
import com.gamestore.data.remote.toLibraryEntity
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val tm: TokenManager
) : ViewModel() {

    private val userId = tm.getUserId()

    val games: StateFlow<UiState<List<Game>>> =
        libraryDao.getUserLibrary(userId)
            .map { list ->
                list.map { it.toGame() }
            }
            .map { games ->
                UiState.Success(games) as UiState<List<Game>>
            }
            .catch { e ->
                emit(UiState.Error(e.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    fun addToLibrary(game: Game) {
        viewModelScope.launch {
            libraryDao.insert(
                game.toLibraryEntity(userId)
            )
        }
    }
}