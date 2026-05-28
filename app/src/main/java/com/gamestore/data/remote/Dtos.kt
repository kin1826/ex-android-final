package com.gamestore.data.remote

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String  = "",
    val data: T?         = null,
)

data class PagedData<T>(
    val items: List<T> = emptyList(),
    val total: Int     = 0,
)

data class GameDto(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    @SerializedName("originalPrice")   val originalPrice: Double = 0.0,
    @SerializedName("discountPercent") val discountPercent: Int = 0,
    val rating: Float = 0f,
    @SerializedName("reviewCount")     val reviewCount: Int = 0,
    val genre: String = "",
    val developer: String = "",
    @SerializedName("thumbnailUrl")    val thumbnailUrl: String = "",
    @SerializedName("isFeatured")      val isFeatured: Boolean = false,
    @SerializedName("isHot")           val isHot: Boolean = false,
    @SerializedName("isNew")           val isNew: Boolean = false,
    val stock: Int = 999,
)

data class CategoryDto(
    val id: Int = 0,
    val name: String = "",
    val iconEmoji: String = "",
)

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val username: String, val email: String,
    val password: String, val displayName: String, val phone: String = "",
)

data class AuthData(
    val accessToken: String = "",
    val userId: Int = 0,
    val user: UserDto = UserDto(),
)

data class UserDto(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val phone: String = "",
    val walletBalance: Double = 0.0,
    val points: Int = 0,
    val membershipLevel: String = "BRONZE",
)

data class CreateOrderRequest(
    val userId: Int,
    val items: List<OrderItemReq>,
    val paymentMethod: String = "WALLET",
    val note: String = "",
)

data class OrderItemReq(val gameId: Int, val quantity: Int)

data class OrderDto(
    val id: Int = 0,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "",
    val paymentMethod: String = "",
    val createdAt: String = "",
    val items: List<OrderItemDto> = emptyList(),
)

data class OrderItemDto(
    val gameId: Int = 0,
    val gameTitle: String = "",
    val gameThumbnail: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
)