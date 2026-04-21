package com.watertank.app.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.watertank.app.domain.FlowCalculator
import com.watertank.app.ui.theme.AccentAmber
import com.watertank.app.ui.theme.AccentGreen
import com.watertank.app.ui.theme.AccentRed
import com.watertank.app.ui.theme.BgCard
import com.watertank.app.ui.theme.BgDeep
import com.watertank.app.ui.theme.BgSurface
import com.watertank.app.ui.theme.StrokeSubtle
import com.watertank.app.ui.theme.TextMuted
import com.watertank.app.ui.theme.TextPrimary
import com.watertank.app.ui.theme.TextSecondary
import com.watertank.app.ui.theme.WaterMid
import com.watertank.app.ui.theme.WaterTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    vm: CalculatorViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Flow calculator", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Input card
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionLabel("READINGS")
                    Spacer(Modifier.height(10.dp))
                    LevelField(
                        label = "Current level (m)",
                        value = state.level1Text,
                        onChange = vm::updateLevel1
                    )
                    Spacer(Modifier.height(10.dp))
                    LevelField(
                        label = "Level after interval (m)",
                        value = state.level2Text,
                        onChange = vm::updateLevel2
                    )
                    Spacer(Modifier.height(10.dp))
                    LevelField(
                        label = "Interval (minutes)",
                        value = state.intervalMinutesText,
                        onChange = vm::updateInterval
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Capacity ${state.settings.capacityM3.toInt()} m³ · max level ${"%.1f".format(state.settings.maxLevelMeters)} m",
                        color = TextMuted, fontSize = 11.sp
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = vm::calculate,
                    colors = ButtonDefaults.buttonColors(containerColor = WaterMid, contentColor = BgDeep),
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("CALCULATE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                OutlinedButton(
                    onClick = vm::reset,
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Refresh, "Reset", tint = TextPrimary)
                }
            }

            state.errorMessage?.let {
                InfoBanner(message = it, color = AccentRed)
            }
            state.savedConfirmation?.let {
                InfoBanner(message = it, color = AccentGreen)
            }

            // Results
            state.result?.let { r ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("RESULTS")
                        Spacer(Modifier.height(10.dp))
                        ResultRow("Flow rate",
                            value = "%.1f m³/h".format(r.flowM3PerHour),
                            accent = AccentGreen)
                        ResultRow("Flow rate",
                            value = "%.2f L/s".format(r.flowLitersPerSecond),
                            accent = WaterTop)
                        ResultRow("Time to full",
                            value = FlowCalculator.formatDuration(r.minutesUntilFull),
                            accent = AccentAmber)
                        ResultRow("Current fill",
                            value = "%.1f %%".format(r.projectedFullPct),
                            accent = WaterMid)
                    }
                }
            }

            // Save / Alarm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = vm::saveReadings,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Save, null, tint = TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Save", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = vm::scheduleFullAlarm,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber, contentColor = BgDeep),
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.NotificationAdd, null, tint = BgDeep)
                    Spacer(Modifier.width(8.dp))
                    Text("Alarm at ${state.settings.fullThresholdPct}%", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LevelField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
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
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
}

@Composable
private fun ResultRow(label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoBanner(message: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(message, color = color, fontWeight = FontWeight.SemiBold)
    }
}
