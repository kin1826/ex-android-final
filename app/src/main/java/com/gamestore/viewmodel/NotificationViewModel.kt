package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.remote.*
import com.gamestore.model.UiState
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val api: NotificationApi,
    private val tm: TokenManager
) : ViewModel() {

    private val _notifications = MutableStateFlow<UiState<List<NotificationDto>>>(UiState.Loading)
    val notifications = _notifications.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val unreadCount = notifications.map {
        if (it is UiState.Success) it.data.count { n -> !n.isRead } else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadNotifications()
        startPolling()
    }

    private var pollingJob: Job? = null

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(30000) // 30 giây kiểm tra một lần
                val userId = tm.getUserId()
                if (userId > 0) {
                    try {
                        val resp = api.getNotifications(userId)
                        if (resp.isSuccessful && resp.body()?.success == true) {
                            _notifications.value = UiState.Success(resp.body()?.data ?: emptyList())
                        }
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun loadNotifications() = viewModelScope.launch {
        val userId = tm.getUserId()
        if (userId <= 0) return@launch

        _isRefreshing.value = true
        try {
            val resp = api.getNotifications(userId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                _notifications.value = UiState.Success(resp.body()?.data ?: emptyList())
            } else {
                _notifications.value = UiState.Error(resp.body()?.message ?: "Lỗi tải thông báo")
            }
        } catch (e: Exception) {
            _notifications.value = UiState.Error("Lỗi kết nối")
        } finally {
            _isRefreshing.value = false
        }
    }

    fun markRead(notificationId: Int = 0) = viewModelScope.launch {
        val userId = tm.getUserId()
        if (userId <= 0) return@launch

        try {
            api.markRead(MarkReadRequest(userId, notificationId))
            // Refresh lại danh sách sau khi đánh dấu
            loadNotifications()
        } catch (e: Exception) {}
    }
}
