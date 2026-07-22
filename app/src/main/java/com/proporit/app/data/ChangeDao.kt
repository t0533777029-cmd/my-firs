package com.proporit.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChangeDao {
    @Insert
    suspend fun insert(event: ChangeEvent)

    @Query("SELECT * FROM change_events ORDER BY timestampEpochMillis DESC")
    fun observeAll(): Flow<List<ChangeEvent>>

    @Query("SELECT * FROM change_events ORDER BY timestampEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ChangeEvent>>
}
