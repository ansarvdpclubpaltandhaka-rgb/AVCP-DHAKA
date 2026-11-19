package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
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
import com.example.model.*
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ImagePickerUploadField
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreen(
    currentUser: UserProfile,
    conversationsState: UiState<List<ConversationRecord>>,
    directoryState: UiState<List<DirectoryEntry>>,
    onBack: () -> Unit,
    onOpenConversation: (ConversationRecord) -> Unit,
    onStartChatWith: (otherUid: String, otherName: String, otherPhoto: String) -> Unit
) {
    var showNewChatDialog by remember { mutableStateOf(false) }
    val conversations = (conversationsState as? UiState.Success)?.data.orEmpty()
    val directory = (directoryState as? UiState.Success)?.data.orEmpty()
        .filter { it.linkedUid.isNotBlank() && it.linkedUid != currentUser.uid }
        .distinctBy { it.linkedUid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("বার্তা (মেসেজিং)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "পিছনে")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewChatDialog = true }) {
                Icon(Icons.Rounded.AddComment, contentDescription = "নতুন চ্যাট শুরু করুন")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (conversationsState) {
                is UiState.Loading -> item { CircularProgressIndicator() }
                is UiState.Error -> item { Text(conversationsState.message, color = MaterialTheme.colorScheme.error) }
                is UiState.Success -> {
                    if (conversations.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.AutoMirrored.Rounded.Chat,
                                title = "কোনো কথোপকথন নেই",
                                subtitle = "অন্য কোনো সদস্যের সাথে বার্তা আদান-প্রদান শুরু করতে নিচের '+' বোতামে ট্যাপ করুন।"
                            )
                        }
                    } else {
                        items(conversations, key = { it.id }) { conv ->
                            val otherUid = conv.participantIds.firstOrNull { it != currentUser.uid }.orEmpty()
                            val otherName = conv.participantNames[otherUid] ?: "সদস্য"
                            val otherPhoto = conv.participantPhotos[otherUid].orEmpty()

                            EmbossedCard(
                                onClick = { onOpenConversation(conv) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (otherPhoto.isNotBlank()) {
                                        AsyncImage(
                                            model = otherPhoto,
                                            contentDescription = otherName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(46.dp).clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(otherName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = conv.lastMessageText.ifBlank { "কথোপকথন শুরু করুন" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("সদস্য নির্বাচন করুন") },
            text = {
                if (directory.isEmpty()) {
                    Text("বর্তমানে অন্য কোনো নিবন্ধিত সদস্য পাওয়া যায়নি।")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(directory, key = { it.linkedUid }) { entry ->
                            OutlinedCard(
                                onClick = {
                                    showNewChatDialog = false
                                    onStartChatWith(entry.linkedUid, entry.name, entry.photoUrl)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(entry.name, style = MaterialTheme.typography.titleSmall)
                                        if (entry.position.isNotBlank()) {
                                            Text(entry.position, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) { Text("বন্ধ করুন") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    currentUser: UserProfile,
    conversation: ConversationRecord?,
    messagesState: UiState<List<ChatMessageRecord>>,
    orgConfig: OrganizationConfig?,
    onBack: () -> Unit,
    onSendMessage: (participantIds: List<String>, text: String, imageUrl: String) -> Unit,
    onMarkSeen: (List<ChatMessageRecord>) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    var pendingImageUrl by remember { mutableStateOf("") }

    val participantIds = conversation?.participantIds?.takeIf { it.size == 2 }
        ?: conversationId.split("_").take(2)
    val otherUid = participantIds.firstOrNull { it != currentUser.uid }.orEmpty()
    val otherName = conversation?.participantNames?.get(otherUid) ?: "সদস্য"
    val messages = (messagesState as? UiState.Success)?.data.orEmpty()

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            onMarkSeen(messages)
        }
    }

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "পিছনে")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Rounded.Chat,
                            title = "কথোপকথন শুরু করুন",
                            subtitle = "এখানে টেক্সট বা ছবির লিংক পাঠিয়ে যোগাযোগ করুন।"
                        )
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        val isMine = msg.senderId == currentUser.uid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 2.dp,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (msg.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = msg.imageUrl,
                                            contentDescription = "ছবি",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        if (msg.text.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    if (msg.text.isNotBlank()) {
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val timeStr = msg.createdAt?.toDate()?.let { timeFormat.format(it) }.orEmpty()
                                    val seenLabel = if (isMine) {
                                        if (msg.seen) " • দেখা হয়েছে" else " • পাঠানো হয়েছে"
                                    } else ""
                                    Text(
                                        text = "$timeStr$seenLabel",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(tonalElevation = 4.dp, shadowElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showImageDialog = true }) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "ছবি পাঠান", tint = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("বার্তা লিখুন...") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(participantIds, messageText, "")
                                messageText = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "পাঠান", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("ছবি পাঠান") },
            text = {
                ImagePickerUploadField(
                    imageUrl = pendingImageUrl,
                    onImageUrlChange = { pendingImageUrl = it },
                    orgConfig = orgConfig,
                    label = "ছবি বাছাই ও আপলোড"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pendingImageUrl.isNotBlank()) {
                        onSendMessage(participantIds, messageText, pendingImageUrl)
                        messageText = ""
                        pendingImageUrl = ""
                        showImageDialog = false
                    }
                }) { Text("পাঠান") }
            },
            dismissButton = { TextButton(onClick = { showImageDialog = false }) { Text("বাতিল") } }
        )
    }
}
