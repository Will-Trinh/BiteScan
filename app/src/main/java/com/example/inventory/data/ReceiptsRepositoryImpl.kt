package com.example.inventory.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ReceiptsRepositoryImpl(
    private val offlineRepo: OfflineReceiptsRepository,
    private val onlineRepo: OnlineReceiptsRepository
) : ReceiptsRepository {
    override fun getAllReceiptsStream(): Flow<List<Receipt>> = offlineRepo.getAllReceiptsStream()
    override suspend fun getReceipt(id: Int): Receipt? = offlineRepo.getReceipt(id)
    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = offlineRepo.getReceiptsForUser(userId)
    override fun searchReceipts(query: String): Flow<List<Receipt>> = offlineRepo.searchReceipts(query)
    override suspend fun insertReceipt(receipt: Receipt): Long = offlineRepo.insertReceipt(receipt)
    override suspend fun deleteReceipt(receipt: Receipt) = offlineRepo.deleteReceipt(receipt)
    override suspend fun updateReceipt(receipt: Receipt) = offlineRepo.updateReceipt(receipt)

    suspend fun fetchAndSyncReceipts(userId: String): List<Receipt> {
        return if (isNetworkAvailable()) {
            val onlineReceipts = onlineRepo.fetchReceiptsFromApi(userId)
            // return all the existing Receipt to syncs with the up coming receipt.
            val existingReceipts = offlineRepo.getAllReceiptsStream().first().associateBy { it.receiptId }

            onlineReceipts.forEach { onlineReceipt ->
                val existingReceipt = existingReceipts[onlineReceipt.receiptId]
                if (existingReceipt == null) {
                    offlineRepo.insertReceipt(onlineReceipt)
                } else if (onlineReceipt != existingReceipt) {
                    offlineRepo.updateReceipt(onlineReceipt)
                }
            }
            onlineReceipts
        } else {
            offlineRepo.getAllReceiptsStream().first()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        //check the connection on device and return true or false

        return true
    }
}