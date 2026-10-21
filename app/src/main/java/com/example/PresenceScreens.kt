package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineStatusAndPrivacyScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val profile by ProfileAndInboxRepository.currentUserProfile.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()
    val lang by AppSettingsManager.currentLanguage.collectAsState()

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF3F6F3)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("অনলাইন স্ট্যাটাস ও সেটিংস", "Online Status & Settings"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppSettingsManager.tr("ফিরে যান", "Back"), tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = AppSettingsManager.tr("স্ট্যাটাসের ধরন", "Presence Status"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(18.dp).background(if (profile.isOnline) Color(0xFF1E8E3E) else Color.Gray, CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (profile.isOnline) AppSettingsManager.tr("বর্তমানে অনলাইন", "Active Now") else AppSettingsManager.tr("অফলাইন", "Offline"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Text(text = if (profile.isOnline) AppSettingsManager.tr("অন্যরা আপনাকে সক্রিয় দেখতে পাচ্ছেন", "Others see you as online") else AppSettingsManager.tr("বর্তমানে আপনি অফলাইন মুডে আছেন", "You are currently appearing offline"), fontSize = 12.sp, color = secondaryText)
                        }
                        Switch(checked = profile.isOnline, onCheckedChange = { ProfileAndInboxRepository.setOnlinePresence(it, profile.showOnlineStatus) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1E8E3E)), modifier = Modifier.testTag("online_status_main_switch"))
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = AppSettingsManager.tr("প্রাইভেসি ও থিম সেটিংস", "Privacy & App Settings"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = AppSettingsManager.tr("আমার অনলাইন স্ট্যাটাস সবার কাছে দেখান", "Show my online status to everyone"), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Text(text = AppSettingsManager.tr("অন্যরা আপনার অনলাইন ও শেষ সক্রিয় সময় দেখতে পাবেন", "Others can see when you're online or last active"), fontSize = 11.5.sp, color = secondaryText)
                        }
                        Switch(checked = profile.showOnlineStatus, onCheckedChange = { ProfileAndInboxRepository.setOnlinePresence(profile.isOnline, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1E8E3E)), modifier = Modifier.testTag("privacy_online_status_switch"))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = secondaryText.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = AppSettingsManager.tr("ডার্ক মোড (Dark Mode)", "Dark Mode"), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Text(text = AppSettingsManager.tr("পুরো অ্যাপজুড়ে ডার্ক থিম সক্রিয় করুন", "Apply dark theme across the entire app"), fontSize = 11.5.sp, color = secondaryText)
                        }
                        Switch(checked = isDark, onCheckedChange = { AppSettingsManager.setDarkMode(context, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1E8E3E)))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = secondaryText.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = AppSettingsManager.tr("ভাষা পরিবর্তন (Language)", "Language Selection"), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Text(text = if (lang == "en") "Active: English" else "সক্রিয় ভাষা: বাংলা (Bangla)", fontSize = 11.5.sp, color = secondaryText)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(selected = lang == "bn", onClick = { AppSettingsManager.setLanguage(context, "bn") }, label = { Text("বাংলা", fontSize = 12.sp) })
                            FilterChip(selected = lang == "en", onClick = { AppSettingsManager.setLanguage(context, "en") }, label = { Text("English", fontSize = 12.sp) })
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3F31))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = AppSettingsManager.tr("নিরাপদ যোগাযোগ", "Secure Communication"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(2.dp))
                        Text(text = AppSettingsManager.tr("শুধুমাত্র লগইন করা এবং অনুমোদিত ব্যবহারকারীরাই মেসেজ পাঠাতে পারবেন।", "Only authenticated users can send text messages."), fontSize = 12.sp, color = Color(0xFFD7F3E3), lineHeight = 17.sp)
                    }
                }
            }
        }
    }
}
