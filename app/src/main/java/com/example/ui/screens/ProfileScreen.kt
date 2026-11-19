package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.AppConstants
import com.example.model.OrganizationConfig
import com.example.model.UserProfile
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import com.example.ui.components.ImagePickerUploadField
import com.example.ui.components.ViewOnlyBanner

@Composable
fun ProfileScreen(
    user: UserProfile,
    isAuthenticated: Boolean,
    orgConfig: OrganizationConfig?,
    isAdmin: Boolean,
    isSuperAdmin: Boolean,
    onMemberLoginClick: () -> Unit,
    onAdminLoginClick: () -> Unit,
    onUpdateProfile: (name: String, phone: String, photoUrl: String, bloodGroup: String) -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenAllowanceDetails: () -> Unit,
    onOpenNotices: () -> Unit,
    onOpenBloodDonation: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onSignOut: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val isMemberSession = isAuthenticated && !isAdmin
    val isMemberPending = isMemberSession &&
        (!AppConstants.isApprovedMemberStatus(user.status) || user.trade.isBlank())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isAdmin) {
            item {
                ViewOnlyBanner(
                    message = if (isMemberPending) {
                        "Pending Approval • আপনার নিবন্ধন অ্যাডমিনের অনুমোদনের অপেক্ষায় রয়েছে (ট্রেড এখনো সেট করা হয়নি)"
                    } else if (isMemberSession) {
                        "গোপনীয়তা সুরক্ষিত • আপনি শুধুমাত্র আপনার নিজের প্রোফাইল, উপস্থিতি, ভাতা ও নোটিশ দেখতে পারবেন"
                    } else {
                        "গোপনীয়তা সুরক্ষিত • শুধুমাত্র অ্যাডমিন সকল সদস্য তালিকা ও ভাতা দেখতে পারবেন"
                    }
                )
            }
        }

        item {
            EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                if (!isAuthenticated) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("সেটিংস ও অ্যাকাউন্ট", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "বর্তমানে View Only মোডে আছেন। সদস্য যোগ/মুছা, ভাতা এডিট ও নোটিশ পাঠাতে Admin Login করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    AnimatedPrimaryActionButton(
                        text = "সদস্য লগইন / নিবন্ধন (Member Login)",
                        icon = Icons.AutoMirrored.Rounded.Login,
                        onClick = onMemberLoginClick,
                        modifier = Modifier.fillMaxWidth().testTag("settings_member_login_button")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AnimatedOutlinedActionButton(
                        text = "Admin Login (অ্যাডমিন লগইন)",
                        icon = Icons.Rounded.AdminPanelSettings,
                        onClick = onAdminLoginClick,
                        modifier = Modifier.fillMaxWidth().testTag("settings_admin_login_button")
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (user.photoURL.isNotBlank()) {
                            AsyncImage(
                                model = user.photoURL,
                                contentDescription = user.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (user.email.isNotBlank()) {
                                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (user.platoon.isNotBlank()) {
                                Text("প্লাটুন: ${user.platoon}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                            if (isMemberSession) {
                                Text(
                                    text = "ট্রেড: ${user.trade.ifBlank { "অপেক্ষমাণ (Pending)" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isMemberPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (user.phone.isNotBlank()) {
                                Text("ফোন: ${user.phone}", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            when {
                                                isSuperAdmin -> "সুপার অ্যাডমিন"
                                                isAdmin -> "অ্যাডমিন"
                                                isMemberPending -> "সদস্য (Pending)"
                                                else -> "সদস্য (${user.trade})"
                                            }
                                        )
                                    }
                                )
                                if (user.bloodGroup.isNotBlank()) {
                                    AssistChip(onClick = {}, label = { Text("রক্ত: ${user.bloodGroup}") })
                                }
                            }
                        }
                    }
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AnimatedOutlinedActionButton(
                            text = "প্রোফাইল ও ছবি সম্পাদনা",
                            icon = Icons.Rounded.Edit,
                            onClick = { showEditDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_btn")
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        AnimatedOutlinedActionButton(
                            text = "Admin Login (অ্যাডমিন হিসেবে লগইন করুন)",
                            icon = Icons.Rounded.AdminPanelSettings,
                            onClick = onAdminLoginClick,
                            modifier = Modifier.fillMaxWidth().testTag("settings_admin_login_button")
                        )
                    }
                }
            }
        }

        // Admin Panel Entry — STRICTLY hidden from normal members & guests!
        if (isAuthenticated && isAdmin) {
            item {
                EmbossedCard(
                    onClick = onOpenAdminPanel,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth().testTag("open_admin_panel_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "অ্যাডমিন প্যানেল",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "সদস্য, সেবা, কার্যক্রম, অর্থ, নোটিশ ও ভূমিকা পরিচালনা করুন",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        if (isAdmin) {
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.HowToReg,
                    title = "সকল সদস্য ও উপস্থিতি তালিকা",
                    subtitle = "সদস্য যোগ, অনুমোদন, মুছা ও উপস্থিতি দেখুন",
                    onClick = onOpenDirectory
                )
            }
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "সকল সদস্যের ভাতা (All Allowance)",
                    subtitle = "ভাতার তালিকা দেখুন ও এডিট করুন",
                    onClick = onOpenAllowanceDetails
                )
            }
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.Campaign,
                    title = "নোটিশ বোর্ড (Notice)",
                    subtitle = "নোটিশ দেখুন ও নতুন নোটিশ পাঠান",
                    onClick = onOpenNotices
                )
            }
        } else if (isMemberSession && !isMemberPending) {
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.HowToReg,
                    title = "আমার উপস্থিতি (My Attendance)",
                    subtitle = "আপনার নিজের উপস্থিতি ও ট্রেডের তথ্য দেখুন",
                    onClick = onOpenDirectory
                )
            }
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "আমার ভাতা (My Allowance)",
                    subtitle = "আপনার নিজের দৈনিক ও মাসিক ভাতার তথ্য দেখুন",
                    onClick = onOpenAllowanceDetails
                )
            }
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.Campaign,
                    title = "আমার নোটিশ (My Notice)",
                    subtitle = "সংগঠনের নোটিশ দেখুন",
                    onClick = onOpenNotices
                )
            }
        } else if (!isMemberPending) {
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.Campaign,
                    title = "নোটিশ বোর্ড (Notice)",
                    subtitle = "সংগঠনের সাধারণ নোটিশ দেখুন",
                    onClick = onOpenNotices
                )
            }
        }

        if (isAdmin) {
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.Bloodtype,
                    title = "রক্তদান ও রক্তের আবেদন",
                    subtitle = "রক্তদাতার তালিকা ও জরুরি রক্তের অনুরোধ",
                    onClick = onOpenBloodDonation
                )
            }
            item {
                ProfileNavTile(
                    icon = Icons.AutoMirrored.Rounded.Chat,
                    title = "অভ্যন্তরীণ মেসেজিং",
                    subtitle = "সদস্যদের সাথে সরাসরি বার্তা বিনিময়",
                    onClick = onOpenMessages
                )
            }
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.Notifications,
                    title = "বিজ্ঞপ্তি",
                    subtitle = "রক্তদান, নোটিশ, ইভেন্ট ও পেমেন্ট আপডেট",
                    onClick = onOpenNotifications
                )
            }
            item {
                ProfileNavTile(
                    icon = Icons.Rounded.AccountBalance,
                    title = "আর্থিক হিসাব ও অবদান",
                    subtitle = "চাঁদা, অনুদান, ব্যয় ও বর্তমান তহবিল",
                    onClick = onOpenFinance
                )
            }
        }

        if (isAuthenticated) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                AnimatedOutlinedActionButton(
                    text = "লগআউট করুন",
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    accentColor = MaterialTheme.colorScheme.error,
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().testTag("logout_button")
                )
            }
        }
    }

    if (showEditDialog) {
        var name by remember { mutableStateOf(user.name) }
        var phone by remember { mutableStateOf(user.phone) }
        var photoUrl by remember { mutableStateOf(user.photoURL) }
        var bloodGroup by remember { mutableStateOf(user.bloodGroup.ifBlank { "O+" }) }
        var showBgMenu by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("প্রোফাইল সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("ফোন নম্বর") })
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
                    ImagePickerUploadField(
                        imageUrl = photoUrl,
                        onImageUrlChange = { photoUrl = it },
                        orgConfig = orgConfig,
                        label = "প্রোফাইল ছবি"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        onUpdateProfile(name, phone, photoUrl, bloodGroup)
                        showEditDialog = false
                    }
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
private fun ProfileNavTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    EmbossedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null)
        }
    }
}
