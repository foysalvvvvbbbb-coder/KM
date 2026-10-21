package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToFeed: (initialFilter: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMadrasah: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()
    val lang by AppSettingsManager.currentLanguage.collectAsState()

    var showPostCandidateDialog by remember { mutableStateOf(false) }
    var showPostNoticeDialog by remember { mutableStateOf(false) }
    var showFullPrayerTimesDialog by remember { mutableStateOf(false) }
    var showVerifiedMadrasahDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF3F6F3)

    Scaffold(
        topBar = {
            HomeTopBar(
                onOpenDrawer = onOpenDrawer,
                onNotificationClick = { showNotificationsDialog = true }
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Hero Banner Card
            HeroBannerCard()

            // 2. Action Grid (2 columns + 1 full width card)
            ActionCardsGrid(
                onPostCandidate = { showPostCandidateDialog = true },
                onPostNotice = { showPostNoticeDialog = true },
                onViewCandidates = { onNavigateToFeed("আবেদন") },
                onViewProfile = onNavigateToProfile,
                onViewBookmarks = { onNavigateToFeed("পছন্দের খেদমত") },
                onViewVerified = { showVerifiedMadrasahDialog = true },
                onViewMadrasahs = onNavigateToMadrasah
            )

            // 3. Prayer Times Section (AlAdhan API + Karachi Hanafi method)
            PrayerTimesCard(
                onShowFullSchedule = { showFullPrayerTimesDialog = true }
            )

            // 4. Date Card (Islamic Hijri + Bangla + English)
            DateCardSection()

            // 5. Daily Hadith / Quote Card (আজকের বাণী)
            DailyHadithCard()

            // 6. Bottom Goal Banner (খেদমতই আমাদের লক্ষ্য)
            BottomGoalBanner()

            Spacer(Modifier.height(14.dp))
        }
    }

    // Dialogs
    if (showPostCandidateDialog) {
        PostCandidateDialog(
            onDismiss = { showPostCandidateDialog = false },
            onSubmitted = {
                showPostCandidateDialog = false
                onNavigateToFeed("আবেদন")
            }
        )
    }

    if (showPostNoticeDialog) {
        PostNoticeDialog(
            onDismiss = { showPostNoticeDialog = false },
            onSubmitted = {
                showPostNoticeDialog = false
                onNavigateToFeed("নিয়োগ বিজ্ঞপ্তি")
            }
        )
    }

    if (showFullPrayerTimesDialog) {
        FullPrayerTimesDialog(
            onDismiss = { showFullPrayerTimesDialog = false }
        )
    }

    if (showVerifiedMadrasahDialog) {
        VerifiedMadrasahDialog(
            onDismiss = { showVerifiedMadrasahDialog = false },
            onViewNotices = {
                showVerifiedMadrasahDialog = false
                onNavigateToFeed("নিয়োগ বিজ্ঞপ্তি")
            }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onOpenDrawer: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val sentNotifs by AdminRepository.sentNotifications.collectAsState()
    val readIds by NotificationHelper.readNotificationIdsFlow.collectAsState()
    val lang by AppSettingsManager.currentLanguage.collectAsState()

    val unreadCount = remember(sentNotifs, readIds) {
        NotificationHelper.getUnreadNotificationCount(sentNotifs, readIds)
    }
    val badgeText = remember(unreadCount, lang) {
        NotificationHelper.getBadgeText(unreadCount)
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppSettingsManager.tr(
                        "কওমি কর্মসংস্থান বাংলাদেশ",
                        "Qawmi Employment BD"
                    ),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("hamburger_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = AppSettingsManager.tr("মেনু", "Menu"),
                    tint = Color.White
                )
            }
        },
        actions = {
            // ONLY place in the entire app where Notification Badge appears: Top-right Bell Icon
            Box(modifier = Modifier.padding(end = 6.dp)) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.testTag("notification_bell_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = AppSettingsManager.tr("নোটিফিকেশন", "Notifications"),
                        tint = Color.White
                    )
                }
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                            .background(Color(0xFFE53935), CircleShape)
                            .border(1.5.dp, QawmiDarkGreen, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 5.dp)
                            .padding(horizontal = 4.dp)
                            .testTag("notification_bell_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = QawmiDarkGreen)
    )
}

@Composable
fun HeroBannerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.5.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4ED)),
        border = BorderStroke(1.dp, Color(0xFFCBE7D8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vector Mosque Illustration
            Box(
                modifier = Modifier
                    .size(width = 86.dp, height = 72.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mosque_illustration),
                    contentDescription = "মাদ্রাসা ও মসজিদ",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AdminRepository.appContent.heroTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = AdminRepository.appContent.heroDescription,
                    fontSize = 11.5.sp,
                    color = Color(0xFF3E6155),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ActionCardsGrid(
    onPostCandidate: () -> Unit,
    onPostNotice: () -> Unit,
    onViewCandidates: () -> Unit,
    onViewProfile: () -> Unit,
    onViewBookmarks: () -> Unit,
    onViewVerified: () -> Unit,
    onViewMadrasahs: () -> Unit
) {
    val lang by AppSettingsManager.currentLanguage.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: আবেদন প্রকাশ করুন | বিজ্ঞপ্তি দিন
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionCardItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.EditNote,
                title = AppSettingsManager.tr("আবেদন প্রকাশ করুন", "Post Application"),
                subtitle = AppSettingsManager.tr("নিজের যোগ্যতা ও অভিজ্ঞতা নিয়ে আবেদন করুন", "Share your qualifications & experience"),
                onClick = onPostCandidate
            )
            ActionCardItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Campaign,
                title = AppSettingsManager.tr("বিজ্ঞপ্তি দিন", "Post Job Notice"),
                subtitle = AppSettingsManager.tr("মাদ্রাসার নিয়োগ বিজ্ঞপ্তি প্রকাশ করুন", "Publish madrasah recruitment notices"),
                onClick = onPostNotice
            )
        }

        // Row 2: অন্যদের আবেদন দেখুন | আমার প্রোফাইল
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionCardItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Groups,
                title = AppSettingsManager.tr("অন্যদের আবেদন দেখুন", "Browse Candidates"),
                subtitle = AppSettingsManager.tr("অন্যান্য প্রার্থীদের আবেদন ও প্রোফাইল দেখুন", "View candidate profiles & applications"),
                onClick = onViewCandidates
            )
            ActionCardItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Person,
                title = AppSettingsManager.tr("আমার প্রোফাইল", "My Profile"),
                subtitle = AppSettingsManager.tr("নিজের তথ্য, যোগ্যতা ও অভিজ্ঞতা আপডেট করুন", "Manage your profile, applications & posts"),
                onClick = onViewProfile
            )
        }

        // Row 3: পছন্দের খেদমত | যাচাইকৃত মাদ্রাসা
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionCardItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Bookmark,
                title = AppSettingsManager.tr("পছন্দের খেদমত", "Saved Posts"),
                subtitle = AppSettingsManager.tr("পছন্দের চাকরি সংরক্ষণ করে পরে আবেদন করুন", "Bookmark posts to apply later"),
                onClick = onViewBookmarks
            )
            ActionCardItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.VerifiedUser,
                title = AppSettingsManager.tr("যাচাইকৃত মাদ্রাসা", "Verified Madrasahs"),
                subtitle = AppSettingsManager.tr("যাচাইকৃত মাদ্রাসাগুলো আলাদাভাবে দেখুন", "Browse verified madrasah institutions"),
                onClick = onViewVerified
            )
        }

        // Row 4 (Full Width): মাদ্রাসা
        ActionCardFullWidth(
            icon = Icons.Default.AccountBalance,
            title = AppSettingsManager.tr("মাদ্রাসা", "Madrasah Directory"),
            subtitle = AppSettingsManager.tr("মাদ্রাসার তথ্য, অবস্থান ও বিশেষ সুবিধা দেখুন", "Explore madrasah details, locations & facilities"),
            onClick = onViewMadrasahs
        )
    }
}

