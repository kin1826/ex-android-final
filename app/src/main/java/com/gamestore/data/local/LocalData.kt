package com.gamestore.data.local

import androidx.room.*
import com.gamestore.model.Game
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountPercent: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val genre: String = "",
    val developer: String = "",
    val thumbnailUrl: String = "",
    val isFeatured: Int = 0,
    val isHot: Int = 0,
    val isNew: Int = 0,
    val stock: Int = 999,
)

fun GameEntity.toModel() = Game(
    id = id, title = title, description = description,
    price = price, originalPrice = originalPrice,
    discountPercent = discountPercent, rating = rating,
    reviewCount = reviewCount, genre = genre, developer = developer,
    thumbnailUrl = thumbnailUrl,
    isFeatured = isFeatured == 1,
    isHot      = isHot == 1,
    isNew      = isNew == 1,
    stock      = stock,
)

@Entity(
    tableName = "cart_items",
    indices   = [Index(value = ["userId", "gameId"], unique = true)]
)
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val gameId: Int,
    val quantity: Int = 1,
    val gameTitle: String = "",
    val gamePrice: Double = 0.0,
    val gameThumbnail: String = "",
    val discountPercent: Int = 0,
)

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE isFeatured = 1 ORDER BY rating DESC")
    fun getFeatured(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isHot = 1 ORDER BY discountPercent DESC")
    fun getHotDeals(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isNew = 1")
    fun getNewReleases(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GameEntity?

    @Query("SELECT * FROM games WHERE title LIKE '%' || :q || '%' OR genre LIKE '%' || :q || '%'")
    suspend fun search(q: String): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<GameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(g: GameEntity)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :uid ORDER BY id DESC")
    fun getItems(uid: Int): Flow<List<CartEntity>>

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :uid")
    fun getCount(uid: Int): Flow<Int>

    @Query("SELECT SUM(quantity) FROM cart_items WHERE userId = :uid")
    fun getTotalQuantity(uid: Int): Flow<Int?>

    @Query("SELECT * FROM cart_items WHERE userId = :uid AND gameId = :gid LIMIT 1")
    suspend fun getItem(uid: Int, gid: Int): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartEntity)

    @Query("UPDATE cart_items SET quantity = :qty WHERE userId = :uid AND gameId = :gid")
    suspend fun updateQty(uid: Int, gid: Int, qty: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM cart_items WHERE userId = :uid")
    suspend fun clearAll(uid: Int)
}

@Database(
    entities  = [GameEntity::class, CartEntity::class],
    version   = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun cartDao(): CartDao
    companion object { const val NAME = "gamestore.db" }
}