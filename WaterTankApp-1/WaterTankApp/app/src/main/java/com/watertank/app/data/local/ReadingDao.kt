package com.watertank.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.watertank.app.data.model.TankReading
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings WHERE tankId = :tankId ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(tankId: Long, limit: Int = 50): Flow<List<TankReading>>

    @Query("SELECT * FROM readings WHERE tankId = :tankId ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(tankId: Long): TankReading?

    @Insert
    suspend fun insert(reading: TankReading): Long

    @Query("DELETE FROM readings WHERE tankId = :tankId")
    suspend fun clear(tankId: Long)
}
