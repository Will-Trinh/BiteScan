package com.example.inventory.data

import retrofit2.http.GET import retrofit2.http.Path

interface ReceipstApiService { @GET("abc/{userId}/2") suspend fun getReceipts(@Path("userId") userId: String): List<Receipt> }
interface ReceiptApiService { @GET("xyz/{userId}/2") suspend fun getReceipts(@Path("userId") userId: String): Receipt}