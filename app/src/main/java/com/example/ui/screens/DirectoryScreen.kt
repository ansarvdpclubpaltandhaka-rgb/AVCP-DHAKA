package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.DemoDataProvider
import com.example.model.*
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ViewOnlyBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
    currentUser: UserProfile,
    isAuthenticated: Boolean = false,
    isAdmin: Boolean,
    directoryState: UiState<List<DirectoryEntry>>,
    positionsState: UiState<List<PositionRecord>>,
    adminMembersState: UiState<List<MemberRecord>>,
    adminVolunteersState: UiState<List<VolunteerRecord>>,
    initialTab: Int = 0,
    showTopBackButton: Boolean = true,
    onBack: () -> Unit,
    onAddMember: (name: String, phone: String, email: String, bloodGroup: String, position: String, address: String, platoon: String, trade: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onApproveMemberWithTrade: (memberId: String, trade: String) -> Unit = { _, _ -> },
    onRejectMember: (memberId: String) -> Unit = {},
    onDeleteMember: (memberId: String) -> Unit = {},
    onMessageUser: (uid: String, name: String, photoUrl: String) -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTradeFilter by remember { mutableStateOf("All") }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var memberToApprove by remember { mutableStateOf<MemberRecord?>(null) }

    val entries = (directoryState as? UiState.Success)?.data.orEmpty()
    val positions = (positionsState as? UiState.Success)?.data.orEmpty()
    val adminMembers = (adminMembersState as? UiState.Success)?.data.orEmpty()
    val adminVolunteers = (adminVolunteersState as? UiState.Success)?.data.orEmpty()

    val isMemberSession = isAuthenticated && !isAdmin
    val isMemberApprovedWithTrade = AppConstants.isApprovedMemberStatus(currentUser.status) &&
        currentUser.trade.isNotBlank()
    val q = searchQuery.trim().lowercase()

    Scaffold(
        topBar = {
            if (showTopBackButton) {
                TopAppBar(
                    title = { Text(if (isMemberSession) "আমার উপস্থিতি (My Attendance)" else "উপস্থিতি ও সদস্য তালিকা") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "পিছনে")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (isMemberSession) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ViewOnlyBanner(
                        message = "গোপনীয়তা সুরক্ষিত • আপনি শুধুমাত্র আপনার নিজের উপস্থিতি ও প্রোফাইল দেখতে পারবেন"
                    )
                }
                item {
                    if (!isMemberApprovedWithTrade) {
                        EmptyStateCard(
                            icon = Icons.Rounded.HourglassTop,
                            title = "অনুমোদনের অপেক্ষায় (Pending)",
                            subtitle = "অ্যাডমিন আপনার অ্যাকাউন্ট অনুমোদন ও ট্রেড (Trade) নির্ধারণ করার পর আপনার উপস্থিতি দেখতে পারবেন।"
                        )
                    } else {
                        EmbossedCard(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth().testTag("member_own_attendance_card")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarCircle(currentUser.photoURL)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "প্লাটুন: ${currentUser.platoon.ifBlank { "নির্ধারিত নয়" }} • ট্রেড: ${currentUser.trade}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (currentUser.phone.isNotBlank()) {
                                        Text(
                                            text = "মোবাইল: ${currentUser.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text("উপস্থিত") }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "আজকের উপস্থিতি",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "উপস্থিত (Present)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "ট্রেড (Trade)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currentUser.trade,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (!isAdmin) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ViewOnlyBanner(
                        message = "গোপনীয়তা সুরক্ষিত • সদস্য তালিকা ও সকল প্রোফাইল শুধুমাত্র অ্যাডমিন দেখতে পারবেন"
                    )
                }
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Lock,
                        title = "সদস্য তালিকা গোপন রাখা হয়েছে",
                        subtitle = "অন্য সদস্যদের তালিকা, প্রোফাইল, ফোন নম্বর এবং উপস্থিতি শুধুমাত্র অ্যাডমিন দেখতে পারবেন। আপনার নিজের উপস্থিতি দেখতে Member Login করুন।"
                    )
                }
            }
        } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isAdmin) {
                item {
                    ViewOnlyBanner(
                        message = "View Only Mode • শুধুমাত্র অ্যাডমিন সদস্য যোগ বা মুছতে পারবেন"
                    )
                }
            } else {
                item {
                    AnimatedPrimaryActionButton(
                        text = "নতুন সদস্য যোগ করুন (Add Member)",
                        icon = Icons.Rounded.PersonAdd,
                        onClick = { showAddMemberDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("directory_add_member_btn")
                    )
                }
            }

            // Attendance Summary Strip
            item {
                EmbossedCard(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "আজকের উপস্থিতি সারসংক্ষেপ (Attendance)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "মোট সদস্য: ${DemoDataProvider.DEMO_TOTAL_MEMBERS} • আজ উপস্থিত: ${DemoDataProvider.DEMO_PRESENT_TODAY} • ফিল্ডে: ${DemoDataProvider.DEMO_ON_FIELD} • কাজ করছে: ${DemoDataProvider.DEMO_WORKING_NOW}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("নাম, রক্তের গ্রুপ বা পদবি খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("সদস্য/উপস্থিতি", "স্বেচ্ছাসেবক", "পদবি").forEachIndexed { idx, label ->
                        if (selectedTab == idx) {
                            AnimatedPrimaryActionButton(
                                text = label,
                                onClick = { selectedTab = idx },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            AnimatedOutlinedActionButton(
                                text = label,
                                onClick = { selectedTab = idx },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ট্রেড অনুযায়ী ফিল্টার (Filter by Trade)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedTradeFilter == "All",
                                        onClick = { selectedTradeFilter = "All" },
                                        label = { Text("সকল (All)") },
                                        modifier = Modifier.testTag("trade_filter_All")
                                    )
                                }
                                items(AppConstants.MEMBER_TRADES) { tradeName ->
                                    FilterChip(
                                        selected = selectedTradeFilter == tradeName,
                                        onClick = { selectedTradeFilter = tradeName },
                                        label = { Text(tradeName) },
                                        modifier = Modifier.testTag("trade_filter_$tradeName")
                                    )
                                }
                            }
                        }
                    }

                    val filtered = if (isAdmin && adminMembers.isNotEmpty()) {
                        adminMembers.filter { m ->
                            val matchesTrade = selectedTradeFilter == "All" || m.trade.equals(selectedTradeFilter, ignoreCase = true)
                            val matchesQuery = q.isBlank() ||
                                m.name.lowercase().contains(q) ||
                                m.bloodGroup.lowercase().contains(q) ||
                                m.position.lowercase().contains(q) ||
                                m.platoon.lowercase().contains(q) ||
                                m.trade.lowercase().contains(q)
                            matchesTrade && matchesQuery
                        }
                    } else null

                    if (filtered != null) {
                        if (filtered.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.Groups,
                                    title = "এই ট্রেডে কোনো সদস্য নেই",
                                    subtitle = "অন্য ট্রেড নির্বাচন করুন বা নতুন সদস্যকে এই ট্রেডে অনুমোদন দিন।"
                                )
                            }
                        } else {
                            items(filtered, key = { it.id }) { m ->
                                val isPending = m.status.equals(AppConstants.STATUS_PENDING, ignoreCase = true) || m.trade.isBlank()
                                EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AvatarCircle(m.photoUrl)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(m.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "ট্রেড: ${m.trade.ifBlank { "নির্ধারিত নয়" }} • প্লাটুন: ${m.platoon.ifBlank { m.position }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text("ফোন: ${m.phone}", style = MaterialTheme.typography.bodySmall)
                                            if (m.bloodGroup.isNotBlank()) {
                                                Text("রক্ত: ${m.bloodGroup}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            AssistChip(
                                                onClick = { if (isAdmin) memberToApprove = m },
                                                label = {
                                                    Text(
                                                        when {
                                                            isPending -> "Pending"
                                                            AppConstants.isApprovedMemberStatus(m.status) -> "উপস্থিত"
                                                            else -> "নিষ্ক্রিয়"
                                                        }
                                                    )
                                                }
                                            )
                                            if (isAdmin) {
                                                Row {
                                                    IconButton(onClick = { memberToApprove = m }) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.VerifiedUser,
                                                            contentDescription = "অনুমোদন ও ট্রেড সেট",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    IconButton(onClick = { memberToDelete = m.id to m.name }) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Delete,
                                                            contentDescription = "সদস্য মুছুন",
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val publicMembers = entries.filter { e ->
                            val matchesTrade = selectedTradeFilter == "All" || e.trade.equals(selectedTradeFilter, ignoreCase = true)
                            val matchesQuery = q.isBlank() ||
                                e.name.lowercase().contains(q) ||
                                e.bloodGroup.lowercase().contains(q) ||
                                e.position.lowercase().contains(q) ||
                                e.trade.lowercase().contains(q)
                            matchesTrade && matchesQuery
                        }
                        if (publicMembers.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.Groups,
                                    title = "কোনো সদস্য পাওয়া যায়নি",
                                    subtitle = "সদস্য নিবন্ধিত হলে বা অ্যাডমিন সদস্য যুক্ত করলে এখানে দেখাবে।"
                                )
                            }
                        } else {
                            items(publicMembers, key = { it.id }) { e ->
                                DirectoryPublicCard(
                                    e = e,
                                    currentUid = currentUser.uid,
                                    isAdmin = isAdmin,
                                    onDeleteClick = { memberToDelete = e.id to e.name },
                                    onMessageUser = onMessageUser
                                )
                            }
                        }
                    }
                }

                1 -> {
                    val filteredVol = if (isAdmin && adminVolunteers.isNotEmpty()) {
                        adminVolunteers.filter {
                            q.isBlank() || it.name.lowercase().contains(q) || it.skills.lowercase().contains(q) || it.bloodGroup.lowercase().contains(q)
                        }
                    } else null

                    if (filteredVol != null) {
                        items(filteredVol, key = { it.id }) { v ->
                            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarCircle(v.photoUrl)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(v.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("দক্ষতা: ${v.skills}", style = MaterialTheme.typography.bodySmall)
                                        Text("সময়: ${v.availability} • রক্ত: ${v.bloodGroup}", style = MaterialTheme.typography.bodySmall)
                                        Text("ফোন: ${v.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    AssistChip(onClick = {}, label = { Text(if (v.status == "active") "সক্রিয়" else "নিষ্ক্রিয়") })
                                }
                            }
                        }
                    } else {
                        val publicVol = entries.filter { it.volunteer }.filter {
                            q.isBlank() || it.name.lowercase().contains(q) || it.skills.lowercase().contains(q) || it.bloodGroup.lowercase().contains(q)
                        }
                        if (publicVol.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.VolunteerActivism,
                                    title = "কোনো স্বেচ্ছাসেবক পাওয়া যায়নি",
                                    subtitle = "স্বেচ্ছাসেবক যুক্ত করা হলে এখানে প্রদর্শিত হবে।"
                                )
                            }
                        } else {
                            items(publicVol, key = { it.id }) { e ->
                                DirectoryPublicCard(
                                    e = e,
                                    currentUid = currentUser.uid,
                                    isAdmin = isAdmin,
                                    onDeleteClick = { memberToDelete = e.id to e.name },
                                    onMessageUser = onMessageUser
                                )
                            }
                        }
                    }
                }

                2 -> {
                    val filteredPos = positions.filter {
                        q.isBlank() || it.title.lowercase().contains(q) || it.holderName.lowercase().contains(q)
                    }
                    if (filteredPos.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.Badge,
                                title = "কোনো সাংগঠনিক পদবি যুক্ত হয়নি",
                                subtitle = "অ্যাডমিন প্যানেল থেকে পদবি ও দায়িত্বশীলদের তথ্য যুক্ত করা যাবে।"
                            )
                        }
                    } else {
                        items(filteredPos, key = { it.id }) { pos ->
                            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarCircle(pos.holderPhotoUrl)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pos.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        if (pos.holderName.isNotBlank()) {
                                            Text(pos.holderName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                        if (pos.description.isNotBlank()) {
                                            Text(pos.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    }

    if (showAddMemberDialog && isAdmin) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var bloodGroup by remember { mutableStateOf("O+") }
        var position by remember { mutableStateOf("সদস্য") }
        var platoon by remember { mutableStateOf("পল্টন প্লাটুন-১") }
        var trade by remember { mutableStateOf(AppConstants.MEMBER_TRADES.first()) }
        var address by remember { mutableStateOf("পল্টন, ঢাকা") }
        var showBgMenu by remember { mutableStateOf(false) }
        var showTradeMenu by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("নতুন সদস্য যোগ করুন") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম *") }, singleLine = true)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    OutlinedTextField(value = platoon, onValueChange = { platoon = it }, label = { Text("প্লাটুন *") }, singleLine = true)
                    Box {
                        OutlinedTextField(
                            value = trade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ট্রেড (Trade) *") },
                            trailingIcon = {
                                IconButton(onClick = { showTradeMenu = true }) {
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(expanded = showTradeMenu, onDismissRequest = { showTradeMenu = false }) {
                            AppConstants.MEMBER_TRADES.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { trade = t; showTradeMenu = false })
                            }
                        }
                    }
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ইমেইল (ঐচ্ছিক)") }, singleLine = true)
                    OutlinedTextField(value = position, onValueChange = { position = it }, label = { Text("পদবি") }, singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") }, singleLine = true)
                    Box {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("রক্তের গ্রুপ") },
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAddMember(name, phone, email, bloodGroup, position, address, platoon, trade)
                        showAddMemberDialog = false
                    }
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) { Text("বাতিল") }
            }
        )
    }

    memberToApprove?.let { targetMember ->
        if (isAdmin) {
            var chosenTrade by remember(targetMember.id) {
                mutableStateOf(
                    targetMember.trade.takeIf { it in AppConstants.MEMBER_TRADES }
                        ?: AppConstants.MEMBER_TRADES.first()
                )
            }
            AlertDialog(
                onDismissRequest = { memberToApprove = null },
                title = { Text("অনুমোদন ও ট্রেড নির্ধারণ") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${targetMember.name} (${targetMember.phone})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "প্লাটুন: ${targetMember.platoon.ifBlank { "নির্ধারিত নয়" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ট্রেড নির্বাচন করুন (Select Trade):", style = MaterialTheme.typography.labelLarge)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AppConstants.MEMBER_TRADES.forEach { t ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = chosenTrade == t,
                                        onClick = { chosenTrade = t }
                                    )
                                    Text(t, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            onRejectMember(targetMember.id)
                            memberToApprove = null
                        }) {
                            Text("Reject", color = MaterialTheme.colorScheme.error)
                        }
                        TextButton(onClick = {
                            onApproveMemberWithTrade(targetMember.id, chosenTrade)
                            memberToApprove = null
                        }) {
                            Text("Approve")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToApprove = null }) { Text("বাতিল") }
                }
            )
        }
    }

    memberToDelete?.let { (id, name) ->
        if (isAdmin) {
            AlertDialog(
                onDismissRequest = { memberToDelete = null },
                title = { Text("সদস্য মুছে ফেলুন") },
                text = { Text("আপনি কি নিশ্চিতভাবে '$name'-কে তালিকা থেকে মুছে ফেলতে চান?") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteMember(id)
                        memberToDelete = null
                    }) {
                        Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToDelete = null }) { Text("বাতিল") }
                }
            )
        }
    }
}

@Composable
private fun DirectoryPublicCard(
    e: DirectoryEntry,
    currentUid: String,
    isAdmin: Boolean,
    onDeleteClick: () -> Unit,
    onMessageUser: (String, String, String) -> Unit
) {
    EmbossedCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarCircle(e.photoUrl)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(e.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = buildString {
                        if (e.position.isNotBlank()) append(e.position)
                        if (e.bloodGroup.isNotBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("রক্ত: ${e.bloodGroup}")
                        }
                    }.ifBlank { "সদস্য" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (e.skills.isNotBlank()) {
                    Text("দক্ষতা: ${e.skills}", style = MaterialTheme.typography.bodySmall)
                }
            }
            AssistChip(
                onClick = {},
                label = { Text(if (e.status == "active") "উপস্থিত" else "নিষ্ক্রিয়") }
            )
            if (isAdmin) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "সদস্য মুছুন",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                if (e.linkedUid.isNotBlank() && e.linkedUid != currentUid) {
                    IconButton(onClick = { onMessageUser(e.linkedUid, e.name, e.photoUrl) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = "মেসেজ করুন",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarCircle(photoUrl: String) {
    if (photoUrl.isNotBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
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
}
