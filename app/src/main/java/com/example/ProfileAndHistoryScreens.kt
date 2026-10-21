package com.example

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KaomiKormoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onNavigateToMyApplications: () -> Unit,
    onNavigateToMyPosts: () -> Unit,
    onNavigateToInbox: () -> Unit = {},
    onNavigateToOnlineStatus: () -> Unit
) {
    val context = LocalContext.current
    val profile by ProfileAndInboxRepository.currentUserProfile.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()
    val lang by AppSettingsManager.currentLanguage.collectAsState()

    var showPhotoPreviewDialog by remember { mutableStateOf(false) }
    var showMoreProfileDetails by remember { mutableStateOf(false) }
    var showCardMenu by remember { mutableStateOf(false) }

    val completion = profile.completionPercentage()
    val completionLabel = if (lang == "en") "$completion%" else "${DateUtils.toBengaliDigits(completion.toString())}%"

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF5F7F2)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)
    val dividerColor = if (isDark) Color(0xFF1C4639) else Color(0xFFEAEFE9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("আমার প্রোফাইল", "My Profile"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                actions = { IconButton(onClick = onEditProfile, modifier = Modifier.testTag("top_edit_profile_button")) { Icon(imageVector = Icons.Default.EditNote, contentDescription = "Edit", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(14.dp, 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(modifier = Modifier.fillMaxWidth().testTag("my_profile_main_card"), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardBg), border = BorderStroke(1.dp, if (isDark) Color(0xFF1F4E3F) else Color(0xFFDFEAE3))) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp, 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { showCardMenu = true }, modifier = Modifier.align(Alignment.TopEnd).size(32.dp)) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = secondaryText) }
                        DropdownMenu(expanded = showCardMenu, onDismissRequest = { showCardMenu = false }) {
                            DropdownMenuItem(text = { Text(AppSettingsManager.tr("প্রোফাইল সম্পাদনা", "Edit Profile")) }, onClick = { showCardMenu = false; onEditProfile() })
                            DropdownMenuItem(text = { Text(AppSettingsManager.tr("অনলাইন স্ট্যাটাস ও প্রাইভেসি", "Online Status & Privacy")) }, onClick = { showCardMenu = false; onNavigateToOnlineStatus() })
                        }
                        Box(modifier = Modifier.align(Alignment.Center).padding(top = 4.dp).size(104.dp)) {
                            Box(modifier = Modifier.size(98.dp).align(Alignment.Center).border(2.5.dp, Color(0xFF1E8E3E), CircleShape).padding(3.dp), contentAlignment = Alignment.Center) { ProfileAvatarCircle(avatarPreset = profile.avatarPreset, photoUri = profile.photoUri, size = 90.dp) }
                            Box(modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).offset((-2).dp, (-2).dp).background(Color(0xFF0E5A44), CircleShape).border(2.dp, cardBg, CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Change", tint = Color.White, modifier = Modifier.size(15.dp)) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(text = profile.fullName.ifBlank { AppSettingsManager.tr("মুহাম্মদ ফয়সাল", "Muhammad Foysal") }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        if (profile.isVerified) { Spacer(Modifier.width(6.dp)); Box(modifier = Modifier.size(18.dp).background(Color(0xFF1E8E3E), CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Check, contentDescription = "Verified", tint = Color.White, modifier = Modifier.size(12.dp)) } }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onNavigateToOnlineStatus() }.padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(if (profile.isOnline) Color(0xFF1E8E3E) else Color.Gray, CircleShape)); Spacer(Modifier.width(5.dp))
                        Text(text = if (profile.isOnline) AppSettingsManager.tr("অনলাইন", "Online") else AppSettingsManager.tr("অফলাইন", "Offline"), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (profile.isOnline) Color(0xFF1E8E3E) else secondaryText)
                    }
                    Text(text = profile.roleLabel.ifBlank { AppSettingsManager.tr("খেদমতপ্রার্থী", "Candidate") }, fontSize = 12.5.sp, color = secondaryText, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(14.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = AppSettingsManager.tr("প্রোফাইল সম্পূর্ণতা $completionLabel", "Profile Completion $completionLabel"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Text(text = AppSettingsManager.tr("আরও তথ্য যোগ করুন", "Add more info"), fontSize = 11.sp, color = Color(0xFF0E6348), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onEditProfile() })
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = completion / 100f, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = Color(0xFF1E8E3E), trackColor = if (isDark) Color(0xFF1C3A2D) else Color(0xFFE8F5E9))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardBg), border = BorderStroke(1.dp, if (isDark) Color(0xFF1F4E3F) else Color(0xFFDFEAE3))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = AppSettingsManager.tr("মূল তথ্য", "Key Information"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    Spacer(Modifier.height(6.dp))
                    ProfileDetailItemRow(Icons.Outlined.Mosque, AppSettingsManager.tr("মাদরাসা:", "Madrasah:"), profile.madrasahName, primaryText, secondaryText)
                    HorizontalDivider(color = dividerColor)
                    ProfileDetailItemRow(Icons.Outlined.School, AppSettingsManager.tr("যোগ্যতা:", "Qualification:"), profile.qualification, primaryText, secondaryText)
                    HorizontalDivider(color = dividerColor)
                    ProfileDetailItemRow(Icons.Outlined.LocationOn, AppSettingsManager.tr("এলাকা:", "Location:"), profile.location, primaryText, secondaryText)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicUserProfileScreen(
    userIdOrName: String,
    onBack: () -> Unit,
    onSendMessage: (ChatConversation) -> Unit,
    onEditOwnProfile: () -> Unit = {},
    onNavigateToMyApplications: () -> Unit = {},
    onNavigateToMyPosts: () -> Unit = {},
    onNavigateToOnlineStatus: () -> Unit = {}
) {
    BackHandler(onBack = onBack)
    val isDark by AppSettingsManager.isDarkMode.collectAsState()
    val isOwn = remember(userIdOrName) { ProfileAndInboxRepository.isOwnProfile(userIdOrName) }

    if (isOwn) {
        ProfessionalProfileScreen(onBack = onBack, onEditProfile = onEditOwnProfile, onNavigateToMyApplications = onNavigateToMyApplications, onNavigateToMyPosts = onNavigateToMyPosts, onNavigateToOnlineStatus = onNavigateToOnlineStatus)
        return
    }

    val publicProfile = remember(userIdOrName) { ProfileAndInboxRepository.getPublicProfile(userIdOrName) }
    val effectiveOnline = publicProfile.getEffectiveOnlineState(viewerIsSelf = false)
    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF5F7F2)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("প্রোফাইল", "User Profile"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(14.dp, 12.dp)) {
            Card(modifier = Modifier.fillMaxWidth().testTag("other_user_profile_card"), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardBg), border = BorderStroke(1.dp, if (isDark) Color(0xFF1F4E3F) else Color(0xFFDFEAE3))) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(82.dp).border(2.dp, Color(0xFF1E8E3E), CircleShape).padding(3.dp), contentAlignment = Alignment.Center) { ProfileAvatarCircle(avatarPreset = publicProfile.avatarPreset, photoUri = publicProfile.photoUri, size = 74.dp) }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = publicProfile.fullName, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(8.dp).background(if (effectiveOnline) Color(0xFF1E8E3E) else Color.Gray, CircleShape)); Spacer(Modifier.width(5.dp)); Text(text = if (effectiveOnline) AppSettingsManager.tr("অনলাইন", "Online") else AppSettingsManager.tr("অফলাইন", "Offline"), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (effectiveOnline) Color(0xFF1E8E3E) else secondaryText) }
                            Text(text = publicProfile.roleLabel.ifBlank { AppSettingsManager.tr("মাদরাসা শিক্ষক", "Teacher") }, fontSize = 13.sp, color = secondaryText, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (publicProfile.allowInboxMessages) {
                        Button(onClick = { val conv = ProfileAndInboxRepository.getOrCreateConversationWith(participantId = publicProfile.uid, participantName = publicProfile.fullName, participantRole = publicProfile.roleLabel, participantPhone = publicProfile.phone, participantMadrasah = publicProfile.madrasahName); onSendMessage(conv) }, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E6348)), modifier = Modifier.fillMaxWidth().height(44.dp).testTag("send_message_from_profile_button")) { Text(text = AppSettingsManager.tr("মেসেজ করুন", "Send Message"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(text = AppSettingsManager.tr("মূল তথ্য", "Key Information"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    ProfileDetailItemRow(Icons.Outlined.Mosque, AppSettingsManager.tr("মাদরাসা:", "Madrasah:"), publicProfile.madrasahName, primaryText, secondaryText)
                    ProfileDetailItemRow(Icons.Outlined.School, AppSettingsManager.tr("যোগ্যতা:", "Qualification:"), publicProfile.qualification, primaryText, secondaryText)
                }
            }
        }
    }
}
