package com.gamestore.model

data class Game(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val longDesc: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountPercent: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val positivePct: Int = 85,
    val genre: String = "",
    val developer: String = "",
    val publisher: String = "",
    val releaseDate: String = "",
    val platforms: String = "",
    val tags: List<String> = emptyList(),
    val ageRating: String = "",
    val downloadSize: String = "",
    val thumbnailUrl: String = "",
    val bannerUrl: String = "",
    val screenshotUrls: List<String> = emptyList(),
    val videoUrl: String = "",
    val isFeatured: Boolean = false,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val stock: Int = 999,
) {
    val finalPrice: Double
        get() = if (discountPercent > 0) price * (1 - discountPercent / 100.0) else price
    val hasDiscount: Boolean get() = discountPercent > 0

    // Nhãn đánh giá theo % tích cực (giống Steam)
    val ratingLabel: String get() = when {
        positivePct >= 95 -> "Cực kỳ tích cực"
        positivePct >= 80 -> "Rất tích cực"
        positivePct >= 70 -> "Tích cực"
        positivePct >= 40 -> "Hỗn hợp"
        else              -> "Tiêu cực"
    }
    val ratingColor: Long get() = when {
        positivePct >= 80 -> 0xFF4ADEAC  // xanh lá
        positivePct >= 40 -> 0xFFFBBF24  // vàng
        else              -> 0xFFF87171  // đỏ
    }
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