package com.example.inventory.data
import kotlinx.coroutines.flow.Flow

class OfflineReceiptsRepository(private val receiptDao: ReceiptDao) : ReceiptsRepository {
    override fun getAllReceiptsStream(): Flow<List<Receipt>> = receiptDao.getAllReceipts()
    override suspend fun getReceipt(id: Int): Receipt? = receiptDao.getReceipt(id)
    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = receiptDao.getReceiptsForUser(userId)
    override fun searchReceipts(query: String): Flow<List<Receipt>> = receiptDao.searchReceipts(query)
    override suspend fun insertReceipt(receipt: Receipt): Long = receiptDao.insertReceipt(receipt)
    override suspend fun deleteReceipt(receipt: Receipt) = receiptDao.deleteReceipt(receipt)
    override suspend fun updateReceipt(receipt: Receipt) = receiptDao.updateReceipt(receipt)

}