package com.watertank.app.ui.screens.chlorine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.watertank.app.data.model.BottleStatus
import com.watertank.app.data.model.ChlorineBottle
import com.watertank.app.ui.theme.AccentAmber
import com.watertank.app.ui.theme.AccentGray
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChlorineScreen(
    onBack: () -> Unit,
    vm: ChlorineViewModel = viewModel()
) {
    val bottles by vm.bottles.collectAsStateWithLifecycle()
    val editing by vm.editing.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    val summary = bottles.groupingBy { it.status }.eachCount()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Chlorine bottles", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        Icons.Filled.ArrowBack, "Back",
                        tint = TextPrimary,
                        modifier = Modifier.padding(12.dp).clickable { onBack() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = WaterMid,
                contentColor = BgDeep,
                shape = CircleShape
            ) { Icon(Icons.Filled.Add, "Add bottle") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary row
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryChip("Working", summary[BottleStatus.WORKING] ?: 0, AccentGreen)
                    SummaryChip("Standby", summary[BottleStatus.STANDBY] ?: 0, AccentAmber)
                    SummaryChip("Empty", summary[BottleStatus.EMPTY] ?: 0, AccentGray)
                    SummaryChip("Fault", summary[BottleStatus.NOT_WORKING] ?: 0, AccentRed)
                }
            }

            Text(
                "Tap to cycle status · long-press to edit",
                color = TextMuted,
                fontSize = 11.sp
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = bottles, key = { it.id }) { bottle ->
                    BottleCard(
                        bottle = bottle,
                        onClick = { vm.toggleStatus(bottle) },
                        onLongPress = { vm.openEditor(bottle) }
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddBottleDialog(
            onDismiss = { showAdd = false },
            onConfirm = { label ->
                vm.addBottle(label)
                showAdd = false
            }
        )
    }

    editing?.let { bottle ->
        EditBottleDialog(
            bottle = bottle,
            onDismiss = { vm.openEditor(null) },
            onSaveNote = { note ->
                vm.updateNote(bottle, note)
                vm.openEditor(null)
            },
            onDelete = {
                vm.delete(bottle)
                vm.openEditor(null)
            }
        )
    }
}

@Composable
private fun SummaryChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun BottleCard(
    bottle: ChlorineBottle,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val meta = statusMeta(bottle.status)
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .border(
                width = 2.dp,
                color = meta.color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(bottle.label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Box(
                    Modifier
                        .background(meta.color, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) { Text(meta.emoji, fontSize = 10.sp) }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(meta.emoji, fontSize = 40.sp)
                Text(meta.title, color = meta.color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            if (bottle.note.isNotBlank()) {
                Text(bottle.note, color = TextMuted, fontSize = 11.sp, maxLines = 2)
            } else {
                Text("tap card to change", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

private data class StatusMeta(val title: String, val emoji: String, val color: Color)
private fun statusMeta(s: BottleStatus): StatusMeta = when (s) {
    BottleStatus.WORKING     -> StatusMeta("Working", "✅", AccentGreen)
    BottleStatus.STANDBY     -> StatusMeta("Standby", "🟡", AccentAmber)
    BottleStatus.EMPTY       -> StatusMeta("Empty", "⚪", AccentGray)
    BottleStatus.NOT_WORKING -> StatusMeta("Fault", "❌", AccentRed)
}

@Composable
private fun AddBottleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgSurface,
        title = { Text("Add bottle", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (e.g. Bottle E)", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = WaterMid,
                    unfocusedBorderColor = StrokeSubtle,
                    cursorColor = WaterMid
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(label) },
                colors = ButtonDefaults.buttonColors(containerColor = WaterMid, contentColor = BgDeep)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun EditBottleDialog(
    bottle: ChlorineBottle,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit,
    onDelete: () -> Unit
) {
    var note by remember { mutableStateOf(bottle.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgSurface,
        title = { Text("Edit ${bottle.label}", color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = WaterMid,
                        unfocusedBorderColor = StrokeSubtle,
                        cursorColor = WaterMid
                    )
                )
                Spacer(Modifier.padding(6.dp))
                TextButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, null, tint = AccentRed)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete bottle", color = AccentRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveNote(note) },
                colors = ButtonDefaults.buttonColors(containerColor = WaterMid, contentColor = BgDeep)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
