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
class OrderViewModel @Inject constructor(
    private val orderApi: OrderApi,
    private val tm: TokenManager,
) : ViewModel() {

    private val _orders = MutableStateFlow<UiState<List<Order>>>(UiState.Loading)
    val orders: StateFlow<UiState<List<Order>>> = _orders.asStateFlow()

    init { loadOrders() }

    fun loadOrders() = viewModelScope.launch {
        _orders.value = UiState.Loading
        try {
            val resp = orderApi.getOrders(tm.getUserId())
            if (resp.isSuccessful && resp.body()?.data != null) {
                _orders.value = UiState.Success(resp.body()!!.data!!.map { it.toModel() })
            } else {
                _orders.value = UiState.Error("Không tải được đơn hàng")
            }
        } catch (e: Exception) {
            _orders.value = UiState.Error("Không có kết nối mạng")
        }
    }
}