package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.UiState
import com.example.model.*
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import com.example.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    isAdmin: Boolean,
    financialSummary: FinancialSummary,
    myContributionsState: UiState<List<ContributionRecord>>,
    adminContributionsState: UiState<List<ContributionRecord>>,
    adminDonationsState: UiState<List<DonationRecord>>,
    adminExpensesState: UiState<List<ExpenseRecord>>,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val myContribs = (myContributionsState as? UiState.Success)?.data.orEmpty()
    val allContribs = (adminContributionsState as? UiState.Success)?.data.orEmpty()
    val allDonations = (adminDonationsState as? UiState.Success)?.data.orEmpty()
    val allExpenses = (adminExpensesState as? UiState.Success)?.data.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAdmin) "আর্থিক সারসংক্ষেপ ও হিসাব" else "আমার চাঁদা ও অবদান") },
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
            if (isAdmin) {
                item {
                    EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                        Text("আর্থিক সারসংক্ষেপ (Firestore রিয়েল-টাইম)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        FinanceRow("মোট সদস্য অবদান", financialSummary.totalContributions)
                        FinanceRow("মোট অনুদান", financialSummary.totalDonations)
                        FinanceRow("মোট ব্যয় (-)", financialSummary.totalExpenses)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("বর্তমান তহবিল", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "৳${financialSummary.currentBalance.toLong()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("চাঁদা", "অনুদান", "ব্যয়").forEachIndexed { idx, label ->
                            if (selectedTab == idx) {
                                AnimatedPrimaryActionButton(text = label, onClick = { selectedTab = idx }, modifier = Modifier.weight(1f))
                            } else {
                                AnimatedOutlinedActionButton(text = label, onClick = { selectedTab = idx }, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0 -> {
                        if (allContribs.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.Payments,
                                    title = "কোনো চাঁদার রেকর্ড নেই",
                                    subtitle = "অ্যাডমিন প্যানেল থেকে সদস্যদের চাঁদা যুক্ত করুন।"
                                )
                            }
                        } else {
                            items(allContribs, key = { it.id }) { c ->
                                ContributionCard(c)
                            }
                        }
                    }
                    1 -> {
                        if (allDonations.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.AccountBalanceWallet,
                                    title = "কোনো অনুদান রেকর্ড নেই",
                                    subtitle = "অ্যাডমিন প্যানেল থেকে প্রাপ্ত অনুদান যুক্ত করুন।"
                                )
                            }
                        } else {
                            items(allDonations, key = { it.id }) { d ->
                                EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(d.donorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("পরিমাণ: ৳${d.amount.toLong()} • মাধ্যম: ${d.paymentMethod}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                                    Text("উদ্দেশ্য: ${d.purpose} • তারিখ: ${d.donationDate}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    2 -> {
                        if (allExpenses.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.AccountBalanceWallet,
                                    title = "কোনো ব্যয়ের রেকর্ড নেই",
                                    subtitle = "অ্যাডমিন প্যানেল থেকে সংগঠনের ব্যয় যুক্ত করুন।"
                                )
                            }
                        } else {
                            items(allExpenses, key = { it.id }) { e ->
                                EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(e.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("পরিমাণ: ৳${e.amount.toLong()} • খাত: ${e.category}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                    Text("তারিখ: ${e.expenseDate}", style = MaterialTheme.typography.bodySmall)
                                    if (e.notes.isNotBlank()) Text(e.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                if (myContribs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Payments,
                            title = "আপনার কোনো চাঁদার রেকর্ড পাওয়া যায়নি",
                            subtitle = "সংগঠনে আপনার প্রদত্ত চাঁদা যুক্ত হলে এখানে বিস্তারিত হিসাব দেখাবে।"
                        )
                    }
                } else {
                    items(myContribs, key = { it.id }) { c ->
                        ContributionCard(c)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributionCard(c: ContributionRecord) {
    EmbossedCard(modifier = Modifier.fillMaxWidth()) {
        Text("${c.memberName} (${c.month})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "নির্ধারিত: ৳${c.expectedAmount.toLong()} • পরিশোধিত: ৳${c.paidAmount.toLong()} • বকেয়া: ৳${c.dueAmount.toLong()}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "তারিখ: ${c.paymentDate} • মাধ্যম: ${c.paymentMethod}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FinanceRow(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("৳${value.toLong()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}
