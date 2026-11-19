package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.UiState
import com.example.model.MedicalCase
import com.example.model.ServiceCategory
import com.example.model.ServiceRequest
import com.example.model.UserProfile
import com.example.ui.components.*

@Composable
fun ServicesScreen(
    currentUser: UserProfile,
    servicesState: UiState<List<ServiceCategory>>,
    myRequestsState: UiState<List<ServiceRequest>>,
    myMedicalCasesState: UiState<List<MedicalCase>>,
    onOpenBloodDonation: () -> Unit,
    onSubmitServiceRequest: (requesterName: String, serviceId: String, serviceTitle: String, details: String, location: String, phone: String, urgency: String) -> Unit,
    onSubmitMedicalCase: (patientName: String, summary: String, hospital: String, cost: Double, phone: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var requestDialogService by remember { mutableStateOf<ServiceCategory?>(null) }
    var showMedicalDialog by remember { mutableStateOf(false) }

    val services = (servicesState as? UiState.Success)?.data.orEmpty().filter { it.active }
    val myRequests = (myRequestsState as? UiState.Success)?.data.orEmpty()
    val myMedical = (myMedicalCasesState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EmbossedCard(
                onClick = onOpenBloodDonation,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth().testTag("services_blood_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bloodtype,
                            contentDescription = "রক্তদান",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "রক্তদান ও রক্তের আবেদন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "রক্তদাতাদের তালিকা দেখুন বা জরুরি রক্তের আবেদন করুন",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == 0) {
                    AnimatedPrimaryActionButton(
                        text = "সেবাসমূহ",
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedOutlinedActionButton(
                        text = "আমার আবেদন",
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    AnimatedOutlinedActionButton(
                        text = "সেবাসমূহ",
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedPrimaryActionButton(
                        text = "আমার আবেদন",
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (selectedTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedOutlinedActionButton(
                        text = "সাধারণ সেবার আবেদন",
                        icon = Icons.Rounded.AddTask,
                        onClick = {
                            requestDialogService = ServiceCategory(
                                id = "",
                                title = services.firstOrNull()?.title ?: "মানবিক সহায়তা"
                            )
                        },
                        modifier = Modifier.weight(1f).testTag("new_service_request_btn")
                    )
                    AnimatedOutlinedActionButton(
                        text = "চিকিৎসা সহায়তা",
                        icon = Icons.Rounded.MedicalServices,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = { showMedicalDialog = true },
                        modifier = Modifier.weight(1f).testTag("new_medical_case_btn")
                    )
                }
            }

            when (servicesState) {
                is UiState.Loading -> item { CircularProgressIndicator() }
                is UiState.Error -> item { Text(servicesState.message, color = MaterialTheme.colorScheme.error) }
                is UiState.Success -> {
                    if (services.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.Handshake,
                                title = "কোনো সেবার ধরন পাওয়া যায়নি",
                                subtitle = "অ্যাডমিন প্যানেল থেকে সেবার বিভাগসমূহ যুক্ত করা হলে এখানে তালিকা দেখা যাবে।"
                            )
                        }
                    } else {
                        items(services, key = { it.id }) { service ->
                            EmbossedCard(
                                onClick = { requestDialogService = service },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = serviceIconForKey(service.iconKey),
                                            contentDescription = service.title,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = service.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = service.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowForwardIos,
                                        contentDescription = "আবেদন করুন",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (myRequests.isEmpty() && myMedical.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Assignment,
                        title = "আপনার কোনো আবেদন নেই",
                        subtitle = "যেকোনো মানবিক বা চিকিৎসা সহায়তার জন্য আবেদন করলে তার অগ্রগতি এখানে দেখাবে।"
                    )
                }
            } else {
                items(myRequests, key = { "req_${it.id}" }) { req ->
                    EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(req.serviceTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            AssistChip(onClick = {}, label = { Text(statusBn(req.status)) })
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(req.details, style = MaterialTheme.typography.bodyMedium)
                        if (req.adminNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "অ্যাডমিন মন্তব্য: ${req.adminNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                items(myMedical, key = { "med_${it.id}" }) { med ->
                    EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("চিকিৎসা সহায়তা: ${med.patientName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            AssistChip(onClick = {}, label = { Text(statusBn(med.status)) })
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(med.conditionSummary, style = MaterialTheme.typography.bodyMedium)
                        if (med.hospitalName.isNotBlank()) {
                            Text(
                                text = "হাসপাতাল: ${med.hospitalName} • আনুমানিক খরচ: ৳${med.estimatedCost.toLong()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (requestDialogService != null) {
        val srv = requestDialogService!!
        var title by remember(srv) { mutableStateOf(srv.title) }
        var details by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf(currentUser.phone) }
        var urgency by remember { mutableStateOf("normal") }

        AlertDialog(
            onDismissRequest = { requestDialogService = null },
            title = { Text("সেবার আবেদন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("সেবার নাম") })
                    OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("বিস্তারিত বিবরণ *") }, minLines = 2)
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("এলাকা / ঠিকানা") })
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("যোগাযোগের নম্বর") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("normal" to "সাধারণ", "high" to "জরুরি", "emergency" to "অতি জরুরি").forEach { (k, label) ->
                            FilterChip(
                                selected = urgency == k,
                                onClick = { urgency = k },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank() && details.isNotBlank()) {
                            onSubmitServiceRequest(currentUser.name, srv.id, title, details, location, phone, urgency)
                            requestDialogService = null
                        }
                    }
                ) { Text("জমা দিন") }
            },
            dismissButton = {
                TextButton(onClick = { requestDialogService = null }) { Text("বাতিল") }
            }
        )
    }

    if (showMedicalDialog) {
        var patientName by remember { mutableStateOf("") }
        var summary by remember { mutableStateOf("") }
        var hospital by remember { mutableStateOf("") }
        var costText by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf(currentUser.phone) }

        AlertDialog(
            onDismissRequest = { showMedicalDialog = false },
            title = { Text("চিকিৎসা সহায়তার আবেদন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("রোগীর নাম *") })
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("রোগের বিবরণ *") }, minLines = 2)
                    OutlinedTextField(value = hospital, onValueChange = { hospital = it }, label = { Text("হাসপাতালের নাম") })
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("আনুমানিক খরচ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("যোগাযোগের নম্বর") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (patientName.isNotBlank() && summary.isNotBlank()) {
                            onSubmitMedicalCase(
                                patientName,
                                summary,
                                hospital,
                                costText.toDoubleOrNull() ?: 0.0,
                                phone
                            )
                            showMedicalDialog = false
                        }
                    }
                ) { Text("জমা দিন") }
            },
            dismissButton = {
                TextButton(onClick = { showMedicalDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

fun statusBn(status: String): String = when (status) {
    "pending" -> "অপেক্ষমাণ"
    "approved" -> "অনুমোদিত"
    "verified" -> "যাচাইকৃত"
    "in_progress" -> "চলমান"
    "assisted" -> "সহায়তা প্রদান"
    "completed" -> "সম্পন্ন"
    "closed" -> "সমাপ্ত"
    "cancelled" -> "বাতিল"
    "open" -> "সক্রিয়"
    "matched" -> "ডোনার পাওয়া গেছে"
    "fulfilled" -> "সম্পন্ন"
    else -> status
}
