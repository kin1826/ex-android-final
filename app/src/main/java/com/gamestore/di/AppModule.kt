package com.gamestore.di

import android.content.Context
import androidx.room.Room
import com.gamestore.BuildConfig
import com.gamestore.data.local.*
import com.gamestore.data.remote.*
import com.gamestore.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideTokenManager(@ApplicationContext ctx: Context) = TokenManager(ctx)

    @Provides @Singleton
    fun provideOkHttp(tm: TokenManager): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = tm.getToken()?.let {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $it").build()
                } ?: chain.request()
                chain.proceed(req)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton fun provideGameApi(r: Retrofit): GameApi   = r.create(GameApi::class.java)
    @Provides @Singleton fun provideAuthApi(r: Retrofit): AuthApi   = r.create(AuthApi::class.java)
    @Provides @Singleton fun provideOrderApi(r: Retrofit): OrderApi = r.create(OrderApi::class.java)
    @Provides @Singleton fun provideLibraryApi(r: Retrofit): LibraryApi = r.create(LibraryApi::class.java)
    @Provides @Singleton fun provideUserApi(r: Retrofit): UserApi   = r.create(UserApi::class.java)
    @Provides @Singleton fun provideAdminApi(r: Retrofit): AdminApi = r.create(AdminApi::class.java)
    @Provides @Singleton fun provideWishlistApi(r: Retrofit): WishlistApi = r.create(WishlistApi::class.java)

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideGameDao(db: AppDatabase) = db.gameDao()
    @Provides fun provideCartDao(db: AppDatabase) = db.cartDao()
    @Provides fun provideLibraryDao(db: AppDatabase): LibraryDao = db.libraryDao()
}