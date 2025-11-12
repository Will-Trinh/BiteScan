package com.example.inventory.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ReceiptsRepositoryImpl(
    private val offlineRepo: OfflineReceiptsRepository,
    private val onlineRepo: OnlineReceiptsRepository,
    private val context: Context
) : ReceiptsRepository {
    override fun getAllReceiptsStream(): Flow<List<Receipt>> = offlineRepo.getAllReceiptsStream()
    override suspend fun getReceipt(id: Int): Receipt? = offlineRepo.getReceipt(id)
    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = offlineRepo.getReceiptsForUser(userId)
    override fun searchReceipts(query: String): Flow<List<Receipt>> = offlineRepo.searchReceipts(query)
    override suspend fun insertReceipt(receipt: Receipt): Long = offlineRepo.insertReceipt(receipt)
    override suspend fun deleteReceipt(receipt: Receipt) = offlineRepo.deleteReceipt(receipt)
    override suspend fun updateReceipt(receipt: Receipt) = offlineRepo.updateReceipt(receipt)


    suspend fun fetchAndSyncReceipts(userId: Int): List<Receipt> {
        return if (isNetworkAvailable()) {
            onlineRepo.syncReceiptsFromServer()
            offlineRepo.getReceiptsForUser(userId).first()
        } else {
            offlineRepo.getReceiptsForUser(userId).first()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}