@Composable
fun ActionCardItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark by AppSettingsManager.isDarkMode.collectAsState()
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val borderColor = if (isDark) Color(0xFF1F4E3F) else Color(0xFFE2EBE5)
    val titleColor = if (isDark) Color(0xFFE8F5EF) else QawmiDarkGreen
    val subColor = if (isDark) Color(0xFF9BC4B5) else Color(0xFF5A786E)

    Card(
        modifier = modifier
            .shadow(1.5.dp, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(0.8.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(QawmiDarkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFA0B4AA),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = subColor,
                lineHeight = 15.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun ActionCardFullWidth(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark by AppSettingsManager.isDarkMode.collectAsState()
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val borderColor = if (isDark) Color(0xFF1F4E3F) else Color(0xFFE2EBE5)
    val titleColor = if (isDark) Color(0xFFE8F5EF) else QawmiDarkGreen
    val subColor = if (isDark) Color(0xFF9BC4B5) else Color(0xFF5A786E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.5.dp, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(0.8.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(QawmiDarkGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = subColor,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFA0B4AA),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PrayerTimesCard(
    onShowFullSchedule: () -> Unit
) {
    var prayerTimes by remember { mutableStateOf<List<PrayerTime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var lastTick by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        try {
            prayerTimes = PrayerTimeManager.getPrayerTimes("Dhaka")
        } catch (_: Exception) {
            prayerTimes = PrayerTimeManager.getFallbackPrayerTimes()
        }
        isLoading = false

        // 30-second live refresh loop for real-time Dhaka active prayer tracking
        while (true) {
            delay(30_000L)
            lastTick = System.currentTimeMillis()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C382E)),
        border = BorderStroke(0.8.dp, Color(0xFFDEC590).copy(alpha = 0.35f))
    ) {
        // Calculate minutes based on Asia/Dhaka Bangladesh Standard Time
        val dhakaCal = remember(lastTick) { DateUtils.getDhakaCalendar() }
        val currentMinutes = dhakaCal.get(Calendar.HOUR_OF_DAY) * 60 + dhakaCal.get(Calendar.MINUTE)

        fun parseMin(timeStr: String): Int {
            return try {
                val clean = timeStr.trim().take(5)
                val parts = clean.split(":")
                parts[0].toInt() * 60 + parts[1].toInt()
            } catch (_: Exception) {
                0
            }
        }

        val effectiveTimes = if (prayerTimes.isEmpty()) PrayerTimeManager.getFallbackPrayerTimes() else prayerTimes
        val fajrStart = effectiveTimes.getOrNull(0)?.let { parseMin(it.start) } ?: (4 * 60 + 42)
        val fajrEnd = effectiveTimes.getOrNull(0)?.let { parseMin(it.end) } ?: (5 * 60 + 58)
        val dhuhrStart = effectiveTimes.getOrNull(1)?.let { parseMin(it.start) } ?: (11 * 60 + 43)
        val asrStart = effectiveTimes.getOrNull(2)?.let { parseMin(it.start) } ?: (15 * 60 + 51)
        val maghribStart = effectiveTimes.getOrNull(3)?.let { parseMin(it.start) } ?: (17 * 60 + 28)
        val ishaStart = effectiveTimes.getOrNull(4)?.let { parseMin(it.start) } ?: (18 * 60 + 44)

        val activeIndex = when {
            currentMinutes in fajrStart until fajrEnd -> 0 // ফজর
            currentMinutes in fajrEnd until dhuhrStart -> -1 // চাশত
            currentMinutes in dhuhrStart until asrStart -> 1 // যোহর
            currentMinutes in asrStart until maghribStart -> 2 // আসর
            currentMinutes in maghribStart until ishaStart -> 3 // মাগরিব
            currentMinutes >= ishaStart || currentMinutes < fajrStart -> 4 // এশা
            else -> -1
        }

        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFFDEC590),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "নামাজের সময়সূচী (ঢাকা)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF164E3E),
                    border = BorderStroke(0.6.dp, Color(0xFF388E6D)),
                    modifier = Modifier.clickable(onClick = onShowFullSchedule)
                ) {
                    Text(
                        text = "সম্পূর্ণ সময়সূচী →",
                        color = Color(0xFFC2E8D7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // 5 Prayer Time Columns in a Single Horizontal Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                effectiveTimes.forEachIndexed { index, prayer ->
                    val isActive = index == activeIndex
                    val (timeBangla, period) = formatTimeAndPeriod(prayer.start)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) Color(0xFF185240) else Color(0xFF0F3B30))
                            .border(
                                BorderStroke(
                                    if (isActive) 1.dp else 0.5.dp,
                                    if (isActive) Color(0xFFDEC590) else Color(0xFF1D5446)
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = prayer.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFFF5E8C8) else Color(0xFFC2E5D4)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = timeBangla,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(1.dp))
                            Text(
                                text = period,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isActive) Color(0xFFF1D184) else Color(0xFF9DC9B7)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatTimeAndPeriod(timeStr: String): Pair<String, String> {
    return try {
        val clean = timeStr.trim().take(5)
        val parts = clean.split(":")
        if (parts.size >= 2) {
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1]
            val period = if (h < 12) "AM" else "PM"
            val h12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            val formattedH = String.format("%02d", h12)
            val timeBangla = DateUtils.toBengaliDigits("$formattedH:$m")
            Pair(timeBangla, period)
        } else {
            Pair(timeStr, "")
        }
    } catch (_: Exception) {
        Pair(timeStr, "")
    }
}

@Composable
fun DateCardSection() {
    val dateInfo = remember { DateUtils.getCurrentDateInfo() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.5.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.8.dp, Color(0xFFD6EAE0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Calendar icon + Title + Hijri Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "তারিখ",
                    tint = QawmiDarkGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "আজকের তারিখ",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = QawmiDarkGreen
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = dateInfo.hijriDate.removeSuffix("*"),
                        fontSize = 11.5.sp,
                        color = Color(0xFF5A786E)
                    )
                }
            }

            // Right: English date + Weekday
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dateInfo.englishDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = QawmiDarkGreen
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = dateInfo.weekday,
                    fontSize = 11.5.sp,
                    color = Color(0xFF5A786E)
                )
            }
        }
    }
}

