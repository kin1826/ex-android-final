package com.gamestore.data.remote

import com.gamestore.data.local.GameEntity
import com.gamestore.data.local.LibraryEntity
import com.gamestore.model.*

fun GameDto.toModel() = Game(
    id = id, title = title,
    description = description,
    longDesc = longDesc,
    price = price, originalPrice = originalPrice,
    discountPercent = discountPercent,
    rating = rating, reviewCount = reviewCount, positivePct = positivePct,
    genre = genre, developer = developer, publisher = publisher,
    releaseDate = releaseDate, platforms = platforms, tags = tags,
    ageRating = ageRating, downloadSize = downloadSize,
    thumbnailUrl = thumbnailUrl, bannerUrl = bannerUrl,
    screenshotUrls = screenshotUrls, videoUrl = videoUrl,
    isFeatured = isFeatured, isHot = isHot, isNew = isNew, stock = stock,
    isOwned = isOwned,
)

fun GameDto.toEntity() = GameEntity(
    id = id, title = title, description = description,
    price = price, originalPrice = originalPrice,
    discountPercent = discountPercent, rating = rating,
    reviewCount = reviewCount, genre = genre, developer = developer,
    thumbnailUrl = thumbnailUrl,
    isFeatured = if (isFeatured) 1 else 0,
    isHot      = if (isHot) 1 else 0,
    isNew      = if (isNew) 1 else 0,
    isOwned    = if (isOwned) 1 else 0,
    stock      = stock,
)

fun UserDto.toModel() = User(
    id = id, username = username, email = email,
    displayName = displayName, phone = phone,
    walletBalance = walletBalance, points = points,
    membershipLevel = membershipLevel,
    isAdmin = isAdmin,
)

fun OrderDto.toModel() = Order(
    id = id, subtotal = subtotal, total = total,
    status = status, paymentMethod = paymentMethod,
    createdAt = createdAt,
    items = items.map {
        OrderItem(
            gameId = it.gameId, gameTitle = it.gameTitle,
            gameThumbnail = it.gameThumbnail,
            price = it.price, quantity = it.quantity,
        )
    },
)
fun LibraryEntity.toGame(): Game =
    Game(
        id = gameId,
        title = title,
        description = "",
        price = price,
        originalPrice = price,
        discountPercent = 0,
        rating = 0f,
        reviewCount = 0,
        genre = genre,
        developer = "",
        thumbnailUrl = thumbnailUrl,
        isFeatured = false,
        isHot = false,
        isNew = false,
        stock = 0
    )

fun Game.toLibraryEntity(userId: Int): LibraryEntity =
    LibraryEntity(
        userId = userId,
        gameId = id,
        title = title,
        genre = genre,
        thumbnailUrl = thumbnailUrl,
        price = price
    )