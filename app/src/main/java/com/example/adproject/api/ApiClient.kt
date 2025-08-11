// api/ApiClient.kt
package com.example.adproject.api

import com.example.adproject.BuildConfig           // ✅ 用你 app 的 BuildConfig
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

object ApiClient {

    // ---- Base URL（Gradle 注入，兜底默认值）----
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/student/"
    private var baseUrl: String = BuildConfig.BASE_URL.ifBlank { DEFAULT_BASE_URL }

    // ---- Cookie ----
    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    // ---- 可选：统一鉴权 Token（登录后设置）----
    @Volatile private var authToken: String? = null
    fun updateAuthToken(token: String?) { authToken = token }

    // ---- 日志（只在 Debug 打印）----
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    // ---- 通用头 ----
    private val headerInterceptor = Interceptor { chain ->
        val b = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", "ADProject/1.0 (Android)")
        authToken?.let { b.header("Authorization", "Bearer $it") }
        chain.proceed(b.build())
    }

    // ---- OkHttp / Retrofit ----
    @Volatile private var okHttp: OkHttpClient = buildClient()
    @Volatile private var retrofit: Retrofit = buildRetrofit(okHttp, baseUrl)

    @Volatile lateinit var api: ApiService
        private set

    init { api = retrofit.create(ApiService::class.java) }

    private fun buildClient(): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

    private fun buildRetrofit(client: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    // ---- 工具 ----
    fun clearCookies() {
        try { cookieManager.cookieStore.removeAll() } catch (_: Exception) {}
    }

    @Synchronized
    fun switchBaseUrl(newBaseUrl: String) {
        baseUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
        okHttp = buildClient()
        retrofit = buildRetrofit(okHttp, baseUrl)
        api = retrofit.create(ApiService::class.java)
    }

    @Synchronized
    fun rebuild() {
        okHttp = buildClient()
        retrofit = buildRetrofit(okHttp, baseUrl)
        api = retrofit.create(ApiService::class.java)
    }
}
