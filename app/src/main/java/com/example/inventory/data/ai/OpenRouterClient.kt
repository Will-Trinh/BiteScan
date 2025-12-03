//package com.example.inventory.data.ai
//
//import kotlinx.serialization.json.Json
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
//import okhttp3.MediaType.Companion.toMediaType
//import retrofit2.converter.gson.GsonConverterFactory
//
//object OpenRouterClient {
//
//    private const val BASE_URL = "https://openrouter.ai/api/v1/"
//    private val API_KEY = "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"
//
//    private val json = Json {
//        ignoreUnknownKeys = true
//        encodeDefaults = true
//    }
//
//    private val authInterceptor = Interceptor { chain ->
//        val newRequest = chain.request().newBuilder()
//            .header("Authorization", "Bearer $API_KEY")
//            .header("HTTP-Referer", "https://github.com/tnguyen4513")
//            .header("X-Title", "PantryRecipesApp")
//            .build()
//        chain.proceed(newRequest)
//    }
//
//    private val logging = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val okHttpClient = OkHttpClient.Builder()
//        .addInterceptor(authInterceptor)
//        .addInterceptor(logging)
//        .build()
//
//    val api: OpenRouterApi by lazy {
//        val contentType = "application/json".toMediaType()
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory((GsonConverterFactory.create())
//            .build()
//            .create(OpenRouterApi::class.java)
//    }
//}
package com.example.inventory.data.ai

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenRouterClient {

    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    private const val API_KEY = "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" // TODO: move to BuildConfig

    private val authInterceptor = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer $API_KEY")
            .header("HTTP-Referer", "https://github.com/tnguyen4513") // required by OpenRouter
            .header("X-Title", "PantryRecipesApp")
            .build()
        chain.proceed(newRequest)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val api: OpenRouterApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())  // ✅ Gson converter
            .build()
            .create(OpenRouterApi::class.java)
    }
}
