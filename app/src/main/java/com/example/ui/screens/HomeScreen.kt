package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.AppConstants
import com.example.core.UiState
import com.example.data.DemoDataProvider
import com.example.model.*
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ViewOnlyBanner
import com.example.ui.components.serviceIconForKey

@Composable
fun HomeScreen(
    currentUser: UserProfile,
    isAuthenticated: Boolean,
    isAdmin: Boolean,
    orgConfig: OrganizationConfig?,
    directoryState: UiState<List<DirectoryEntry>>,
    servicesState: UiState<List<ServiceCategory>>,
    activitiesState: UiState<List<ActivityRecord>>,
    bloodRequestsState: UiState<List<BloodRequest>>,
    noticesState: UiState<List<NoticeRecord>>,
    allowancesState: UiState<List<AllowanceRecord>>,
    onNavigateMemberLogin: () -> Unit,
    onNavigateAdminLogin: () -> Unit,
    onMemberSignOut: () -> Unit = {},
    onNavigateServices: () -> Unit,
    onNavigateBloodDonation: () -> Unit,
    onNavigateActivities: () -> Unit,
    onNavigateDirectory: () -> Unit,
    onNavigateFieldDuty: () -> Unit,
    onNavigateAllowanceDetails: () -> Unit,
    onNavigateNotices: () -> Unit
) {
    val entries = (directoryState as? UiState.Success)?.data.orEmpty()
    val services = (servicesState as? UiState.Success)?.data.orEmpty().filter { it.active }
    val activities = (activitiesState as? UiState.Success)?.data.orEmpty()
    val bloodRequests = (bloodRequestsState as? UiState.Success)?.data.orEmpty().filter { it.status == "open" }
    val notices = (noticesState as? UiState.Success)?.data.orEmpty()
    val allowances = (allowancesState as? UiState.Success)?.data.orEmpty()

    val isMemberSession = isAuthenticated && !isAdmin
    val isMemberApprovedWithTrade = AppConstants.isApprovedMemberStatus(currentUser.status) &&
        currentUser.trade.isNotBlank()

    val activeMembersCount = if (isAdmin) entries.count { AppConstants.isApprovedMemberStatus(it.status) } else 0
    val baselineActiveCount = DemoDataProvider.demoDirectory.count { AppConstants.isApprovedMemberStatus(it.status) }
    val memberDelta = if (isAdmin) activeMembersCount - baselineActiveCount else 0
    val totalMembers = (DemoDataProvider.DEMO_TOTAL_MEMBERS + memberDelta).coerceAtLeast(0)
    val presentToday = (DemoDataProvider.DEMO_PRESENT_TODAY + memberDelta).coerceAtLeast(0)
    val onFieldCount = DemoDataProvider.DEMO_ON_FIELD
    val workingNowCount = DemoDataProvider.DEMO_WORKING_NOW
    val allowanceAmount = DemoDataProvider.DEMO_ALLOWANCE_BDT
    val volunteerCount = if (isAdmin) entries.count { it.volunteer && it.status == "active" } else 0
    val completedActivities = activities.count { it.status == "completed" }

    val memberOwnAllowances = if (isMemberSession) allowances else emptyList()
    val memberOwnMonthlyAllowance = memberOwnAllowances.sumOf { it.monthlyTotal }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isMemberSession && !isMemberApprovedWithTrade) {
            item {
                EmbossedCard(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().testTag("member_pending_approval_card")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HourglassTop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "অনুমোদনের অপেক্ষায় (Pending)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentUser.name} • ${currentUser.phone}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "প্লাটুন: ${currentUser.platoon.ifBlank { "নির্ধারিত নয়" }} • স্ট্যাটাস: Pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "আপনার নিবন্ধন বর্তমানে Pending অবস্থায় আছে। অ্যাডমিন প্যানেল থেকে অ্যাডমিন আপনার অ্যাকাউন্ট অনুমোদন (Approve) করে Trade নির্ধারণ করার পরই আপনি আপনার ড্যাশবোর্ড, উপস্থিতি ও নোটিশ দেখতে পারবেন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AnimatedOutlinedActionButton(
                        text = "লগআউট করুন (Logout)",
                        icon = Icons.Rounded.Logout,
                        accentColor = MaterialTheme.colorScheme.error,
                        onClick = onMemberSignOut,
                        modifier = Modifier.fillMaxWidth().testTag("pending_member_logout_btn")
                    )
                }
            }
        } else if (isMemberSession) {
            // Personalized Approved Member-Only Dashboard (Strict Privacy: Own Profile, Attendance, Allowance & Notice only)
            item {
                ViewOnlyBanner(
                    message = "গোপনীয়তা সুরক্ষিত • শুধুমাত্র আপনার প্রোফাইল, উপস্থিতি, ভাতা ও নোটিশ"
                )
            }

            item {
                EmbossedCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().testTag("member_home_profile_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.org_logo),
                                contentDescription = AppConstants.ORG_NAME_BN,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "আমার প্রোফাইল (My Profile)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "প্লাটুন: ${currentUser.platoon.ifBlank { "নির্ধারিত নয়" }} • ট্রেড: ${currentUser.trade}",
                                    style = MaterialTheme.typography.bodyMedium,
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
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedOutlinedActionButton(
                            text = "আমার উপস্থিতি",
                            icon = Icons.Rounded.HowToReg,
                            onClick = onNavigateDirectory,
                            modifier = Modifier.weight(1f).testTag("member_home_attendance_btn")
                        )
                        AnimatedOutlinedActionButton(
                            text = "আমার নোটিশ",
                            icon = Icons.Rounded.Campaign,
                            onClick = onNavigateNotices,
                            modifier = Modifier.weight(1f).testTag("member_home_notices_btn")
                        )
                    }
                }
            }

            item {
                Text(
                    text = "আমার ড্যাশবোর্ড (My Dashboard)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChipCard(
                        title = "আমার উপস্থিতি\nMy Attendance",
                        value = "উপস্থিত",
                        icon = Icons.Rounded.VerifiedUser,
                        accent = MaterialTheme.colorScheme.secondary,
                        onClick = onNavigateDirectory,
                        modifier = Modifier.weight(1f).testTag("member_stat_attendance")
                    )
                    StatChipCard(
                        title = "আমার ট্রেড\nMy Trade",
                        value = currentUser.trade,
                        icon = Icons.Rounded.Work,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateDirectory,
                        modifier = Modifier.weight(1f).testTag("member_stat_trade")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                EmbossedCard(
                    onClick = onNavigateAllowanceDetails,
                    modifier = Modifier.fillMaxWidth().testTag("member_stat_allowance"),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = "আমার ভাতা",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "আমার ভাতা (My Allowance)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "আপনার নিজস্ব ভাতার তথ্য দেখতে ট্যাপ করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "৳$memberOwnMonthlyAllowance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "বিস্তারিত",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Recent Notices for logged-in member
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আমার নোটিশ (My Notice)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateNotices) {
                        Text("সব দেখুন")
                    }
                }
            }
            if (notices.isEmpty()) {
                item {
                    EmbossedCard(
                        onClick = onNavigateNotices,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "কোনো নতুন নোটিশ নেই",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(notices.take(3), key = { "member_notice_${it.id}" }) { notice ->
                    EmbossedCard(
                        onClick = onNavigateNotices,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notice.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            // Guest (View Only) or Admin Dashboard
            if (!isAdmin) {
                item {
                    ViewOnlyBanner(
                        message = "View Only Mode • শুধুমাত্র দেখার মোড (সদস্য বা অ্যাডমিন লগইন করতে নিচের বাটন ব্যবহার করুন)"
                    )
                }
            }

            // Hero Organization Card
            item {
                EmbossedCard(
                    onClick = if (isAdmin) onNavigateActivities else onNavigateDirectory,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().testTag("home_hero_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.org_logo),
                                contentDescription = AppConstants.ORG_NAME_BN,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = orgConfig?.clubName?.ifBlank { AppConstants.CLUB_NAME_BN } ?: AppConstants.CLUB_NAME_BN,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = orgConfig?.name?.ifBlank { AppConstants.ORG_NAME_BN } ?: AppConstants.ORG_NAME_BN,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = orgConfig?.tagline?.ifBlank { AppConstants.ORG_TAGLINE_BN } ?: AppConstants.ORG_TAGLINE_BN,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isAdmin) {
                            AnimatedOutlinedActionButton(
                                text = "রক্তদান কেন্দ্র",
                                icon = Icons.Rounded.Bloodtype,
                                accentColor = MaterialTheme.colorScheme.error,
                                onClick = onNavigateBloodDonation,
                                modifier = Modifier.weight(1f).testTag("home_blood_button")
                            )
                            AnimatedOutlinedActionButton(
                                text = "সদস্য তালিকা",
                                icon = Icons.Rounded.Groups,
                                onClick = onNavigateDirectory,
                                modifier = Modifier.weight(1f).testTag("home_directory_button")
                            )
                        } else {
                            AnimatedOutlinedActionButton(
                                text = "সদস্য লগইন",
                                icon = Icons.AutoMirrored.Rounded.Login,
                                onClick = onNavigateMemberLogin,
                                modifier = Modifier.weight(1f).testTag("home_directory_button")
                            )
                            AnimatedOutlinedActionButton(
                                text = "নোটিশ বোর্ড",
                                icon = Icons.Rounded.Campaign,
                                onClick = onNavigateNotices,
                                modifier = Modifier.weight(1f).testTag("home_notices_button")
                            )
                        }
                    }
                }
            }

            // Separate Member Login button on Home screen (separate from Admin Login)
            if (!isAuthenticated) {
                item {
                    EmbossedCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "অ্যাকাউন্ট অ্যাক্সেস (Account Access)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "সদস্যরা নিজস্ব ড্যাশবোর্ড দেখতে Member Login এবং পরিচালনার জন্য Admin Login ব্যবহার করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AnimatedPrimaryActionButton(
                            text = "সদস্য লগইন / নিবন্ধন (Member Login)",
                            icon = Icons.AutoMirrored.Rounded.Login,
                            onClick = onNavigateMemberLogin,
                            modifier = Modifier.fillMaxWidth().testTag("home_member_login_button")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedOutlinedActionButton(
                            text = "অ্যাডমিন লগইন (Admin Login)",
                            icon = Icons.Rounded.AdminPanelSettings,
                            onClick = onNavigateAdminLogin,
                            modifier = Modifier.fillMaxWidth().testTag("home_admin_login_button")
                        )
                    }
                }
            }

            // Primary Dashboard Summary: Total Members (48), Present Today (34), On Field (28), Working Now (22), Allowance (17000)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ড্যাশবোর্ড সারসংক্ষেপ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = onNavigateAllowanceDetails,
                        label = { Text(if (isAdmin) "Admin Mode" else "View Only Mode") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChipCard(
                        title = "Total Members\nমোট সদস্য",
                        value = "$totalMembers",
                        icon = Icons.Rounded.People,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateDirectory,
                        modifier = Modifier.weight(1f).testTag("dashboard_total_members")
                    )
                    StatChipCard(
                        title = "Present Today\nআজ উপস্থিত",
                        value = "$presentToday",
                        icon = Icons.Rounded.HowToReg,
                        accent = MaterialTheme.colorScheme.secondary,
                        onClick = onNavigateDirectory,
                        modifier = Modifier.weight(1f).testTag("dashboard_present_today")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChipCard(
                        title = "ফিল্ডে কতজন আছে\nTotal On Duty",
                        value = "$onFieldCount",
                        icon = Icons.Rounded.Badge,
                        accent = MaterialTheme.colorScheme.tertiary,
                        onClick = onNavigateFieldDuty,
                        modifier = Modifier.weight(1f).testTag("dashboard_on_field")
                    )
                    StatChipCard(
                        title = "কতজন কাজ করছে\nActive Working Now",
                        value = "$workingNowCount",
                        icon = Icons.Rounded.Engineering,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = if (isAdmin) onNavigateActivities else onNavigateFieldDuty,
                        modifier = Modifier.weight(1f).testTag("dashboard_working_now")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                EmbossedCard(
                    onClick = onNavigateAllowanceDetails,
                    modifier = Modifier.fillMaxWidth().testTag("dashboard_allowance"),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = "ভাতা / বরাদ্দ",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Allowance • দৈনিক/মাসিক ভাতা",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "বিস্তারিত তালিকা দেখতে ক্লিক করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "৳$allowanceAmount",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "বিস্তারিত",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatChipCard(
                            title = "স্বেচ্ছাসেবক",
                            value = "$volunteerCount",
                            icon = Icons.Rounded.VolunteerActivism,
                            accent = MaterialTheme.colorScheme.secondary,
                            onClick = onNavigateFieldDuty,
                            modifier = Modifier.weight(1f).testTag("dashboard_volunteers")
                        )
                        StatChipCard(
                            title = "সক্রিয় সেবা",
                            value = "${services.size}",
                            icon = Icons.Rounded.Handshake,
                            accent = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateServices,
                            modifier = Modifier.weight(1f).testTag("dashboard_services")
                        )
                        StatChipCard(
                            title = "সম্পন্ন কাজ",
                            value = "$completedActivities",
                            icon = Icons.Rounded.CheckCircle,
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateActivities,
                            modifier = Modifier.weight(1f).testTag("dashboard_completed")
                        )
                    }
                }
            }

            // Recent Notices (visible for all users including guests)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক নোটিশ (Notice)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateNotices) {
                        Text("সব দেখুন")
                    }
                }
            }
            if (notices.isEmpty()) {
                item {
                    EmbossedCard(
                        onClick = onNavigateNotices,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "কোনো নতুন নোটিশ নেই",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(notices.take(2), key = { "home_notice_${it.id}" }) { notice ->
                    EmbossedCard(
                        onClick = onNavigateNotices,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notice.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Admin-only extended overview sections
            if (isAdmin && bloodRequests.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "জরুরি রক্তের প্রয়োজন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateBloodDonation) {
                            Text("সব দেখুন")
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(bloodRequests.take(5), key = { it.id }) { req ->
                            EmbossedCard(
                                onClick = onNavigateBloodDonation,
                                modifier = Modifier.width(240.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.errorContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = req.bloodGroup,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = req.patientName,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = req.hospital,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${req.units} ব্যাগ • তারিখ: ${req.requiredDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Humanitarian Services (Admin view)
            if (isAdmin) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মানবিক সেবাসমূহ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateServices) {
                            Text("সকল সেবা")
                        }
                    }
                    when (servicesState) {
                        is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                        is UiState.Error -> Text(servicesState.message, color = MaterialTheme.colorScheme.error)
                        is UiState.Success -> {
                            if (services.isEmpty()) {
                                EmptyStateCard(
                                    icon = Icons.Rounded.Handshake,
                                    title = "এখনো কোনো সেবা যুক্ত করা হয়নি",
                                    subtitle = "অ্যাডমিন প্যানেল থেকে মানবিক সেবার ধরন যুক্ত করা হলে এখানে প্রদর্শিত হবে।"
                                )
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(services, key = { it.id }) { service ->
                                        EmbossedCard(
                                            onClick = onNavigateServices,
                                            modifier = Modifier.width(164.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = serviceIconForKey(service.iconKey),
                                                    contentDescription = service.title,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = service.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = service.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Activities
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সাম্প্রতিক মানবিক কার্যক্রম",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateActivities) {
                            Text("সব দেখুন")
                        }
                    }
                }

                when (activitiesState) {
                    is UiState.Loading -> item { CircularProgressIndicator(modifier = Modifier.padding(12.dp)) }
                    is UiState.Error -> item { Text(activitiesState.message, color = MaterialTheme.colorScheme.error) }
                    is UiState.Success -> {
                        if (activities.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.EventNote,
                                    title = "এখনো কোনো কার্যক্রম যুক্ত হয়নি",
                                    subtitle = "সংগঠনের চলমান ও সম্পন্ন মানবিক কার্যক্রম যুক্ত হলে এখানে দেখা যাবে।"
                                )
                            }
                        } else {
                            items(activities.take(4), key = { it.id }) { activity ->
                                EmbossedCard(
                                    onClick = onNavigateActivities,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (activity.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = activity.imageUrl,
                                            contentDescription = activity.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = onNavigateActivities,
                                            label = { Text(activity.category) }
                                        )
                                        Text(
                                            text = if (activity.status == "completed") "সম্পন্ন" else "চলমান",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (activity.status == "completed") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = activity.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activity.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "স্থান: ${activity.location}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "উপকারভোগী: ${activity.beneficiaries}",
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
        }
    }
}

@Composable
private fun StatChipCard(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmbossedCard(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
