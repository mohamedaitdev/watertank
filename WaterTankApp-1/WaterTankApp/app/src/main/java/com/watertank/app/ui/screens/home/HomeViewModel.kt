package com.watertank.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watertank.app.WaterTankApplication
import com.watertank.app.data.model.Tank
import com.watertank.app.data.model.TankReading
import com.watertank.app.data.preferences.PreferencesManager
import com.watertank.app.data.repository.TankRepository
import com.watertank.app.domain.FlowCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val tank: Tank? = null,
    val settings: PreferencesManager.Settings = PreferencesManager.Settings(),
    val currentLevelM: Double = 0.0,
    val currentVolumeM3: Double = 0.0,
    val fillFraction: Float = 0f,
    val flowM3PerHour: Double = 0.0,
    val flowLitersPerSec: Double = 0.0,
    val minutesUntilFull: Double? = null,
    val etaMillis: Long? = null,
    val lastTwoReadings: List<TankReading> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {

    private val app = WaterTankApplication.instance
    private val tankRepo: TankRepository = app.tankRepo
    private val prefs: PreferencesManager = app.prefs

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(tankRepo.observeTanks(), prefs.flow) { tanks, settings ->
                tanks to settings
            }.collect { (tanks, settings) ->
                val tank = tanks.firstOrNull()
                if (tank != null) {
                    observeReadings(tank, settings)
                } else {
                    _state.value = _state.value.copy(settings = settings, isLoading = false)
                }
            }
        }
    }

    private fun observeReadings(tank: Tank, settings: PreferencesManager.Settings) {
        viewModelScope.launch {
            tankRepo.observeRecentReadings(tank.id).collect { readings ->
                val last = readings.firstOrNull()
                val prev = readings.getOrNull(1)
                val levelM = last?.levelMeters ?: 0.0
                val volume = last?.volumeM3
                    ?: FlowCalculator.linearVolume(levelM, settings.maxLevelMeters, settings.capacityM3)
                val fraction = (volume / settings.capacityM3).toFloat().coerceIn(0f, 1f)

                val flowResult = if (last != null && prev != null) {
                    val dtMin = (last.timestamp - prev.timestamp) / 60_000.0
                    if (dtMin > 0.01) {
                        FlowCalculator.compute(
                            level1M = prev.levelMeters,
                            level2M = last.levelMeters,
                            deltaMinutes = dtMin,
                            capacityM3 = settings.capacityM3,
                            maxLevelM = settings.maxLevelMeters
                        )
                    } else null
                } else null

                _state.value = HomeUiState(
                    tank = tank,
                    settings = settings,
                    currentLevelM = levelM,
                    currentVolumeM3 = volume,
                    fillFraction = fraction,
                    flowM3PerHour = flowResult?.flowM3PerHour ?: 0.0,
                    flowLitersPerSec = flowResult?.flowLitersPerSecond ?: 0.0,
                    minutesUntilFull = flowResult?.minutesUntilFull,
                    etaMillis = flowResult?.etaEpochMillis,
                    lastTwoReadings = readings.take(2),
                    isLoading = false
                )
            }
        }
    }

    /** Quick-record a level reading from Home (optional convenience). */
    fun recordQuickReading(levelMeters: Double) {
        val current = _state.value
        val tank = current.tank ?: return
        viewModelScope.launch {
            val vol = FlowCalculator.linearVolume(levelMeters, current.settings.maxLevelMeters, current.settings.capacityM3)
            tankRepo.saveReading(TankReading(tankId = tank.id, levelMeters = levelMeters, volumeM3 = vol))
        }
    }
}
