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
        @Query("id") id: Int
    ): Response<ApiResponse<GameDto>>

    @GET("games.php")
    suspend fun search(
        @Query("action") action: String = "search",
        @Query("q") q: String,
    ): Response<ApiResponse<List<GameDto>>>

    @GET("games.php")
    suspend fun getCategories(
        @Query("action") action: String = "categories"
    ): Response<ApiResponse<List<CategoryDto>>>
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
