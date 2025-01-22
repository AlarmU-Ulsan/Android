package com.example.uou_alarm_it

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Bookmark::class], exportSchema = false, version = 1)
abstract class BookmarkDB : RoomDatabase() {
    abstract fun getDao() : BookmarkDAO

    companion object {
        private var INSTANCE: BookmarkDB? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        @Synchronized
        fun getDatabase(context: Context) : BookmarkDB {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context, BookmarkDB::class.java, "school_database")
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
            return INSTANCE as BookmarkDB
        }
    }
}