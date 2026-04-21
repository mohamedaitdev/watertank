package com.watertank.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val CAPACITY    = doublePreferencesKey("capacity_m3")
        private val MAX_LEVEL   = doublePreferencesKey("max_level_m")
        private val THRESHOLD   = intPreferencesKey("full_threshold_pct")
        private val ALARM_ON    = booleanPreferencesKey("alarm_enabled")
        private val VIBRATE_ON  = booleanPreferencesKey("vibrate_enabled")
    }

    data class Settings(
        val capacityM3: Double = 10_000.0,
        val maxLevelMeters: Double = 6.0,
        val fullThresholdPct: Int = 90,
        val alarmEnabled: Boolean = true,
        val vibrateEnabled: Boolean = true
    )

    val flow: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            capacityM3 = p[CAPACITY] ?: 10_000.0,
            maxLevelMeters = p[MAX_LEVEL] ?: 6.0,
            fullThresholdPct = p[THRESHOLD] ?: 90,
            alarmEnabled = p[ALARM_ON] ?: true,
            vibrateEnabled = p[VIBRATE_ON] ?: true
        )
    }

    suspend fun setCapacity(v: Double)       = context.dataStore.edit { it[CAPACITY] = v }
    suspend fun setMaxLevel(v: Double)       = context.dataStore.edit { it[MAX_LEVEL] = v }
    suspend fun setThreshold(v: Int)         = context.dataStore.edit { it[THRESHOLD] = v.coerceIn(10, 100) }
    suspend fun setAlarmEnabled(v: Boolean)  = context.dataStore.edit { it[ALARM_ON] = v }
    suspend fun setVibrateEnabled(v: Boolean)= context.dataStore.edit { it[VIBRATE_ON] = v }
}
