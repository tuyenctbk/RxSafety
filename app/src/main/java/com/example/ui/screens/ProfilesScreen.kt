package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.clinical.DoseFlowBridge
import com.example.data.model.DailyMedicationEntity
import com.example.data.model.MedicationEntity
import com.example.data.model.PatientProfileEntity
import com.example.data.model.SeverityLevel
import com.example.ui.components.FoodInteractionCard
import com.example.ui.components.InteractionCard
import com.example.ui.components.RecallCard
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.MedicalCyan
import com.example.ui.theme.OnSeverityCriticalContainer
import com.example.ui.theme.SeverityCriticalContainer
import com.example.ui.theme.SeverityCriticalRed
import com.example.ui.theme.SeverityModerateAmber
import com.example.ui.theme.SeverityModerateContainer
import com.example.ui.theme.SeveritySafeContainer
import com.example.ui.theme.SeveritySafeGreen
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.RxSafetyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfilesScreen(
    viewModel: RxSafetyViewModel,
    modifier: Modifier = Modifier
) {
    val dailyMedications by viewModel.dailyMedications.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val medications by viewModel.profileMeds.collectAsState()
    val auditResult by viewModel.profileAuditResult.collectAsState()
    val isAuditing by viewModel.isAuditingProfile.collectAsState()
    val isSupporter by viewModel.isSupporter.collectAsState()
    val doseFlowDialogText by viewModel.doseFlowDialogText.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Daily Meds (Local Room), 1 = Caregiver Profiles (DoseFlow)
    var showAddDailyMedDialog by remember { mutableStateOf(false) }
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var showDoseFlowPresetsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profiles_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Navigation Switcher: Daily Meds vs Profiles
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.widthIn(max = 640.dp)) {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TealPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = {
                                Text(
                                    "Daily Meds (Local Device)",
                                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            icon = { Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = {
                                Text(
                                    "Profiles & DoseFlow",
                                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        }

        // TAB 0: DAILY MEDICATION LIST (ROOM LOCAL STORAGE)
        if (activeTab == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.widthIn(max = 640.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "My Daily Medications",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Stored locally on device in SQLite database via Room",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }

                            Button(
                                onClick = { showAddDailyMedDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_daily_med_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.add_med), fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Adherence and Conflict Scan Banner Card
                        val takenCount = dailyMedications.count { it.isTakenToday }
                        val totalCount = dailyMedications.size
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.local_device_adherence),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                    }

                                    Surface(
                                        color = if (takenCount == totalCount && totalCount > 0)
                                            SeveritySafeContainer else TealContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.adherence_status, takenCount, totalCount),
                                            color = if (takenCount == totalCount && totalCount > 0)
                                                SeveritySafeGreen else TealPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.adherence_footer),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.checkDailyMedicationsConflicts() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("scan_daily_meds_conflicts_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.check_regimen_safety), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.resetDailyTakenStatus() },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.reset_day), fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.daily_schedule_title, dailyMedications.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (dailyMedications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No daily medications recorded yet. Tap 'Add Med' above to create your schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }
            } else {
                items(dailyMedications, key = { it.id }) { dailyMed ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.widthIn(max = 640.dp)) {
                            DailyMedicationCard(
                                item = dailyMed,
                                onToggleTaken = { viewModel.toggleDailyMedicationTaken(dailyMed.id, dailyMed.isTakenToday) },
                                onDelete = { viewModel.deleteDailyMedication(dailyMed) }
                            )
                        }
                    }
                }
            }
        }

        // TAB 1: CAREGIVER PROFILES & DOSEFLOW SYNERGY
        if (activeTab == 1) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.widthIn(max = 640.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Patient Profiles & DoseFlow",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Synchronized medication cabinet and automated conflict scans",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }

                            Button(
                                onClick = { showCreateProfileDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_profile_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.new_profile), fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Profile Selector Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profiles.forEach { profile ->
                                val isSelected = selectedProfile?.id == profile.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectProfile(profile) },
                                    label = {
                                        Text(
                                            text = "${profile.name} (${profile.relationship})",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isSelected) TealPrimary else Slate600,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.testTag("profile_chip_${profile.id}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // DoseFlow Integration Banner Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "DoseFlow App Synergy",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                    }

                                    Surface(
                                        color = TealContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "v1 Sync Active",
                                            color = TealPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Automated bi-directional syncing of patient profiles and real-time alert notifications with your DoseFlow application.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.exportDoseFlowSync() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("export_doseflow_button"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.export_sync_json), fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { showDoseFlowPresetsDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("demo_sync_presets_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicalCyan)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.demo_doseflow_sync), fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected Profile Details Card
                        if (selectedProfile != null) {
                            val profile = selectedProfile!!
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = profile.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${profile.relationship} • Age ${profile.age} • ${profile.gender}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate600
                                            )
                                        }

                                        Button(
                                            onClick = { viewModel.auditProfileRegimen() },
                                            enabled = !isAuditing,
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("scan_profile_conflicts_button")
                                        ) {
                                            if (isAuditing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(stringResource(R.string.scan_conflicts), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (profile.conditions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Conditions: ${profile.conditions.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (profile.allergies.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "⚠️ Known Allergies: ${profile.allergies.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SeverityCriticalRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Audit Result Banner for Profile
                        if (auditResult != null) {
                            val audit = auditResult!!
                            ProfileAuditSummaryCard(audit = audit)
                            
                            // Value realization success card
                            com.example.ui.components.PostOpSuccessCard(
                                isSupporter = isSupporter,
                                onOpenSupportDialog = { viewModel.openDonationDialog() }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Medication Regimen List Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.profile_regimen, medications.size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            TextButton(
                                onClick = { showAddMedDialog = true },
                                modifier = Modifier.testTag("add_medication_to_profile_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = TealPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.add_medication), color = TealPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (medications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No medications in this profile yet. Tap 'Add Medication' or sync via DoseFlow.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }
            } else {
                items(medications, key = { it.id }) { med ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.widthIn(max = 640.dp)) {
                            MedicationCard(
                                med = med,
                                onDelete = { viewModel.removeMedicationFromProfile(med) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Daily Medication Dialog (for Room DailyMedicationEntity)
    if (showAddDailyMedDialog) {
        var dName by remember { mutableStateOf("") }
        var dDosage by remember { mutableStateOf("") }
        var dFrequency by remember { mutableStateOf("Once daily") }
        var dForm by remember { mutableStateOf("Tablet") }
        var dTime by remember { mutableStateOf("08:00 AM") }
        var dInstructions by remember { mutableStateOf("") }
        var dPrescriber by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDailyMedDialog = false },
            title = { Text(stringResource(R.string.add_daily_med_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dName,
                        onValueChange = { dName = it },
                        label = { Text(stringResource(R.string.med_name_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_daily_med_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dDosage,
                        onValueChange = { dDosage = it },
                        label = { Text(stringResource(R.string.dosage_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dForm,
                            onValueChange = { dForm = it },
                            label = { Text(stringResource(R.string.form_label)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dTime,
                            onValueChange = { dTime = it },
                            label = { Text(stringResource(R.string.time_label)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = dFrequency,
                        onValueChange = { dFrequency = it },
                        label = { Text(stringResource(R.string.frequency_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dInstructions,
                        onValueChange = { dInstructions = it },
                        label = { Text(stringResource(R.string.instructions_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dPrescriber,
                        onValueChange = { dPrescriber = it },
                        label = { Text(stringResource(R.string.prescriber_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dName.isNotBlank()) {
                            viewModel.addDailyMedication(
                                name = dName,
                                dosage = dDosage,
                                frequency = dFrequency,
                                dosageForm = dForm,
                                scheduledTime = dTime,
                                instructions = dInstructions,
                                prescriber = dPrescriber
                            )
                            showAddDailyMedDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    modifier = Modifier.testTag("confirm_add_daily_med_button")
                ) {
                    Text(stringResource(R.string.save_to_device))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDailyMedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Add Profile Medication Dialog
    if (showAddMedDialog) {
        var name by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }
        var frequency by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMedDialog = false },
            title = { Text(stringResource(R.string.add_med_profile_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.med_name_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_med_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text(stringResource(R.string.dosage_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_med_dosage_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text(stringResource(R.string.frequency_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_med_frequency_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.notes_instructions_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_med_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addMedicationToProfile(name, dosage, frequency, notes)
                            showAddMedDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    modifier = Modifier.testTag("confirm_add_med_button")
                ) {
                    Text(stringResource(R.string.save_medication))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Create Profile Dialog
    if (showCreateProfileDialog) {
        var pName by remember { mutableStateOf("") }
        var pRel by remember { mutableStateOf("Self") }
        var pAge by remember { mutableStateOf("60") }
        var pCond by remember { mutableStateOf("") }
        var pAllergies by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateProfileDialog = false },
            title = { Text(stringResource(R.string.add_profile_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text(stringResource(R.string.patient_name_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_profile_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pRel,
                        onValueChange = { pRel = it },
                        label = { Text(stringResource(R.string.relationship_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pAge,
                        onValueChange = { pAge = it },
                        label = { Text(stringResource(R.string.age_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pCond,
                        onValueChange = { pCond = it },
                        label = { Text(stringResource(R.string.conditions_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pAllergies,
                        onValueChange = { pAllergies = it },
                        label = { Text(stringResource(R.string.allergies_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pName.isNotBlank()) {
                            val age = pAge.toIntOrNull() ?: 50
                            val condList = pCond.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val allergyList = pAllergies.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            viewModel.createProfile(pName, pRel, age, "Unspecified", condList, allergyList)
                            showCreateProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.create_profile))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProfileDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Demo Sync Presets Dialog
    if (showDoseFlowPresetsDialog) {
        AlertDialog(
            onDismissRequest = { showDoseFlowPresetsDialog = false },
            title = { Text(stringResource(R.string.demo_sync_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.demo_sync_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    DoseFlowBridge.sampleDoseFlowPresets.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.syncDoseFlowPreset(preset)
                                    showDoseFlowPresetsDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = preset.patientName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${preset.medications.size} Meds: ${preset.medications.joinToString(", ") { it.name }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDoseFlowPresetsDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    // DoseFlow Export JSON Dialog
    if (doseFlowDialogText != null) {
        val json = doseFlowDialogText!!
        AlertDialog(
            onDismissRequest = { viewModel.closeDoseFlowDialog() },
            title = { Text(stringResource(R.string.doseflow_json_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.doseflow_json_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = json,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("DoseFlow JSON", json)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.getString(R.string.toast_payload_copied), Toast.LENGTH_SHORT).show()
                        viewModel.closeDoseFlowDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.copy_to_clipboard))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDoseFlowDialog() }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
fun DailyMedicationCard(
    item: DailyMedicationEntity,
    onToggleTaken: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_med_card_${item.name}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isTakenToday)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isTakenToday) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark button to toggle adherence for today
            IconButton(
                onClick = onToggleTaken,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_taken_${item.id}")
            ) {
                Icon(
                    imageVector = if (item.isTakenToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (item.isTakenToday) "Mark as untaken" else "Mark as taken",
                    tint = if (item.isTakenToday) SeveritySafeGreen else Slate600,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isTakenToday) Slate600 else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = TealContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.dosage,
                            fontSize = 10.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.dosageForm,
                            fontSize = 9.sp,
                            color = Slate600,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.scheduledTime} • ${item.frequency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }

                if (item.instructions.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.instructions,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate700,
                        fontSize = 11.sp
                    )
                }

                if (item.prescriber.isNotBlank()) {
                    Text(
                        text = "Rx: ${item.prescriber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_daily_med_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Slate600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MedicationCard(
    med: MedicationEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medication_item_${med.name}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(TealContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (med.rxcui != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "RxCUI: ${med.rxcui}",
                                    fontSize = 9.sp,
                                    color = Slate600,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${med.dosage} • ${med.frequency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                    if (med.notes.isNotBlank()) {
                        Text(
                            text = med.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_med_${med.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Slate600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileAuditSummaryCard(audit: com.example.data.model.AuditResult) {
    val isCritical = audit.highestSeverity == SeverityLevel.CRITICAL
    val cardBg = if (isCritical) SeverityCriticalContainer else SeverityModerateContainer
    val accentColor = if (isCritical) SeverityCriticalRed else SeverityModerateAmber

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCritical)
                        "⚠️ Regimen Safety Audit: Action Required!"
                    else
                        "⚡ Regimen Safety Audit Complete",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCritical) OnSeverityCriticalContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Evaluated ${audit.evaluatedDrugs.size} medications for ${audit.profileName ?: "patient"}.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "• ${audit.drugInteractions.size} Drug Conflicts",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• ${audit.conditionContraindications.size} Condition Alerts",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• ${audit.foodInteractions.size} Dietary Alerts",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (audit.conditionContraindications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                audit.conditionContraindications.forEach { contra ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color.White.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "⛔ Condition Conflict: ${contra.drugName} vs ${contra.condition} - ${contra.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = SeverityCriticalRed,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
