package com.gamestore.data.remote

import retrofit2.Response
import retrofit2.http.*

interface GameApi {

    // featured, hot-deals, new-releases đều trả về PagedData (có items bên trong)
    @GET("games.php")
    suspend fun getFeatured(
        @Query("action") action: String = "featured"
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("games.php")
    suspend fun getHotDeals(
        @Query("action") action: String = "hot-deals"
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("games.php")
    suspend fun getNewReleases(
        @Query("action") action: String = "new-releases"
    ): Response<ApiResponse<PagedData<GameDto>>>

    @GET("games.php")
    suspend fun getGames(
        @Query("genre")    genre: String?  = null,
        @Query("search")   search: String? = null,
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
    ): Response<ApiResponse<List<GameDto>>>

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
}