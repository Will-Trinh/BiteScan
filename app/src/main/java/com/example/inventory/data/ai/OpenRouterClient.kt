package com.example.inventory.data.ai

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenRouterClient {
    private var apiKey: String? = null
    private var isInitialized = false
    private const val TAG = "OpenRouterClient"

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            apiKey = appInfo.metaData?.getString("openRouterApiKey")
            if (apiKey.isNullOrBlank()) {
                Log.e(TAG, "API key not found in manifest!")
                return
            }
            isInitialized = true
            Log.d(TAG, "OpenRouterClient initialized.")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Failed to load metadata", e)
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val key = apiKey ?: throw IllegalStateException("Not initialized! Call init(context).")
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $key")
            .addHeader("HTTP-Referer", "https://github.com/yourusername")  // Your GitHub
            .addHeader("X-Title", "InventoryApp")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // Change to NONE for release
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: OpenRouterApi by lazy {
        if (!isInitialized) throw IllegalStateException("Init first!")
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApi::class.java)
    }
}