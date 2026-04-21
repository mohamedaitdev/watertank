package com.watertank.app.ui.screens.chlorine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watertank.app.WaterTankApplication
import com.watertank.app.data.model.ChlorineBottle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChlorineViewModel : ViewModel() {

    private val repo = WaterTankApplication.instance.chlorineRepo

    val bottles: StateFlow<List<ChlorineBottle>> = repo.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editing = MutableStateFlow<ChlorineBottle?>(null)
    val editing: StateFlow<ChlorineBottle?> = _editing.asStateFlow()

    fun toggleStatus(bottle: ChlorineBottle) {
        viewModelScope.launch { repo.cycleStatus(bottle) }
    }

    fun addBottle(label: String) {
        val trimmed = label.trim().ifBlank { "Bottle" }
        viewModelScope.launch { repo.add(trimmed) }
    }

    fun delete(bottle: ChlorineBottle) {
        viewModelScope.launch { repo.delete(bottle) }
    }

    fun updateNote(bottle: ChlorineBottle, note: String) {
        viewModelScope.launch { repo.update(bottle.copy(note = note)) }
    }

    fun openEditor(bottle: ChlorineBottle?) { _editing.value = bottle }
}
