package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.local.LibraryDao
import com.gamestore.data.remote.LibraryApi
import com.gamestore.data.remote.toGame
import com.gamestore.data.remote.toLibraryEntity
import com.gamestore.model.Game
import com.gamestore.model.UiState
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val libraryApi: LibraryApi,
    private val tm: TokenManager
) : ViewModel() {

    private val userId = tm.getUserId()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        syncLibrary()
    }

    val games: StateFlow<UiState<List<Game>>> =
        libraryDao.getUserLibrary(userId)
            .map { entities ->
                entities.map { it.toGame() }
            }
            .map<List<Game>, UiState<List<Game>>> {
                UiState.Success(it)
            }
            .catch { e ->
                emit(UiState.Error(e.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    fun syncLibrary() = viewModelScope.launch {
        val currentUserId = tm.getUserId()
        if (currentUserId <= 0) return@launch
        
        _isRefreshing.value = true
        try {
            val response = libraryApi.getLibrary(currentUserId)
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data.orEmpty()
                libraryDao.clearUserLibrary(currentUserId)
                libraryDao.insertAll(items.map { it.toLibraryEntity() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isRefreshing.value = false
        }
    }
}