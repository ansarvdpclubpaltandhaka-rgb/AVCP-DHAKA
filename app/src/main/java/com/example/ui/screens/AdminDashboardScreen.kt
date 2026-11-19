package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppConstants
import com.example.core.UiState
import com.example.model.*
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: UserProfile,
    viewModel: MainViewModel,
    onApproveMemberWithTrade: (memberId: String, trade: String) -> Unit = { id, trade ->
        viewModel.approveMemberWithTrade(id, trade)
    },
    onRejectMember: (memberId: String) -> Unit = { id ->
        viewModel.rejectMember(id)
    },
    onDeleteMemberWithAccount: (memberId: String) -> Unit = { id ->
        viewModel.deleteMember(id)
    },
    onBack: () -> Unit,
    onOpenBloodRequestFlow: () -> Unit
) {
    val isSuper = viewModel.isSuperAdmin
    fun hasPerm(p: String) = isSuper || p in currentUser.permissions

    val orgConfig by viewModel.orgConfig.collectAsState()
    val servicesState by viewModel.servicesState.collectAsState()
    val membersState by viewModel.adminMembersState.collectAsState()
    val volunteersState by viewModel.adminVolunteersState.collectAsState()
    val positionsState by viewModel.positionsState.collectAsState()
    val activitiesState by viewModel.activitiesState.collectAsState()
    val bloodState by viewModel.bloodRequestsState.collectAsState()
    val requestsState by viewModel.adminServiceRequestsState.collectAsState()
    val medicalState by viewModel.adminMedicalCasesState.collectAsState()
    val contribsState by viewModel.adminContributionsState.collectAsState()
    val donationsState by viewModel.adminDonationsState.collectAsState()
    val expensesState by viewModel.adminExpensesState.collectAsState()
    val noticesState by viewModel.noticesState.collectAsState()
    val eventsState by viewModel.eventsState.collectAsState()
    val usersState by viewModel.adminUsersState.collectAsState()
    val finSummary by viewModel.financialSummary.collectAsState()

    val services = (servicesState as? UiState.Success)?.data.orEmpty()
    val members = (membersState as? UiState.Success)?.data.orEmpty()
    val volunteers = (volunteersState as? UiState.Success)?.data.orEmpty()
    val positions = (positionsState as? UiState.Success)?.data.orEmpty()
    val activities = (activitiesState as? UiState.Success)?.data.orEmpty()
    val bloodRequests = (bloodState as? UiState.Success)?.data.orEmpty()
    val serviceRequests = (requestsState as? UiState.Success)?.data.orEmpty()
    val medicalCases = (medicalState as? UiState.Success)?.data.orEmpty()
    val contributions = (contribsState as? UiState.Success)?.data.orEmpty()
    val donations = (donationsState as? UiState.Success)?.data.orEmpty()
    val expenses = (expensesState as? UiState.Success)?.data.orEmpty()
    val notices = (noticesState as? UiState.Success)?.data.orEmpty()
    val events = (eventsState as? UiState.Success)?.data.orEmpty()
    val users = (usersState as? UiState.Success)?.data.orEmpty()

    var activeSection by remember { mutableStateOf("overview") }
    var selectedTradeFilter by remember { mutableStateOf("All") }

    // Dialog states
    var showMemberDialog by remember { mutableStateOf<MemberRecord?>(null) }
    var approveMemberTarget by remember { mutableStateOf<MemberRecord?>(null) }
    var openNewMember by remember { mutableStateOf(false) }
    var showVolunteerDialog by remember { mutableStateOf<VolunteerRecord?>(null) }
    var openNewVolunteer by remember { mutableStateOf(false) }
    var showPositionDialog by remember { mutableStateOf<PositionRecord?>(null) }
    var openNewPosition by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf<ServiceCategory?>(null) }
    var openNewService by remember { mutableStateOf(false) }
    var showActivityDialog by remember { mutableStateOf<ActivityRecord?>(null) }
    var openNewActivity by remember { mutableStateOf(false) }
    var openContributionDialog by remember { mutableStateOf(false) }
    var openDonationDialog by remember { mutableStateOf(false) }
    var openExpenseDialog by remember { mutableStateOf(false) }
    var openNoticeDialog by remember { mutableStateOf(false) }
    var openEventDialog by remember { mutableStateOf(false) }
    var editRoleUser by remember { mutableStateOf<UserProfile?>(null) }

    val sectionTabs = remember(isSuper, currentUser.permissions) {
        buildList {
            add("overview" to "ড্যাশবোর্ড")
            if (hasPerm(AppConstants.PERM_SERVICES)) add("services" to "সেবা")
            if (hasPerm(AppConstants.PERM_MEMBERS)) add("members" to "সদস্য ও স্বেচ্ছাসেবক")
            if (hasPerm(AppConstants.PERM_ACTIVITIES)) add("activities" to "কার্যক্রম")
            if (hasPerm(AppConstants.PERM_REQUESTS)) add("requests" to "আবেদন")
            if (hasPerm(AppConstants.PERM_FINANCE)) add("finance" to "অর্থ হিসাব")
            if (hasPerm(AppConstants.PERM_NOTICES)) add("notices" to "নোটিশ ও ইভেন্ট")
            if (isSuper || hasPerm(AppConstants.PERM_SETTINGS)) add("settings" to "ভূমিকা ও সেটিংস")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSuper) "সুপার অ্যাডমিন প্যানেল" else "অ্যাডমিন প্যানেল") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sectionTabs) { (key, label) ->
                        FilterChip(
                            selected = activeSection == key,
                            onClick = { activeSection = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            when (activeSection) {
                "overview" -> {
                    val pendingMembers = members.filter {
                        it.status.equals(AppConstants.STATUS_PENDING, ignoreCase = true) ||
                            (!it.status.equals(AppConstants.STATUS_REJECTED, ignoreCase = true) && it.trade.isBlank())
                    }
                    val approvedMembers = members.filter {
                        AppConstants.isApprovedMemberStatus(it.status) && it.trade.isNotBlank()
                    }

                    item {
                        Text(
                            text = "Pending Members • অপেক্ষমাণ আবেদন (${pendingMembers.size} জন)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("admin_pending_section_header")
                        )
                    }
                    if (pendingMembers.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.VerifiedUser,
                                title = "কোনো অপেক্ষমাণ (Pending) আবেদন নেই",
                                subtitle = "নতুন সদস্য নিবন্ধন করলে এখানে নাম, ফোন, প্লাটুন সহ দেখাবে — ট্রেড নির্বাচন করে Approve বা Reject করতে পারবেন।"
                            )
                        }
                    } else {
                        items(pendingMembers, key = { "ov_pending_${it.id}" }) { pm ->
                            PendingMemberApprovalCard(
                                member = pm,
                                onApprove = { trade -> onApproveMemberWithTrade(pm.id, trade) },
                                onReject = { onRejectMember(pm.id) }
                            )
                        }
                    }

                    item {
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Text("রিয়েল-টাইম পরিসংখ্যান", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            AdminStatLine("মোট অনুমোদিত সদস্য (Approved)", "${approvedMembers.size}")
                            AdminStatLine("অপেক্ষমাণ আবেদন (Pending)", "${pendingMembers.size}")
                            AdminStatLine("মোট স্বেচ্ছাসেবক", "${volunteers.size}")
                            AdminStatLine("সক্রিয় সেবা", "${services.count { it.active }}")
                            AdminStatLine("সম্পন্ন কার্যক্রম", "${activities.count { it.status == "completed" }}")
                            AdminStatLine("রক্তের আবেদন", "${bloodRequests.size}")
                            AdminStatLine("মোট চাঁদা/অবদান", "৳${finSummary.totalContributions.toLong()}")
                            AdminStatLine("মোট অনুদান", "৳${finSummary.totalDonations.toLong()}")
                            AdminStatLine("মোট ব্যয়", "৳${finSummary.totalExpenses.toLong()}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            AdminStatLine("বর্তমান তহবিল", "৳${finSummary.currentBalance.toLong()}", highlight = true)
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ট্রেড অনুযায়ী অনুমোদিত সদস্য দেখুন (Filter by Trade)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedTradeFilter == "All",
                                        onClick = {
                                            selectedTradeFilter = "All"
                                            if (hasPerm(AppConstants.PERM_MEMBERS)) activeSection = "members"
                                        },
                                        label = { Text("সকল (${approvedMembers.size})") }
                                    )
                                }
                                items(AppConstants.MEMBER_TRADES) { tradeName ->
                                    val count = approvedMembers.count { it.trade.equals(tradeName, ignoreCase = true) }
                                    FilterChip(
                                        selected = selectedTradeFilter == tradeName,
                                        onClick = {
                                            selectedTradeFilter = tradeName
                                            if (hasPerm(AppConstants.PERM_MEMBERS)) activeSection = "members"
                                        },
                                        label = { Text("$tradeName ($count)") }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("দ্রুত কার্যক্রম (Quick Actions)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (hasPerm(AppConstants.PERM_MEMBERS)) {
                                    QuickActionBtn("সদস্য যোগ", Icons.Rounded.PersonAdd, Modifier.weight(1f)) { openNewMember = true }
                                    QuickActionBtn("স্বেচ্ছাসেবক যোগ", Icons.Rounded.GroupAdd, Modifier.weight(1f)) { openNewVolunteer = true }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (hasPerm(AppConstants.PERM_ACTIVITIES)) {
                                    QuickActionBtn("কার্যক্রম যোগ", Icons.Rounded.AddCircleOutline, Modifier.weight(1f)) { openNewActivity = true }
                                }
                                if (hasPerm(AppConstants.PERM_BLOOD)) {
                                    QuickActionBtn("রক্তের আবেদন", Icons.Rounded.Bloodtype, Modifier.weight(1f), onOpenBloodRequestFlow)
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (hasPerm(AppConstants.PERM_FINANCE)) {
                                    QuickActionBtn("চাঁদা যোগ", Icons.Rounded.Payments, Modifier.weight(1f)) { openContributionDialog = true }
                                    QuickActionBtn("অনুদান যোগ", Icons.Rounded.VolunteerActivism, Modifier.weight(1f)) { openDonationDialog = true }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (hasPerm(AppConstants.PERM_FINANCE)) {
                                    QuickActionBtn("ব্যয় যোগ", Icons.Rounded.ReceiptLong, Modifier.weight(1f)) { openExpenseDialog = true }
                                }
                                if (hasPerm(AppConstants.PERM_NOTICES)) {
                                    QuickActionBtn("নোটিশ তৈরি", Icons.Rounded.Campaign, Modifier.weight(1f)) { openNoticeDialog = true }
                                }
                            }
                        }
                    }
                }

                "services" -> {
                    item {
                        AnimatedPrimaryActionButton(
                            text = "নতুন সেবার ধরন যোগ করুন",
                            icon = Icons.Rounded.Add,
                            onClick = { openNewService = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (services.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.Handshake,
                                title = "কোনো সেবার ধরন নেই",
                                subtitle = "রক্তদান, চিকিৎসা সহায়তা, খাদ্য বিতরণ ইত্যাদি সেবা যুক্ত করুন।"
                            )
                        }
                    } else {
                        items(services, key = { it.id }) { srv ->
                            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(srv.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(srv.description, style = MaterialTheme.typography.bodySmall)
                                        Text(if (srv.active) "সক্রিয়" else "নিষ্ক্রিয়", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showServiceDialog = srv }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "সম্পাদনা")
                                    }
                                    IconButton(onClick = { viewModel.deleteServiceCategory(srv.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                "members" -> {
                    val pendingMembers = members.filter {
                        it.status.equals(AppConstants.STATUS_PENDING, ignoreCase = true) ||
                            (!it.status.equals(AppConstants.STATUS_REJECTED, ignoreCase = true) && it.trade.isBlank())
                    }
                    val approvedMembers = members.filter {
                        AppConstants.isApprovedMemberStatus(it.status) && it.trade.isNotBlank()
                    }

                    item {
                        Text(
                            text = "Pending Members • অপেক্ষমাণ আবেদন (${pendingMembers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (pendingMembers.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.VerifiedUser,
                                title = "কোনো অপেক্ষমাণ আবেদন নেই",
                                subtitle = "সদস্য নিবন্ধন করলে এখানে নাম, ফোন, প্লাটুন দেখাবে এবং ট্রেড সিলেক্ট করে Approve / Reject করতে পারবেন।"
                            )
                        }
                    } else {
                        items(pendingMembers, key = { "pending_${it.id}" }) { pm ->
                            PendingMemberApprovalCard(
                                member = pm,
                                onApprove = { trade -> onApproveMemberWithTrade(pm.id, trade) },
                                onReject = { onRejectMember(pm.id) }
                            )
                        }
                    }

                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimatedPrimaryActionButton(text = "সদস্য যোগ", icon = Icons.Rounded.PersonAdd, onClick = { openNewMember = true }, modifier = Modifier.weight(1f))
                            AnimatedOutlinedActionButton(text = "স্বেচ্ছাসেবক যোগ", icon = Icons.Rounded.GroupAdd, onClick = { openNewVolunteer = true }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedOutlinedActionButton(text = "সাংগঠনিক পদবি যোগ", icon = Icons.Rounded.Badge, onClick = { openNewPosition = true }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("অনুমোদিত সদস্য ট্রেড ফিল্টার (Approved by Trade)", style = MaterialTheme.typography.labelMedium)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedTradeFilter == "All",
                                        onClick = { selectedTradeFilter = "All" },
                                        label = { Text("সকল অনুমোদিত (${approvedMembers.size})") }
                                    )
                                }
                                items(AppConstants.MEMBER_TRADES) { tradeName ->
                                    val count = approvedMembers.count { it.trade.equals(tradeName, ignoreCase = true) }
                                    FilterChip(
                                        selected = selectedTradeFilter == tradeName,
                                        onClick = { selectedTradeFilter = tradeName },
                                        label = { Text("$tradeName ($count)") }
                                    )
                                }
                            }
                        }
                    }

                    val filteredApprovedMembers = approvedMembers.filter { m ->
                        selectedTradeFilter == "All" || m.trade.equals(selectedTradeFilter, ignoreCase = true)
                    }
                    item {
                        Text(
                            text = if (selectedTradeFilter == "All") "অনুমোদিত সদস্য তালিকা (${filteredApprovedMembers.size})"
                            else "$selectedTradeFilter ট্রেডের অনুমোদিত সদস্য (${filteredApprovedMembers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (filteredApprovedMembers.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.Groups,
                                title = "এই ট্রেডে কোনো অনুমোদিত সদস্য নেই",
                                subtitle = "অন্য ট্রেড বাছাই করুন অথবা অপেক্ষমাণ সদস্যকে অনুমোদন দিয়ে এই ট্রেড সেট করুন।"
                            )
                        }
                    } else {
                        items(filteredApprovedMembers, key = { "m_${it.id}" }) { m ->
                            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(m.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "ট্রেড: ${m.trade} • প্লাটুন: ${m.platoon.ifBlank { m.position }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${m.phone} • Approved",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { approveMemberTarget = m }) {
                                        Icon(Icons.Rounded.VerifiedUser, contentDescription = "ট্রেড পরিবর্তন", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showMemberDialog = m }) { Icon(Icons.Rounded.Edit, contentDescription = "সম্পাদনা") }
                                    IconButton(onClick = { onDeleteMemberWithAccount(m.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }

                    item { Text("স্বেচ্ছাসেবক তালিকা (${volunteers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(volunteers, key = { "v_${it.id}" }) { v ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(v.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${v.skills} • ${v.phone}", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(
                                    checked = v.status == "active",
                                    onCheckedChange = { checked -> viewModel.toggleVolunteerStatus(v.id, if (checked) "active" else "inactive") }
                                )
                                IconButton(onClick = { showVolunteerDialog = v }) { Icon(Icons.Rounded.Edit, contentDescription = "সম্পাদনা") }
                                IconButton(onClick = { viewModel.deleteVolunteer(v.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }

                    item { Text("পদবি তালিকা (${positions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(positions, key = { "p_${it.id}" }) { pos ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${pos.rankOrder}. ${pos.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    if (pos.holderName.isNotBlank()) Text(pos.holderName, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { showPositionDialog = pos }) { Icon(Icons.Rounded.Edit, contentDescription = "সম্পাদনা") }
                                IconButton(onClick = { viewModel.deletePosition(pos.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }

                "activities" -> {
                    item {
                        AnimatedPrimaryActionButton(
                            text = "নতুন মানবিক কার্যক্রম যোগ",
                            icon = Icons.Rounded.Add,
                            onClick = { openNewActivity = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(activities, key = { it.id }) { act ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(act.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${act.category} • ${act.date} • ${if (act.status == "completed") "সম্পন্ন" else "চলমান"}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { showActivityDialog = act }) { Icon(Icons.Rounded.Edit, contentDescription = "সম্পাদনা") }
                                IconButton(onClick = { viewModel.deleteActivity(act.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }

                "requests" -> {
                    item { Text("সেবার আবেদন (${serviceRequests.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(serviceRequests, key = { "sr_${it.id}" }) { req ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Text("${req.serviceTitle} — ${req.requesterName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(req.details, style = MaterialTheme.typography.bodySmall)
                            Text("ফোন: ${req.contactPhone} • অবস্থা: ${statusBn(req.status)}", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = { viewModel.updateServiceRequestStatus(req, "approved", "অনুমোদিত হয়েছে") }) { Text("অনুমোদন") }
                                TextButton(onClick = { viewModel.updateServiceRequestStatus(req, "completed", "সেবা প্রদান সম্পন্ন") }) { Text("সম্পন্ন") }
                                TextButton(onClick = { viewModel.updateServiceRequestStatus(req, "cancelled", "বাতিল করা হয়েছে") }) { Text("বাতিল") }
                            }
                        }
                    }

                    item { Text("চিকিৎসা সহায়তা (${medicalCases.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(medicalCases, key = { "mc_${it.id}" }) { med ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Text("${med.patientName} • ৳${med.estimatedCost.toLong()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(med.conditionSummary, style = MaterialTheme.typography.bodySmall)
                            Text("অবস্থা: ${statusBn(med.status)}", style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = { viewModel.updateMedicalCaseStatus(med, "verified") }) { Text("যাচাইকৃত") }
                                TextButton(onClick = { viewModel.updateMedicalCaseStatus(med, "assisted") }) { Text("সহায়তা প্রদান") }
                            }
                        }
                    }
                }

                "finance" -> {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimatedPrimaryActionButton(text = "চাঁদা", icon = Icons.Rounded.Add, onClick = { openContributionDialog = true }, modifier = Modifier.weight(1f))
                            AnimatedOutlinedActionButton(text = "অনুদান", icon = Icons.Rounded.Add, onClick = { openDonationDialog = true }, modifier = Modifier.weight(1f))
                            AnimatedOutlinedActionButton(text = "ব্যয়", icon = Icons.Rounded.Add, onClick = { openExpenseDialog = true }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            AdminStatLine("মোট চাঁদা", "৳${finSummary.totalContributions.toLong()}")
                            AdminStatLine("মোট অনুদান", "৳${finSummary.totalDonations.toLong()}")
                            AdminStatLine("মোট ব্যয়", "৳${finSummary.totalExpenses.toLong()}")
                            AdminStatLine("বর্তমান তহবিল", "৳${finSummary.currentBalance.toLong()}", highlight = true)
                        }
                    }
                    items(contributions, key = { "c_${it.id}" }) { c ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("${c.memberName} (${c.month})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("পরিশোধিত: ৳${c.paidAmount.toLong()} • বকেয়া: ৳${c.dueAmount.toLong()}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteContribution(c.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                    items(donations, key = { "d_${it.id}" }) { d ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("অনুদান: ${d.donorName} (৳${d.amount.toLong()})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${d.purpose} • ${d.donationDate}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteDonation(d.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                    items(expenses, key = { "e_${it.id}" }) { e ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("ব্যয়: ${e.title} (৳${e.amount.toLong()})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${e.category} • ${e.expenseDate}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteExpense(e.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }

                "notices" -> {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimatedPrimaryActionButton(text = "নোটিশ তৈরি", icon = Icons.Rounded.Campaign, onClick = { openNoticeDialog = true }, modifier = Modifier.weight(1f))
                            AnimatedOutlinedActionButton(text = "ইভেন্ট/সভা", icon = Icons.Rounded.Event, onClick = { openEventDialog = true }, modifier = Modifier.weight(1f))
                        }
                    }
                    items(notices, key = { "n_${it.id}" }) { n ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(n.body, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
                                IconButton(onClick = { viewModel.deleteNotice(n.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                    items(events, key = { "ev_${it.id}" }) { ev ->
                        EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(ev.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${ev.eventDate} • ${ev.location}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteEvent(ev.id) }) { Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }

                "settings" -> {
                    if (hasPerm(AppConstants.PERM_SETTINGS)) {
                        item {
                            OrgAndCdnSettingsCard(
                                orgConfig = orgConfig,
                                onSave = { n, t, c, a, p, e, prov, cloud, preset, customUrl ->
                                    viewModel.saveOrganizationConfig(n, t, c, a, p, e, prov, cloud, preset, customUrl)
                                }
                            )
                        }
                    }
                    if (isSuper) {
                        item {
                            Text("ব্যবহারকারী ও অ্যাডমিন ভূমিকা ব্যবস্থাপনা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(users, key = { "u_${it.uid}" }) { u ->
                            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(u.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${u.email} • ভূমিকা: ${u.role}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (u.uid != AppConstants.SUPER_ADMIN_UID) {
                                        AnimatedOutlinedActionButton(
                                            text = "ভূমিকা",
                                            onClick = { editRoleUser = u }
                                        )
                                    } else {
                                        AssistChip(onClick = {}, label = { Text("সুপার অ্যাডমিন") })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Renderers
    if (openNewService || showServiceDialog != null) {
        ServiceCategoryDialog(
            initial = showServiceDialog,
            onDismiss = { openNewService = false; showServiceDialog = null },
            onSave = { id, t, d, icon, active, sort -> viewModel.saveServiceCategory(id, t, d, icon, active, sort) }
        )
    }
    if (openNewMember || showMemberDialog != null) {
        MemberFormDialog(
            initial = showMemberDialog,
            orgConfig = orgConfig,
            onDismiss = { openNewMember = false; showMemberDialog = null },
            onSave = { id, lUid, name, photo, phone, email, bg, pos, jDate, addr, st, bDonor, dAvail, dCount, lastD, platoon, trade ->
                viewModel.saveMember(id, lUid, name, photo, phone, email, bg, pos, jDate, addr, st, bDonor, dAvail, dCount, lastD, platoon, trade)
            }
        )
    }
    approveMemberTarget?.let { target ->
        var chosenTrade by remember(target.id) {
            mutableStateOf(
                target.trade.takeIf { it in AppConstants.MEMBER_TRADES }
                    ?: AppConstants.MEMBER_TRADES.first()
            )
        }
        AlertDialog(
            onDismissRequest = { approveMemberTarget = null },
            title = { Text("সদস্য অনুমোদন ও ট্রেড সেট করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${target.name} • ${target.phone}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("প্লাটুন: ${target.platoon.ifBlank { "নির্ধারিত নয়" }}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ট্রেড নির্বাচন করুন (Trade):", style = MaterialTheme.typography.labelLarge)
                    AppConstants.MEMBER_TRADES.forEach { t ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            RadioButton(selected = chosenTrade == t, onClick = { chosenTrade = t })
                            Text(t, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onApproveMemberWithTrade(target.id, chosenTrade)
                    approveMemberTarget = null
                }) { Text("অনুমোদন করুন") }
            },
            dismissButton = {
                TextButton(onClick = { approveMemberTarget = null }) { Text("বাতিল") }
            }
        )
    }
    if (openNewVolunteer || showVolunteerDialog != null) {
        VolunteerFormDialog(
            initial = showVolunteerDialog,
            orgConfig = orgConfig,
            onDismiss = { openNewVolunteer = false; showVolunteerDialog = null },
            onSave = { id, lUid, name, photo, phone, skills, avail, bg, jDate, st ->
                viewModel.saveVolunteer(id, lUid, name, photo, phone, skills, avail, bg, jDate, st)
            }
        )
    }
    if (openNewPosition || showPositionDialog != null) {
        PositionFormDialog(
            initial = showPositionDialog,
            onDismiss = { openNewPosition = false; showPositionDialog = null },
            onSave = { id, t, rank, hName, hPhoto, desc -> viewModel.savePosition(id, t, rank, hName, hPhoto, desc) }
        )
    }
    if (openNewActivity || showActivityDialog != null) {
        ActivityFormDialog(
            initial = showActivityDialog,
            orgConfig = orgConfig,
            onDismiss = { openNewActivity = false; showActivityDialog = null },
            onSave = { id, t, d, c, img, date, loc, p, b, budget, st ->
                viewModel.saveActivity(id, t, d, c, img, date, loc, p, b, budget, st)
            }
        )
    }
    if (openContributionDialog) {
        ContributionFormDialog(
            onDismiss = { openContributionDialog = false },
            onSave = { uid, name, exp, paid, month, date, method ->
                viewModel.addContribution(uid, name, exp, paid, month, date, method)
            }
        )
    }
    if (openDonationDialog) {
        DonationFormDialog(
            onDismiss = { openDonationDialog = false },
            onSave = { donor, amt, purpose, date, method ->
                viewModel.addDonation(donor, amt, purpose, date, method)
            }
        )
    }
    if (openExpenseDialog) {
        ExpenseFormDialog(
            onDismiss = { openExpenseDialog = false },
            onSave = { title, cat, amt, date, notes ->
                viewModel.addExpense(title, cat, amt, date, notes)
            }
        )
    }
    if (openNoticeDialog) {
        NoticeFormDialog(
            onDismiss = { openNoticeDialog = false },
            onSave = { t, b, type, pinned -> viewModel.createNotice(t, b, type, pinned) }
        )
    }
    if (openEventDialog) {
        EventFormDialog(
            onDismiss = { openEventDialog = false },
            onSave = { t, d, type, date, time, loc -> viewModel.createEvent(t, d, type, date, time, loc) }
        )
    }
    if (editRoleUser != null) {
        RolePermissionDialog(
            user = editRoleUser!!,
            onDismiss = { editRoleUser = null },
            onSave = { role, perms, status, pos ->
                viewModel.updateUserRoleAndPermissions(editRoleUser!!.uid, role, perms, status, pos)
            }
        )
    }
}

@Composable
private fun OrgAndCdnSettingsCard(
    orgConfig: OrganizationConfig?,
    onSave: (String, String, String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember(orgConfig) { mutableStateOf(orgConfig?.name ?: AppConstants.ORG_NAME_BN) }
    var tagline by remember(orgConfig) { mutableStateOf(orgConfig?.tagline ?: AppConstants.ORG_TAGLINE_BN) }
    var clubName by remember(orgConfig) { mutableStateOf(orgConfig?.clubName ?: AppConstants.CLUB_NAME_BN) }
    var address by remember(orgConfig) { mutableStateOf(orgConfig?.address ?: "পল্টন, ঢাকা") }
    var phone by remember(orgConfig) { mutableStateOf(orgConfig?.phone ?: "") }
    var email by remember(orgConfig) { mutableStateOf(orgConfig?.email ?: "") }
    var provider by remember(orgConfig) { mutableStateOf(orgConfig?.imageUploadProvider ?: "none") }
    var cloudName by remember(orgConfig) { mutableStateOf(orgConfig?.cloudinaryCloudName ?: "") }
    var preset by remember(orgConfig) { mutableStateOf(orgConfig?.cloudinaryUploadPreset ?: "") }
    var customUrl by remember(orgConfig) { mutableStateOf(orgConfig?.customUploadUrl ?: "") }

    EmbossedCard(modifier = Modifier.fillMaxWidth()) {
        Text("সংগঠন ও এক্সটার্নাল ইমেজ হোস্টিং সেটিংস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = clubName, onValueChange = { clubName = it }, label = { Text("ক্লাবের নাম") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("সংগঠনের নাম") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("স্লোগান") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text("ছবি আপলোড প্রোভাইডার (Firebase Storage ব্যতীত)", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = provider == "none", onClick = { provider = "none" }, label = { Text("শুধু URL") })
            FilterChip(selected = provider == "cloudinary", onClick = { provider = "cloudinary" }, label = { Text("Cloudinary") })
            FilterChip(selected = provider == "custom", onClick = { provider = "custom" }, label = { Text("Custom HTTPS") })
        }
        if (provider == "cloudinary") {
            OutlinedTextField(value = cloudName, onValueChange = { cloudName = it }, label = { Text("Cloudinary Cloud Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = preset, onValueChange = { preset = it }, label = { Text("Unsigned Upload Preset") }, modifier = Modifier.fillMaxWidth())
        } else if (provider == "custom") {
            OutlinedTextField(value = customUrl, onValueChange = { customUrl = it }, label = { Text("Custom HTTPS Upload Endpoint") }, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedPrimaryActionButton(
            text = "সেটিংস সংরক্ষণ করুন",
            icon = Icons.Rounded.Save,
            onClick = { onSave(name, tagline, clubName, address, phone, email, provider, cloudName, preset, customUrl) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuickActionBtn(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AnimatedOutlinedActionButton(
        text = text,
        icon = icon,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun AdminStatLine(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PendingMemberApprovalCard(
    member: MemberRecord,
    onApprove: (trade: String) -> Unit,
    onReject: () -> Unit
) {
    var selectedTrade by remember(member.id) {
        mutableStateOf(member.trade.takeIf { it in AppConstants.MEMBER_TRADES } ?: "")
    }
    var tradeSelectionError by remember(member.id) { mutableStateOf(false) }

    EmbossedCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.50f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ফোন: ${member.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "প্লাটুন: ${member.platoon.ifBlank { "নির্ধারিত নয়" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text("Pending") }
                )
            }

            Text(
                text = "ট্রেড নির্বাচন করুন (${AppConstants.MEMBER_TRADES.size}টি):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AppConstants.MEMBER_TRADES) { tradeName ->
                    FilterChip(
                        selected = selectedTrade == tradeName,
                        onClick = {
                            selectedTrade = tradeName
                            tradeSelectionError = false
                        },
                        label = { Text(tradeName) }
                    )
                }
            }

            if (tradeSelectionError) {
                Text(
                    text = "অনুগ্রহ করে উপরের তালিকা থেকে একটি ট্রেড (Trade) নির্বাচন করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedPrimaryActionButton(
                    text = "Approve",
                    icon = Icons.Rounded.CheckCircle,
                    onClick = {
                        if (selectedTrade in AppConstants.MEMBER_TRADES) {
                            onApprove(selectedTrade)
                        } else {
                            tradeSelectionError = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                AnimatedOutlinedActionButton(
                    text = "Reject",
                    icon = Icons.Rounded.Close,
                    accentColor = MaterialTheme.colorScheme.error,
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
