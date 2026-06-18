package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.remote.*
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

    private val _depositRequestState = MutableStateFlow<UiState<DepositResponse>?>(null)
    val depositRequestState = _depositRequestState.asStateFlow()

    private val _adminDeposits = MutableStateFlow<UiState<List<DepositDto>>>(UiState.Loading)
    val adminDeposits = _adminDeposits.asStateFlow()

    private val _adminActionState = MutableStateFlow<UiState<Unit>?>(null)
    val adminActionState = _adminActionState.asStateFlow()

    private val _myDeposits = MutableStateFlow<UiState<List<DepositDto>>>(UiState.Loading)
    val myDeposits = _myDeposits.asStateFlow()

    fun requestDeposit(amount: Double) = viewModelScope.launch {
        val userId = tm.getUserId()
        if (userId <= 0) return@launch

        _depositRequestState.value = UiState.Loading
        try {
            val resp = userApi.requestDeposit(DepositRequest(userId, amount))
            if (resp.isSuccessful && resp.body()?.success == true) {
                _depositRequestState.value = UiState.Success(resp.body()!!.data!!)
            } else {
                _depositRequestState.value = UiState.Error(resp.body()?.message ?: "Gửi yêu cầu thất bại")
            }
        } catch (e: Exception) {
            _depositRequestState.value = UiState.Error("Lỗi kết nối")
        }
    }

    fun loadMyDeposits() = viewModelScope.launch {
        val userId = tm.getUserId()
        if (userId <= 0) return@launch
        _myDeposits.value = UiState.Loading
        try {
            val resp = userApi.getMyDeposits(userId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                _myDeposits.value = UiState.Success(resp.body()?.data ?: emptyList())
            } else {
                _myDeposits.value = UiState.Error(resp.body()?.message ?: "Không tải được lịch sử")
            }
        } catch (e: Exception) {
            _myDeposits.value = UiState.Error("Lỗi kết nối")
        }
    }

    fun loadAdminDeposits() = viewModelScope.launch {
        _adminDeposits.value = UiState.Loading
        try {
            val resp = userApi.getAdminDeposits(tm.getUserId())
            if (resp.isSuccessful && resp.body()?.success == true) {
                _adminDeposits.value = UiState.Success(resp.body()?.data ?: emptyList())
            } else {
                _adminDeposits.value = UiState.Error("Không tải được danh sách")
            }
        } catch (e: Exception) {
            _adminDeposits.value = UiState.Error("Lỗi kết nối")
        }
    }

    fun updateDepositStatus(depositId: Int, action: String, adminNote: String? = null) = viewModelScope.launch {
        _adminActionState.value = UiState.Loading
        try {
            val resp = userApi.updateDepositStatus(
                AdminDepositActionRequest(tm.getUserId(), depositId, action, adminNote)
            )
            if (resp.isSuccessful) {
                _adminActionState.value = UiState.Success(Unit)
                loadAdminDeposits() // Tải lại danh sách sau khi duyệt
            } else {
                _adminActionState.value = UiState.Error("Thao tác thất bại")
            }
        } catch (e: Exception) {
            _adminActionState.value = UiState.Error("Lỗi kết nối")
        }
    }

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