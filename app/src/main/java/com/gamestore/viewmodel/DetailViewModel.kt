package com.gamestore.viewmodel

import androidx.lifecycle.*
import com.gamestore.data.local.*
import com.gamestore.data.remote.*
import com.gamestore.model.*
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val api: GameApi,
    private val gameDao: GameDao,
    private val cartDao: CartDao,
    private val tm: TokenManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameId = savedStateHandle.get<Int>("gameId") ?: 0

    private val _game    = MutableStateFlow<UiState<Game>>(UiState.Loading)
    private val _message = MutableStateFlow<String?>(null)

    val game:    StateFlow<UiState<Game>> = _game.asStateFlow()
    val message: StateFlow<String?>       = _message.asStateFlow()

    init { loadGame() }

    private fun loadGame() = viewModelScope.launch {
        _game.value = UiState.Loading
        val userId = tm.getUserId()
        val cached = gameDao.getById(gameId)
        if (cached != null) _game.value = UiState.Success(cached.toModel())
        try {
            val resp = api.getById(gameId, if (userId > 0) userId else null)
            if (resp.isSuccessful && resp.body()?.data != null) {
                val dto = resp.body()!!.data!!
                gameDao.insert(dto.toEntity())
                _game.value = UiState.Success(dto.toModel())
            } else if (cached == null) {
                _game.value = UiState.Error("Không tìm thấy game")
            }
        } catch (e: Exception) {
            if (cached == null) _game.value = UiState.Error("Lỗi kết nối")
        }
    }

    fun addToCart() {
        val userId = tm.getUserId()
        if (userId == 0) { _message.value = "Vui lòng đăng nhập trước"; return }
        val game = (_game.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val existing = cartDao.getItem(userId, game.id)
            if (existing != null) cartDao.updateQty(userId, game.id, existing.quantity + 1)
            else cartDao.insert(CartEntity(
                userId = userId, gameId = game.id, quantity = 1,
                gameTitle = game.title, gamePrice = game.finalPrice,
                gameThumbnail = game.thumbnailUrl, discountPercent = game.discountPercent,
            ))
            _message.value = "✓ Đã thêm vào giỏ hàng"
        }
    }

    fun clearMessage() { _message.value = null }
}