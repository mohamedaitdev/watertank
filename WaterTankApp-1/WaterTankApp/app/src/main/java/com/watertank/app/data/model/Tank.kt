package com.watertank.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A tank in the facility. Capacity in m³, max level in meters describes the physical height.
 * For horizontal cylindrical tanks you can still use a capacity-based model and treat level
 * as a percentage; for more exact computation, pass a dimension profile later.
 */
@Entity(tableName = "tanks")
data class Tank(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "Tank 1",
    val capacityM3: Double = 10_000.0,
    val maxLevelMeters: Double = 6.0,
    val fullThresholdPct: Int = 90,     // notify when level >= this
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "readings")
data class TankReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tankId: Long,
    val levelMeters: Double,
    val volumeM3: Double,
    val timestamp: Long = System.currentTimeMillis()
)
