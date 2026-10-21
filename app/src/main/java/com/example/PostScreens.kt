package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun MyPostsScreen(
    onBack: () -> Unit,
    onOpenChat: (ChatConversation) -> Unit
) {
    BackHandler(onBack = onBack)
    val posts by ProfileAndInboxRepository.myPostedNotices.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF5F7F2)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("আমার বিজ্ঞপ্তি", "My Posts"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = AppSettingsManager.tr("আপনি এখনো কোনো বিজ্ঞপ্তি পোস্ট করেননি", "You haven't posted any notices yet"), color = secondaryText)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(posts, key = { it.postId }) { post ->
                    Card(modifier = Modifier.fillMaxWidth().testTag("post_card_${post.postId}"), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = post.roleTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Text(text = post.madrasahName, fontSize = 13.sp, color = Color(0xFF0E6348), fontWeight = FontWeight.Medium)
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = if (post.status == "Active") Color(0xFFE8F5E9) else Color(0xFFF5F5F5)) {
                                    Text(text = post.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (post.status == "Active") Color(0xFF2E7D32) else Color.Gray)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                Column { Text(text = AppSettingsManager.tr("আবেদন", "Apps"), fontSize = 11.sp, color = secondaryText); Text(text = AppSettingsManager.formatNumber(post.applicationsCount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryText) }
                                Column { Text(text = AppSettingsManager.tr("দেখা হয়েছে", "Views"), fontSize = 11.sp, color = secondaryText); Text(text = AppSettingsManager.formatNumber(post.viewsCount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryText) }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = { /* Edit Post */ }, modifier = Modifier.height(34.dp)) { Text(AppSettingsManager.tr("সম্পাদনা", "Edit"), fontSize = 12.sp) }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { /* View Applications List */ }, modifier = Modifier.height(34.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E6348))) { Text(AppSettingsManager.tr("আবেদনকারী", "Applicants"), fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}
