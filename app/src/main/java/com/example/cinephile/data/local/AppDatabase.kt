package com.example.cinephile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, MovieEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    // abstract fun movieDao(): MovieDao // etc.
}