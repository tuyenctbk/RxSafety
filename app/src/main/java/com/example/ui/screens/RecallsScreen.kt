package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RecallCard
import com.example.ui.theme.SeverityCriticalContainer
import com.example.ui.theme.SeverityCriticalRed
import com.example.ui.theme.Slate600
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.RxSafetyViewModel

@Composable
fun RecallsScreen(
    viewModel: RxSafetyViewModel,
    modifier: Modifier = Modifier
) {
    val recalls by viewModel.recallsList.collectAsState()
    val isLoading by viewModel.isLoadingRecalls.collectAsState()
    val searchQuery by viewModel.recallSearchQuery.collectAsState()
    val filterMyMeds by viewModel.filterRecallsByMyMeds.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recalls_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.widthIn(max = 640.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.recalls_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.recalls_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                        IconButton(
                            onClick = { viewModel.loadRecalls() },
                            modifier = Modifier.testTag("refresh_recalls_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(R.string.recalls_title), tint = TealPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // FDA Classification Education Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.fda_classifications_title),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.fda_classifications_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onRecallSearchChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recall_search_input"),
                        placeholder = { Text(stringResource(R.string.recall_search_placeholder)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TealPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onRecallSearchChanged("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.clear_all))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter by My Regimen Toggle
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = if (filterMyMeds) TealContainer else MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = if (filterMyMeds) TealPrimary else Slate600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.filter_my_meds),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stringResource(R.string.filter_my_meds_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Switch(
                                checked = filterMyMeds,
                                onCheckedChange = { viewModel.toggleFilterRecallsByMyMeds() },
                                modifier = Modifier.testTag("filter_my_meds_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.recent_enforcement_reports, recalls.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }
        } else if (recalls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 500.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (filterMyMeds) "No Recalls for Your Saved Medications" else "No Recalls Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (filterMyMeds)
                                    "Great news! None of the drugs in your active patient profiles match currently reported recall batches."
                                else
                                    "No enforcement reports matched your search criteria.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(recalls, key = { it.recallNumber }) { recall ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.widthIn(max = 640.dp)) {
                        RecallCard(recall = recall)
                    }
                }
            }
        }
    }
}
