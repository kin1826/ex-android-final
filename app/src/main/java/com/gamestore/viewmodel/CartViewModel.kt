package com.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamestore.data.local.*
import com.gamestore.data.remote.*
import com.gamestore.model.*
import com.gamestore.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartDao: CartDao,
    private val gameDao: GameDao,
    private val orderApi: OrderApi,
    private val tm: TokenManager,
) : ViewModel() {

    private val userId get() = tm.getUserId()

    private val _cart        = MutableStateFlow(Cart())
    private val _orderResult = MutableStateFlow<UiState<Order>?>(null)
    private val _message     = MutableStateFlow<String?>(null)

    val cart:        StateFlow<Cart>            = _cart.asStateFlow()
    val orderResult: StateFlow<UiState<Order>?> = _orderResult.asStateFlow()
    val message:     StateFlow<String?>         = _message.asStateFlow()

    init { observeCart() }

    private fun observeCart() = viewModelScope.launch {
        cartDao.getItems(userId).collect { entities ->
            val items = entities.mapNotNull { e ->
                val game = gameDao.getById(e.gameId)?.toModel()
                    ?: Game(id = e.gameId, title = e.gameTitle,
                        price = e.gamePrice, thumbnailUrl = e.gameThumbnail,
                        discountPercent = e.discountPercent)
                CartItem(id = e.id, game = game, quantity = e.quantity)
            }
            _cart.value = Cart(items = items)
        }
    }

    fun increaseQty(gameId: Int) = viewModelScope.launch {
        val item = _cart.value.items.find { it.game.id == gameId } ?: return@launch
        cartDao.updateQty(userId, gameId, item.quantity + 1)
    }

    fun decreaseQty(gameId: Int) = viewModelScope.launch {
        val item = _cart.value.items.find { it.game.id == gameId } ?: return@launch
        if (item.quantity <= 1) cartDao.deleteById(item.id)
        else cartDao.updateQty(userId, gameId, item.quantity - 1)
    }

    fun removeItem(id: Int) = viewModelScope.launch { cartDao.deleteById(id) }

    fun placeOrder(paymentMethod: String) {
        val items = _cart.value.items
        if (items.isEmpty()) { _message.value = "Giỏ hàng trống"; return }
        viewModelScope.launch {
            _orderResult.value = UiState.Loading
            try {
                val resp = orderApi.createOrder(CreateOrderRequest(
                    userId = userId,
                    items  = items.map { OrderItemReq(it.game.id, it.quantity) },
                    paymentMethod = paymentMethod,
                ))
                if (resp.isSuccessful && resp.body()?.data != null) {
                    val order = resp.body()!!.data!!
                    // Đánh dấu các game đã mua trong local DB
                    order.items.forEach { item ->
                        gameDao.markAsOwned(item.gameId)
                    }
                    cartDao.clearAll(userId)
                    _orderResult.value = UiState.Success(order.toModel())
                } else {
                    _orderResult.value = UiState.Error(resp.body()?.message ?: "Đặt hàng thất bại")
                }
            } catch (e: Exception) {
                _orderResult.value = UiState.Error("Không có kết nối mạng")
            }
        }
    }

    fun clearMessage()     { _message.value = null }
    fun clearOrderResult() { _orderResult.value = null }
}