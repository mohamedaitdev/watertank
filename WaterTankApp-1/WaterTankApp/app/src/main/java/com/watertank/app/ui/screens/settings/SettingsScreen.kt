package com.watertank.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.watertank.app.ui.theme.AccentRed
import com.watertank.app.ui.theme.BgCard
import com.watertank.app.ui.theme.BgDeep
import com.watertank.app.ui.theme.BgSurface
import com.watertank.app.ui.theme.StrokeSubtle
import com.watertank.app.ui.theme.TextMuted
import com.watertank.app.ui.theme.TextPrimary
import com.watertank.app.ui.theme.TextSecondary
import com.watertank.app.ui.theme.WaterMid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        Icons.Filled.ArrowBack, "Back",
                        tint = TextPrimary,
                        modifier = Modifier.padding(12.dp).clickable { onBack() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard("Tank geometry") {
                NumberRow(
                    label = "Capacity (m³)",
                    value = s.capacityM3.toInt().toString(),
                    onCommit = { it.toDoubleOrNull()?.let(vm::setCapacity) }
                )
                Spacer(Modifier.height(10.dp))
                NumberRow(
                    label = "Max level (m)",
                    value = "%.2f".format(s.maxLevelMeters),
                    onCommit = { it.toDoubleOrNull()?.let(vm::setMaxLevel) }
                )
            }

            SectionCard("Fill alarm") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Alarm enabled", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Notify when tank nearly full", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = s.alarmEnabled,
                        onCheckedChange = vm::setAlarmEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WaterMid,
                            checkedTrackColor = WaterMid.copy(alpha = 0.4f)
                        )
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vibrate", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Vibrate when alarm fires", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = s.vibrateEnabled,
                        onCheckedChange = vm::setVibrateEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WaterMid,
                            checkedTrackColor = WaterMid.copy(alpha = 0.4f)
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Threshold: ${s.fullThresholdPct}%", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = s.fullThresholdPct.toFloat(),
                    onValueChange = { vm.setThreshold(it.toInt()) },
                    valueRange = 50f..100f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = WaterMid,
                        activeTrackColor = WaterMid,
                        inactiveTrackColor = StrokeSubtle
                    )
                )
            }

            SectionCard("Data") {
                OutlinedButton(
                    onClick = { vm.clearReadings() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear all readings", color = AccentRed, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                "WaterTank · v1.0",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title.uppercase(), color = TextMuted, fontSize = 11.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun NumberRow(
    label: String,
    value: String,
    onCommit: (String) -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label, color = TextMuted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = WaterMid,
            unfocusedBorderColor = StrokeSubtle,
            focusedContainerColor = BgCard,
            unfocusedContainerColor = BgCard,
            cursorColor = WaterMid
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(6.dp))
    OutlinedButton(
        onClick = { onCommit(text) },
        modifier = Modifier.fillMaxWidth().height(42.dp),
        shape = RoundedCornerShape(10.dp)
    ) { Text("Save", color = TextSecondary) }
}
