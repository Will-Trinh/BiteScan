package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date


//store information about the user in each session
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    @ColumnInfo(name = "userId")
    val userId: Int, // need to change to String when use UUID
    @ColumnInfo(name = "userName")
    val userName: String,
    @ColumnInfo(name = "startTime")
    val startTime: Date,
    @ColumnInfo(name = "endTime")
    val endTime: Date,
    @ColumnInfo(name = "status")
    val status: String
)