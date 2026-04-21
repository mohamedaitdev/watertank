package com.watertank.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BottleStatus { WORKING, EMPTY, NOT_WORKING, STANDBY }

@Entity(tableName = "chlorine_bottles")
data class ChlorineBottle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val status: BottleStatus = BottleStatus.STANDBY,
    val note: String = "",
    val lastChangedAt: Long = System.currentTimeMillis()
)
