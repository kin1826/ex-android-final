package com.gamestore.data.remote

import retrofit2.Response
import retrofit2.http.*

interface GameApi {

    @GET("games.php")
    suspend fun getFeatured(
        @Query("action") action: String = "featured",
        @Query("genre") genre: String? = null
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("games.php")
    suspend fun getHotDeals(
        @Query("action") action: String = "hot-deals",
        @Query("genre") genre: String? = null
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("games.php")
    suspend fun getNewReleases(
        @Query("action") action: String = "new-releases",
        @Query("genre") genre: String? = null
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("games.php")
    suspend fun getGames(
        @Query("genre")    genre: String?  = null,
        @Query("search")   search: String? = null,
        @Query("platform") platform: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sortBy")   sortBy: String?   = null,
        @Query("onlyDiscounted") onlyDiscounted: Boolean? = null,
        @Query("page")     page: Int       = 0,
        @Query("pageSize") size: Int       = 20,
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("games.php")
    suspend fun getById(
        @Query("id") id: Int,
        @Query("userId") userId: Int? = null,
    ): Response<ApiResponse<GameDto>>

    @GET("games.php")
    suspend fun search(
        @Query("action") action: String = "search",
        @Query("q") q: String,
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("categories.php")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>

    // ── ADMIN ──
    @POST("games.php")
    suspend fun addGame(@Body body: GameRequest): Response<ApiResponse<Int>>

    @PUT("games.php")
    suspend fun updateGame(@Body body: GameRequest): Response<ApiResponse<Unit>>

    @DELETE("games.php")
    suspend fun deleteGame(
        @Query("id") id: Int,
        @Query("adminId") adminId: Int
    ): Response<ApiResponse<Unit>>
}

interface AuthApi {
    @POST("auth.php")
    suspend fun login(
        @Query("action") action: String = "login",
        @Body body: LoginRequest,
    ): Response<ApiResponse<AuthData>>

    @POST("auth.php")
    suspend fun register(
        @Query("action") action: String = "register",
        @Body body: RegisterRequest,
    ): Response<ApiResponse<AuthData>>
}

interface OrderApi {
    @GET("orders.php")
    suspend fun getOrders(
        @Query("userId") userId: Int
    ): Response<ApiResponse<List<OrderDto>>>

    @POST("orders.php")
    suspend fun createOrder(
        @Body body: CreateOrderRequest
    ): Response<ApiResponse<OrderDto>>
}

interface LibraryApi {
    @GET("libraries.php")
    suspend fun getLibrary(
        @Query("user_id") userId: Int
    ): Response<ApiResponse<List<LibraryItemDto>>>
}

interface UserApi {
    @GET("users.php")
    suspend fun getProfile(
        @Query("action") action: String = "profile",
        @Query("userId") userId: Int
    ): Response<ApiResponse<UserDto>>

    @POST("users.php")
    suspend fun deposit(
        @Query("action") action: String = "deposit",
        @Body body: DepositRequest
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("deposits.php")
    suspend fun requestDeposit(
        @Body body: DepositRequest
    ): Response<ApiResponse<DepositResponse>>

    @GET("deposits.php")
    suspend fun getMyDeposits(
        @Query("userId") userId: Int
    ): Response<ApiResponse<List<DepositDto>>>

    @GET("deposits.php")
    suspend fun getAdminDeposits(
        @Query("adminId") adminId: Int
    ): Response<ApiResponse<List<DepositDto>>>

    @POST("deposits.php")
    suspend fun updateDepositStatus(
        @Body body: AdminDepositActionRequest
    ): Response<ApiResponse<Unit>>
}

interface AdminApi {
    @GET("admin_stats.php")
    suspend fun getStats(
        @Query("adminId") adminId: Int
    ): Response<ApiResponse<AdminStatsDto>>

    @POST("categories.php")
    suspend fun addCategory(@Body body: CategoryRequest): Response<ApiResponse<String>>

    @DELETE("categories.php")
    suspend fun deleteCategory(
        @Query("id") id: Int,
        @Query("adminId") adminId: Int
    ): Response<ApiResponse<String>>

    // ── QUẢN LÝ NGƯỜI DÙNG ──
    @GET("admin_users.php")
    suspend fun getUsers(
        @Query("adminId") adminId: Int,
        @Query("q") search: String? = null
    ): Response<ApiResponse<List<UserDto>>>

    @POST("admin_users.php")
    suspend fun updateWallet(
        @Query("action") action: String = "update_wallet",
        @Body body: WalletUpdateRequest
    ): Response<ApiResponse<Unit>>

    @POST("admin_users.php")
    suspend fun resetPassword(
        @Query("action") action: String = "reset_password",
        @Body body: GenericAdminRequest
    ): Response<ApiResponse<Unit>>

    @POST("admin_users.php")
    suspend fun updateUserStatus(
        @Query("action") action: String = "update_status",
        @Body body: UserStatusUpdateRequest
    ): Response<ApiResponse<Unit>>
}

interface WishlistApi {
    @GET("wishlist.php")
    suspend fun getWishlist(
        @Query("userId") userId: Int
    ): Response<ApiResponse<List<GameDto>>>

    @POST("wishlist.php")
    suspend fun toggleWishlist(
        @Body body: WishlistToggleRequest
    ): Response<ApiResponse<WishlistToggleResponse>>
}

interface NotificationApi {
    @GET("notifications.php")
    suspend fun getNotifications(
        @Query("userId") userId: Int
    ): Response<ApiResponse<List<NotificationDto>>>

    @POST("notifications.php")
    suspend fun markRead(
        @Body body: MarkReadRequest
    ): Response<ApiResponse<Unit>>
}

