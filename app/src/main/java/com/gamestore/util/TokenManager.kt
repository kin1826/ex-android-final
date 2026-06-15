package com.gamestore.util

import android.content.Context
import com.gamestore.model.User
import com.google.gson.Gson

class TokenManager(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("gs_prefs", Context.MODE_PRIVATE)
    private val gson  = Gson()

    fun save(token: String, userId: Int) =
        prefs.edit().putString("token", token).putInt("uid", userId).apply()

    fun saveUser(user: User) {
        prefs.edit().putString("user_data", gson.toJson(user)).apply()
    }

    fun getUser(): User? {
        val json = prefs.getString("user_data", null) ?: return null
        return try { gson.fromJson(json, User::class.java) } catch (e: Exception) { null }
    }

    fun getToken(): String?   = prefs.getString("token", null)
    fun getUserId(): Int      = prefs.getInt("uid", 0)
    fun isLoggedIn(): Boolean = getToken() != null
    fun clear()               = prefs.edit().clear().apply()
}

fun Double.toVND(): String = "%,.0f₫".format(this)