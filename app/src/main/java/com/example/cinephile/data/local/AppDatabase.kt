package com.example.cinephile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        MovieEntity::class, // Your existing entity is now augmented
        UserListEntity::class,
        UserListMovieCrossRef::class
    ],
    version = 5, // IMPORTANT: We have changed the MovieEntity, so we MUST increment the version number.
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun userListDao(): UserListDao

    // SINGLETON PATTERN: Ensures only one database instance exists
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cinephile_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}