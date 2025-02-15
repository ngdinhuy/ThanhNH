package com.example.myapplication

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Message::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDAO(): MessageDAO
}