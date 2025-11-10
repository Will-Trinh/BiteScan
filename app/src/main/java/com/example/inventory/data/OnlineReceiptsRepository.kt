package com.example.inventory.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Date

class OnlineReceiptsRepository(private val apiService: ReceipstApiService) {
    suspend fun fetchReceiptsFromApi(userId: String): List<Receipt> {
        return apiService.getReceipts(userId)
    }

    companion object {
        fun create(): OnlineReceiptsRepository {
            val retrofit = Retrofit.Builder()
                .baseUrl("http:HoangHandsome.com/api/") //change api when available
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val apiService = retrofit.create(ReceipstApiService::class.java)
            return OnlineReceiptsRepository(apiService)
        }
    }
}