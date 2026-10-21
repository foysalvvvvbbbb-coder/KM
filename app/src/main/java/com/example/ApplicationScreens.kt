package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    onBack: () -> Unit,
    onViewPublicProfile: (String) -> Unit,
    onOpenChat: (ChatConversation) -> Unit
) {
    BackHandler(onBack = onBack)
    val apps by ProfileAndInboxRepository.userApplications.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF5F7F2)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("আমার আবেদন", "My Applications"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        if (apps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = AppSettingsManager.tr("আপনি এখনো কোনো আবেদন করেননি", "You haven't applied to any posts yet"), color = secondaryText, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(apps, key = { it.id }) { app ->
                    Card(modifier = Modifier.fillMaxWidth().testTag("app_card_${app.id}"), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = app.roleTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Text(text = app.madrasahName, fontSize = 13.sp, color = Color(0xFF0E6348), fontWeight = FontWeight.Medium)
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = when(app.status) { "গৃহীত" -> Color(0xFFE8F5E9); "প্রত্যাখ্যাত" -> Color(0xFFFBE9E7); else -> Color(0xFFE3F2FD) }) {
                                    Text(text = app.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = when(app.status) { "গৃহীত" -> Color(0xFF2E7D32); "প্রত্যাখ্যাত" -> Color(0xFFC62828); else -> Color(0xFF1565C0) })
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(text = "${AppSettingsManager.tr("আবেদনের তারিখ:", "Applied on:")} ${app.appliedDate}", fontSize = 12.sp, color = secondaryText)
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = { onViewPublicProfile("post_owner_${app.postId}") }, modifier = Modifier.height(34.dp)) { Text(AppSettingsManager.tr("কর্তৃপক্ষ", "Owner"), fontSize = 12.sp) }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { val conv = ProfileAndInboxRepository.getOrCreateConversationWith(participantId = "post_owner_${app.postId}", participantName = app.madrasahName, participantRole = "মাদরাসা কর্তৃপক্ষ"); onOpenChat(conv) }, modifier = Modifier.height(34.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E6348))) { Text(AppSettingsManager.tr("মেসেজ", "Message"), fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}
