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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.UiState
import com.example.model.NoticeRecord
import com.example.model.OrgEventRecord
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ViewOnlyBanner

@Composable
fun NoticesScreen(
    isAdmin: Boolean,
    noticesState: UiState<List<NoticeRecord>>,
    eventsState: UiState<List<OrgEventRecord>>,
    onSendNotice: (title: String, body: String, noticeType: String, pinned: Boolean) -> Unit,
    onDeleteNotice: (id: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSendDialog by remember { mutableStateOf(false) }

    val notices = (noticesState as? UiState.Success)?.data.orEmpty()
    val events = (eventsState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isAdmin) {
            item {
                ViewOnlyBanner(
                    message = "View Only Mode • শুধুমাত্র অ্যাডমিন নতুন নোটিশ পাঠাতে পারবেন"
                )
            }
        } else {
            item {
                AnimatedPrimaryActionButton(
                    text = "নতুন নোটিশ পাঠান (Send Notice)",
                    icon = Icons.Rounded.Campaign,
                    onClick = { showSendDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("send_notice_btn")
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    AnimatedPrimaryActionButton(
                        text = "নোটিশ ও ঘোষণা (${notices.size})",
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedOutlinedActionButton(
                        text = "সভা ও ইভেন্ট (${events.size})",
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    AnimatedOutlinedActionButton(
                        text = "নোটিশ ও ঘোষণা (${notices.size})",
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedPrimaryActionButton(
                        text = "সভা ও ইভেন্ট (${events.size})",
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (selectedTab == 0) {
            when (noticesState) {
                is UiState.Loading -> item { CircularProgressIndicator() }
                is UiState.Error -> item { Text(noticesState.message, color = MaterialTheme.colorScheme.error) }
                is UiState.Success -> {
                    if (notices.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.Campaign,
                                title = "কোনো নোটিশ বা ঘোষণা নেই",
                                subtitle = "অ্যাডমিন নতুন নোটিশ প্রকাশ করলে এখানে তাৎক্ষণিকভাবে দেখা যাবে।"
                            )
                        }
                    } else {
                        items(notices, key = { it.id }) { notice ->
                            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                when (notice.noticeType) {
                                                    "announcement" -> "ঘোষণা"
                                                    "activity_update" -> "কার্যক্রম আপডেট"
                                                    else -> "নোটিশ"
                                                }
                                            )
                                        }
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (notice.pinned) {
                                            Icon(
                                                imageVector = Icons.Rounded.PushPin,
                                                contentDescription = "পিন করা",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        if (isAdmin) {
                                            IconButton(onClick = { onDeleteNotice(notice.id) }) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Delete,
                                                    contentDescription = "নোটিশ মুছুন",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(notice.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        } else {
            when (eventsState) {
                is UiState.Loading -> item { CircularProgressIndicator() }
                is UiState.Error -> item { Text(eventsState.message, color = MaterialTheme.colorScheme.error) }
                is UiState.Success -> {
                    if (events.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.Event,
                                title = "কোনো আসন্ন সভা বা ইভেন্ট নেই",
                                subtitle = "সংগঠনের পরবর্তী সভা বা মানবিক ইভেন্টের সময়সূচি এখানে প্রদর্শিত হবে।"
                            )
                        }
                    } else {
                        items(events, key = { it.id }) { ev ->
                            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(if (ev.eventType == "meeting") "সাংগঠনিক সভা" else "ইভেন্ট") }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(ev.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(ev.description, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "তারিখ: ${ev.eventDate} ${ev.eventTime} • স্থান: ${ev.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSendDialog && isAdmin) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var noticeType by remember { mutableStateOf("notice") }
        var pinned by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = { Text("নতুন নোটিশ পাঠান") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("শিরোনাম *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("বিস্তারিত বিবরণ *") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("পিন করে উপরে রাখুন")
                        Switch(checked = pinned, onCheckedChange = { pinned = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onSendNotice(title, body, noticeType, pinned)
                        showSendDialog = false
                    }
                }) { Text("প্রকাশ করুন") }
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) { Text("বাতিল") }
            }
        )
    }
}
