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
    val longDesc: String = "",
    val price: Double = 0.0,
    @SerializedName("originalPrice")   val originalPrice: Double = 0.0,
    @SerializedName("discountPercent") val discountPercent: Int = 0,
    val rating: Float = 0f,
    @SerializedName("reviewCount")     val reviewCount: Int = 0,
    @SerializedName("positivePct")     val positivePct: Int = 85,
    val genre: String = "",
    val developer: String = "",
    val publisher: String = "",
    val releaseDate: String = "",
    val platforms: String = "",
    val tags: List<String> = emptyList(),
    val ageRating: String = "",
    val downloadSize: String = "",
    @SerializedName("thumbnailUrl")    val thumbnailUrl: String = "",
    @SerializedName("bannerUrl")       val bannerUrl: String = "",
    @SerializedName("screenshotUrls")  val screenshotUrls: List<String> = emptyList(),
    @SerializedName("videoUrl")        val videoUrl: String = "",
    @SerializedName("isFeatured")      val isFeatured: Boolean = false,
    @SerializedName("isHot")           val isHot: Boolean = false,
    @SerializedName("isNew")           val isNew: Boolean = false,
    @SerializedName("isOwned")         val isOwned: Boolean = false,
    @SerializedName("isFavorite")      val isFavorite: Boolean = false,
    val stock: Int = 999,
)

data class CategoryDto(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("iconEmoji") val iconEmoji: String = "",
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
    val isAdmin: Boolean = false,
    val isActive: Boolean = true,
)

data class CreateOrderRequest(
    val userId: Int,
    val items: List<OrderItemReq>,
    val paymentMethod: String = "WALLET",
    val note: String = "",
)

data class DepositRequest(
    val userId: Int,
    val amount: Double
)

data class DepositResponse(
    val id: Int,
    val amount: Double,
    val memo: String,
    val status: String,
    @SerializedName("bank_info") val bankInfo: BankInfo
)

data class BankInfo(
    val account_name: String,
    val account_number: String,
    val bank_name: String
)

data class DepositDto(
    val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    val amount: Double = 0.0,
    val memo: String = "",
    val status: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("display_name") val userName: String? = null,
    @SerializedName("email") val userEmail: String? = null
)

data class AdminDepositActionRequest(
    val adminId: Int,
    val depositId: Int,
    val action: String, // approve, reject
    val adminNote: String? = null
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

data class LibraryItemDto(
    val id: Int,
    val userId: Int,
    val gameId: Int,

    val purchaseDate: String = "",
    val isFavorite: Boolean = false,
    val playtimeMinutes: Int = 0,

    val gameTitle: String,
    val genre: String,
    val price: Double,
    val thumbnailUrl: String = "",
    val description: String = "",
    val discountPercent: Double = 0.0
)


data class AdminStatsDto(
    val totalUsers: Int = 0,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val topGames: List<AdminTopGameDto> = emptyList()
)

data class AdminTopGameDto(
    val id: Int = 0,
    val title: String = "",
    val thumbnailUrl: String = "",
    val genre: String = "",
    val price: Double = 0.0,
    val salesCount: Int = 0
)

data class CategoryRequest(
    val adminId: Int,
    val name: String,
    val iconEmoji: String
)

data class GameRequest(
    val adminId: Int,
    val id: Int? = null,
    val title: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val genre: String,
    val developer: String,
    val publisher: String,
    val releaseDate: String,
    val platforms: String,
    val downloadSize: String,
    val thumbnailUrl: String,
    val isFeatured: Int,
    val isHot: Int,
    val isNew: Int
)

data class WalletUpdateRequest(
    val adminId: Int,
    val userId: Int,
    val amount: Double
)

data class UserStatusUpdateRequest(
    val adminId: Int,
    val userId: Int,
    val isAdmin: Int? = null,
    val isActive: Int? = null
)

data class GenericAdminRequest(
    val adminId: Int,
    val userId: Int
)

data class WishlistToggleRequest(
    val userId: Int,
    val gameId: Int
)

data class WishlistToggleResponse(
    val isFavorite: Boolean
)

data class NotificationDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val title: String,
    val message: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class MarkReadRequest(
    val userId: Int,
    @SerializedName("notificationId") val notificationId: Int = 0
)
