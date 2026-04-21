package com.watertank.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.watertank.app.domain.FlowCalculator
import com.watertank.app.ui.components.AnimatedTank
import com.watertank.app.ui.components.FlowGauge
import com.watertank.app.ui.components.InlineKpi
import com.watertank.app.ui.components.PipeNetwork
import com.watertank.app.ui.theme.AccentAmber
import com.watertank.app.ui.theme.AccentGreen
import com.watertank.app.ui.theme.BgCard
import com.watertank.app.ui.theme.BgDeep
import com.watertank.app.ui.theme.BgSurface
import com.watertank.app.ui.theme.StrokeSubtle
import com.watertank.app.ui.theme.TextMuted
import com.watertank.app.ui.theme.TextPrimary
import com.watertank.app.ui.theme.TextSecondary
import com.watertank.app.ui.theme.WaterMid
import com.watertank.app.ui.theme.WaterTop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenCalculator: () -> Unit,
    onOpenChlorine: () -> Unit,
    onOpenSettings: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WaterTank", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Text(
                            state.tank?.name ?: "No tank configured",
                            color = TextMuted, fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onOpenSettings() }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- The tank visual: tap to open calculator ---
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCalculator() }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedTank(fillFraction = state.fillFraction)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InlineKpi(
                            label = "VOLUME",
                            value = "%.0f m³".format(state.currentVolumeM3),
                            accent = WaterTop
                        )
                        InlineKpi(
                            label = "LEVEL",
                            value = "%.2f m".format(state.currentLevelM),
                            accent = TextPrimary
                        )
                        InlineKpi(
                            label = "CAPACITY",
                            value = "%.0f m³".format(state.settings.capacityM3),
                            accent = TextSecondary
                        )
                    }
                    Text(
                        "tap tank to calculate flow",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // --- Flow + ETA row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f)) {
                    FlowGauge(
                        value = state.flowM3PerHour.toFloat().coerceAtLeast(0f),
                        maxValue = 5000f,  // adjust for your facility
                        unit = "m³/h",
                        label = "FLOW RATE",
                        accent = AccentGreen
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "%.2f L/s".format(state.flowLitersPerSec),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                StatCard(modifier = Modifier.weight(1f)) {
                    Text("TIME TO FULL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        FlowCalculator.formatDuration(state.minutesUntilFull),
                        color = if (state.minutesUntilFull != null) WaterMid else TextMuted,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    state.etaMillis?.let { eta ->
                        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text("ETA ${fmt.format(Date(eta))}", color = TextSecondary, fontSize = 12.sp)
                    } ?: Text("record 2 readings", color = TextMuted, fontSize = 11.sp)
                }
            }

            // --- Schematic ---
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("FACILITY OVERVIEW", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    PipeNetwork(modifier = Modifier.fillMaxWidth().height(140.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LegendDot(AccentAmber, "Chlorine")
                        LegendDot(WaterMid, "Water / Pumps")
                    }
                }
            }

            // --- Action tiles ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionTile(
                    icon = Icons.Filled.Calculate,
                    title = "Flow calculator",
                    subtitle = "Record levels & predict full",
                    tint = WaterMid,
                    modifier = Modifier.weight(1f)
                ) { onOpenCalculator() }
                ActionTile(
                    icon = Icons.Filled.Science,
                    title = "Chlorine",
                    subtitle = "Bottle status & log",
                    tint = AccentAmber,
                    modifier = Modifier.weight(1f)
                ) { onOpenChlorine() }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(10.dp)
                .height(10.dp)
                .background(color, RoundedCornerShape(5.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun ActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .background(tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Spacer(Modifier.height(10.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, color = TextMuted, fontSize = 11.sp)
        }
    }
}
