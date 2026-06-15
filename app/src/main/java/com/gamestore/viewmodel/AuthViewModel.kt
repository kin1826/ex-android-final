package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.remote.*
import com.gamestore.model.*
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tm: TokenManager,
) : ViewModel() {

    private val _state       = MutableStateFlow<UiState<User>?>(null)
    private val _isLoggedIn  = MutableStateFlow(tm.isLoggedIn())
    private val _currentUser = MutableStateFlow<User?>(tm.getUser())

    val state:       StateFlow<UiState<User>?> = _state.asStateFlow()
    val isLoggedIn:  StateFlow<Boolean>         = _isLoggedIn.asStateFlow()
    val currentUser: StateFlow<User?>           = _currentUser.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _isLoggedIn.value = tm.isLoggedIn()
        _currentUser.value = tm.getUser()
        
        // Cập nhật từ server nếu đã login
        if (tm.isLoggedIn()) {
            viewModelScope.launch {
                try {
                    val resp = userApi.getProfile(userId = tm.getUserId())
                    if (resp.isSuccessful && resp.body()?.data != null) {
                        val user = resp.body()!!.data!!.toModel()
                        tm.saveUser(user)
                        _currentUser.value = user
                    }
                } catch (e: Exception) {}
            }
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val resp = authApi.login(body = LoginRequest(email, password))
            if (resp.isSuccessful && resp.body()?.data != null) {
                val data = resp.body()!!.data!!
                val user = data.user.toModel()
                
                tm.save(data.accessToken, data.userId)
                tm.saveUser(user)
                
                _currentUser.value = user
                _isLoggedIn.value  = true
                _state.value       = UiState.Success(user)
            } else {
                _state.value = UiState.Error(resp.body()?.message ?: "Sai email hoặc mật khẩu")
            }
        } catch (e: Exception) {
            _state.value = UiState.Error("Không có kết nối mạng")
        }
    }

    fun register(username: String, email: String, password: String,
                 displayName: String, phone: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val resp = authApi.register(
                body = RegisterRequest(username, email, password, displayName, phone)
            )
            if (resp.isSuccessful && resp.body()?.data != null) {
                val data = resp.body()!!.data!!
                val user = data.user.toModel()

                tm.save(data.accessToken, data.userId)
                tm.saveUser(user)

                _currentUser.value = user
                _isLoggedIn.value  = true
                _state.value       = UiState.Success(user)
            } else {
                _state.value = UiState.Error(resp.body()?.message ?: "Đăng ký thất bại")
            }
        } catch (e: Exception) {
            _state.value = UiState.Error("Không có kết nối mạng")
        }
    }

    fun logout() {
        tm.clear()
        _isLoggedIn.value  = false
        _currentUser.value = null
        _state.value       = null
    }

    fun clearState() { _state.value = null }
}