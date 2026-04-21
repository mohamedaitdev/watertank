package com.watertank.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.watertank.app.data.model.ChlorineBottle
import kotlinx.coroutines.flow.Flow

@Dao
interface ChlorineDao {
    @Query("SELECT * FROM chlorine_bottles ORDER BY id ASC")
    fun observeAll(): Flow<List<ChlorineBottle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bottle: ChlorineBottle): Long

    @Update
    suspend fun update(bottle: ChlorineBottle)

    @Delete
    suspend fun delete(bottle: ChlorineBottle)

    @Query("SELECT COUNT(*) FROM chlorine_bottles")
    suspend fun count(): Int
}
