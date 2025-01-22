package com.example.uou_alarm_it

import androidx.room.*

@Entity(tableName = "Bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = false)
    val notice : Notice
)
