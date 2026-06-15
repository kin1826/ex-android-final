package com.gamestore.model

data class Game(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountPercent: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val genre: String = "",
    val developer: String = "",
    val thumbnailUrl: String = "",
    val isFeatured: Boolean = false,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val isOwned: Boolean = false,
    val stock: Int = 999,
) {
    val finalPrice: Double
        get() = if (discountPercent > 0) price * (1 - discountPercent / 100.0) else price
    val hasDiscount: Boolean get() = discountPercent > 0
}

data class CartItem(
    val id: Int = 0,
    val game: Game = Game(),
    val quantity: Int = 1,
) {
    val totalPrice: Double get() = game.finalPrice * quantity
}

data class Cart(
    val items: List<CartItem> = emptyList(),
    val couponDiscount: Double = 0.0,
) {
    val subtotal: Double get() = items.sumOf { it.totalPrice }
    val total: Double    get() = (subtotal - couponDiscount).coerceAtLeast(0.0)
    val count: Int       get() = items.sumOf { it.quantity }
    val isEmpty: Boolean get() = items.isEmpty()
}

data class User(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val phone: String = "",
    val walletBalance: Double = 0.0,
    val points: Int = 0,
    val membershipLevel: String = "BRONZE",
    val isAdmin: Boolean = false,
)

data class Order(
    val id: Int = 0,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "PENDING",
    val paymentMethod: String = "WALLET",
    val createdAt: String = "",
    val items: List<OrderItem> = emptyList(),
)

data class OrderItem(
    val gameId: Int = 0,
    val gameTitle: String = "",
    val gameThumbnail: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
)

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}