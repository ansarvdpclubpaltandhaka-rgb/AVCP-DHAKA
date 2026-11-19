package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppConstants
import com.example.ui.components.AnimatedOutlinedActionButton
import com.example.ui.components.AnimatedPrimaryActionButton
import com.example.ui.components.EmbossedCard
import kotlinx.coroutines.flow.SharedFlow

private val platoonSuggestions = listOf(
    "পল্টন প্লাটুন-১",
    "পল্টন প্লাটুন-২",
    "সদর প্লাটুন",
    "জরুরি সেবা প্লাটুন"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberAuthScreen(
    isBusy: Boolean,
    authMessage: String?,
    isSuccessMessage: Boolean,
    initialRegisterMode: Boolean = false,
    memberRegisteredEvent: SharedFlow<String>,
    onClearMessage: () -> Unit,
    onMemberSignIn: (phone: String, password: String) -> Unit,
    onMemberRegister: (name: String, phone: String, platoon: String, password: String) -> Unit,
    onBackToDashboard: () -> Unit
) {
    var isRegisterMode by remember(initialRegisterMode) { mutableStateOf(initialRegisterMode) }

    // Login state
    var loginPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register state
    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPlatoon by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(memberRegisteredEvent) {
        memberRegisteredEvent.collect { registeredPhone ->
            loginPhone = registeredPhone
            loginPassword = ""
            regName = ""
            regPhone = ""
            regPlatoon = ""
            regPassword = ""
            isRegisterMode = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সদস্য লগইন ও নিবন্ধন") },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "হোমে ফিরে যান"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 540.dp)
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.org_logo),
                    contentDescription = AppConstants.ORG_NAME_BN,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = AppConstants.CLUB_NAME_BN,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "সদস্য পোর্টাল • লগইন ও নতুন সদস্য নিবন্ধন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Mode Tabs: Member Login / Member Register
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isRegisterMode) {
                        AnimatedPrimaryActionButton(
                            text = "সদস্য লগইন",
                            icon = Icons.AutoMirrored.Rounded.Login,
                            onClick = { isRegisterMode = false; onClearMessage() },
                            modifier = Modifier.weight(1f).testTag("member_tab_login")
                        )
                        AnimatedOutlinedActionButton(
                            text = "নিবন্ধন করুন",
                            icon = Icons.Rounded.PersonAdd,
                            onClick = { isRegisterMode = true; onClearMessage() },
                            modifier = Modifier.weight(1f).testTag("member_tab_register")
                        )
                    } else {
                        AnimatedOutlinedActionButton(
                            text = "সদস্য লগইন",
                            icon = Icons.AutoMirrored.Rounded.Login,
                            onClick = { isRegisterMode = false; onClearMessage() },
                            modifier = Modifier.weight(1f).testTag("member_tab_login")
                        )
                        AnimatedPrimaryActionButton(
                            text = "নিবন্ধন করুন",
                            icon = Icons.Rounded.PersonAdd,
                            onClick = { isRegisterMode = true; onClearMessage() },
                            modifier = Modifier.weight(1f).testTag("member_tab_register")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!authMessage.isNullOrBlank()) {
                    val container = if (isSuccessMessage) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                    val onContainer = if (isSuccessMessage) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                    EmbossedCard(
                        containerColor = container,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuccessMessage) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
                                contentDescription = null,
                                tint = onContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = onContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onClearMessage) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "বন্ধ করুন",
                                    tint = onContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                EmbossedCard(modifier = Modifier.fillMaxWidth()) {
                    if (!isRegisterMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সদস্য লগইন (Member Login)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "অ্যাডমিন আপনার আবেদন অনুমোদন (Approve) করার পর লগইন করলে আপনার নিজের ড্যাশবোর্ড, উপস্থিতি ও নোটিশ দেখতে পারবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = loginPhone,
                            onValueChange = {
                                loginPhone = it
                                if (!authMessage.isNullOrBlank()) onClearMessage()
                            },
                            label = { Text("মোবাইল নম্বর (Phone) *") },
                            placeholder = { Text("০১XXXXXXXXX") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_login_phone_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                if (!authMessage.isNullOrBlank()) onClearMessage()
                            },
                            label = { Text("পাসওয়ার্ড (Password) *") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "পাসওয়ার্ড দেখুন"
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_login_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        AnimatedPrimaryActionButton(
                            text = "সদস্য প্রবেশ করুন (Login)",
                            icon = Icons.AutoMirrored.Rounded.Login,
                            isLoading = isBusy,
                            onClick = { onMemberSignIn(loginPhone, loginPassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_login_submit_btn")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "নতুন সদস্য নিবন্ধন (Register)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "আপনার নাম, মোবাইল নম্বর, প্লাটুন এবং পাসওয়ার্ড দিয়ে নিবন্ধন করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = regName,
                            onValueChange = {
                                regName = it
                                if (!authMessage.isNullOrBlank()) onClearMessage()
                            },
                            label = { Text("পূর্ণ নাম (Name) *") },
                            placeholder = { Text("আপনার নাম লিখুন") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_reg_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = {
                                regPhone = it
                                if (!authMessage.isNullOrBlank()) onClearMessage()
                            },
                            label = { Text("মোবাইল নম্বর (Phone) *") },
                            placeholder = { Text("০১XXXXXXXXX") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_reg_phone_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPlatoon,
                            onValueChange = {
                                regPlatoon = it
                                if (!authMessage.isNullOrBlank()) onClearMessage()
                            },
                            label = { Text("প্লাটুন (Platoon) *") },
                            placeholder = { Text("যেমন: পল্টন প্লাটুন-১") },
                            leadingIcon = { Icon(Icons.Rounded.Shield, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_reg_platoon_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            platoonSuggestions.forEach { preset ->
                                FilterChip(
                                    selected = regPlatoon == preset,
                                    onClick = { regPlatoon = preset },
                                    label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("ট্রেড (Trade)") },
                            placeholder = { Text("অ্যাডমিন অনুমোদনের পর সেট করবেন") },
                            supportingText = {
                                Text("নিবন্ধনের পর আবেদন Pending থাকবে এবং লগইন করা যাবে না। অ্যাডমিন Approve ও ট্রেড সেট করলে লগইন করতে পারবেন।")
                            },
                            leadingIcon = { Icon(Icons.Rounded.WorkOutline, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_reg_trade_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = {
                                regPassword = it
                                if (!authMessage.isNullOrBlank()) onClearMessage()
                            },
                            label = { Text("পাসওয়ার্ড (Password) *") },
                            placeholder = { Text("কমপক্ষে ৪ অক্ষর") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "পাসওয়ার্ড দেখুন"
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_reg_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        AnimatedPrimaryActionButton(
                            text = "নিবন্ধন সম্পন্ন করুন (Register)",
                            icon = Icons.Rounded.HowToReg,
                            isLoading = isBusy,
                            onClick = { onMemberRegister(regName, regPhone, regPlatoon, regPassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("member_reg_submit_btn")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedOutlinedActionButton(
                    text = "হোম ড্যাশবোর্ডে ফিরে যান",
                    icon = Icons.Rounded.Dashboard,
                    onClick = onBackToDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_auth_back_btn")
                )
            }
        }
    }
}
