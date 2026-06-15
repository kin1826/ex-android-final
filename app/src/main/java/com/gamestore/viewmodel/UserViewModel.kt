package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.remote.DepositRequest
import com.gamestore.data.remote.UserApi
import com.gamestore.model.UiState
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userApi: UserApi,
    private val tm: TokenManager
) : ViewModel() {

    private val _depositState = MutableStateFlow<UiState<Double>?>(null)
    val depositState = _depositState.asStateFlow()

    fun deposit(amount: Double) = viewModelScope.launch {
        val userId = tm.getUserId()
        if (userId == 0) return@launch

        _depositState.value = UiState.Loading
        try {
            val resp = userApi.deposit(body = DepositRequest(userId, amount))
            
            if (resp.isSuccessful && resp.body()?.success == true) {
                // Cách lấy số dư an toàn cho cả kiểu Int và Double
                val data = resp.body()?.data
                val newBalance = when (val nb = data?.get("newBalance")) {
                    is Double -> nb
                    is Number -> nb.toDouble()
                    else -> 0.0
                }
                
                // Cập nhật lại User trong máy để Profile hiện số tiền mới
                val user = tm.getUser()
                user?.let {
                    tm.saveUser(it.copy(walletBalance = newBalance))
                }
                
                _depositState.value = UiState.Success(newBalance)
            } else {
                _depositState.value = UiState.Error(resp.body()?.message ?: "Nạp tiền thất bại")
            }
        } catch (e: Exception) {
            e.printStackTrace() // In ra Logcat để kiểm tra
            _depositState.value = UiState.Error("Lỗi: ${e.localizedMessage}")
        }
    }

    fun clearState() { _depositState.value = null }
}