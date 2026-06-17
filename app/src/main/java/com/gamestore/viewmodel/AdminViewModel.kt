package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.remote.AdminApi
import com.gamestore.data.remote.AdminStatsDto
import com.gamestore.data.remote.CategoryDto
import com.gamestore.data.remote.CategoryRequest
import com.gamestore.data.remote.GameApi
import com.gamestore.data.remote.GameRequest
import com.gamestore.data.remote.GenericAdminRequest
import com.gamestore.data.remote.UserStatusUpdateRequest
import com.gamestore.data.remote.WalletUpdateRequest
import com.gamestore.data.remote.toModel
import com.gamestore.model.Game
import com.gamestore.model.UiState
import com.gamestore.model.User
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val api: GameApi,
    private val adminApi: AdminApi,
    private val tm: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<String>?>(null)
    val state = _state.asStateFlow()

    private val _stats = MutableStateFlow<UiState<AdminStatsDto>>(UiState.Loading)
    val stats = _stats.asStateFlow()

    private val _allGames = MutableStateFlow<UiState<List<Game>>>(UiState.Loading)
    val allGames = _allGames.asStateFlow()

    private val _allCategories = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Loading)
    val allCategories = _allCategories.asStateFlow()

    private val _allUsers = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val allUsers = _allUsers.asStateFlow()

    fun loadAllCategories() = viewModelScope.launch {
        _allCategories.value = UiState.Loading
        try {
            // Mượn API có sẵn từ GameApi
            val resp = api.getCategories()
            if (resp.isSuccessful) {
                _allCategories.value = UiState.Success(resp.body()?.data ?: emptyList())
            }
        } catch (e: Exception) { _allCategories.value = UiState.Error("Lỗi kết nối") }
    }

    fun loadAllUsers(query: String? = null) = viewModelScope.launch {
        _allUsers.value = UiState.Loading
        try {
            val resp = adminApi.getUsers(tm.getUserId(), query)
            if (resp.isSuccessful && resp.body()?.data != null) {
                _allUsers.value = UiState.Success(resp.body()!!.data!!.map { it.toModel() })
            } else {
                _allUsers.value = UiState.Error(resp.body()?.message ?: "Lỗi tải danh sách người dùng")
            }
        } catch (e: Exception) {
            _allUsers.value = UiState.Error("Lỗi kết nối: ${e.message}")
        }
    }

    fun updateWallet(userId: Int, amount: Double) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val req = WalletUpdateRequest(tm.getUserId(), userId, amount)
            val resp = adminApi.updateWallet(body = req)
            if (resp.isSuccessful) {
                _state.value = UiState.Success("Đã cập nhật số dư ví")
                loadAllUsers()
            } else {
                _state.value = UiState.Error(resp.body()?.message ?: "Thất bại")
            }
        } catch (e: Exception) { _state.value = UiState.Error("Lỗi kết nối") }
    }

    fun resetPassword(userId: Int) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val req = GenericAdminRequest(tm.getUserId(), userId)
            val resp = adminApi.resetPassword(body = req)
            if (resp.isSuccessful) {
                _state.value = UiState.Success("Mật khẩu đã đặt lại về 123456")
            } else {
                _state.value = UiState.Error(resp.body()?.message ?: "Thất bại")
            }
        } catch (e: Exception) { _state.value = UiState.Error("Lỗi kết nối") }
    }

    fun updateUserStatus(userId: Int, isAdmin: Boolean? = null, isActive: Boolean? = null) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val req = UserStatusUpdateRequest(
                adminId = tm.getUserId(),
                userId = userId,
                isAdmin = isAdmin?.let { if (it) 1 else 0 },
                isActive = isActive?.let { if (it) 1 else 0 }
            )
            val resp = adminApi.updateUserStatus(body = req)
            if (resp.isSuccessful) {
                _state.value = UiState.Success("Đã cập nhật trạng thái người dùng")
                loadAllUsers()
            } else {
                _state.value = UiState.Error(resp.body()?.message ?: "Thất bại")
            }
        } catch (e: Exception) { _state.value = UiState.Error("Lỗi kết nối") }
    }

    fun addCategory(name: String, icon: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val req = CategoryRequest(adminId = tm.getUserId(), name = name, iconEmoji = icon)
            val resp = adminApi.addCategory(req)
            if (resp.isSuccessful) {
                _state.value = UiState.Success("Đã thêm thể loại")
                loadAllCategories()
            } else {
                val errorMsg = resp.body()?.message ?: "Lỗi server (${resp.code()})"
                _state.value = UiState.Error(errorMsg)
            }
        } catch (e: Exception) { 
            _state.value = UiState.Error("Lỗi kết nối: ${e.message}") 
        }
    }

    fun deleteCategory(id: Int) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val resp = adminApi.deleteCategory(id, tm.getUserId())
            if (resp.isSuccessful) {
                _state.value = UiState.Success("Đã xóa thể loại")
                loadAllCategories()
            } else {
                val errorMsg = resp.body()?.message ?: "Không thể xóa (có thể thể loại đang chứa game)"
                _state.value = UiState.Error(errorMsg)
            }
        } catch (e: Exception) { 
            _state.value = UiState.Error("Lỗi kết nối: ${e.message}") 
        }
    }

    fun loadAllGames() = viewModelScope.launch {
        _allGames.value = UiState.Loading
        try {
            val resp = api.getGames(size = 100)
            if (resp.isSuccessful && resp.body()?.data != null) {
                _allGames.value = UiState.Success(resp.body()!!.data!!.items.map { it.toModel() })
            } else {
                _allGames.value = UiState.Error("Không thể tải danh sách")
            }
        } catch (e: Exception) {
            _allGames.value = UiState.Error("Lỗi kết nối")
        }
    }

    fun loadStats() = viewModelScope.launch {
        _stats.value = UiState.Loading
        try {
            val resp = adminApi.getStats(tm.getUserId())
            if (resp.isSuccessful && resp.body()?.data != null) {
                _stats.value = UiState.Success(resp.body()!!.data!!)
            } else {
                _stats.value = UiState.Error(resp.body()?.message ?: "Lỗi tải thống kê")
            }
        } catch (e: Exception) {
            _stats.value = UiState.Error("Lỗi kết nối")
        }
    }

    fun deleteGame(gameId: Int, onSuccess: () -> Unit) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val resp = api.deleteGame(gameId, tm.getUserId())
            if (resp.isSuccessful) {
                _state.value = UiState.Success("Xóa game thành công")
                onSuccess()
            } else {
                _state.value = UiState.Error(resp.body()?.message ?: "Xóa thất bại")
            }
        } catch (e: Exception) {
            _state.value = UiState.Error("Lỗi kết nối")
        }
    }

    fun saveGame(game: Game, isEdit: Boolean, onSuccess: () -> Unit) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val req = GameRequest(
                adminId = tm.getUserId(),
                id = if (isEdit) game.id else null,
                title = game.title,
                description = game.description,
                price = game.price,
                originalPrice = game.originalPrice,
                discountPercent = game.discountPercent,
                genre = game.genre,
                developer = game.developer,
                publisher = game.publisher,
                releaseDate = game.releaseDate,
                platforms = game.platforms,
                downloadSize = game.downloadSize,
                thumbnailUrl = game.thumbnailUrl,
                isFeatured = if (game.isFeatured) 1 else 0,
                isHot = if (game.isHot) 1 else 0,
                isNew = if (game.isNew) 1 else 0
            )

            val resp = if (isEdit) api.updateGame(req) else api.addGame(req)
            
            if (resp.isSuccessful) {
                _state.value = UiState.Success("Lưu game thành công")
                onSuccess()
            } else {
                val errorMsg = resp.body()?.message ?: "Lưu thất bại (${resp.code()})"
                _state.value = UiState.Error(errorMsg)
            }
        } catch (e: Exception) {
            _state.value = UiState.Error("Lỗi kết nối: ${e.message}")
        }
    }

    fun clearState() { _state.value = null }
}