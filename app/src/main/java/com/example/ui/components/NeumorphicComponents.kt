package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.AppConstants
import com.example.core.CompressedImagePayload
import com.example.core.ImageUploadService
import com.example.model.OrganizationConfig
import com.example.ui.theme.DarkHighlight
import com.example.ui.theme.LightHighlight
import kotlinx.coroutines.launch

@Composable
fun EmbossedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val highlight = if (isDark) DarkHighlight else LightHighlight
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "card_press"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (onClick != null && isPressed) 2.dp else 6.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.outlineVariant,
                spotColor = MaterialTheme.colorScheme.outlineVariant
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        highlight,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                    )
                ),
                shape = shape
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun AnimatedOutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "btn_press"
    )
    val shape = RoundedCornerShape(16.dp)

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        border = BorderStroke(1.2.dp, if (enabled) accentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = accentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
            .scale(scale)
            .heightIn(min = 48.dp)
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.outlineVariant,
                spotColor = MaterialTheme.colorScheme.outlineVariant
            )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun AnimatedPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "primary_btn_press"
    )
    val shape = RoundedCornerShape(16.dp)

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
            .scale(scale)
            .heightIn(min = 48.dp)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = shape,
                ambientColor = containerColor.copy(alpha = 0.35f),
                spotColor = containerColor.copy(alpha = 0.35f)
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun OrgHeaderBar(
    orgName: String = AppConstants.ORG_NAME_BN,
    isAuthenticated: Boolean,
    unreadNotifications: Int,
    userPhotoUrl: String,
    isAdmin: Boolean,
    onLoginClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.org_logo),
                contentDescription = AppConstants.ORG_NAME_BN,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = orgName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        isAdmin -> "${AppConstants.CLUB_NAME_BN} • Admin"
                        isAuthenticated -> "${AppConstants.CLUB_NAME_BN} • Member"
                        else -> "${AppConstants.CLUB_NAME_BN} • View Only"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isAuthenticated && isAdmin) {
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.testTag("header_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AdminPanelSettings,
                        contentDescription = "অ্যাডমিন প্যানেল",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onMessagesClick,
                    modifier = Modifier.testTag("header_messages_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = "বার্তা",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("header_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge { Text(if (unreadNotifications > 9) "9+" else "$unreadNotifications") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "বিজ্ঞপ্তি",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (!isAuthenticated) {
                OutlinedButton(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .testTag("header_login_button")
                ) {
                    Text(
                        text = "অ্যাডমিন",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("header_profile_button")
                ) {
                    if (userPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "প্রোফাইল",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "প্রোফাইল",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewOnlyBanner(
    message: String = "View Only Mode • শুধুমাত্র দেখার মোড (এডিট করতে Admin Login করুন)",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth().testTag("view_only_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Visibility,
                contentDescription = "View Only Mode",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    EmbossedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ImagePickerUploadField(
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    orgConfig: OrganizationConfig?,
    label: String = "ছবি নির্বাচন ও আপলোড",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var compressedPayload by remember { mutableStateOf<CompressedImagePayload?>(null) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            scope.launch {
                statusText = "ছবি কম্প্রেস করা হচ্ছে..."
                val res = ImageUploadService.compressImage(context, uri)
                res.onSuccess { payload ->
                    compressedPayload = payload
                    statusText = "কম্প্রেস সম্পন্ন: ${payload.originalKb} KB → ${payload.compressedKb} KB (${payload.width}x${payload.height})"
                }.onFailure { err ->
                    statusText = err.localizedMessage ?: "কম্প্রেস ব্যর্থ"
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val previewModel: Any? = selectedUri ?: imageUrl.takeIf { it.isNotBlank() }
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "ছবির প্রিভিউ",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = "কোনো ছবি নেই",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AnimatedOutlinedActionButton(
                    text = "গ্যালারি থেকে ছবি বাছাই",
                    icon = Icons.Rounded.PhotoLibrary,
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (compressedPayload != null) {
                    AnimatedPrimaryActionButton(
                        text = if (isUploading) "আপলোড হচ্ছে..." else "এক্সটার্নাল সার্ভারে আপলোড",
                        icon = Icons.Rounded.CloudUpload,
                        isLoading = isUploading,
                        onClick = {
                            val payload = compressedPayload ?: return@AnimatedPrimaryActionButton
                            scope.launch {
                                isUploading = true
                                val uploadRes = ImageUploadService.uploadImage(payload, orgConfig)
                                isUploading = false
                                uploadRes.onSuccess { httpsUrl ->
                                    onImageUrlChange(httpsUrl)
                                    statusText = "সফলভাবে আপলোড হয়েছে"
                                }.onFailure { err ->
                                    statusText = err.localizedMessage ?: "আপলোড ব্যর্থ"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (!statusText.isNullOrBlank()) {
            Text(
                text = statusText!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedTextField(
            value = imageUrl,
            onValueChange = onImageUrlChange,
            label = { Text("ছবির HTTPS URL (স্বয়ংক্রিয় বা সরাসরি)") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun serviceIconForKey(key: String): ImageVector = when (key) {
    "blood" -> Icons.Rounded.Bloodtype
    "medical" -> Icons.Rounded.MedicalServices
    "food" -> Icons.Rounded.Restaurant
    "education" -> Icons.Rounded.School
    "winter" -> Icons.Rounded.Checkroom
    "relief" -> Icons.Rounded.VolunteerActivism
    "emergency" -> Icons.Rounded.Emergency
    "family" -> Icons.Rounded.FamilyRestroom
    "construction" -> Icons.Rounded.Construction
    "environment" -> Icons.Rounded.Eco
    "volunteer" -> Icons.Rounded.Groups
    else -> Icons.Rounded.Handshake
}
