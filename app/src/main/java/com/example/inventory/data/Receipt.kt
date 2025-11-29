package com.example.inventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import androidx.room.ForeignKey

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Receipt(
    @PrimaryKey(autoGenerate = true) val receiptId: Int = 0,
    val userId: Int,
    val date: Date,
    val source: String,  // market source
    val status: String   // Pending, Processing, Completed, Failed
)
