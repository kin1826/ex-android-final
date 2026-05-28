package com.gamestore.util

import android.content.Context

class TokenManager(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("gs_prefs", Context.MODE_PRIVATE)

    fun save(token: String, userId: Int) =
        prefs.edit().putString("token", token).putInt("uid", userId).apply()

    fun getToken(): String?   = prefs.getString("token", null)
    fun getUserId(): Int      = prefs.getInt("uid", 0)
    fun isLoggedIn(): Boolean = getToken() != null
    fun clear()               = prefs.edit().clear().apply()
}

fun Double.toVND(): String = "%,.0f₫".format(this)