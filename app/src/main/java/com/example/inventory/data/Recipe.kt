package com.example.inventory.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date

/**
 * after Ai create new recipe, the new recipe will be added to the database.
 * Entity data class represents a single row in the database.
 * In each recipe includes the following fields: item (name, price, quantity) is used from the receipt.
 * Also, the recipe must be a linked list of steps to define the time for each step,
 */
@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Recipe(
    @PrimaryKey(autoGenerate = true) var recipeId: Int = 0,
    val userId: Int,
    val source: String,
    val title: String,
    val description: String,  // short desc, flavor notes
    val ingredients: String,  // JSON string of list (e.g., "[{name: 'chicken', quantity: 4}]")
    val instructions: String, // step-by-step with the time for each step
    val nutrition: String,    // JSON for calories, protein, etc.
    val totalTime: Int,       // minutes
    val dateSaved: Date,
    val servings: Int
)