package com.watertank.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watertank.app.WaterTankApplication
import com.watertank.app.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val prefs: PreferencesManager = WaterTankApplication.instance.prefs
    private val tankRepo = WaterTankApplication.instance.tankRepo

    val state: StateFlow<PreferencesManager.Settings> = prefs.flow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreferencesManager.Settings())

    fun setCapacity(m3: Double) {
        viewModelScope.launch {
            prefs.setCapacity(m3)
            tankRepo.getFirstTank()?.let { tankRepo.updateTank(it.copy(capacityM3 = m3)) }
        }
    }

    fun setMaxLevel(m: Double) {
        viewModelScope.launch {
            prefs.setMaxLevel(m)
            tankRepo.getFirstTank()?.let { tankRepo.updateTank(it.copy(maxLevelMeters = m)) }
        }
    }

    fun setThreshold(pct: Int) {
        viewModelScope.launch {
            prefs.setThreshold(pct)
            tankRepo.getFirstTank()?.let { tankRepo.updateTank(it.copy(fullThresholdPct = pct)) }
        }
    }

    fun setAlarmEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setAlarmEnabled(enabled) }
    fun setVibrateEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setVibrateEnabled(enabled) }

    fun clearReadings() = viewModelScope.launch {
        tankRepo.getFirstTank()?.let { tankRepo.clearReadings(it.id) }
    }
}
