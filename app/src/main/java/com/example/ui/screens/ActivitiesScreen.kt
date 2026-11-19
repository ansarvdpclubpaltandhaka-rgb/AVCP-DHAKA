package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.UiState
import com.example.model.ActivityRecord
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard

@Composable
fun ActivitiesScreen(
    activitiesState: UiState<List<ActivityRecord>>
) {
    var statusFilter by remember { mutableStateOf("all") }
    var selectedActivity by remember { mutableStateOf<ActivityRecord?>(null) }

    val activities = (activitiesState as? UiState.Success)?.data.orEmpty()
    val filtered = remember(activities, statusFilter) {
        when (statusFilter) {
            "ongoing" -> activities.filter { it.status == "ongoing" }
            "completed" -> activities.filter { it.status == "completed" }
            else -> activities
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = statusFilter == "all",
                    onClick = { statusFilter = "all" },
                    label = { Text("সকল (${activities.size})") }
                )
                FilterChip(
                    selected = statusFilter == "ongoing",
                    onClick = { statusFilter = "ongoing" },
                    label = { Text("চলমান") }
                )
                FilterChip(
                    selected = statusFilter == "completed",
                    onClick = { statusFilter = "completed" },
                    label = { Text("সম্পন্ন") }
                )
            }
        }

        when (activitiesState) {
            is UiState.Loading -> item { CircularProgressIndicator() }
            is UiState.Error -> item { Text(activitiesState.message, color = MaterialTheme.colorScheme.error) }
            is UiState.Success -> {
                if (filtered.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.EventNote,
                            title = "কোনো কার্যক্রম পাওয়া যায়নি",
                            subtitle = "সংগঠনের চলমান বা সম্পন্ন মানবিক কার্যক্রম যুক্ত করা হলে এখানে দেখা যাবে।"
                        )
                    }
                } else {
                    items(filtered, key = { it.id }) { act ->
                        EmbossedCard(
                            onClick = { selectedActivity = act },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (act.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = act.imageUrl,
                                    contentDescription = act.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(onClick = { selectedActivity = act }, label = { Text(act.category) })
                                Text(
                                    text = if (act.status == "completed") "সম্পন্ন" else "চলমান",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (act.status == "completed") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(act.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(act.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("তারিখ: ${act.date}", style = MaterialTheme.typography.bodySmall)
                                Text("অংশগ্রহণকারী: ${act.participants}", style = MaterialTheme.typography.bodySmall)
                                Text("উপকারভোগী: ${act.beneficiaries}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedActivity != null) {
        val act = selectedActivity!!
        AlertDialog(
            onDismissRequest = { selectedActivity = null },
            title = { Text(act.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("বিভাগ: ${act.category}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(act.description, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("তারিখ: ${act.date}", style = MaterialTheme.typography.bodySmall)
                    Text("স্থান: ${act.location}", style = MaterialTheme.typography.bodySmall)
                    Text("অংশগ্রহণকারী: ${act.participants} জন", style = MaterialTheme.typography.bodySmall)
                    Text("উপকারভোগী: ${act.beneficiaries} জন", style = MaterialTheme.typography.bodySmall)
                    Text("বাজেট: ৳${act.budget.toLong()}", style = MaterialTheme.typography.bodySmall)
                    Text("অবস্থা: ${if (act.status == "completed") "সম্পন্ন" else "চলমান"}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedActivity = null }) { Text("বন্ধ করুন") }
            }
        )
    }
}
