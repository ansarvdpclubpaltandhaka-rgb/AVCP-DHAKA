package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.UiState
import com.example.data.DemoDataProvider
import com.example.model.AllowanceRecord
import com.example.model.UserProfile
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ViewOnlyBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllowanceDetailsScreen(
    currentUser: UserProfile = UserProfile(),
    isAuthenticated: Boolean = false,
    isAdmin: Boolean,
    allowancesState: UiState<List<AllowanceRecord>>,
    onBack: () -> Unit,
    onEditAllowance: (id: String, memberName: String, areaOrDuty: String, date: String, dailyAmount: Long, monthlyTotal: Long, paid: Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableIntStateOf(0) } // 0 = All, 1 = Paid, 2 = Unpaid
    var selectedRecord by remember { mutableStateOf<AllowanceRecord?>(null) }

    val isMemberSession = isAuthenticated && !isAdmin
    val rawRecords = (allowancesState as? UiState.Success)?.data.orEmpty()
    val allRecords = remember(rawRecords, isAdmin, isMemberSession, currentUser.uid, currentUser.phone, currentUser.name) {
        when {
            isAdmin -> rawRecords
            isMemberSession -> rawRecords.filter { rec ->
                (currentUser.uid.isNotBlank() && rec.memberUid == currentUser.uid) ||
                    (currentUser.phone.isNotBlank() && rec.memberPhone.isNotBlank() &&
                        com.example.core.AppConstants.normalizePhone(rec.memberPhone) ==
                        com.example.core.AppConstants.normalizePhone(currentUser.phone)) ||
                    (currentUser.name.isNotBlank() && rec.memberName.equals(currentUser.name.trim(), ignoreCase = true))
            }
            else -> emptyList()
        }
    }
    val paidCount = allRecords.count { it.paid }
    val unpaidCount = allRecords.count { !it.paid }
    val monthlyGrandTotal = allRecords.sumOf { it.monthlyTotal }

    val filtered = remember(allRecords, searchQuery, filterMode) {
        val q = searchQuery.trim().lowercase()
        allRecords.filter { rec ->
            val matchesFilter = when (filterMode) {
                1 -> rec.paid
                2 -> !rec.paid
                else -> true
            }
            val matchesQuery = q.isBlank() ||
                rec.memberName.lowercase().contains(q) ||
                rec.date.lowercase().contains(q) ||
                rec.areaOrDuty.lowercase().contains(q)
            matchesFilter && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("দৈনিক ও মাসিক ভাতা", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isAdmin) "Allowance Details • Admin Edit" else "Allowance Details • View Only",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("allowance_back_btn")) {
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
            if (!isAdmin) {
                item {
                    ViewOnlyBanner(
                        message = if (isMemberSession) {
                            "গোপনীয়তা সুরক্ষিত • আপনি শুধুমাত্র আপনার নিজের ভাতার তথ্য দেখতে পারবেন"
                        } else {
                            "গোপনীয়তা সুরক্ষিত • শুধুমাত্র অ্যাডমিন সকল সদস্যের ভাতা দেখতে ও এডিট করতে পারবেন"
                        }
                    )
                }
            }

            // Summary Card
            item {
                EmbossedCard(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth().testTag("allowance_summary_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isMemberSession) "আমার মোট ভাতা (My Allowance)" else "মোট বরাদ্দকৃত ভাতা (Allowance)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳${if (isMemberSession) monthlyGrandTotal else DemoDataProvider.DEMO_ALLOWANCE_BDT}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isMemberSession) "সদস্য: ${currentUser.name} • প্লাটুন: ${currentUser.platoon}" else "সদস্যদের মাসিক মোট হিসাব: ৳$monthlyGrandTotal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Paid / পরিশোধিত: $paidCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Unpaid / বকেয়া: $unpaidCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            if (!isAdmin && !isMemberSession) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Lock,
                        title = "সদস্যদের ভাতার তালিকা সুরক্ষিত (Private)",
                        subtitle = "অন্য সদস্যদের নাম, ফোন ও ভাতার হিসাব শুধুমাত্র অ্যাডমিন দেখতে পারবেন। আপনার নিজের ভাতা দেখতে Member Login করুন।"
                    )
                }
            } else {
                if (isAdmin) {
                    // Search Bar (Admin only)
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("নাম, তারিখ বা দায়িত্ব দিয়ে খুঁজুন...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("allowance_search_input")
                        )
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("সকল (${allRecords.size})", "Paid ($paidCount)", "Unpaid ($unpaidCount)").forEachIndexed { idx, label ->
                            FilterChip(
                                selected = filterMode == idx,
                                onClick = { filterMode = idx },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.ReceiptLong,
                            title = if (isMemberSession) "আপনার কোনো ভাতার হিসাব এখনো যুক্ত হয়নি" else "কোনো ভাতার তথ্য পাওয়া যায়নি",
                            subtitle = if (isMemberSession) "অ্যাডমিন আপনার নামে ভাতা যুক্ত করলে এখানে প্রদর্শিত হবে।" else "অনুসন্ধান বা ফিল্টার পরিবর্তন করে দেখুন।"
                        )
                    }
                } else {
                    items(filtered, key = { it.id }) { record ->
                        AllowanceRowCard(
                            record = record,
                            isAdmin = isAdmin,
                            onClick = { selectedRecord = record }
                        )
                    }
                }
            }
        }
    }

    selectedRecord?.let { rec ->
        val latest = allRecords.firstOrNull { it.id == rec.id } ?: rec
        if (isAdmin) {
            var editDate by remember(latest.id) { mutableStateOf(latest.date) }
            var editDaily by remember(latest.id) { mutableStateOf(latest.dailyAmount.toString()) }
            var editMonthly by remember(latest.id) { mutableStateOf(latest.monthlyTotal.toString()) }
            var editPaid by remember(latest.id) { mutableStateOf(latest.paid) }

            AlertDialog(
                onDismissRequest = { selectedRecord = null },
                title = { Text("ভাতা সম্পাদনা • ${latest.memberName}", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editDate,
                            onValueChange = { editDate = it },
                            label = { Text("তারিখ (Date)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editDaily,
                            onValueChange = { editDaily = it },
                            label = { Text("দৈনিক ভাতা (Daily Amount)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editMonthly,
                            onValueChange = { editMonthly = it },
                            label = { Text("মাসিক মোট (Monthly Total)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (editPaid) "অবস্থা: Paid (পরিশোধিত)" else "অবস্থা: Unpaid (বকেয়া)",
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(checked = editPaid, onCheckedChange = { editPaid = it })
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onEditAllowance(
                            latest.id,
                            latest.memberName,
                            latest.areaOrDuty,
                            editDate,
                            editDaily.toLongOrNull() ?: latest.dailyAmount,
                            editMonthly.toLongOrNull() ?: latest.monthlyTotal,
                            editPaid
                        )
                        selectedRecord = null
                    }) {
                        Text("সংরক্ষণ করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedRecord = null }) {
                        Text("বাতিল")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { selectedRecord = null },
                title = { Text(latest.memberName, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (latest.areaOrDuty.isNotBlank()) {
                            Text("দায়িত্ব: ${latest.areaOrDuty}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("তারিখ (Date): ${latest.date}", style = MaterialTheme.typography.bodyMedium)
                        Text("দৈনিক ভাতা (Daily Amount): ৳${latest.dailyAmount}", style = MaterialTheme.typography.bodyMedium)
                        Text("মাসিক মোট (Monthly Total): ৳${latest.monthlyTotal}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (latest.paid) "অবস্থা: পরিশোধিত (Paid)" else "অবস্থা: বকেয়া (Unpaid)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (latest.paid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedRecord = null }) {
                        Text("বন্ধ করুন")
                    }
                }
            )
        }
    }
}

@Composable
private fun AllowanceRowCard(
    record: AllowanceRecord,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    EmbossedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("allowance_row_${record.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (record.paid) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (record.paid) Icons.Rounded.Verified else Icons.Rounded.PendingActions,
                    contentDescription = if (record.paid) "Paid" else "Unpaid",
                    tint = if (record.paid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.memberName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (record.areaOrDuty.isNotBlank()) {
                    Text(
                        text = record.areaOrDuty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "তারিখ: ${record.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (record.paid) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (record.paid) "Paid • পরিশোধিত" else "Unpaid • বকেয়া",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (record.paid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (isAdmin) {
                    Text(
                        text = "ট্যাপ করে এডিট করুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "দৈনিক ভাতা (Daily Amount)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "৳${record.dailyAmount}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "মাসিক মোট (Monthly Total)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "৳${record.monthlyTotal}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