@Composable
fun DailyHadithCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FAF6)),
        border = BorderStroke(0.8.dp, Color(0xFFD0E8DC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "আজকের বাণী",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "“জ্ঞান অর্জন করা প্রত্যেক মুসলিমের উপর ফরজ।”",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF164638),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = "— (ইবনে মাজাহ, ২২৪)",
                fontSize = 11.5.sp,
                color = Color(0xFF5A786E),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BottomGoalBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.5.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4ED)),
        border = BorderStroke(0.8.dp, Color(0xFFCBE7D8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                tint = QawmiDarkGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "۞  খেদমতই আমাদের লক্ষ্য  ۞",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "কওমি মাদ্রাসার উন্নয়নে\nএকসাথে এগিয়ে চলি",
                    fontSize = 11.5.sp,
                    color = Color(0xFF3E6155),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ----------------- Interactive Dialogs -----------------

@Composable
fun PostCandidateDialog(
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var preferredDistricts by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "আবেদন প্রকাশ করুন",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
                Text(
                    text = "নিজের যোগ্যতা ও অভিজ্ঞতা নিয়ে খেদমতের আবেদন পোস্ট করুন",
                    fontSize = 12.sp,
                    color = Color(0xFF5A786E)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পূর্ণ নাম") },
                    placeholder = { Text("যেমন: মাওলানা আব্দুল্লাহ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("আকাঙ্ক্ষিত পদ") },
                    placeholder = { Text("যেমন: সহকারী শিক্ষক (আরবি / ফিকহ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("শিক্ষাগত যোগ্যতা") },
                    placeholder = { Text("যেমন: দাওরায়ে হাদীস / ইফতা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("অভিজ্ঞতা") },
                    placeholder = { Text("যেমন: ৩ বছর / নতুন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("বর্তমান জেলা") },
                    placeholder = { Text("যেমন: ঢাকা, নোয়াখালী") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = preferredDistricts,
                    onValueChange = { preferredDistricts = it },
                    label = { Text("পছন্দের জেলা") },
                    placeholder = { Text("যেমন: ঢাকা, কুমিল্লা, চট্টগ্রাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("যোগাযোগ নম্বর") },
                    placeholder = { Text("017XXXXXXXX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color(0xFFB00020), fontSize = 12.sp)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                error = "আপনার নাম লিখুন।"
                            } else if (role.isBlank()) {
                                error = "আকাঙ্ক্ষিত পদ লিখুন।"
                            } else if (phone.isBlank()) {
                                error = "মোবাইল নম্বর লিখুন।"
                            } else {
                                val item = FeedItem(
                                    id = "cand_${System.currentTimeMillis()}",
                                    type = FeedItemType.CANDIDATE_APPLICATION,
                                    title = name.trim(),
                                    location = if (district.isNotBlank()) district.trim() else "বাংলাদেশ",
                                    role = role.trim(),
                                    qualification = if (qualification.isNotBlank()) qualification.trim() else "দাওরায়ে হাদীস",
                                    extraInfo = if (experience.isNotBlank()) experience.trim() else "নতুন",
                                    secondaryInfo = if (preferredDistricts.isNotBlank()) preferredDistricts.trim() else "সারাদেশ",
                                    timeAgo = "এইমাত্র",
                                    isVerified = true,
                                    contactPhone = phone.trim()
                                )
                                FeedRepository.addPost(item)
                                Toast.makeText(context, "আপনার খেদমতের আবেদন সফলভাবে প্রকাশিত হয়েছে!", Toast.LENGTH_LONG).show()
                                onSubmitted()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen)
                    ) {
                        Text("আবেদন প্রকাশ করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PostNoticeDialog(
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var madrasahName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "নিয়োগ বিজ্ঞপ্তি দিন",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
                Text(
                    text = "মাদ্রাসার শিক্ষক বা স্টাফ নিয়োগের বিজ্ঞপ্তি প্রকাশ করুন",
                    fontSize = 12.sp,
                    color = Color(0xFF5A786E)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = madrasahName,
                    onValueChange = { madrasahName = it },
                    label = { Text("মাদ্রাসার নাম") },
                    placeholder = { Text("যেমন: দারুল উলুম কওমি মাদ্রাসা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("অবস্থান ও জেলা") },
                    placeholder = { Text("যেমন: কুমিল্লা, কুমিল্লা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("পদের নাম") },
                    placeholder = { Text("যেমন: সহকারী শিক্ষক (আরবি, তাফসীর)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("প্রয়োজনীয় যোগ্যতা") },
                    placeholder = { Text("যেমন: দাওরায়ে হাদীস / সমমান") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("বেতন / সুবিধা") },
                    placeholder = { Text("যেমন: ১০,০০০ - ১৫,০০০ টাকা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("আবেদনের শেষ তারিখ") },
                    placeholder = { Text("যেমন: ২৫ অক্টোবর ২০২৫") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("যোগাযোগের নম্বর") },
                    placeholder = { Text("017XXXXXXXX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color(0xFFB00020), fontSize = 12.sp)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (madrasahName.isBlank()) {
                                error = "মাদ্রাসার নাম লিখুন।"
                            } else if (role.isBlank()) {
                                error = "পদের নাম লিখুন।"
                            } else if (phone.isBlank()) {
                                error = "মোবাইল নম্বর লিখুন।"
                            } else {
                                val item = FeedItem(
                                    id = "notice_${System.currentTimeMillis()}",
                                    type = FeedItemType.MADRASAH_NOTICE,
                                    title = madrasahName.trim(),
                                    location = if (location.isNotBlank()) location.trim() else "বাংলাদেশ",
                                    role = role.trim(),
                                    qualification = if (qualification.isNotBlank()) qualification.trim() else "দাওরায়ে হাদীস",
                                    extraInfo = if (salary.isNotBlank()) salary.trim() else "আলোচনা সাপেক্ষ",
                                    secondaryInfo = if (deadline.isNotBlank()) deadline.trim() else "চলমান",
                                    timeAgo = "এইমাত্র",
                                    isVerified = true,
                                    contactPhone = phone.trim()
                                )
                                FeedRepository.addPost(item)
                                Toast.makeText(context, "মাদ্রাসার নিয়োগ বিজ্ঞপ্তি সফলভাবে প্রকাশিত হয়েছে!", Toast.LENGTH_LONG).show()
                                onSubmitted()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen)
                    ) {
                        Text("বিজ্ঞপ্তি প্রকাশ করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FullPrayerTimesDialog(
    onDismiss: () -> Unit
) {
    val times = remember { PrayerTimeManager.getFallbackPrayerTimes() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "۞ নামাজের সম্পূর্ণ সময়সূচী ۞",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = QawmiDarkGreen
                        )
                        Text(
                            text = "ঢাকা, বাংলাদেশ • হানাফি মাযহাব (করাচি পদ্ধতি)",
                            fontSize = 11.5.sp,
                            color = Color(0xFF5A786E)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    times.forEach { prayer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F7F5), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prayer.name,
                                fontWeight = FontWeight.Bold,
                                color = QawmiDarkGreen,
                                fontSize = 14.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "শুরু: ${DateUtils.formatTimeTo12Hour(prayer.start)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2C5E50)
                                )
                                Text(
                                    text = "শেষ: ${DateUtils.formatTimeTo12Hour(prayer.end)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1B4332)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "• আসরের সময় হানাফি মাযহাবের দ্বিগুণ ছায়া (Asr 2x) নিয়ম অনুযায়ী নির্ধারিত।",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun VerifiedMadrasahDialog(
    onDismiss: () -> Unit,
    onViewNotices: () -> Unit
) {
    val verifiedMadrasahs = listOf(
        "দারুল উলুম মুঈনুল ইসলাম" to "হাটহাজারী, চট্টগ্রাম",
        "জামিয়া ইসলামিয়া দারুল উলুম মাদানিয়া" to "যাত্রাবাড়ী, ঢাকা",
        "জামিয়া শারইয়্যাহ মালিবাগ" to "মালিবাগ, ঢাকা",
        "আল জামিয়াতুল আরাবিয়া দারুল কুরআন" to "সিলেট",
        "আল জামিয়া আল ইসলামিয়া পটিয়া" to "পটিয়া, চট্টগ্রাম",
        "জামিয়া ইসলামিয়া আরাবিয়া" to "বানারীপাড়া, বরিশাল"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "যাচাইকৃত কওমি মাদ্রাসা",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
                Text(
                    text = "বাংলাদেশের বিশ্বস্ত ও নির্ভরযোগ্য কওমি প্রতিষ্ঠানসমূহ",
                    fontSize = 12.sp,
                    color = Color(0xFF5A786E)
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    verifiedMadrasahs.forEach { (name, location) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F7F5), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = QawmiDarkGreen
                                )
                                Text(
                                    text = location,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF5A786E)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বন্ধ করুন", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onViewNotices,
                        colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen)
                    ) {
                        Text("নিয়োগ বিজ্ঞপ্তি দেখুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val adminNotifications by AdminRepository.sentNotifications.collectAsState()
    val readIds by NotificationHelper.readNotificationIdsFlow.collectAsState()
    val boostPref by NotificationHelper.boostNotificationsEnabledFlow.collectAsState()
    val appPref by NotificationHelper.applicationNotificationsEnabledFlow.collectAsState()
    val announcePref by NotificationHelper.announcementNotificationsEnabledFlow.collectAsState()

    var selectedCategory by remember { mutableStateOf("সব") }
    var showPrefsPanel by remember { mutableStateOf(false) }

    val uniqueAdminNotifications = remember(adminNotifications) {
        adminNotifications.filter { it.id.isNotBlank() }.distinctBy { it.id }
    }

    val filteredNotifications = remember(uniqueAdminNotifications, selectedCategory) {
        if (selectedCategory == "সব") {
            uniqueAdminNotifications
        } else {
            uniqueAdminNotifications.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val unreadCount = remember(uniqueAdminNotifications, readIds) {
        NotificationHelper.getUnreadNotificationCount(uniqueAdminNotifications, readIds)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "নোটিফিকেশন কেন্দ্র",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = QawmiDarkGreen
                        )
                        Text(
                            text = "অপঠিত: ${DateUtils.toBengaliDigits(unreadCount.toString())} টি • মোট: ${DateUtils.toBengaliDigits(adminNotifications.size.toString())} টি",
                            fontSize = 11.sp,
                            color = Color(0xFF5A786E)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (unreadCount > 0) {
                            TextButton(
                                onClick = {
                                    NotificationHelper.markAllAsRead(context, adminNotifications.map { it.id })
                                    Toast.makeText(context, "সব নোটিফিকেশন পঠিত হিসেবে চিহ্নিত হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("সব পঠিত করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QawmiDarkGreen)
                            }
                        }
                        IconButton(
                            onClick = { showPrefsPanel = !showPrefsPanel },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "নোটিফিকেশন পছন্দসমূহ",
                                tint = QawmiDarkGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (showPrefsPanel) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3F8F5),
                        border = BorderStroke(1.dp, Color(0xFFD2E5DC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "বিভাগভিত্তিক নোটিফিকেশন চালু/বন্ধ (Opt-in / Opt-out):",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = QawmiDarkGreen
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🚀 Boost ও বিশেষ প্রচারিত বিজ্ঞপ্তি", fontSize = 12.sp)
                                Switch(
                                    checked = boostPref,
                                    onCheckedChange = { NotificationHelper.setCategoryEnabled(context, "Boost", it) }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📝 আবেদন ও নিয়োগ আপডেট", fontSize = 12.sp)
                                Switch(
                                    checked = appPref,
                                    onCheckedChange = { NotificationHelper.setCategoryEnabled(context, "Applications", it) }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📢 সাধারণ ঘোষণা (Announcements)", fontSize = 12.sp)
                                Switch(
                                    checked = announcePref,
                                    onCheckedChange = { NotificationHelper.setCategoryEnabled(context, "Announcements", it) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Category Filter Chips
                val categories = listOf(
                    "সব" to "সব",
                    "Boost" to "🚀 Boost",
                    "Applications" to "📝 আবেদন",
                    "Announcements" to "📢 ঘোষণা",
                    "Security" to "🛡 নিরাপত্তা"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (key, label) ->
                        val isSelected = selectedCategory == key
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) QawmiDarkGreen else Color(0xFFF0F5F2),
                            modifier = Modifier.clickable { selectedCategory = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF1F382E),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (filteredNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("এই বিভাগে কোনো নোটিফিকেশন নেই", fontSize = 13.sp, color = Color.Gray)
                        }
                    } else {
                        filteredNotifications.forEach { notif ->
                            val isRead = readIds.contains(notif.id)
                            NotificationItem(
                                title = notif.title,
                                description = notif.body,
                                time = notif.sentTime,
                                category = notif.category,
                                isRead = isRead,
                                onClick = {
                                    NotificationHelper.markAsRead(context, notif.id)
                                    val route = notif.targetRoute.ifBlank { "khedmot" }
                                    NotificationHelper.triggerDirectNavigation(route, notif.targetItemId)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("বন্ধ করুন", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    description: String,
    time: String,
    category: String = "Announcements",
    isRead: Boolean = false,
    onClick: () -> Unit = {}
) {
    val categoryColor = when (category) {
        "Boost" -> Color(0xFFB45309)
        "Applications" -> Color(0xFF1D4ED8)
        "Security" -> Color(0xFFB91C1C)
        else -> QawmiDarkGreen
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isRead) Color(0xFFF7F9F8) else Color(0xFFEAF5F0),
                RoundedCornerShape(10.dp)
            )
            .border(
                width = if (isRead) 0.5.dp else 1.dp,
                color = if (isRead) Color(0xFFE0E8E4) else Color(0xFF8CCAB0),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = when (category) {
                "Boost" -> Icons.Default.AutoAwesome
                "Applications" -> Icons.Default.Assignment
                "Security" -> Icons.Default.Security
                else -> Icons.Default.NotificationsActive
            },
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFFE53935), CircleShape)
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = QawmiDarkGreen
                    )
                }
                Text(text = time, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = Color(0xFF4A6358),
                lineHeight = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = category,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "সংশ্লিষ্ট পেজে যান →",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
            }
        }
    }
}
