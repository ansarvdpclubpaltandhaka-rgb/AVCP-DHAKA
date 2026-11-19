package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.AppConstants
import com.example.core.UiState
import com.example.model.BloodRequest
import com.example.model.DirectoryEntry
import com.example.model.MemberRecord
import com.example.model.UserProfile
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodDonationScreen(
    currentUser: UserProfile,
    isAdmin: Boolean,
    directoryState: UiState<List<DirectoryEntry>>,
    adminMembersState: UiState<List<MemberRecord>>,
    bloodRequestsState: UiState<List<BloodRequest>>,
    onBack: () -> Unit,
    onCreateBloodRequest: (patient: String, bg: String, hospital: String, location: String, date: String, units: Long, contact: String, urgency: String, notes: String) -> Unit,
    onUpdateBloodStatus: (id: String, status: String) -> Unit,
    onUpdateOwnDonorSettings: (bloodDonor: Boolean, available: Boolean, count: Long, lastDate: String, bloodGroup: String) -> Unit,
    onMessageUser: (uid: String, name: String, photoUrl: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedGroup by remember { mutableStateOf("সকল") }
    var showCreateRequest by remember { mutableStateOf(false) }
    var showDonorSettings by remember { mutableStateOf(false) }

    val entries = (directoryState as? UiState.Success)?.data.orEmpty()
    val adminMembers = (adminMembersState as? UiState.Success)?.data.orEmpty()
    val requests = (bloodRequestsState as? UiState.Success)?.data.orEmpty()

    val donors = remember(entries, selectedGroup) {
        entries.filter { it.bloodDonor || it.bloodGroup.isNotBlank() }
            .filter { selectedGroup == "সকল" || it.bloodGroup == selectedGroup }
    }

    val filteredRequests = remember(requests, selectedGroup) {
        requests.filter { selectedGroup == "সকল" || it.bloodGroup == selectedGroup }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("রক্তদান ও রক্তের আবেদন") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "পিছনে")
                    }
                },
                actions = {
                    IconButton(onClick = { showDonorSettings = true }) {
                        Icon(Icons.Rounded.VolunteerActivism, contentDescription = "আমার রক্তদাতা তথ্য")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateRequest = true },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.testTag("add_blood_request_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "রক্তের আবেদন করুন")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        AnimatedPrimaryActionButton(
                            text = "রক্তের আবেদন (${filteredRequests.size})",
                            onClick = { selectedTab = 0 },
                            containerColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedOutlinedActionButton(
                            text = "রক্তদাতা (${donors.size})",
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        AnimatedOutlinedActionButton(
                            text = "রক্তের আবেদন (${filteredRequests.size})",
                            onClick = { selectedTab = 0 },
                            accentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedPrimaryActionButton(
                            text = "রক্তদাতা (${donors.size})",
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("সকল") + AppConstants.BLOOD_GROUPS) { bg ->
                        FilterChip(
                            selected = selectedGroup == bg,
                            onClick = { selectedGroup = bg },
                            label = { Text(bg) }
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                if (filteredRequests.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Bloodtype,
                            title = "কোনো রক্তের আবেদন নেই",
                            subtitle = "নতুন জরুরি রক্তের প্রয়োজনে নিচের '+' বোতামে ট্যাপ করে আবেদন প্রকাশ করুন।"
                        )
                    }
                } else {
                    items(filteredRequests, key = { it.id }) { req ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = req.bloodGroup,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(req.patientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${req.hospital} • ${req.location}", style = MaterialTheme.typography.bodySmall)
                                }
                                AssistChip(onClick = {}, label = { Text(statusBn(req.status)) })
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "প্রয়োজন: ${req.units} ব্যাগ • তারিখ: ${req.requiredDate} • জরুরি অবস্থা: ${urgencyBn(req.urgency)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "যোগাযোগ: ${req.contact}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (req.notes.isNotBlank()) {
                                Text(req.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isAdmin || req.createdByUid == currentUser.uid) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (req.status != "matched") {
                                        TextButton(onClick = { onUpdateBloodStatus(req.id, "matched") }) {
                                            Text("ডোনার পাওয়া গেছে")
                                        }
                                    }
                                    if (req.status != "fulfilled") {
                                        TextButton(onClick = { onUpdateBloodStatus(req.id, "fulfilled") }) {
                                            Text("সম্পন্ন চিহ্নিত করুন")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (donors.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Bloodtype,
                            title = "কোনো রক্তদাতা পাওয়া যায়নি",
                            subtitle = "সদস্যরা তাদের প্রোফাইল থেকে রক্তদাতা হিসেবে যুক্ত হলে এখানে দেখাবে।"
                        )
                    }
                } else {
                    items(donors, key = { it.id }) { donor ->
                        val adminPhone = if (isAdmin) {
                            adminMembers.firstOrNull {
                                (donor.linkedUid.isNotBlank() && it.linkedUid == donor.linkedUid) || it.name == donor.name
                            }?.phone.orEmpty()
                        } else ""

                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (donor.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = donor.photoUrl,
                                        contentDescription = donor.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(48.dp).clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(donor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "গ্রুপ: ${donor.bloodGroup.ifBlank { "অজানা" }} • রক্তদান: ${donor.donationCount} বার",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (donor.lastDonation.isNotBlank()) {
                                        Text(
                                            text = "সর্বশেষ রক্তদান: ${donor.lastDonation}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (adminPhone.isNotBlank()) {
                                        Text(
                                            text = "ফোন: $adminPhone",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(if (donor.donorAvailable) "প্রস্তুত" else "ব্যস্ত") }
                                    )
                                    if (donor.linkedUid.isNotBlank() && donor.linkedUid != currentUser.uid) {
                                        IconButton(onClick = {
                                            onMessageUser(donor.linkedUid, donor.name, donor.photoUrl)
                                        }) {
                                            Icon(
                                                Icons.AutoMirrored.Rounded.Chat,
                                                contentDescription = "মেসেজ করুন",
                                                tint = MaterialTheme.colorScheme.primary
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

    if (showCreateRequest) {
        var patient by remember { mutableStateOf("") }
        var bloodGroup by remember { mutableStateOf("O+") }
        var hospital by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var requiredDate by remember { mutableStateOf("") }
        var unitsText by remember { mutableStateOf("1") }
        var contact by remember { mutableStateOf(currentUser.phone) }
        var urgency by remember { mutableStateOf("urgent") }
        var notes by remember { mutableStateOf("") }
        var showBgMenu by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateRequest = false },
            title = { Text("নতুন রক্তের আবেদন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = patient, onValueChange = { patient = it }, label = { Text("রোগীর নাম *") })
                    Box {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("রক্তের গ্রুপ *") },
                            trailingIcon = {
                                IconButton(onClick = { showBgMenu = true }) {
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(expanded = showBgMenu, onDismissRequest = { showBgMenu = false }) {
                            AppConstants.BLOOD_GROUPS.forEach { bg ->
                                DropdownMenuItem(text = { Text(bg) }, onClick = { bloodGroup = bg; showBgMenu = false })
                            }
                        }
                    }
                    OutlinedTextField(value = hospital, onValueChange = { hospital = it }, label = { Text("হাসপাতালের নাম *") })
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("এলাকা / ঠিকানা *") })
                    OutlinedTextField(value = requiredDate, onValueChange = { requiredDate = it }, label = { Text("প্রয়োজনের তারিখ * (যেমন: ২০-১১-২০২৬)") })
                    OutlinedTextField(
                        value = unitsText,
                        onValueChange = { unitsText = it },
                        label = { Text("ব্যাগ সংখ্যা") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("যোগাযোগ নম্বর *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("normal" to "সাধারণ", "urgent" to "জরুরি", "critical" to "সঙ্কটাপন্ন").forEach { (k, label) ->
                            FilterChip(selected = urgency == k, onClick = { urgency = k }, label = { Text(label) })
                        }
                    }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("অতিরিক্ত তথ্য") })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (patient.isNotBlank() && hospital.isNotBlank() && location.isNotBlank() && requiredDate.isNotBlank() && contact.isNotBlank()) {
                            onCreateBloodRequest(
                                patient, bloodGroup, hospital, location, requiredDate,
                                unitsText.toLongOrNull() ?: 1L, contact, urgency, notes
                            )
                            showCreateRequest = false
                        }
                    }
                ) { Text("প্রকাশ করুন") }
            },
            dismissButton = { TextButton(onClick = { showCreateRequest = false }) { Text("বাতিল") } }
        )
    }

    if (showDonorSettings) {
        val myDir = entries.firstOrNull { it.id == currentUser.uid || it.linkedUid == currentUser.uid }
        var isDonor by remember(myDir) { mutableStateOf(myDir?.bloodDonor ?: true) }
        var isAvailable by remember(myDir) { mutableStateOf(myDir?.donorAvailable ?: true) }
        var countText by remember(myDir) { mutableStateOf((myDir?.donationCount ?: 0L).toString()) }
        var lastDate by remember(myDir) { mutableStateOf(myDir?.lastDonation ?: "") }
        var bg by remember(myDir) { mutableStateOf(myDir?.bloodGroup?.ifBlank { currentUser.bloodGroup } ?: currentUser.bloodGroup.ifBlank { "O+" }) }
        var showMenu by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDonorSettings = false },
            title = { Text("আমার রক্তদাতা তথ্য") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("আমি রক্তদান করতে আগ্রহী")
                        Switch(checked = isDonor, onCheckedChange = { isDonor = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("বর্তমানে রক্তদানের জন্য প্রস্তুত")
                        Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    }
                    Box {
                        OutlinedTextField(
                            value = bg,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("রক্তের গ্রুপ") },
                            trailingIcon = {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            AppConstants.BLOOD_GROUPS.forEach { g ->
                                DropdownMenuItem(text = { Text(g) }, onClick = { bg = g; showMenu = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = countText,
                        onValueChange = { countText = it },
                        label = { Text("মোট রক্তদানের সংখ্যা") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(value = lastDate, onValueChange = { lastDate = it }, label = { Text("সর্বশেষ রক্তদানের তারিখ") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateOwnDonorSettings(isDonor, isAvailable, countText.toLongOrNull() ?: 0L, lastDate, bg)
                    showDonorSettings = false
                }) { Text("সংরক্ষণ করুন") }
            },
            dismissButton = { TextButton(onClick = { showDonorSettings = false }) { Text("বাতিল") } }
        )
    }
}

private fun urgencyBn(urgency: String): String = when (urgency) {
    "critical" -> "অতি সঙ্কটাপন্ন"
    "urgent" -> "জরুরি"
    else -> "সাধারণ"
}
