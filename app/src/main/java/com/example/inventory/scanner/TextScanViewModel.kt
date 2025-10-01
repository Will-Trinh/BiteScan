package com.example.inventory.scanner

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.inventory.data.Item

// ReceiptScanner module to extract receipt info from image
object ReceiptScanner {

    suspend fun scanReceipt(context: Context, @DrawableRes drawableId: Int): List<Item> {
        // Load bitmap from drawable
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val image = InputImage.fromBitmap(bitmap, 0)

        // Set up text recognizer
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Process image
        val result = recognizer.process(image).await()

        // Extract lines, sorted by vertical position to maintain order
        val lines = result.textBlocks.flatMap { it.lines }.sortedBy { it.boundingBox?.top ?: 0 }
        val textLines = lines.map { it.text.trim() }

        // Extract store (assuming first line is store name)
        val store = textLines.firstOrNull { it.isNotBlank() } ?: "Unknown Store"

        // Extract date (look for line matching MM/dd/yyyy)
        var dateStr = textLines.find { it.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) }
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val parsedDate = try {
            if (dateStr != null) Date(dateFormat.parse(dateStr)!!.time) else Date(System.currentTimeMillis())
        } catch (e: Exception) {
            Date(System.currentTimeMillis())
        }

        // Find item lines: between header and subtotal/tax/total, containing $
        val itemStartIndex = textLines.indexOfFirst { it.contains("Cashier:") } + 1
        val itemEndIndex = textLines.indexOfFirst { it.contains("SUBTOTAL") }
        val itemLines = if (itemStartIndex > 0 && itemEndIndex > itemStartIndex) {
            textLines.subList(itemStartIndex, itemEndIndex)
        } else {
            textLines.filter { it.contains("$") && !it.contains("SUBTOTAL") && !it.contains("TAX") && !it.contains("TOTAL") }
        }

        // Parse items
        val items = mutableListOf<Item>()
        val itemRegexWithQty = Regex("(.*?)(\\d+) @ (\\d+\\.\\d+)/EA \\$(\\d+\\.\\d+) TFA")
        val itemRegexWithoutQty = Regex("(.*?)(\\d+\\.\\d+)/EA \\$(\\d+\\.\\d+) TFA")

        for (line in itemLines) {
            itemRegexWithQty.find(line)?.let { match ->
                val name = match.groupValues[1].trim()
                val quantity = match.groupValues[2].toFloat()
                val unitPrice = match.groupValues[3].toDouble()
                val total = match.groupValues[4].toDouble()
                // Verify quantity if needed: if (total != quantity * unitPrice) adjust, but assume correct
                items.add(Item(name = name, price = unitPrice, quantity = quantity, date = parsedDate, store = store, category = "Food")) // Category default
            } ?: itemRegexWithoutQty.find(line)?.let { match ->
                val name = match.groupValues[1].trim()
                val unitPrice = match.groupValues[2].toDouble()
                val total = match.groupValues[3].toDouble()
                val quantity = if (unitPrice > 0) (total / unitPrice).toFloat() else 1f // Calculate quantity if multiple
                items.add(Item(name = name, price = unitPrice, quantity = quantity, date = parsedDate, store = store, category = "Food"))
            }
        }

        return items
    }


}
