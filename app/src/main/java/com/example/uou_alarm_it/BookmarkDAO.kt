package com.example.uou_alarm_it

import androidx.room.*

@Dao
interface BookmarkDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(notice: Notice)

    @Query("SELECT * FROM Bookmarks")
    fun getBookmarks(): ArrayList<Notice>

    @Delete
    suspend fun deleteBookmark(notice: Notice)
}