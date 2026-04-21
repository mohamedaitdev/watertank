package com.watertank.app.ui.screens.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watertank.app.WaterTankApplication
import com.watertank.app.data.model.TankReading
import com.watertank.app.data.preferences.PreferencesManager
import com.watertank.app.domain.FlowCalculator
import com.watertank.app.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val settings: PreferencesManager.Settings = PreferencesManager.Settings(),
    val level1Text: String = "",
    val level2Text: String = "",
    val intervalMinutesText: String = "15",
    val result: FlowCalculator.Result? = null,
    val errorMessage: String? = null,
    val savedConfirmation: String? = null
)

class CalculatorViewModel : ViewModel() {

    private val app = WaterTankApplication.instance
    private val tankRepo = app.tankRepo
    private val prefs = app.prefs

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.flow.collect { s -> _state.value = _state.value.copy(settings = s) }
        }
    }

    fun updateLevel1(v: String) { _state.value = _state.value.copy(level1Text = v, errorMessage = null, savedConfirmation = null) }
    fun updateLevel2(v: String) { _state.value = _state.value.copy(level2Text = v, errorMessage = null, savedConfirmation = null) }
    fun updateInterval(v: String) { _state.value = _state.value.copy(intervalMinutesText = v, errorMessage = null) }

    fun calculate() {
        val s = _state.value
        val l1 = s.level1Text.toDoubleOrNull()
        val l2 = s.level2Text.toDoubleOrNull()
        val dt = s.intervalMinutesText.toDoubleOrNull() ?: 15.0

        if (l1 == null || l2 == null) {
            _state.value = s.copy(errorMessage = "Enter both levels in meters", result = null)
            return
        }
        if (dt <= 0) {
            _state.value = s.copy(errorMessage = "Interval must be greater than 0", result = null)
            return
        }

        val result = FlowCalculator.compute(
            level1M = l1,
            level2M = l2,
            deltaMinutes = dt,
            capacityM3 = s.settings.capacityM3,
            maxLevelM = s.settings.maxLevelMeters
        )
        _state.value = s.copy(result = result, errorMessage = null)
    }

    /** Save both readings so the Home screen picks up the ETA. */
    fun saveReadings() {
        val s = _state.value
        val l1 = s.level1Text.toDoubleOrNull() ?: return
        val l2 = s.level2Text.toDoubleOrNull() ?: return
        val dt = s.intervalMinutesText.toDoubleOrNull() ?: 15.0

        viewModelScope.launch {
            val tank = tankRepo.getFirstTank() ?: return@launch
            val now = System.currentTimeMillis()
            val earlier = now - (dt * 60_000L).toLong()

            val v1 = FlowCalculator.linearVolume(l1, s.settings.maxLevelMeters, s.settings.capacityM3)
            val v2 = FlowCalculator.linearVolume(l2, s.settings.maxLevelMeters, s.settings.capacityM3)

            tankRepo.saveReading(TankReading(tankId = tank.id, levelMeters = l1, volumeM3 = v1, timestamp = earlier))
            tankRepo.saveReading(TankReading(tankId = tank.id, levelMeters = l2, volumeM3 = v2, timestamp = now))

            _state.value = _state.value.copy(savedConfirmation = "Readings saved ✓")
        }
    }

    /**
     * Schedule an alarm when the tank will hit [thresholdPct] based on current flow.
     * If no result yet, compute first.
     */
    fun scheduleFullAlarm() {
        val s = _state.value
        val r = s.result ?: run {
            calculate(); _state.value.result
        } ?: return

        val capacity = s.settings.capacityM3
        val flow = r.flowM3PerHour
        if (flow <= 0.001) {
            _state.value = s.copy(errorMessage = "Flow must be positive to schedule alarm")
            return
        }

        // Current volume from level2
        val l2 = s.level2Text.toDoubleOrNull() ?: return
        val currentVol = FlowCalculator.linearVolume(l2, s.settings.maxLevelMeters, capacity)
        val thresholdVol = capacity * (s.settings.fullThresholdPct / 100.0)
        val remaining = (thresholdVol - currentVol).coerceAtLeast(0.0)

        val minutesToThreshold = (remaining / flow) * 60.0
        val triggerAt = System.currentTimeMillis() + (minutesToThreshold * 60_000L).toLong()

        NotificationHelper.scheduleAt(
            ctx = app.applicationContext,
            triggerAtMillis = triggerAt,
            title = "Tank nearing ${s.settings.fullThresholdPct}%",
            message = "Based on a flow of %.0f m³/h, the tank should reach %d%% soon."
                .format(flow, s.settings.fullThresholdPct),
            vibrate = s.settings.vibrateEnabled
        )
        _state.value = _state.value.copy(
            savedConfirmation = "Alarm set in ${FlowCalculator.formatDuration(minutesToThreshold)}"
        )
    }

    fun reset() {
        _state.value = _state.value.copy(
            level1Text = "", level2Text = "", result = null,
            errorMessage = null, savedConfirmation = null
        )
    }
}
