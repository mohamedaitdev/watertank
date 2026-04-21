package com.watertank.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.watertank.app.data.model.Tank
import kotlinx.coroutines.flow.Flow

@Dao
interface TankDao {
    @Query("SELECT * FROM tanks ORDER BY id ASC")
    fun observeAll(): Flow<List<Tank>>

    @Query("SELECT * FROM tanks WHERE id = :id")
    suspend fun getById(id: Long): Tank?

    @Query("SELECT * FROM tanks ORDER BY id ASC LIMIT 1")
    suspend fun getFirst(): Tank?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tank: Tank): Long

    @Update
    suspend fun update(tank: Tank)

    @Query("DELETE FROM tanks WHERE id = :id")
    suspend fun delete(id: Long)
}
