package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.UiState
import com.example.model.NotificationRecord
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    currentUid: String,
    notificationsState: UiState<List<NotificationRecord>>,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit
) {
    val itemsList = (notificationsState as? UiState.Success)?.data.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("বিজ্ঞপ্তি") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "পিছনে")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (notificationsState) {
                is UiState.Loading -> item { CircularProgressIndicator() }
                is UiState.Error -> item { Text(notificationsState.message, color = MaterialTheme.colorScheme.error) }
                is UiState.Success -> {
                    if (itemsList.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.NotificationsNone,
                                title = "কোনো বিজ্ঞপ্তি নেই",
                                subtitle = "নতুন বার্তা, রক্তের আবেদন, নোটিশ, ইভেন্ট বা পেমেন্ট আপডেট হলে এখানে দেখাবে।"
                            )
                        }
                    } else {
                        items(itemsList, key = { it.id }) { notif ->
                            val isUnread = currentUid !in notif.readBy
                            val icon = when (notif.category) {
                                "message" -> Icons.AutoMirrored.Rounded.Chat
                                "blood_request" -> Icons.Rounded.Bloodtype
                                "event" -> Icons.Rounded.Event
                                "payment_update" -> Icons.Rounded.Payments
                                "service_update" -> Icons.Rounded.Handshake
                                else -> Icons.Rounded.Campaign
                            }

                            EmbossedCard(
                                onClick = { if (isUnread) onMarkRead(notif.id) },
                                containerColor = if (isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium)
                                        Text(notif.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isUnread) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
