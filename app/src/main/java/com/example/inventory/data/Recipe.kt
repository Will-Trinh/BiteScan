package com.example.inventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

/**
 * sau khi AI tạo ra recipes, thông tin được lưu ở đây
 * Entity data class represents a single row in the database.
 * receipts.kt để group nhiều items thành một receipt
 * Receipts: Một receipt có nhiều products (items).
 */
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,  // short desc, flavor notes
    val ingredients: String,  // JSON string of list (e.g., "[{name: 'chicken', quantity: 4}]") hoặc table riêng
    val instructions: String, // step-by-step text
    val nutrition: String,    // JSON for calories, protein, etc.
    val totalTime: Int,       // minutes
    val dateSaved: Date
)