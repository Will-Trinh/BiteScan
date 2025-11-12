package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date

/**
 * thông tin hóa đơn của user được lưu tại đây
 */
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["receiptId"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    @ColumnInfo(name = "quantity")
    val quantity: Float,
    val date: Date,
    val store: String,
    val category: String,
    @ColumnInfo(name = "receiptId")
    val receiptId: Int,
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fats: Double? = null,
)