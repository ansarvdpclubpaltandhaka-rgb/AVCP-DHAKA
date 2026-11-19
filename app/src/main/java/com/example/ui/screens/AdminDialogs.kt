package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppConstants
import com.example.model.*
import com.example.ui.components.ImagePickerUploadField

@Composable
fun ServiceCategoryDialog(
    initial: ServiceCategory?,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, desc: String, iconKey: String, active: Boolean, sortOrder: Long) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: "humanitarian") }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var sortText by remember { mutableStateOf((initial?.sortOrder ?: 1L).toString()) }
    var showMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন সেবা যুক্ত করুন" else "সেবা সম্পাদনা") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("সেবার নাম *") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("বিবরণ") }, minLines = 2)
                Box {
                    val label = AppConstants.SERVICE_ICON_KEYS.firstOrNull { it.first == iconKey }?.second ?: iconKey
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("আইকন ধরন") },
                        trailingIcon = {
                            IconButton(onClick = { showMenu = true }) { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) }
                        }
                    )
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        AppConstants.SERVICE_ICON_KEYS.forEach { (k, lbl) ->
                            DropdownMenuItem(text = { Text(lbl) }, onClick = { iconKey = k; showMenu = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = sortText,
                    onValueChange = { sortText = it },
                    label = { Text("ক্রম নম্বর") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("সক্রিয় রাখুন")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(initial?.id, title, desc, iconKey, active, sortText.toLongOrNull() ?: 1L)
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun MemberFormDialog(
    initial: MemberRecord?,
    orgConfig: OrganizationConfig?,
    onDismiss: () -> Unit,
    onSave: (
        id: String?, linkedUid: String, name: String, photoUrl: String, phone: String, email: String,
        bloodGroup: String, position: String, joiningDate: String, address: String, status: String,
        bloodDonor: Boolean, donorAvailable: Boolean, donationCount: Long, lastDonation: String,
        platoon: String, trade: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var bloodGroup by remember { mutableStateOf(initial?.bloodGroup?.ifBlank { "O+" } ?: "O+") }
    var position by remember { mutableStateOf(initial?.position ?: "সদস্য") }
    var platoon by remember { mutableStateOf(initial?.platoon ?: "পল্টন প্লাটুন-১") }
    var trade by remember { mutableStateOf(initial?.trade?.ifBlank { AppConstants.MEMBER_TRADES.first() } ?: AppConstants.MEMBER_TRADES.first()) }
    var joiningDate by remember { mutableStateOf(initial?.joiningDate ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl ?: "") }
    var linkedUid by remember { mutableStateOf(initial?.linkedUid ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: AppConstants.STATUS_APPROVED) }
    var bloodDonor by remember { mutableStateOf(initial?.bloodDonor ?: false) }
    var donorAvailable by remember { mutableStateOf(initial?.donorAvailable ?: false) }
    var countText by remember { mutableStateOf((initial?.donationCount ?: 0L).toString()) }
    var lastDonation by remember { mutableStateOf(initial?.lastDonation ?: "") }
    var showBgMenu by remember { mutableStateOf(false) }
    var showTradeMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন সদস্য যোগ" else "সদস্যের তথ্য সম্পাদনা") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পূর্ণ নাম *") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = platoon, onValueChange = { platoon = it }, label = { Text("প্লাটুন (Platoon) *") })
                Box {
                    OutlinedTextField(
                        value = trade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ট্রেড (Trade) *") },
                        trailingIcon = { IconButton(onClick = { showTradeMenu = true }) { Icon(Icons.Rounded.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = showTradeMenu, onDismissRequest = { showTradeMenu = false }) {
                        AppConstants.MEMBER_TRADES.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { trade = t; showTradeMenu = false })
                        }
                    }
                }
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ইমেইল") })
                OutlinedTextField(value = position, onValueChange = { position = it }, label = { Text("পদবি *") })
                Box {
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("রক্তের গ্রুপ *") },
                        trailingIcon = { IconButton(onClick = { showBgMenu = true }) { Icon(Icons.Rounded.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = showBgMenu, onDismissRequest = { showBgMenu = false }) {
                        AppConstants.BLOOD_GROUPS.forEach { bg ->
                            DropdownMenuItem(text = { Text(bg) }, onClick = { bloodGroup = bg; showBgMenu = false })
                        }
                    }
                }
                OutlinedTextField(value = joiningDate, onValueChange = { joiningDate = it }, label = { Text("যোগদানের তারিখ") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") })
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("অনুমোদিত / সক্রিয় সদস্য")
                    Switch(
                        checked = AppConstants.isApprovedMemberStatus(status),
                        onCheckedChange = { status = if (it) AppConstants.STATUS_APPROVED else AppConstants.STATUS_PENDING }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("রক্তদাতা")
                    Switch(checked = bloodDonor, onCheckedChange = { bloodDonor = it })
                }
                if (bloodDonor) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("রক্তদানে প্রস্তুত")
                        Switch(checked = donorAvailable, onCheckedChange = { donorAvailable = it })
                    }
                    OutlinedTextField(value = countText, onValueChange = { countText = it }, label = { Text("রক্তদানের সংখ্যা") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = lastDonation, onValueChange = { lastDonation = it }, label = { Text("সর্বশেষ রক্তদানের তারিখ") })
                }
                ImagePickerUploadField(imageUrl = photoUrl, onImageUrlChange = { photoUrl = it }, orgConfig = orgConfig, label = "সদস্যের ছবি")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && phone.isNotBlank() && position.isNotBlank()) {
                    onSave(
                        initial?.id, linkedUid, name, photoUrl, phone, email, bloodGroup, position,
                        joiningDate, address, status, bloodDonor, donorAvailable, countText.toLongOrNull() ?: 0L, lastDonation,
                        platoon, trade
                    )
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun VolunteerFormDialog(
    initial: VolunteerRecord?,
    orgConfig: OrganizationConfig?,
    onDismiss: () -> Unit,
    onSave: (id: String?, linkedUid: String, name: String, photoUrl: String, phone: String, skills: String, availability: String, bloodGroup: String, joiningDate: String, status: String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var skills by remember { mutableStateOf(initial?.skills ?: "") }
    var availability by remember { mutableStateOf(initial?.availability ?: "সপ্তাহান্তে / জরুরি") }
    var bloodGroup by remember { mutableStateOf(initial?.bloodGroup?.ifBlank { "O+" } ?: "O+") }
    var joiningDate by remember { mutableStateOf(initial?.joiningDate ?: "") }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl ?: "") }
    var linkedUid by remember { mutableStateOf(initial?.linkedUid ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: "active") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন স্বেচ্ছাসেবক যোগ" else "স্বেচ্ছাসেবক সম্পাদনা") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম *") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("ফোন নম্বর *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("দক্ষতা / অভিজ্ঞতা *") })
                OutlinedTextField(value = availability, onValueChange = { availability = it }, label = { Text("সময় / প্রাপ্যতা *") })
                OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("রক্তের গ্রুপ") })
                OutlinedTextField(value = joiningDate, onValueChange = { joiningDate = it }, label = { Text("যোগদানের তারিখ") })
                OutlinedTextField(value = linkedUid, onValueChange = { linkedUid = it }, label = { Text("Firebase UID (ঐচ্ছিক)") })
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("সক্রিয় অবস্থা")
                    Switch(checked = status == "active", onCheckedChange = { status = if (it) "active" else "inactive" })
                }
                ImagePickerUploadField(imageUrl = photoUrl, onImageUrlChange = { photoUrl = it }, orgConfig = orgConfig, label = "ছবি")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && phone.isNotBlank() && skills.isNotBlank()) {
                    onSave(initial?.id, linkedUid, name, photoUrl, phone, skills, availability, bloodGroup, joiningDate, status)
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun ActivityFormDialog(
    initial: ActivityRecord?,
    orgConfig: OrganizationConfig?,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, desc: String, category: String, imageUrl: String, date: String, location: String, participants: Long, beneficiaries: Long, budget: Double, status: String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "ত্রাণ বিতরণ") }
    var imageUrl by remember { mutableStateOf(initial?.imageUrl ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var participantsText by remember { mutableStateOf((initial?.participants ?: 0L).toString()) }
    var beneficiariesText by remember { mutableStateOf((initial?.beneficiaries ?: 0L).toString()) }
    var budgetText by remember { mutableStateOf((initial?.budget ?: 0.0).toLong().toString()) }
    var status by remember { mutableStateOf(initial?.status ?: "ongoing") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন কার্যক্রম যোগ" else "কার্যক্রম সম্পাদনা") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("বিবরণ *") }, minLines = 2)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("বিভাগ *") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ *") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("স্থান *") })
                OutlinedTextField(value = participantsText, onValueChange = { participantsText = it }, label = { Text("অংশগ্রহণকারী সংখ্যা") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = beneficiariesText, onValueChange = { beneficiariesText = it }, label = { Text("উপকারভোগী সংখ্যা") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = budgetText, onValueChange = { budgetText = it }, label = { Text("বাজেট (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = status == "ongoing", onClick = { status = "ongoing" }, label = { Text("চলমান") })
                    FilterChip(selected = status == "completed", onClick = { status = "completed" }, label = { Text("সম্পন্ন") })
                }
                ImagePickerUploadField(imageUrl = imageUrl, onImageUrlChange = { imageUrl = it }, orgConfig = orgConfig, label = "কার্যক্রমের ছবি")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank() && desc.isNotBlank() && date.isNotBlank() && location.isNotBlank()) {
                    onSave(
                        initial?.id, title, desc, category, imageUrl, date, location,
                        participantsText.toLongOrNull() ?: 0L,
                        beneficiariesText.toLongOrNull() ?: 0L,
                        budgetText.toDoubleOrNull() ?: 0.0,
                        status
                    )
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun ContributionFormDialog(
    onDismiss: () -> Unit,
    onSave: (memberUid: String, memberName: String, expected: Double, paid: Double, month: String, date: String, method: String) -> Unit
) {
    var memberUid by remember { mutableStateOf("") }
    var memberName by remember { mutableStateOf("") }
    var expectedText by remember { mutableStateOf("") }
    var paidText by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("নগদ") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("সদস্যের চাঁদা/অবদান যোগ") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = memberName, onValueChange = { memberName = it }, label = { Text("সদস্যের নাম *") })
                OutlinedTextField(value = memberUid, onValueChange = { memberUid = it }, label = { Text("সদস্যের UID (ঐচ্ছিক)") })
                OutlinedTextField(value = expectedText, onValueChange = { expectedText = it }, label = { Text("নির্ধারিত পরিমাণ (৳) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = paidText, onValueChange = { paidText = it }, label = { Text("পরিশোধিত পরিমাণ (৳) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("মাস * (যেমন: নভেম্বর ২০২৬)") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("পরিশোধের তারিখ *") })
                OutlinedTextField(value = method, onValueChange = { method = it }, label = { Text("পেমেন্ট মাধ্যম") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val exp = expectedText.toDoubleOrNull() ?: 0.0
                val paid = paidText.toDoubleOrNull() ?: 0.0
                if (memberName.isNotBlank() && month.isNotBlank() && date.isNotBlank()) {
                    onSave(memberUid, memberName, exp, paid, month, date, method)
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun DonationFormDialog(
    onDismiss: () -> Unit,
    onSave: (donorName: String, amount: Double, purpose: String, date: String, method: String) -> Unit
) {
    var donorName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("নগদ") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("অনুদান যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = donorName, onValueChange = { donorName = it }, label = { Text("দাতার নাম *") })
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("পরিমাণ (৳) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("উদ্দেশ্য *") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ *") })
                OutlinedTextField(value = method, onValueChange = { method = it }, label = { Text("মাধ্যম") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (donorName.isNotBlank() && amt > 0 && purpose.isNotBlank() && date.isNotBlank()) {
                    onSave(donorName, amt, purpose, date, method)
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun ExpenseFormDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, amount: Double, date: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("মানবিক সহায়তা") }
    var amountText by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ব্যয় যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("ব্যয়ের শিরোনাম *") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("খাত *") })
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("পরিমাণ (৳) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ *") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("নোট") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && amt > 0 && date.isNotBlank()) {
                    onSave(title, category, amt, date, notes)
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun NoticeFormDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, body: String, type: String, pinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var noticeType by remember { mutableStateOf("notice") }
    var pinned by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন নোটিশ তৈরি") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") })
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("বিস্তারিত *") }, minLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = noticeType == "notice", onClick = { noticeType = "notice" }, label = { Text("নোটিশ") })
                    FilterChip(selected = noticeType == "announcement", onClick = { noticeType = "announcement" }, label = { Text("ঘোষণা") })
                    FilterChip(selected = noticeType == "activity_update", onClick = { noticeType = "activity_update" }, label = { Text("আপডেট") })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("শীর্ষে পিন করুন")
                    Switch(checked = pinned, onCheckedChange = { pinned = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank() && body.isNotBlank()) {
                    onSave(title, body, noticeType, pinned)
                    onDismiss()
                }
            }) { Text("প্রকাশ করুন") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun EventFormDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, type: String, date: String, time: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("event") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন সভা / ইভেন্ট তৈরি") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("বিবরণ *") }, minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = eventType == "event", onClick = { eventType = "event" }, label = { Text("ইভেন্ট") })
                    FilterChip(selected = eventType == "meeting", onClick = { eventType = "meeting" }, label = { Text("সভা") })
                }
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ *") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("সময়") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("স্থান *") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank() && desc.isNotBlank() && date.isNotBlank() && location.isNotBlank()) {
                    onSave(title, desc, eventType, date, time, location)
                    onDismiss()
                }
            }) { Text("প্রকাশ করুন") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun PositionFormDialog(
    initial: PositionRecord?,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, rankOrder: Long, holderName: String, holderPhotoUrl: String, desc: String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var rankText by remember { mutableStateOf((initial?.rankOrder ?: 1L).toString()) }
    var holderName by remember { mutableStateOf(initial?.holderName ?: "") }
    var holderPhoto by remember { mutableStateOf(initial?.holderPhotoUrl ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "সাংগঠনিক পদবি যোগ" else "পদবি সম্পাদনা") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("পদবির নাম *") })
                OutlinedTextField(value = rankText, onValueChange = { rankText = it }, label = { Text("ক্রম") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = holderName, onValueChange = { holderName = it }, label = { Text("দায়িত্বশীল ব্যক্তির নাম") })
                OutlinedTextField(value = holderPhoto, onValueChange = { holderPhoto = it }, label = { Text("ছবির HTTPS URL") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("দায়িত্বের বিবরণ") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(initial?.id, title, rankText.toLongOrNull() ?: 1L, holderName, holderPhoto, desc)
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun RolePermissionDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSave: (role: String, permissions: List<String>, status: String, positionTitle: String) -> Unit
) {
    var role by remember { mutableStateOf(user.role) }
    val selectedPerms = remember { mutableStateListOf<String>().apply { addAll(user.permissions) } }
    var status by remember { mutableStateOf(user.status) }
    var positionTitle by remember { mutableStateOf(user.positionTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ভূমিকা ও অনুমতি: ${user.name}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = role == "member", onClick = { role = "member"; selectedPerms.clear() }, label = { Text("সদস্য") })
                    FilterChip(selected = role == "admin", onClick = { role = "admin" }, label = { Text("অ্যাডমিন") })
                }
                OutlinedTextField(value = positionTitle, onValueChange = { positionTitle = it }, label = { Text("পদবি") })
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("অ্যাকাউন্ট সক্রিয়")
                    Switch(checked = status == "active", onCheckedChange = { status = if (it) "active" else "inactive" })
                }
                if (role == "admin") {
                    Text("অনুমতিসমূহ:", style = MaterialTheme.typography.titleSmall)
                    AppConstants.ALL_PERMISSIONS.forEach { (permKey, permLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = permKey in selectedPerms,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (permKey !in selectedPerms) selectedPerms.add(permKey)
                                    } else {
                                        selectedPerms.remove(permKey)
                                    }
                                }
                            )
                            Text(permLabel, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(role, if (role == "admin") selectedPerms.toList() else emptyList(), status, positionTitle)
                onDismiss()
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
