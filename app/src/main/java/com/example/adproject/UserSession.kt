package com.example.adproject

import android.content.Context

object UserSession {
    private const val PREF = "user_session"
    private const val KEY_ID = "user_id"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"

    fun save(ctx: Context, userId: Int, userName: String, email: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_ID, userId)
            .putString(KEY_NAME, userName)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun id(ctx: Context): Int? {
        val v = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_ID, -1)
        return if (v == -1) null else v
    }

    fun name(ctx: Context): String? =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_NAME, null)

    fun email(ctx: Context): String? =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_EMAIL, null)

    fun isLoggedIn(ctx: Context): Boolean = id(ctx) != null
}
