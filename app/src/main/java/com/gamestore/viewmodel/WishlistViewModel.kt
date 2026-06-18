package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.remote.*
import com.gamestore.model.Game
import com.gamestore.model.UiState
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val api: WishlistApi,
    private val tm: TokenManager
) : ViewModel() {

    private val _wishlistState = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    private val _isRefreshing = MutableStateFlow(false)

    val wishlistState: StateFlow<UiState<List<Game>>> = _wishlistState.asStateFlow()
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        val userId = tm.getUserId()
        if (userId <= 0) {
            _wishlistState.value = UiState.Error("Vui lòng đăng nhập để xem danh sách yêu thích")
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val resp = api.getWishlist(userId)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val games = resp.body()?.data?.map { it.toModel() } ?: emptyList()
                    _wishlistState.value = UiState.Success(games)
                } else {
                    _wishlistState.value = UiState.Error(resp.body()?.message ?: "Lỗi tải danh sách")
                }
            } catch (e: Exception) {
                _wishlistState.value = UiState.Error("Không có kết nối mạng")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleWishlist(gameId: Int, onComplete: (Boolean) -> Unit = {}) {
        val userId = tm.getUserId()
        if (userId <= 0) return

        viewModelScope.launch {
            try {
                val resp = api.toggleWishlist(WishlistToggleRequest(userId, gameId))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val isFav = resp.body()?.data?.isFavorite ?: false
                    onComplete(isFav)
                    // Sau khi toggle, refresh lại danh sách nếu đang ở màn hình wishlist
                    loadWishlist()
                }
            } catch (e: Exception) {}
        }
    }
}
