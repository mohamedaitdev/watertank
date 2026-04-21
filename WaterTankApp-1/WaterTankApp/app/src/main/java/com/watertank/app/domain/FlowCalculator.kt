package com.watertank.app.domain

import kotlin.math.max

/**
 * Pure functions for tank math. No framework deps - easy to unit test.
 *
 * Simplifying assumption: volume scales linearly with level (true for vertical tanks,
 * an approximation for horizontal cylinders). For a real horizontal cylinder use
 * [horizontalCylinderVolume] which accounts for the curved geometry.
 */
object FlowCalculator {

    data class Result(
        val flowM3PerHour: Double,
        val flowLitersPerSecond: Double,
        val minutesUntilFull: Double?,   // null if flow is zero or negative
        val etaEpochMillis: Long?,
        val projectedFullPct: Double
    )

    /** Linear model: volume = capacity * (level / maxLevel). */
    fun linearVolume(levelMeters: Double, maxLevelMeters: Double, capacityM3: Double): Double {
        if (maxLevelMeters <= 0) return 0.0
        return capacityM3 * (levelMeters / maxLevelMeters).coerceIn(0.0, 1.0)
    }

    /**
     * Exact partial volume for a horizontal cylinder.
     * @param h fill height (m), @param r radius (m), @param L length (m)
     * Returns m³.
     */
    fun horizontalCylinderVolume(h: Double, r: Double, L: Double): Double {
        if (r <= 0 || L <= 0) return 0.0
        val fill = h.coerceIn(0.0, 2 * r)
        val term1 = r * r * Math.acos((r - fill) / r)
        val term2 = (r - fill) * Math.sqrt(max(0.0, 2 * r * fill - fill * fill))
        return L * (term1 - term2)
    }

    /**
     * Compute flow rate and ETA from two level readings.
     * @param level1M     current level in meters
     * @param level2M     level after [deltaMinutes] minutes
     * @param deltaMinutes elapsed time between readings
     * @param capacityM3  tank capacity
     * @param maxLevelM   max physical level (for linear volume)
     */
    fun compute(
        level1M: Double,
        level2M: Double,
        deltaMinutes: Double,
        capacityM3: Double,
        maxLevelM: Double,
        nowMillis: Long = System.currentTimeMillis()
    ): Result {
        val v1 = linearVolume(level1M, maxLevelM, capacityM3)
        val v2 = linearVolume(level2M, maxLevelM, capacityM3)
        val deltaV = v2 - v1
        val flowPerHour = if (deltaMinutes > 0) deltaV * (60.0 / deltaMinutes) else 0.0
        val flowPerSec = flowPerHour * 1000.0 / 3600.0  // m³/h -> L/s

        val minutesLeft = if (flowPerHour > 0.001) {
            val remaining = capacityM3 - v2
            (remaining / flowPerHour) * 60.0
        } else null

        val eta = minutesLeft?.let { nowMillis + (it * 60_000L).toLong() }
        val projectedPct = if (capacityM3 > 0) (v2 / capacityM3) * 100.0 else 0.0

        return Result(
            flowM3PerHour = flowPerHour,
            flowLitersPerSecond = flowPerSec,
            minutesUntilFull = minutesLeft,
            etaEpochMillis = eta,
            projectedFullPct = projectedPct.coerceIn(0.0, 100.0)
        )
    }

    /** Human-readable "Xh Ym" formatter. */
    fun formatDuration(minutes: Double?): String {
        if (minutes == null) return "—"
        if (minutes.isNaN() || minutes.isInfinite()) return "—"
        val total = minutes.toLong().coerceAtLeast(0)
        val h = total / 60
        val m = total % 60
        return when {
            h == 0L -> "${m}m"
            m == 0L -> "${h}h"
            else    -> "${h}h ${m}m"
        }
    }
}
