package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KaomiKormoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val sentNotifications by AdminRepository.sentNotifications.collectAsState()
    val readIds by NotificationHelper.readNotificationIdsFlow.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()

    var selectedTab by remember { mutableStateOf("all") }

    val tabs = listOf(
        "all" to AppSettingsManager.tr("সব", "All"),
        "boost" to AppSettingsManager.tr("বুস্ট", "Boost"),
        "applications" to AppSettingsManager.tr("আবেদন", "Applications"),
        "announcements" to AppSettingsManager.tr("ঘোষণা", "Announcements")
    )

    val uniqueNotifications = remember(sentNotifications) {
        sentNotifications.filter { it.id.isNotBlank() }.distinctBy { it.id }
    }

    val filtered = remember(uniqueNotifications, selectedTab) {
        uniqueNotifications.filter { item ->
            when (selectedTab) {
                "boost" -> item.category == "Boost" || item.title.contains("Boost", ignoreCase = true) || item.title.contains("বুস্ট")
                "applications" -> item.category == "Applications" || item.title.contains("আবেদন") || item.title.contains("বিজ্ঞপ্তি")
                "announcements" -> item.category == "Announcements" || item.category == "Security" || item.title.contains("Admin") || item.title.contains("ঘোষণা")
                else -> true
            }
        }
    }

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF3F6F3)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("নোটিফিকেশন", "Notifications"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppSettingsManager.tr("ফিরে যান", "Back"), tint = Color.White) } },
                actions = {
                    if (uniqueNotifications.isNotEmpty()) {
                        TextButton(onClick = { NotificationHelper.markAllAsRead(context, uniqueNotifications.map { it.id }) }, modifier = Modifier.testTag("mark_all_notifications_read_btn")) {
                            Text(AppSettingsManager.tr("সব পঠিত", "Mark All Read"), color = Color(0xFFD7F3E3), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(modifier = Modifier.fillMaxWidth().background(cardBg).padding(horizontal = 12.dp, vertical = 10.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEach { (key, label) ->
                    val isSelected = selectedTab == key
                    Surface(shape = RoundedCornerShape(18.dp), color = if (isSelected) Color(0xFF0E6348) else if (isDark) Color(0xFF163D30) else Color(0xFFF0F5F2), modifier = Modifier.clickable { selectedTab = key }) {
                        Text(text = label, color = if (isSelected) Color.White else primaryText, fontSize = 12.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(text = AppSettingsManager.tr("কোনো নোটিফিকেশন নেই", "No notifications"), color = secondaryText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { notif ->
                        val isRead = readIds.contains(notif.id)
                        val isSecurity = notif.category == "Security" || notif.title.contains("সতর্কতা")
                        val iconBg = if (isSecurity) Color(0xFFE67E22) else Color(0xFF0E6348)
                        val iconVec = when {
                            notif.category == "Boost" || notif.title.contains("Boost") || notif.title.contains("বুস্ট") -> Icons.Default.Stars
                            notif.category == "Applications" || notif.title.contains("আবেদন") -> Icons.Default.AssignmentTurnedIn
                            notif.title.contains("Admin") || notif.title.contains("ঘোষণা") -> Icons.Default.Campaign
                            isSecurity -> Icons.Default.NotificationsActive
                            else -> Icons.Default.Notifications
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { NotificationHelper.markAsRead(context, notif.id); onNavigateToRoute(notif.targetRoute.ifBlank { "feed" }) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isRead) cardBg else if (isDark) Color(0xFF143C2F) else Color(0xFFF2FAF6)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(42.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = iconVec, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = notif.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = primaryText, modifier = Modifier.weight(1f))
                                        Spacer(Modifier.width(6.dp))
                                        Text(text = notif.sentTime, fontSize = 11.sp, color = secondaryText)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = notif.body, fontSize = 12.5.sp, color = secondaryText, lineHeight = 17.sp, modifier = Modifier.weight(1f))
                                        if (!isRead) {
                                            Spacer(Modifier.width(8.dp))
                                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFE53935), CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
