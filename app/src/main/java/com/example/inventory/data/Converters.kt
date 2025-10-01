package com.example.inventory.data

import androidx.room.TypeConverter
import java.sql.Date
//Để hỗ trợ java.sql.Date
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}