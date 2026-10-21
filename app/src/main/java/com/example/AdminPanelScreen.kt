package com.example

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Brand Theme Colors matching the Islamic Green, Mint, and Cream design
val AdminDarkGreen = Color(0xFF064B3D)
val AdminPrimaryGreen = Color(0xFF075747)
val AdminTeal = Color(0xFF087C6B)
val AdminMintBg = Color(0xFFE1F6EF)
val AdminLightMint = Color(0xFFD0F1E7)
val AdminCreamWhite = Color(0xFFF7F8F1)
val AdminTextDark = Color(0xFF075747)
val AdminTextMuted = Color(0xFF477F73)
val AdminDivider = Color(0xFFBFE6DA)
val AdminLogoutRed = Color(0xFFB4233A)
val AdminGoldAccent = Color(0xFFE2C98F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit,
    overrideEmailForTesting: String? = null
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AdminRepository.init(context)
    }

    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    val userEmail = overrideEmailForTesting ?: currentUser?.email ?: ""

    var selectedSection by remember { mutableStateOf("dashboard") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Intercept system back press: close mobile drawer first if open, otherwise exit Admin Panel
    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            onNavigateBack()
        }
    }

    // Strict Authorization Guard: Block unauthenticated or non-allowlisted users
    if (!AdminRepository.isUserAdmin(userEmail)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminCreamWhite)
                .padding(24.dp)
                .testTag("admin_access_denied_screen"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Denied",
                        tint = AdminLogoutRed,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "অনুমতি অস্বীকৃত (Access Denied)",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminLogoutRed,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    val displayIdentity = if (userEmail.isBlank()) "লগইন করা নেই (Unauthenticated)" else userEmail
                    Text(
                        text = "আপনার অ্যাকাউন্ট ($displayIdentity) দিয়ে Admin Panel ব্যবহার করার অনুমতি নেই। শুধুমাত্র অনুমোদিত Admin ব্যবহারকারীরা প্রবেশ করতে পারবেন।",
                        fontSize = 13.sp,
                        color = AdminTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                        modifier = Modifier.testTag("access_denied_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("← অ্যাপে ফিরে যান")
                    }
                }
            }
        }
        return
    }

    val adminRole = AdminRepository.getAdminRole(userEmail)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminCreamWhite)
            .testTag("admin_panel_root")
    ) {
        val isExpanded = maxWidth >= 720.dp

        if (isExpanded) {
            // Tablet / Desktop Layout: Permanent Sidebar on Left + Full Content on Right
            Scaffold(
                topBar = {
                    AdminTopBar(
                        userEmail = userEmail,
                        roleName = adminRole.displayName,
                        showMenuIcon = false,
                        onOpenSidebar = {},
                        onNavigateBack = onNavigateBack
                    )
                },
                containerColor = AdminCreamWhite
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .fillMaxHeight()
                            .background(AdminDarkGreen)
                            .testTag("admin_desktop_sidebar")
                    ) {
                        AdminSidebarContent(
                            selectedSection = selectedSection,
                            userEmail = userEmail,
                            roleName = adminRole.displayName,
                            onSelectSection = { selectedSection = it },
                            onNavigateBack = onNavigateBack
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(AdminCreamWhite)
                            .padding(16.dp)
                            .testTag("admin_content_container")
                    ) {
                        AdminSectionContentHost(
                            selectedSection = selectedSection,
                            isExpanded = true,
                            actorEmail = userEmail,
                            onNavigateSection = { selectedSection = it }
                        )
                    }
                }
            }
        } else {
            // Mobile Layout: Full-Width Content + Responsive ModalNavigationDrawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = AdminDarkGreen,
                        modifier = Modifier
                            .widthIn(max = 290.dp)
                            .fillMaxHeight()
                            .testTag("admin_mobile_drawer")
                    ) {
                        AdminSidebarContent(
                            selectedSection = selectedSection,
                            userEmail = userEmail,
                            roleName = adminRole.displayName,
                            onSelectSection = { section ->
                                selectedSection = section
                                coroutineScope.launch { drawerState.close() }
                            },
                            onNavigateBack = {
                                coroutineScope.launch { drawerState.close() }
                                onNavigateBack()
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        AdminTopBar(
                            userEmail = userEmail,
                            roleName = adminRole.displayName,
                            showMenuIcon = true,
                            onOpenSidebar = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onNavigateBack = onNavigateBack
                        )
                    },
                    containerColor = AdminCreamWhite
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(AdminCreamWhite)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("admin_content_container")
                    ) {
                        AdminSectionContentHost(
                            selectedSection = selectedSection,
                            isExpanded = false,
                            actorEmail = userEmail,
                            onNavigateSection = { selectedSection = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSectionContentHost(
    selectedSection: String,
    isExpanded: Boolean,
    actorEmail: String,
    onNavigateSection: (String) -> Unit
) {
    when (selectedSection) {
        "dashboard" -> AdminDashboardSection(isExpanded = isExpanded, onNavigateSection = onNavigateSection)
        "users" -> AdminUsersSection(actorEmail = actorEmail)
        "madrasahs" -> AdminMadrasahsSection(actorEmail = actorEmail)
        "notices" -> AdminNoticesSection(actorEmail = actorEmail)
        "applications" -> AdminApplicationsSection(actorEmail = actorEmail)
        "boost" -> AdminBoostSection(actorEmail = actorEmail)
        "notifications" -> AdminNotificationsSection(actorEmail = actorEmail)
        "forms" -> AdminFormBuilderSection(actorEmail = actorEmail)
        "content" -> AdminContentManagementSection(actorEmail = actorEmail)
        "telegram" -> AdminTelegramSection(actorEmail = actorEmail)
        "support" -> AdminSupportSection(actorEmail = actorEmail)
        "roles" -> AdminRolesSection(actorEmail = actorEmail)
        "audit" -> AdminAuditLogSection(actorEmail = actorEmail)
        else -> AdminDashboardSection(isExpanded = isExpanded, onNavigateSection = onNavigateSection)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(
    userEmail: String,
    roleName: String,
    showMenuIcon: Boolean,
    onOpenSidebar: () -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "কওমি কর্মসংস্থান — Admin",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$roleName (${userEmail.substringBefore("@")})",
                    color = AdminGoldAccent,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (showMenuIcon) {
                IconButton(
                    onClick = onOpenSidebar,
                    modifier = Modifier.testTag("admin_drawer_menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Admin Menu", tint = Color.White)
                }
            } else {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("admin_back_icon_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        actions = {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A4A)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, AdminGoldAccent.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("admin_exit_top_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit Admin Panel",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "← অ্যাপে ফিরে যান",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminDarkGreen)
    )
}

@Composable
fun AdminSidebarContent(
    selectedSection: String,
    userEmail: String,
    roleName: String,
    onSelectSection: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val items = listOf(
        Triple("dashboard", "ড্যাশবোর্ড", Icons.Default.Dashboard),
        Triple("users", "ব্যবহারকারী", Icons.Default.People),
        Triple("madrasahs", "মাদ্রাসা", Icons.Default.AccountBalance),
        Triple("notices", "খেদমতের বিজ্ঞপ্তি", Icons.Default.Work),
        Triple("applications", "আবেদন", Icons.Default.Assignment),
        Triple("boost", "Boost অনুরোধ", Icons.Default.RocketLaunch),
        Triple("notifications", "নোটিফিকেশন ও ঘোষণা", Icons.Default.Notifications),
        Triple("forms", "ফর্ম ম্যানেজমেন্ট", Icons.Default.EditNote),
        Triple("content", "কনটেন্ট ও সেটিংস", Icons.Default.Tune),
        Triple("telegram", "Telegram", Icons.Default.Send),
        Triple("support", "অভিযোগ ও সহায়তা", Icons.Default.HelpOutline),
        Triple("roles", "Admin ব্যবস্থাপনা", Icons.Default.AdminPanelSettings),
        Triple("audit", "অডিট লগ ও ব্যাকআপ", Icons.Default.History)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        // Sidebar Brand Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF145D4C))
                            .border(1.dp, AdminGoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mosque, contentDescription = null, tint = AdminGoldAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("কওমি কর্মসংস্থান বাংলাদেশ", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("দ্বীনি খেদমতে সুযোগ, আল্লাহর সন্তুষ্টি", color = Color(0xFFB2D8CD), fontSize = 10.5.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF0F5A4A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(userEmail, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(roleName, color = AdminGoldAccent, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFF125B4B), modifier = Modifier.padding(vertical = 6.dp))

        items.forEach { (id, label, icon) ->
            val isSelected = selectedSection == id
            val itemBg = if (isSelected) Color(0xFF0F5A4A) else Color.Transparent
            val itemTint = if (isSelected) AdminGoldAccent else Color(0xFFCCE4DC)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(itemBg)
                    .clickable { onSelectSection(id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("admin_nav_$id"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = itemTint, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color(0xFFD6EBE4),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        HorizontalDivider(color = Color(0xFF125B4B), modifier = Modifier.padding(vertical = 10.dp))

        // Exit Admin Panel Item at bottom of Sidebar/Drawer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF8A1C2E))
                .clickable { onNavigateBack() }
                .padding(horizontal = 12.dp, vertical = 11.dp)
                .testTag("admin_exit_sidebar_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Exit Admin",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "← মূল অ্যাপে ফিরে যান",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// 1. DASHBOARD SECTION (Responsive + Real Repository Statistics + Fixed Chart)
// ============================================================================
@Composable
fun AdminDashboardSection(
    isExpanded: Boolean,
    onNavigateSection: (String) -> Unit
) {
    val users by AdminRepository.usersList.collectAsState()
    val madrasahs by AdminRepository.madrasahsList.collectAsState()
    val notices by AdminRepository.noticesList.collectAsState()
    val applications by AdminRepository.applicationsList.collectAsState()
    val boosts by AdminRepository.boostRequests.collectAsState()
    val sentNotifs by AdminRepository.sentNotifications.collectAsState()
    val auditLogs by AdminRepository.auditLogs.collectAsState()

    val dhakaCal = remember { DateUtils.getDhakaCalendar() }
    val todayFormatted = remember {
        "আজ: ${DateUtils.getEnglishDate(dhakaCal)}, ${DateUtils.getWeekday(dhakaCal)} (${DateUtils.getBengaliDate(dhakaCal)})"
    }

    val activeUsersCount = users.count { it.status == "সক্রিয়" }
    val verifiedMadrasahsCount = madrasahs.count { it.verificationStatus == "যাচাইকৃত" }
    val approvedNoticesCount = notices.count { it.status == "অনুমোদিত" }

    val newAppsCount = applications.count { it.status == "নতুন" }
    val processingAppsCount = applications.count { it.status == "প্রক্রিয়াধীন" }
    val approvedAppsCount = applications.count { it.status == "অনুমোদিত" }
    val rejectedAppsCount = applications.count { it.status == "প্রত্যাখ্যাত" }

    val pendingBoostsCount = boosts.count { it.status == "Pending" || it.status == "Under Review" }
    val activeBoostsCount = boosts.count { it.status == "Active" || it.status == "Approved" }

    val sentNotifCount = sentNotifs.count { it.status == "প্রেরিত" }
    val failedNotifCount = sentNotifs.count { it.status == "ব্যর্থ" }

    val activityPoints = remember(users.size, applications.size, notices.size) {
        AdminRepository.getLast7DaysActivity()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_dashboard_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row with Real Live Date & Firebase Cloud Sync Status
        val syncStatus by FirebaseSyncManager.lastSyncStatus.collectAsState()
        val isCloudConnected by FirebaseSyncManager.isConnectedToFirestore.collectAsState()
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ড্যাশবোর্ড",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminPrimaryGreen
                )
                Surface(
                    color = if (isCloudConnected) Color(0xFFD0F1E7) else Color(0xFFE8F2EE),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AdminDivider)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (isCloudConnected) Color(0xFF2E7D32) else AdminTeal,
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = syncStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AdminPrimaryGreen
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "স্বাগতম, প্রধান Admin! • $todayFormatted",
                fontSize = 12.sp,
                color = AdminTextMuted
            )
        }

        // 4 Summary Stat Cards: 2x2 Grid on Mobile, 4-Column Row on Tablet/Desktop
        if (isExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard(
                    title = "মোট ব্যবহারকারী",
                    count = DateUtils.toBengaliDigits(users.size),
                    subtext = "${DateUtils.toBengaliDigits(activeUsersCount)} জন সক্রিয়",
                    icon = Icons.Default.People,
                    bgColor = Color(0xFFE3EFF9),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection("users") }
                )
                SummaryStatCard(
                    title = "মোট মাদ্রাসা",
                    count = DateUtils.toBengaliDigits(madrasahs.size),
                    subtext = "${DateUtils.toBengaliDigits(verifiedMadrasahsCount)} টি যাচাইকৃত",
                    icon = Icons.Default.AccountBalance,
                    bgColor = Color(0xFFE1F6EF),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection("madrasahs") }
                )
                SummaryStatCard(
                    title = "মোট বিজ্ঞপ্তি",
                    count = DateUtils.toBengaliDigits(notices.size),
                    subtext = "${DateUtils.toBengaliDigits(approvedNoticesCount)} টি প্রকাশিত",
                    icon = Icons.Default.Work,
                    bgColor = Color(0xFFF3E5F5),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection("notices") }
                )
                SummaryStatCard(
                    title = "মোট আবেদন",
                    count = DateUtils.toBengaliDigits(applications.size),
                    subtext = "${DateUtils.toBengaliDigits(approvedAppsCount)} টি অনুমোদিত",
                    icon = Icons.Default.Assignment,
                    bgColor = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection("applications") }
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "মোট ব্যবহারকারী",
                        count = DateUtils.toBengaliDigits(users.size),
                        subtext = "${DateUtils.toBengaliDigits(activeUsersCount)} জন সক্রিয়",
                        icon = Icons.Default.People,
                        bgColor = Color(0xFFE3EFF9),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection("users") }
                    )
                    SummaryStatCard(
                        title = "মোট মাদ্রাসা",
                        count = DateUtils.toBengaliDigits(madrasahs.size),
                        subtext = "${DateUtils.toBengaliDigits(verifiedMadrasahsCount)} টি যাচাইকৃত",
                        icon = Icons.Default.AccountBalance,
                        bgColor = Color(0xFFE1F6EF),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection("madrasahs") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "মোট বিজ্ঞপ্তি",
                        count = DateUtils.toBengaliDigits(notices.size),
                        subtext = "${DateUtils.toBengaliDigits(approvedNoticesCount)} টি প্রকাশিত",
                        icon = Icons.Default.Work,
                        bgColor = Color(0xFFF3E5F5),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection("notices") }
                    )
                    SummaryStatCard(
                        title = "মোট আবেদন",
                        count = DateUtils.toBengaliDigits(applications.size),
                        subtext = "${DateUtils.toBengaliDigits(approvedAppsCount)} টি অনুমোদিত",
                        icon = Icons.Default.Assignment,
                        bgColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection("applications") }
                    )
                }
            }
        }

        // Breakdown Cards: Stacked on Mobile, Side-by-Side on Desktop
        if (isExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ApplicationBreakdownCard(
                    newCount = newAppsCount,
                    processingCount = processingAppsCount,
                    approvedCount = approvedAppsCount,
                    rejectedCount = rejectedAppsCount,
                    modifier = Modifier.weight(1.6f)
                )
                BoostBreakdownCard(
                    pendingCount = pendingBoostsCount,
                    activeCount = activeBoostsCount,
                    modifier = Modifier.weight(1f)
                )
                NotificationBreakdownCard(
                    sentCount = sentNotifCount,
                    failedCount = failedNotifCount,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            ApplicationBreakdownCard(
                newCount = newAppsCount,
                processingCount = processingAppsCount,
                approvedCount = approvedAppsCount,
                rejectedCount = rejectedAppsCount,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BoostBreakdownCard(
                    pendingCount = pendingBoostsCount,
                    activeCount = activeBoostsCount,
                    modifier = Modifier.weight(1f)
                )
                NotificationBreakdownCard(
                    sentCount = sentNotifCount,
                    failedCount = failedNotifCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7-Day Activity Chart Card (Properly Sized & Responsive)
        AdminActivityChartCard(activityPoints = activityPoints)

        // Recent Activity & Quick Actions
        if (isExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RecentActivityCard(
                    logs = auditLogs.take(5),
                    modifier = Modifier.weight(1.4f),
                    onViewAll = { onNavigateSection("audit") }
                )
                QuickActionsCard(
                    onNavigateSection = onNavigateSection,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            RecentActivityCard(
                logs = auditLogs.take(5),
                modifier = Modifier.fillMaxWidth(),
                onViewAll = { onNavigateSection("audit") }
            )
            QuickActionsCard(
                onNavigateSection = onNavigateSection,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ApplicationBreakdownCard(
    newCount: Int,
    processingCount: Int,
    approvedCount: Int,
    rejectedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("আবেদন (বিস্তারিত)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeBox("নতুন", DateUtils.toBengaliDigits(newCount), Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f))
                BadgeBox("প্রক্রিয়াধীন", DateUtils.toBengaliDigits(processingCount), Color(0xFFFFF8E1), Color(0xFFF57F17), Modifier.weight(1f))
                BadgeBox("অনুমোদিত", DateUtils.toBengaliDigits(approvedCount), Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f))
                BadgeBox("প্রত্যাখ্যাত", DateUtils.toBengaliDigits(rejectedCount), Color(0xFFFFEBEE), Color(0xFFC62828), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BoostBreakdownCard(
    pendingCount: Int,
    activeCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Boost (বিস্তারিত)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeBox("অপেক্ষমাণ", DateUtils.toBengaliDigits(pendingCount), Color(0xFFFFF3E0), Color(0xFFE65100), Modifier.weight(1f))
                BadgeBox("সক্রিয়", DateUtils.toBengaliDigits(activeCount), Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NotificationBreakdownCard(
    sentCount: Int,
    failedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("নোটিফিকেশন", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeBox("মোট পাঠানো", DateUtils.toBengaliDigits(sentCount), Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f))
                BadgeBox("ব্যর্থ", DateUtils.toBengaliDigits(failedCount), Color(0xFFFFEBEE), Color(0xFFC62828), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AdminActivityChartCard(activityPoints: List<DailyActivityPoint>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_chart_card")
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "গত ৭ দিনের পরিসংখ্যান (প্রকৃত রেকর্ড ভিত্তিক)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AdminPrimaryGreen
            )
            Spacer(Modifier.height(8.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = Color(0xFF1976D2), label = "ব্যবহারকারী")
                ChartLegendItem(color = Color(0xFFE65100), label = "আবেদন")
                ChartLegendItem(color = Color(0xFF2E7D32), label = "বিজ্ঞপ্তি")
            }

            Spacer(Modifier.height(14.dp))

            if (activityPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("পরিসংখ্যানের জন্য পর্যাপ্ত ডেটা নেই", color = AdminTextMuted, fontSize = 12.sp)
                }
            } else {
                val maxVal = remember(activityPoints) {
                    activityPoints.maxOf {
                        maxOf(it.usersCount, it.applicationsCount, it.noticesCount)
                    }.coerceAtLeast(4)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Y-Axis Labels
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 8.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(DateUtils.toBengaliDigits(maxVal), fontSize = 10.sp, color = AdminTextMuted)
                        Text(DateUtils.toBengaliDigits(maxVal / 2), fontSize = 10.sp, color = AdminTextMuted)
                        Text("০", fontSize = 10.sp, color = AdminTextMuted)
                    }

                    // Plot Area + X-Axis Labels
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("admin_trend_canvas")
                        ) {
                            val w = size.width
                            val h = size.height
                            if (w <= 1f || h <= 1f) return@Canvas

                            val topPad = 10.dp.toPx()
                            val bottomPad = 10.dp.toPx()
                            val plotH = (h - topPad - bottomPad).coerceAtLeast(10f)

                            // Horizontal grid lines
                            listOf(0f, 0.5f, 1f).forEach { fraction ->
                                val y = topPad + plotH * fraction
                                drawLine(
                                    color = Color(0xFFE0ECE7),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            fun drawSeries(values: List<Int>, lineColor: Color) {
                                if (values.isEmpty()) return
                                val stepX = if (values.size > 1) w / (values.size - 1) else w
                                val path = Path()
                                val coords = values.mapIndexed { idx, v ->
                                    val ratio = (v.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                                    val x = idx * stepX
                                    val y = topPad + plotH * (1f - ratio)
                                    Offset(x, y)
                                }
                                coords.forEachIndexed { i, pt ->
                                    if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                                }
                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                )
                                coords.forEach { pt ->
                                    drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = pt)
                                    drawCircle(color = lineColor, radius = 3.dp.toPx(), center = pt)
                                }
                            }

                            drawSeries(activityPoints.map { it.usersCount }, Color(0xFF1976D2))
                            drawSeries(activityPoints.map { it.applicationsCount }, Color(0xFFE65100))
                            drawSeries(activityPoints.map { it.noticesCount }, Color(0xFF2E7D32))
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            activityPoints.forEach { pt ->
                                Text(
                                    text = pt.dateLabel,
                                    fontSize = 9.5.sp,
                                    color = AdminTextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.5.sp, color = AdminTextDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecentActivityCard(
    logs: List<AuditLogEntry>,
    modifier: Modifier = Modifier,
    onViewAll: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("সাম্প্রতিক কার্যক্রম", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                TextButton(onClick = onViewAll) {
                    Text("সব দেখুন", fontSize = 12.sp, color = AdminTeal)
                }
            }
            Spacer(Modifier.height(6.dp))
            logs.forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AdminMintBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AdminPrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.action, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = AdminPrimaryGreen)
                        Text("${log.user} • ${log.time}", fontSize = 10.5.sp, color = AdminTextMuted)
                    }
                }
                HorizontalDivider(color = AdminDivider.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onNavigateSection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Quick Actions (দ্রুত অ্যাকশন)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
            Spacer(Modifier.height(2.dp))

            QuickActionButton(
                text = "নতুন বিজ্ঞপ্তি তৈরি",
                icon = Icons.Default.AddBusiness,
                onClick = { onNavigateSection("notices") }
            )
            QuickActionButton(
                text = "নতুন মাদ্রাসা যোগ",
                icon = Icons.Default.AccountBalance,
                onClick = { onNavigateSection("madrasahs") }
            )
            QuickActionButton(
                text = "ফর্ম তৈরি/সম্পাদনা",
                icon = Icons.Default.EditNote,
                onClick = { onNavigateSection("forms") }
            )
            QuickActionButton(
                text = "নোটিফিকেশন পাঠান",
                icon = Icons.Default.NotificationsActive,
                onClick = { onNavigateSection("notifications") }
            )
            QuickActionButton(
                text = "Admin যোগ করুন",
                icon = Icons.Default.PersonAdd,
                onClick = { onNavigateSection("roles") }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AdminGoldAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    count: String,
    subtext: String,
    icon: ImageVector,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() }
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AdminPrimaryGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    color = AdminTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = count,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminPrimaryGreen,
                    maxLines = 1
                )
                Text(
                    text = subtext,
                    fontSize = 10.5.sp,
                    color = Color(0xFF2E7D32),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BadgeBox(
    title: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 10.5.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1
        )
    }
}

// ============================================================================
// 2. USER MANAGEMENT SECTION
// ============================================================================
@Composable
fun AdminUsersSection(actorEmail: String) {
    val users by AdminRepository.usersList.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("সব") }
    var roleFilter by remember { mutableStateOf("সব") }
    var showAddUserDialog by remember { mutableStateOf(false) }

    val filteredUsers = users.filter { user ->
        val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) ||
            user.email.contains(searchQuery, ignoreCase = true) ||
            user.phone.contains(searchQuery)
        val matchesStatus = statusFilter == "সব" || user.status == statusFilter
        val matchesRole = roleFilter == "সব" || user.role == roleFilter
        matchesSearch && matchesStatus && matchesRole
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_users_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ব্যবহারকারী ব্যবস্থাপনা", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
            Button(
                onClick = { showAddUserDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("+ নতুন ব্যবহারকারী", fontSize = 12.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("নাম, ইমেইল বা ফোন নম্বর দিয়ে খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_user_search_input")
                )

                Spacer(Modifier.height(10.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("সব", "সক্রিয়", "নিষ্ক্রিয়").forEach { st ->
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = st },
                            label = { Text("স্ট্যাটাস: $st", fontSize = 12.sp) }
                        )
                    }
                    listOf("সব", "আবেদনকারী", "মাদ্রাসা প্রতিনিধি").forEach { rl ->
                        FilterChip(
                            selected = roleFilter == rl,
                            onClick = { roleFilter = rl },
                            label = { Text("ভূমিকা: $rl", fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "মোট ${DateUtils.toBengaliDigits(filteredUsers.size)} জন ব্যবহারকারী পাওয়া গেছে",
                    fontSize = 12.sp,
                    color = AdminTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))

                filteredUsers.forEachIndexed { idx, user ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${DateUtils.toBengaliDigits(idx + 1)}. ${user.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AdminPrimaryGreen
                                )
                                Text(
                                    text = "${user.email} • ${user.phone}",
                                    fontSize = 12.sp,
                                    color = AdminTextMuted
                                )
                                Text(
                                    text = "ভূমিকা: ${user.role}",
                                    fontSize = 11.5.sp,
                                    color = AdminTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val nextRole = if (user.role == "আবেদনকারী") "মাদ্রাসা প্রতিনিধি" else "আবেদনকারী"
                                        AdminRepository.updateUserRole(user.id, nextRole, actorEmail)
                                        Toast.makeText(context, "ভূমিকা '$nextRole'-এ পরিবর্তন হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("রোল বদল", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        AdminRepository.toggleUserStatus(user.id, actorEmail)
                                        Toast.makeText(context, "স্ট্যাটাস আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (user.status == "সক্রিয়") Color(0xFF2E7D32) else AdminLogoutRed
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("toggle_user_status_${user.id}")
                                ) {
                                    Text(user.status, fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }

    if (showAddUserDialog) {
        var newName by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newRole by remember { mutableStateOf("আবেদনকারী") }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("নতুন ব্যবহারকারী যোগ করুন", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("পূর্ণ নাম") }, singleLine = true)
                    OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("ইমেইল") }, singleLine = true)
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("মোবাইল নম্বর") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("আবেদনকারী", "মাদ্রাসা প্রতিনিধি").forEach { r ->
                            FilterChip(selected = newRole == r, onClick = { newRole = r }, label = { Text(r, fontSize = 12.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newEmail.contains("@") && newPhone.length >= 10) {
                            AdminRepository.addUser(newName, newEmail, newPhone, newRole, actorEmail)
                            showAddUserDialog = false
                            Toast.makeText(context, "নতুন ব্যবহারকারী যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "সঠিক নাম, ইমেইল ও ফোন নম্বর দিন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

// ============================================================================
// 3. MADRASAH MANAGEMENT SECTION
// ============================================================================
@Composable
fun AdminMadrasahsSection(actorEmail: String) {
    val madrasahs by AdminRepository.madrasahsList.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("সব") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = madrasahs.filter { m ->
        val matchesSearch = m.name.contains(searchQuery, ignoreCase = true) ||
            m.location.contains(searchQuery, ignoreCase = true) ||
            m.regNumber.contains(searchQuery, ignoreCase = true)
        val matchesStatus = statusFilter == "সব" || m.verificationStatus == statusFilter
        matchesSearch && matchesStatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_madrasahs_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("মাদ্রাসা ব্যবস্থাপনা ও যাচাইকরণ", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("মাদ্রাসা যোগ", fontSize = 12.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("মাদ্রাসার নাম, জেলা বা নিবন্ধন নম্বর দিয়ে খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("সব", "যাচাইকৃত", "অপেক্ষমাণ", "প্রত্যাখ্যাত").forEach { st ->
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = st },
                            label = { Text(st, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                filtered.forEachIndexed { idx, m ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "${DateUtils.toBengaliDigits(idx + 1)}. ${m.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = AdminPrimaryGreen
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${m.location} • নিবন্ধন: ${m.regNumber} • ফোন: ${m.phone}",
                            fontSize = 12.sp,
                            color = AdminTextMuted
                        )
                        Text(
                            text = "মুহতামিম: ${m.muhtamim} • বিভাগ: ${m.departments}",
                            fontSize = 11.5.sp,
                            color = AdminTeal
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeColor = when (m.verificationStatus) {
                                "যাচাইকৃত" -> Color(0xFF2E7D32)
                                "অপেক্ষমাণ" -> Color(0xFFF57F17)
                                else -> AdminLogoutRed
                            }
                            Surface(
                                color = badgeColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = m.verificationStatus,
                                    color = badgeColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (m.verificationStatus != "যাচাইকৃত") {
                                Button(
                                    onClick = {
                                        AdminRepository.updateMadrasahStatus(m.id, "যাচাইকৃত", actorEmail)
                                        Toast.makeText(context, "মাদ্রাসা যাচাইকৃত হিসেবে অনুমোদিত হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("যাচাই অনুমোদন", fontSize = 11.sp)
                                }
                            }
                            if (m.verificationStatus != "প্রত্যাখ্যাত") {
                                OutlinedButton(
                                    onClick = {
                                        AdminRepository.updateMadrasahStatus(m.id, "প্রত্যাখ্যাত", actorEmail)
                                        Toast.makeText(context, "মাদ্রাসা প্রত্যাখ্যাত তালিকাভুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("প্রত্যাখ্যান", fontSize = 11.sp, color = AdminLogoutRed)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var loc by remember { mutableStateOf("") }
        var muhtamim by remember { mutableStateOf("") }
        var depts by remember { mutableStateOf("দাওরায়ে হাদীস, হিফজ") }
        var phone by remember { mutableStateOf("") }
        var reg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন মাদ্রাসা যুক্ত করুন", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("মাদ্রাসার নাম") }, singleLine = true)
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("ঠিকানা / জেলা") }, singleLine = true)
                    OutlinedTextField(value = muhtamim, onValueChange = { muhtamim = it }, label = { Text("মুহতামিমের নাম") }, singleLine = true)
                    OutlinedTextField(value = depts, onValueChange = { depts = it }, label = { Text("বিভাগসমূহ") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("যোগাযোগের নম্বর") }, singleLine = true)
                    OutlinedTextField(value = reg, onValueChange = { reg = it }, label = { Text("নিবন্ধন নম্বর") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && loc.isNotBlank() && phone.isNotBlank()) {
                            AdminRepository.addMadrasah(name, loc, muhtamim.ifBlank { "মুহতামিম" }, depts, phone, reg.ifBlank { "REG-NEW" }, actorEmail)
                            showAddDialog = false
                            Toast.makeText(context, "নতুন মাদ্রাসা যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "মাদ্রাসার নাম, ঠিকানা ও ফোন নম্বর আবশ্যক", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("যুক্ত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

// ============================================================================
// 4. KHIDMAT NOTICE MANAGEMENT SECTION
// ============================================================================
@Composable
fun AdminNoticesSection(actorEmail: String) {
    val notices by AdminRepository.noticesList.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showCreateNoticeDialog by remember { mutableStateOf(false) }

    val filtered = notices.filter {
        it.madrasahName.contains(searchQuery, ignoreCase = true) ||
            it.roleTitle.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_notices_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("খেদমতের বিজ্ঞপ্তি ব্যবস্থাপনা", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
            Button(
                onClick = { showCreateNoticeDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("+ নতুন বিজ্ঞপ্তি", fontSize = 12.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("পদের নাম, মাদ্রাসা বা জেলা দিয়ে খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                filtered.forEachIndexed { idx, notice ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${DateUtils.toBengaliDigits(idx + 1)}. ${notice.roleTitle}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = AdminPrimaryGreen,
                                modifier = Modifier.weight(1f)
                            )
                            if (notice.isBoosted) {
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE65100))
                                ) {
                                    Text(
                                        text = "🚀 Boosted",
                                        color = Color(0xFFE65100),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${notice.madrasahName} • ${notice.location}",
                            fontSize = 12.5.sp,
                            color = AdminTextDark,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "হাদিয়া/বেতন: ${notice.salaryInfo} • শেষ তারিখ: ${notice.deadline} • ফোন: ${notice.phone}",
                            fontSize = 11.5.sp,
                            color = AdminTextMuted
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    AdminRepository.updateNoticeStatus(notice.postId, "অনুমোদিত", actorEmail)
                                    Toast.makeText(context, "বিজ্ঞপ্তি অনুমোদিত হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (notice.status == "অনুমোদিত") Color(0xFF2E7D32) else AdminTeal
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(notice.status, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    AdminRepository.deleteNotice(notice.postId, actorEmail)
                                    Toast.makeText(context, "বিজ্ঞপ্তি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("মুছে ফেলুন", fontSize = 11.sp, color = AdminLogoutRed)
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }

    if (showCreateNoticeDialog) {
        var madrasah by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }
        var loc by remember { mutableStateOf("") }
        var qual by remember { mutableStateOf("দাওরায়ে হাদীস") }
        var salary by remember { mutableStateOf("১৫,০০০ - ২০,০০০ টাকা") }
        var deadline by remember { mutableStateOf("৩০ নভেম্বর ২০২৬") }
        var phone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateNoticeDialog = false },
            title = { Text("নতুন খেদমত বিজ্ঞপ্তি প্রকাশ", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = madrasah, onValueChange = { madrasah = it }, label = { Text("মাদ্রাসার নাম") }, singleLine = true)
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("পদের নাম (যেমন: মুহাদ্দিস)") }, singleLine = true)
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("জেলা ও থানা") }, singleLine = true)
                    OutlinedTextField(value = qual, onValueChange = { qual = it }, label = { Text("শিক্ষাগত যোগ্যতা") }, singleLine = true)
                    OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("বেতন / হাদিয়া") }, singleLine = true)
                    OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("আবেদনের শেষ তারিখ") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("যোগাযোগ নম্বর") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (madrasah.isNotBlank() && role.isNotBlank() && phone.isNotBlank()) {
                            AdminRepository.addKhidmatNotice(madrasah, role, loc.ifBlank { "ঢাকা" }, qual, salary, deadline, phone, actorEmail)
                            showCreateNoticeDialog = false
                            Toast.makeText(context, "বিজ্ঞপ্তি মূল ফিডে প্রকাশিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "মাদ্রাসার নাম, পদ ও ফোন নম্বর আবশ্যক", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("প্রকাশ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoticeDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

// ============================================================================
// 5. APPLICATION MANAGEMENT SECTION
// ============================================================================
@Composable
fun AdminApplicationsSection(actorEmail: String) {
    val applications by AdminRepository.applicationsList.collectAsState()
    val context = LocalContext.current
    var statusFilter by remember { mutableStateOf("সব") }

    val filtered = applications.filter {
        statusFilter == "সব" || it.status == statusFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_applications_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("আবেদন ব্যবস্থাপনা (Applications)", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("সব", "নতুন", "প্রক্রিয়াধীন", "অনুমোদিত", "প্রত্যাখ্যাত").forEach { st ->
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = st },
                            label = { Text(st, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                filtered.forEachIndexed { idx, app ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "${DateUtils.toBengaliDigits(idx + 1)}. ${app.candidateName} — ${app.roleTitle}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = AdminPrimaryGreen
                        )
                        Text(
                            text = "যোগ্যতা: ${app.qualification} • অভিজ্ঞতা: ${app.experience}",
                            fontSize = 12.sp,
                            color = AdminTextMuted
                        )
                        Text(
                            text = "জেলা: ${app.location} • মোবাইল: ${app.phone} • স্ট্যাটাস: ${app.status}",
                            fontSize = 11.5.sp,
                            color = AdminTeal,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("প্রক্রিয়াধীন", "অনুমোদিত", "প্রত্যাখ্যাত").forEach { st ->
                                val isCurrent = app.status == st
                                OutlinedButton(
                                    onClick = {
                                        AdminRepository.updateApplicationStatus(app.postId, st, actorEmail)
                                        Toast.makeText(context, "আবেদনের স্ট্যাটাস '$st' করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isCurrent) AdminMintBg else Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(st, fontSize = 11.sp, color = AdminPrimaryGreen, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }
}

// ============================================================================
// 6. BOOST REQUEST MANAGEMENT SECTION
// ============================================================================
@Composable
fun AdminBoostSection(actorEmail: String) {
    val boostList by AdminRepository.boostRequests.collectAsState()
    val notices by AdminRepository.noticesList.collectAsState()
    val boostPolicy by AdminRepository.boostPolicy.collectAsState()
    val context = LocalContext.current
    var statusFilter by remember { mutableStateOf("সব") }
    var selectedDurationDays by remember { mutableStateOf(7) }
    var showNewBoostDialog by remember { mutableStateOf(false) }
    var showPolicyEditor by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AdminRepository.checkExpiredBoosts()
    }

    val filtered = boostList.filter { statusFilter == "সব" || it.status == statusFilter }

    // Calculate Boost vs Normal Performance Analytics
    val boostedNotices = notices.filter { it.isBoosted }
    val normalNotices = notices.filter { !it.isBoosted }
    val boostedViews = boostedNotices.sumOf { it.viewsCount } + boostList.sumOf { it.viewsCount }
    val boostedClicks = boostedNotices.sumOf { it.clicksCount } + boostList.sumOf { it.clicksCount }
    val boostedApps = boostedNotices.sumOf { it.applicationsCount } + boostList.sumOf { it.applicationsCount }
    val normalViews = normalNotices.sumOf { it.viewsCount }
    val normalClicks = normalNotices.sumOf { it.clicksCount }
    val normalApps = normalNotices.sumOf { it.applicationsCount }

    val activeBoostCount = boostList.count { it.status == "Active" || it.status == "Approved" }
    val pendingBoostCount = boostList.count { it.status == "Pending" || it.status == "Under Review" }
    val pausedBoostCount = boostList.count { it.status == "Paused" }
    val expiredBoostCount = boostList.count { it.status == "Expired" || it.status == "Cancelled" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_boost_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Boost ও প্রচার ব্যবস্থাপনা", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { showPolicyEditor = !showPolicyEditor },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("boost_policy_toggle_btn")
                ) {
                    Text(if (showPolicyEditor) "নীতিমালা লুকান" else "Boost নীতিমালা", fontSize = 11.5.sp, color = AdminPrimaryGreen)
                }
                Button(
                    onClick = { showNewBoostDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("new_boost_request_btn")
                ) {
                    Text("+ নতুন Boost", fontSize = 11.5.sp)
                }
            }
        }

        // 1. Boost Analytics & Performance Comparison Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📊 Boost অ্যানালিটিক্স ও পারফরম্যান্স তুলনা",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AdminPrimaryGreen
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(DateUtils.toBengaliDigits(boostList.size), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AdminPrimaryGreen)
                            Text("মোট আবেদন", fontSize = 10.5.sp, color = AdminTextMuted)
                        }
                    }
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(DateUtils.toBengaliDigits(activeBoostCount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE65100))
                            Text("সক্রিয় Boost", fontSize = 10.5.sp, color = AdminTextMuted)
                        }
                    }
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(DateUtils.toBengaliDigits(pendingBoostCount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1565C0))
                            Text("অপেক্ষমাণ", fontSize = 10.5.sp, color = AdminTextMuted)
                        }
                    }
                    Surface(
                        color = Color(0xFFFCE4EC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(DateUtils.toBengaliDigits(pausedBoostCount + expiredBoostCount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFC62828))
                            Text("বিরতি/মেয়াদোত্তীর্ণ", fontSize = 10.5.sp, color = AdminTextMuted)
                        }
                    }
                }

                HorizontalDivider(color = AdminDivider)

                // Boosted vs Normal comparison table
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🚀 বিশেষভাবে প্রচারিত (Boosted)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                        Text(
                            "👁 ভিউ: ${DateUtils.toBengaliDigits(boostedViews)} • 👆 ক্লিক: ${DateUtils.toBengaliDigits(boostedClicks)} • 📝 আবেদন: ${DateUtils.toBengaliDigits(boostedApps)}",
                            fontSize = 11.sp,
                            color = AdminTextDark
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("📄 সাধারণ বিজ্ঞপ্তি (Normal)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminPrimaryGreen)
                        Text(
                            "👁 ভিউ: ${DateUtils.toBengaliDigits(normalViews)} • 👆 ক্লিক: ${DateUtils.toBengaliDigits(normalClicks)} • 📝 আবেদন: ${DateUtils.toBengaliDigits(normalApps)}",
                            fontSize = 11.sp,
                            color = AdminTextDark
                        )
                    }
                }
            }
        }

        // 2. Boost Policy & Frequency Control Editor (Collapsible)
        if (showPolicyEditor) {
            var maxActiveInput by remember(boostPolicy) { mutableStateOf(boostPolicy.maxConcurrentBoosts.toString()) }
            var maxPerUserInput by remember(boostPolicy) { mutableStateOf(boostPolicy.maxActivePerUser.toString()) }
            var freqHoursInput by remember(boostPolicy) { mutableStateOf(boostPolicy.notificationFrequencyLimitHours.toString()) }

            Card(
                colors = CardDefaults.cardColors(containerColor = AdminMintBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "⚙️ Boost নীতিমালা ও নোটিফিকেশন ফ্রিকোয়েন্সি নিয়ন্ত্রণ (Firebase Synced)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = AdminPrimaryGreen
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = maxActiveInput,
                            onValueChange = { maxActiveInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("সর্বোচ্চ সক্রিয় Boost", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxPerUserInput,
                            onValueChange = { maxPerUserInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("প্রতি ইউজার সীমা", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = freqHoursInput,
                            onValueChange = { freqHoursInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("নোটিফিকেশন বিরতি (ঘণ্টা)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            AdminRepository.updateBoostPolicy(
                                maxConcurrentBoosts = maxActiveInput.toIntOrNull() ?: 5,
                                maxActivePerUser = maxPerUserInput.toIntOrNull() ?: 2,
                                notificationFrequencyLimitHours = freqHoursInput.toIntOrNull() ?: 24,
                                actorEmail = actorEmail
                            )
                            Toast.makeText(context, "Boost নীতিমালা সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                            showPolicyEditor = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("নীতিমালা সংরক্ষণ করুন", fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Honest Boost Process Info Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = AdminMintBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = "Boost প্রক্রিয়া ও মেয়াদ নির্দেশিকা:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AdminPrimaryGreen
                )
                Text(
                    text = "• অনুমোদিত বিজ্ঞপ্তিগুলো নির্ধারিত মেয়াদের জন্য Feed-এর শীর্ষে Featured Section-এ এবং '🚀 বিশেষভাবে প্রচারিত' ব্যাজসহ প্রদর্শিত হবে।\n• মেয়াদ শেষ হলে বা Cancel করলে স্বয়ংক্রিয়ভাবে সাধারণ Feed-এ ফিরে যাবে। ভুয়া বা অননুমোদিত বিজ্ঞপ্তি Boost করা যাবে না।",
                    fontSize = 11.5.sp,
                    color = AdminTextDark
                )
            }
        }

        // 4. Boost Requests Management List
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("অনুমোদনের জন্য ডিফল্ট মেয়াদ নির্ধারণ করুন:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 7, 14, 30).forEach { days ->
                        FilterChip(
                            selected = selectedDurationDays == days,
                            onClick = { selectedDurationDays = days },
                            label = { Text("${DateUtils.toBengaliDigits(days)} দিন", fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("সব", "Pending", "Under Review", "Approved", "Active", "Paused", "Rejected", "Expired", "Cancelled").forEach { st ->
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (filtered.isEmpty()) {
                    Text(
                        text = "এই ফিল্টারে কোনো Boost আবেদন পাওয়া যায়নি।",
                        fontSize = 12.5.sp,
                        color = AdminTextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                filtered.forEachIndexed { idx, boost ->
                    val linkedNotice = notices.find { it.postId == boost.noticeId || it.roleTitle == boost.title }
                    val isNoticeValid = linkedNotice == null || linkedNotice.status == "অনুমোদিত"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${DateUtils.toBengaliDigits(idx + 1)}. ${boost.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AdminPrimaryGreen,
                                modifier = Modifier.weight(1f)
                            )
                            val stColor = when (boost.status) {
                                "Active", "Approved" -> Color(0xFF2E7D32)
                                "Paused" -> Color(0xFF1565C0)
                                "Pending", "Under Review" -> Color(0xFFE65100)
                                else -> AdminLogoutRed
                            }
                            Surface(
                                color = stColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = boost.status,
                                    color = stColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "আবেদনকারী: ${boost.requestedBy} • তারিখ: ${boost.requestDate} • মেয়াদ: ${DateUtils.toBengaliDigits(boost.durationDays)} দিন • টার্গেট: ${boost.targetScope}",
                            fontSize = 11.5.sp,
                            color = AdminTextMuted
                        )
                        Text(
                            text = "📊 পারফরম্যান্স: 👁 ভিউ ${DateUtils.toBengaliDigits(boost.viewsCount)} • 👆 ক্লিক ${DateUtils.toBengaliDigits(boost.clicksCount)} • 📝 আবেদন ${DateUtils.toBengaliDigits(boost.applicationsCount)}",
                            fontSize = 11.sp,
                            color = AdminPrimaryGreen,
                            fontWeight = FontWeight.Medium
                        )
                        if (!isNoticeValid) {
                            Text(
                                text = "⚠️ সতর্কবার্তা: সংশ্লিষ্ট বিজ্ঞপ্তিটি এখনো অনুমোদিত নয় (${linkedNotice?.status})। অনুমোদন ছাড়া Boost চালু করা যাবে না।",
                                fontSize = 11.sp,
                                color = AdminLogoutRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Approve / Active Button
                            Button(
                                onClick = {
                                    if (!isNoticeValid) {
                                        Toast.makeText(context, "ভুয়া বা অননুমোদিত বিজ্ঞপ্তি Boost করা যাবে না! আগে বিজ্ঞপ্তি অনুমোদন করুন।", Toast.LENGTH_LONG).show()
                                    } else {
                                        val ok = AdminRepository.updateBoostStatus(boost.id, "Active", selectedDurationDays, actorEmail)
                                        if (ok) {
                                            Toast.makeText(context, "Boost অনুমোদিত ও সক্রিয় হয়েছে (${selectedDurationDays} দিন) এবং নোটিফিকেশন পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "সক্রিয় Boost সীমা পূর্ণ হয়েছে বা বিজ্ঞপ্তি অনুমোদিত নয়।", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("boost_activate_${boost.id}")
                            ) {
                                Text("Approve / চালু করুন", fontSize = 11.sp)
                            }
                            // Pause Button (when Active/Approved)
                            if (boost.status == "Active" || boost.status == "Approved") {
                                OutlinedButton(
                                    onClick = {
                                        AdminRepository.updateBoostStatus(boost.id, "Paused", boost.durationDays, actorEmail)
                                        Toast.makeText(context, "Boost সাময়িকভাবে বিরতি (Paused) দেওয়া হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("boost_pause_${boost.id}")
                                ) {
                                    Text("Pause (বিরতি)", fontSize = 11.sp, color = Color(0xFF1565C0))
                                }
                            }
                            // Resume Button (when Paused)
                            if (boost.status == "Paused") {
                                Button(
                                    onClick = {
                                        AdminRepository.updateBoostStatus(boost.id, "Active", boost.durationDays, actorEmail)
                                        Toast.makeText(context, "Boost পুনরায় চালু (Resumed) করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("boost_resume_${boost.id}")
                                ) {
                                    Text("Resume (চালু)", fontSize = 11.sp)
                                }
                            }
                            // Reject Button
                            OutlinedButton(
                                onClick = {
                                    AdminRepository.updateBoostStatus(boost.id, "Rejected", selectedDurationDays, actorEmail)
                                    Toast.makeText(context, "Boost অনুরোধ প্রত্যাখ্যান (Rejected) করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("boost_reject_${boost.id}")
                            ) {
                                Text("Reject", fontSize = 11.sp, color = AdminLogoutRed)
                            }
                            // Cancel Button
                            OutlinedButton(
                                onClick = {
                                    AdminRepository.updateBoostStatus(boost.id, "Cancelled", selectedDurationDays, actorEmail)
                                    Toast.makeText(context, "Boost বাতিল (Cancelled) করা হয়েছে এবং বিজ্ঞপ্তি স্বাভাবিক ফিডে ফিরে গেছে", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("boost_cancel_${boost.id}")
                            ) {
                                Text("Cancel (বাতিল)", fontSize = 11.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }

    if (showNewBoostDialog) {
        val validNotices = notices.filter { it.status == "অনুমোদিত" }
        var selectedNoticeId by remember { mutableStateOf(validNotices.firstOrNull()?.postId ?: "") }
        var title by remember {
            mutableStateOf(
                validNotices.firstOrNull()?.let { "${it.roleTitle} - ${it.madrasahName}" } ?: ""
            )
        }
        var requester by remember {
            mutableStateOf(validNotices.firstOrNull()?.madrasahName ?: "")
        }
        var duration by remember { mutableStateOf(7) }
        var targetScope by remember { mutableStateOf("সকল ব্যবহারকারী") }

        AlertDialog(
            onDismissRequest = { showNewBoostDialog = false },
            title = { Text("নতুন Boost অনুরোধ তৈরি করুন", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (validNotices.isNotEmpty()) {
                        Text("অনুমোদিত বিজ্ঞপ্তি নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            validNotices.forEach { n ->
                                FilterChip(
                                    selected = selectedNoticeId == n.postId,
                                    onClick = {
                                        selectedNoticeId = n.postId
                                        title = "${n.roleTitle} - ${n.madrasahName}"
                                        requester = n.madrasahName
                                    },
                                    label = { Text(n.roleTitle.take(18), fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("বিজ্ঞপ্তির শিরোনাম") }, singleLine = true)
                    OutlinedTextField(value = requester, onValueChange = { requester = it }, label = { Text("আবেদনকারীর নাম/মাদ্রাসা") }, singleLine = true)
                    Text("মেয়াদ নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 7, 14, 30).forEach { d ->
                            FilterChip(selected = duration == d, onClick = { duration = d }, label = { Text("${DateUtils.toBengaliDigits(d)} দিন") })
                        }
                    }
                    Text("প্রচারের পরিধি (Target Scope):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("সকল ব্যবহারকারী", "বিভাগভিত্তিক", "জেলাভিত্তিক", "পদভিত্তিক").forEach { sc ->
                            FilterChip(
                                selected = targetScope == sc,
                                onClick = { targetScope = sc },
                                label = { Text(sc, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && requester.isNotBlank()) {
                            val ok = AdminRepository.submitBoostRequest(
                                title = title,
                                requestedBy = requester,
                                durationDays = duration,
                                noticeId = selectedNoticeId,
                                targetScope = targetScope
                            )
                            if (ok) {
                                showNewBoostDialog = false
                                Toast.makeText(context, "Boost অনুরোধ জমা হয়েছে!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "অননুমোদিত বা বাতিল বিজ্ঞপ্তি Boost করা যাবে না!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("জমা দিন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBoostDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

// ============================================================================
// 7. NOTIFICATIONS & ANNOUNCEMENTS SECTION
// ============================================================================
@Composable
fun AdminNotificationsSection(actorEmail: String) {
    val context = LocalContext.current
    val sentList by AdminRepository.sentNotifications.collectAsState()
    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("সকল ব্যবহারকারী") }
    var selectedCategory by remember { mutableStateOf("Announcements") }
    var targetRoute by remember { mutableStateOf("khedmot") }
    var logFilterCategory by remember { mutableStateOf("সব") }

    val categoryOptions = listOf(
        "Announcements" to "📢 ঘোষণা",
        "Boost" to "🚀 বিশেষ প্রচার",
        "Applications" to "📝 আবেদন আপডেট",
        "Security" to "🔒 নিরাপত্তা/অ্যাকাউন্ট"
    )

    val audienceOptions = listOf(
        "সকল ব্যবহারকারী",
        "মাদ্রাসা প্রতিনিধি",
        "খেদমতপ্রার্থী",
        "বিভাগভিত্তিক (ঢাকা/চট্টগ্রাম/সিলেট)",
        "পদভিত্তিক (মুহতামিম/মুহাদ্দিস/মুফতি/হাফেজ)"
    )

    val routeOptions = listOf(
        "khedmot" to "খেদমত ফিড",
        "abedon" to "প্রার্থী আবেদন ফিড",
        "feed" to "মূল ফিড",
        "home" to "হোম পেজ"
    )

    val filteredLogs = sentList.filter { logFilterCategory == "সব" || it.category == logFilterCategory }
    val deliveredCount = sentList.count { it.isDeliveredToDevice }
    val failedCount = sentList.count { !it.isDeliveredToDevice }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_notifications_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("নোটিফিকেশন ও ঘোষণা ব্যবস্থাপনা (FCM & In-App)", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        // Delivery Analytics Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = AdminMintBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("মোট প্রেরিত: ${DateUtils.toBengaliDigits(sentList.size)} টি", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminPrimaryGreen)
                    Text("সফল ডেলিভারি: ${DateUtils.toBengaliDigits(deliveredCount)} টি • ব্যর্থ/পেন্ডিং: ${DateUtils.toBengaliDigits(failedCount)} টি", fontSize = 11.5.sp, color = AdminTextDark)
                }
                Surface(
                    color = AdminPrimaryGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🔔 FCM Active",
                        color = AdminPrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Compose & Broadcast Notification Card
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("১. নোটিফিকেশন ক্যাটাগরি নির্বাচন করুন:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryOptions.forEach { (catKey, catLabel) ->
                        FilterChip(
                            selected = selectedCategory == catKey,
                            onClick = { selectedCategory = catKey },
                            label = { Text(catLabel, fontSize = 11.5.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("২. প্রাপক / টার্গেট গ্রুপ নির্বাচন করুন:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    audienceOptions.forEach { aud ->
                        FilterChip(
                            selected = audience == aud,
                            onClick = { audience = aud },
                            label = { Text(aud, fontSize = 11.5.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("৩. ক্লিক করলে কোন পেজে যাবে (Deep-Link Route):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    routeOptions.forEach { (rKey, rLabel) ->
                        FilterChip(
                            selected = targetRoute == rKey,
                            onClick = { targetRoute = rKey },
                            label = { Text(rLabel, fontSize = 11.5.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = notifTitle,
                    onValueChange = { notifTitle = it },
                    label = { Text("নোটিফিকেশন শিরোনাম *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_notif_title_input")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = notifBody,
                    onValueChange = { notifBody = it },
                    label = { Text("নোটিফিকেশনের মূল বার্তা *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_notif_body_input")
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (notifTitle.isNotBlank() && notifBody.isNotBlank()) {
                            AdminRepository.sendBroadcastNotification(
                                title = notifTitle.trim(),
                                body = notifBody.trim(),
                                targetAudience = audience,
                                actorEmail = actorEmail,
                                targetRoute = targetRoute,
                                category = selectedCategory
                            )
                            Toast.makeText(context, "নোটিফিকেশন সফলভাবে পাঠানো ও ডেলিভারি লগে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                            notifTitle = ""
                            notifBody = ""
                        } else {
                            Toast.makeText(context, "শিরোনাম ও মূল বার্তা লিখুন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_send_notif_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("নোটিফিকেশন পাঠান (Push + In-App)")
                }
            }
        }

        // Sent Notifications & Delivery Log Card
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("পূর্ববর্তী প্রেরিত নোটিফিকেশন ও ডেলিভারি লগ:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("সব", "Announcements", "Boost", "Applications", "Security").forEach { cat ->
                        FilterChip(
                            selected = logFilterCategory == cat,
                            onClick = { logFilterCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                filteredLogs.forEach { item ->
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = AdminPrimaryGreen, modifier = Modifier.weight(1f))
                            Surface(
                                color = if (item.isDeliveredToDevice) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.deliveryStatus,
                                    color = if (item.isDeliveredToDevice) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(item.body, fontSize = 12.sp, color = AdminTextDark)
                        Text(
                            text = "ক্যাটাগরি: ${item.category} • প্রাপক: ${item.targetAudience} • সময়: ${item.sentTime} • পেজ: ${item.targetRoute}",
                            fontSize = 11.sp,
                            color = AdminTextMuted
                        )
                        if (item.failureReason.isNotBlank()) {
                            Text("ত্রুটি বিবরণ: ${item.failureReason}", fontSize = 11.sp, color = AdminLogoutRed)
                        }
                        if (!item.isDeliveredToDevice) {
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    AdminRepository.retryNotificationDelivery(item.id, actorEmail)
                                    Toast.makeText(context, "পুনরায় নোটিফিকেশন ডেলিভারি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("🔄 পুনরায় পাঠান (Retry)", fontSize = 11.sp, color = AdminPrimaryGreen)
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }
}

// ============================================================================
// 8. DYNAMIC FORM BUILDER SECTION
// ============================================================================
@Composable
fun AdminFormBuilderSection(actorEmail: String) {
    val fields by AdminRepository.formFields.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("খেদমতের আবেদন ফর্ম") }
    var newLabel by remember { mutableStateOf("") }
    var newPlaceholder by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("Short Text") }
    var isRequired by remember { mutableStateOf(true) }
    var validationRule by remember { mutableStateOf("None") }
    var optionsCsv by remember { mutableStateOf("") }
    var showPreviewDialog by remember { mutableStateOf(false) }

    val supportedTypes = listOf(
        "Short Text", "Long Text", "Email", "Phone Number", "Number",
        "Date", "Dropdown", "Checkbox", "Single Choice", "Multiple Choice"
    )
    val categoryFields = fields.filter { it.formCategory == selectedCategory }.sortedBy { it.order }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_forms_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ফর্ম নির্মাতা (Dynamic Form Builder)", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
            OutlinedButton(onClick = { showPreviewDialog = true }) {
                Text("পূর্বরূপ (Preview)", fontSize = 12.sp, color = AdminPrimaryGreen)
            }
        }

        // Form Category Selector
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("ফর্ম নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminPrimaryGreen)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("খেদমতের আবেদন ফর্ম", "মাদ্রাসা নিবন্ধন ফর্ম", "খেদমত বিজ্ঞপ্তি ফর্ম").forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("ফিল্ড টাইপ নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminPrimaryGreen)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedTypes.forEach { typeName ->
                        FilterChip(
                            selected = newType == typeName,
                            onClick = { newType = typeName },
                            label = { Text(typeName, fontSize = 11.5.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    label = { Text("ফিল্ডের নাম (Label) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_builder_label_input")
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPlaceholder,
                    onValueChange = { newPlaceholder = it },
                    label = { Text("Placeholder টেক্সট") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (newType in listOf("Dropdown", "Single Choice", "Multiple Choice", "Checkbox")) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = optionsCsv,
                        onValueChange = { optionsCsv = it },
                        label = { Text("অপশনসমূহ (কমা দিয়ে আলাদা করুন, যেমন: দাওরা, ইফতা)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRequired, onCheckedChange = { isRequired = it })
                        Text(if (isRequired) "আবশ্যক (Required)" else "ঐচ্ছিক (Optional)", fontSize = 13.sp, color = AdminPrimaryGreen)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("None", "Email Format", "Phone Format").forEach { valRule ->
                            FilterChip(
                                selected = validationRule == valRule,
                                onClick = { validationRule = valRule },
                                label = { Text(valRule, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (newLabel.isNotBlank()) {
                            AdminRepository.addFormField(
                                DynamicFormField(
                                    id = "f_${System.currentTimeMillis()}",
                                    formCategory = selectedCategory,
                                    label = newLabel.trim(),
                                    placeholder = newPlaceholder.trim().ifBlank { "${newLabel.trim()} লিখুন" },
                                    type = newType,
                                    isRequired = isRequired,
                                    order = categoryFields.size + 1,
                                    validation = validationRule,
                                    optionsCsv = optionsCsv.trim()
                                ),
                                actorEmail = actorEmail
                            )
                            newLabel = ""
                            newPlaceholder = ""
                            optionsCsv = ""
                            Toast.makeText(context, "নতুন ফর্ম ফিল্ড যুক্ত ও সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "অনুগ্রহ করে ফিল্ডের নাম লিখুন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_builder_add_button")
                ) {
                    Text("+ ফিল্ড যোগ ও সংরক্ষণ করুন")
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = AdminDivider)
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "$selectedCategory — বর্তমান ফিল্ডসমূহ (${DateUtils.toBengaliDigits(categoryFields.size)} টি):",
                    fontWeight = FontWeight.Bold,
                    color = AdminPrimaryGreen
                )
                Spacer(Modifier.height(8.dp))

                categoryFields.forEach { field ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${DateUtils.toBengaliDigits(field.order)}. ${field.label} ${if (field.isRequired) "*" else ""}",
                                fontWeight = FontWeight.SemiBold,
                                color = AdminPrimaryGreen
                            )
                            Text(
                                text = "টাইপ: ${field.type} • Validation: ${field.validation} • Placeholder: ${field.placeholder}",
                                fontSize = 11.5.sp,
                                color = AdminTextMuted
                            )
                        }
                        IconButton(onClick = { AdminRepository.removeFormField(field.id, actorEmail) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Field", tint = AdminLogoutRed)
                        }
                    }
                    HorizontalDivider(color = AdminDivider.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showPreviewDialog) {
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = { Text("ফর্ম পূর্বরূপ: $selectedCategory", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categoryFields.forEach { f ->
                        var sampleVal by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = sampleVal,
                            onValueChange = { sampleVal = it },
                            label = { Text("${f.label} (${f.type}) ${if (f.isRequired) "*" else ""}") },
                            placeholder = { Text(f.placeholder) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPreviewDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

// ============================================================================
// 9. APP CONTENT & SETTINGS MANAGEMENT SECTION
// ============================================================================
@Composable
fun AdminContentManagementSection(actorEmail: String) {
    val context = LocalContext.current
    val currentConfig = AdminRepository.appContent

    var title by remember(currentConfig) { mutableStateOf(currentConfig.heroTitle) }
    var slogan by remember(currentConfig) { mutableStateOf(currentConfig.heroSlogan) }
    var desc by remember(currentConfig) { mutableStateOf(currentConfig.heroDescription) }
    var email by remember(currentConfig) { mutableStateOf(currentConfig.contactEmail) }
    var phone by remember(currentConfig) { mutableStateOf(currentConfig.contactPhone) }
    var faq by remember(currentConfig) { mutableStateOf(currentConfig.faqText) }
    var terms by remember(currentConfig) { mutableStateOf(currentConfig.termsText) }
    var privacy by remember(currentConfig) { mutableStateOf(currentConfig.privacyText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_content_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("হোমপেজ কনটেন্ট ও নীতিমালা সম্পাদনা", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        // Live Banner Preview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = AdminDarkGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("লাইভ ব্যানার পূর্বরূপ (Preview):", fontSize = 11.sp, color = AdminGoldAccent)
                Spacer(Modifier.height(6.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(slogan, fontSize = 12.5.sp, color = AdminLightMint)
                Spacer(Modifier.height(4.dp))
                Text(desc, fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ব্যানার শিরোনাম") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("content_hero_title_input")
                )
                OutlinedTextField(
                    value = slogan,
                    onValueChange = { slogan = it },
                    label = { Text("ব্যানার স্লোগান") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("ব্যানার বিবরণ") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("যোগাযোগের ইমেইল") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("হেল্পলাইন নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = faq,
                    onValueChange = { faq = it },
                    label = { Text("সাহায্য ও সাধারণ প্রশ্নোত্তর (FAQ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = { Text("নিয়মাবলি ও শর্তাবলি") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = privacy,
                    onValueChange = { privacy = it },
                    label = { Text("গোপনীয়তা নীতি") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        AdminRepository.updateAppContent(
                            AppContentConfig(
                                heroTitle = title.trim(),
                                heroSlogan = slogan.trim(),
                                heroDescription = desc.trim(),
                                contactEmail = email.trim(),
                                contactPhone = phone.trim(),
                                faqText = faq.trim(),
                                termsText = terms.trim(),
                                privacyText = privacy.trim(),
                                isPublished = true
                            ),
                            actorEmail = actorEmail
                        )
                        Toast.makeText(context, "অ্যাপ কনটেন্ট স্থায়ীভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("content_save_button")
                ) {
                    Text("সংরক্ষণ করুন (Save Changes)")
                }
            }
        }
    }
}

// ============================================================================
// 10. TELEGRAM INTEGRATION SECTION
// ============================================================================
@Composable
fun AdminTelegramSection(actorEmail: String) {
    val context = LocalContext.current
    val currentSettings = AdminRepository.telegramSettings

    var notifyAdmin by remember(currentSettings) { mutableStateOf(currentSettings.notifyNewAdmin) }
    var notifyBoost by remember(currentSettings) { mutableStateOf(currentSettings.notifyBoostRequest) }
    var notifyChanges by remember(currentSettings) { mutableStateOf(currentSettings.notifyImportantChanges) }
    var notifySecurity by remember(currentSettings) { mutableStateOf(currentSettings.notifySecurityAlert) }
    var approvedIds by remember(currentSettings) { mutableStateOf(currentSettings.approvedUserIds) }
    var newNumericId by remember { mutableStateOf("") }
    var newAdminLabel by remember { mutableStateOf("Admin") }

    // Password-Protected File Vault states
    val isVaultUnlocked by TelegramSecretConfig.isVaultUnlocked.collectAsState()
    val deliveryStatus by TelegramSecretConfig.lastTelegramDeliveryStatus.collectAsState()
    val botInfo by TelegramSecretConfig.connectedBotInfo.collectAsState()
    val failedCount by TelegramSecretConfig.failedAttempts.collectAsState()
    val deliveryLogs by TelegramSecretConfig.deliveryLogs.collectAsState()

    var isPasswordInit by remember(isVaultUnlocked) {
        mutableStateOf(TelegramSecretConfig.isPasswordInitialized(context))
    }

    // First-time setup states
    var initialPassInput by remember { mutableStateOf("") }
    var initialPassConfirm by remember { mutableStateOf("") }
    var showInitialPass by remember { mutableStateOf(false) }
    var setupMessage by remember { mutableStateOf<String?>(null) }

    // Unlock states
    var passwordInput by remember { mutableStateOf("") }
    var showUnlockPass by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Unlocked Vault credential states
    var editableToken by remember(isVaultUnlocked) {
        mutableStateOf(TelegramSecretConfig.getBotTokenForUi(context))
    }
    var showBotTokenPlain by remember { mutableStateOf(false) }
    var editableChatId by remember(isVaultUnlocked) {
        mutableStateOf(TelegramSecretConfig.getPrimaryChatIdForUi(context))
    }
    var currentVaultPassForChange by remember { mutableStateOf("") }
    var newVaultPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_telegram_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Telegram সেটিংস ও সিকিউর ভল্ট", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        // 1. Password-Protected Telegram Vault Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AdminDivider)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Vault Lock Status",
                            tint = if (isVaultUnlocked) Color(0xFF2E7D32) else AdminLogoutRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Telegram Secure Vault (পাসওয়ার্ড সুরক্ষিত)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = AdminPrimaryGreen
                            )
                            Text(
                                text = if (!isPasswordInit) {
                                    "অবস্থা: প্রথমবার পাসওয়ার্ড নির্ধারণ প্রয়োজন"
                                } else if (isVaultUnlocked) {
                                    "অবস্থা: আনলককৃত (সেশন সক্রিয়)"
                                } else {
                                    "অবস্থা: লক করা আছে • টোকেন: ${TelegramSecretConfig.getMaskedBotToken(context)}"
                                },
                                fontSize = 11.5.sp,
                                color = AdminTextMuted
                            )
                        }
                    }
                    if (isVaultUnlocked) {
                        OutlinedButton(
                            onClick = {
                                passwordInput = ""
                                editableToken = ""
                                editableChatId = ""
                                showBotTokenPlain = false
                                TelegramSecretConfig.lockVault()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("telegram_vault_lock_button")
                        ) {
                            Text("লক করুন", fontSize = 11.5.sp, color = AdminLogoutRed)
                        }
                    }
                }

                if (!isPasswordInit) {
                    // STATE 1: First-Time Password Setup (Mandatory before using Vault)
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "প্রাথমিক নিরাপত্তা সেটআপ: নিরাপত্তার কারণে কোনো দুর্বল ডিফল্ট পাসওয়ার্ড (যেমন উদাহরণস্বরূপ 'qawmi786') সক্রিয় রাখা হয়নি। Bot Token ও Numeric Chat ID সংরক্ষণ বা টেস্ট মেসেজ পাঠানোর পূর্বে আপনার নিজস্ব শক্তিশালী ভল্ট পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর) নির্ধারণ করুন।",
                            fontSize = 11.5.sp,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = initialPassInput,
                        onValueChange = {
                            initialPassInput = it
                            setupMessage = null
                        },
                        label = { Text("নতুন ভল্ট পাসওয়ার্ড নির্ধারণ করুন (কমপক্ষে ৬ অক্ষর)") },
                        placeholder = { Text("শক্তিশালী পাসওয়ার্ড লিখুন ('qawmi786' নয়)") },
                        singleLine = true,
                        visualTransformation = if (showInitialPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showInitialPass = !showInitialPass }) {
                                Icon(
                                    imageVector = if (showInitialPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "পাসওয়ার্ড দেখুন বা লুকান"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_vault_initial_password_input")
                    )

                    OutlinedTextField(
                        value = initialPassConfirm,
                        onValueChange = {
                            initialPassConfirm = it
                            setupMessage = null
                        },
                        label = { Text("পাসওয়ার্ড পুনরায় লিখুন (Confirm Password)") },
                        placeholder = { Text("একই পাসওয়ার্ড আবার লিখুন") },
                        singleLine = true,
                        visualTransformation = if (showInitialPass) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_vault_initial_confirm_input")
                    )

                    setupMessage?.let {
                        Text(it, color = AdminLogoutRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            val (ok, msg) = TelegramSecretConfig.setupInitialPassword(
                                context = context,
                                newPassword = initialPassInput,
                                confirmPassword = initialPassConfirm,
                                actorEmail = actorEmail
                            )
                            if (ok) {
                                isPasswordInit = true
                                passwordInput = initialPassInput.trim()
                                initialPassInput = ""
                                initialPassConfirm = ""
                                setupMessage = null
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            } else {
                                setupMessage = msg
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_vault_setup_password_button")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ভল্ট পাসওয়ার্ড নির্ধারণ ও আনলক করুন")
                    }
                } else if (!isVaultUnlocked) {
                    // STATE 2: Vault Locked — Token & Chat ID are strictly hidden and blocked
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "নিরাপত্তা সুরক্ষা: সঠিক পাসওয়ার্ড যাচাই না হওয়া পর্যন্ত Bot Token ও Numeric Chat ID দেখা, পরিবর্তন, সংরক্ষণ বা টেস্ট মেসেজ পাঠানো সম্পূর্ণরূপে বন্ধ রয়েছে।",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE65100)
                            )
                            if (failedCount > 0) {
                                Text(
                                    text = "সতর্কতা: ব্যর্থ পাসওয়ার্ড প্রচেষ্টা ${DateUtils.toBengaliDigits(failedCount)} বার।",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AdminLogoutRed
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        label = { Text("ভল্ট পাসওয়ার্ড দিন (Vault Password)") },
                        placeholder = { Text("আপনার গোপন ভল্ট পাসওয়ার্ড লিখুন...") },
                        singleLine = true,
                        visualTransformation = if (showUnlockPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showUnlockPass = !showUnlockPass }) {
                                Icon(
                                    imageVector = if (showUnlockPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "পাসওয়ার্ড দেখুন বা লুকান"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_vault_password_input")
                    )

                    passwordError?.let {
                        Text(it, color = AdminLogoutRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            val ok = TelegramSecretConfig.unlockVault(passwordInput, context, actorEmail)
                            if (ok) {
                                editableToken = TelegramSecretConfig.getBotTokenForUi(context)
                                editableChatId = TelegramSecretConfig.getPrimaryChatIdForUi(context)
                                currentVaultPassForChange = passwordInput.trim()
                                passwordError = null
                                Toast.makeText(context, "Telegram Vault আনলক হয়েছে!", Toast.LENGTH_SHORT).show()
                            } else {
                                passwordError = TelegramSecretConfig.lastTelegramDeliveryStatus.value
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_vault_unlock_button")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("পাসওয়ার্ড দিয়ে ভল্ট আনলক করুন")
                    }
                } else {
                    // STATE 3: Vault Unlocked — Full Encrypted Configuration & Live Bot Controls
                    Surface(
                        color = AdminMintBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "স্ট্যাটাস: $deliveryStatus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminPrimaryGreen
                            )
                            Text(
                                text = "সক্রিয় টোকেন (মাস্কড): ${TelegramSecretConfig.getMaskedBotToken(context)}",
                                fontSize = 11.5.sp,
                                color = AdminTextDark
                            )
                            Text(
                                text = "বট সংযোগ তথ্য: $botInfo",
                                fontSize = 11.5.sp,
                                color = AdminTeal
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editableToken,
                        onValueChange = { editableToken = it },
                        label = { Text("Telegram Bot Token (@BotFather থেকে প্রাপ্ত)") },
                        placeholder = { Text("যেমন: 7123456789:AAHxyz...") },
                        singleLine = true,
                        visualTransformation = if (showBotTokenPlain) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showBotTokenPlain = !showBotTokenPlain }) {
                                Icon(
                                    imageVector = if (showBotTokenPlain) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "টোকেন দেখুন বা লুকান"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_bot_token_input")
                    )

                    OutlinedTextField(
                        value = editableChatId,
                        onValueChange = { editableChatId = it.filter { ch -> ch.isDigit() || ch == '-' } },
                        label = { Text("প্রধান Admin Numeric Chat/User ID") },
                        placeholder = { Text("যেমন: 123456789 বা -100123456789") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_chat_id_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val activePass = passwordInput.ifBlank { currentVaultPassForChange }
                                val saved = TelegramSecretConfig.saveCredentialsWithPassword(
                                    context = context,
                                    password = activePass,
                                    botToken = editableToken,
                                    primaryChatId = editableChatId,
                                    actorEmail = actorEmail
                                )
                                if (saved) {
                                    AdminRepository.recordAuditLog(actorEmail, "Telegram Bot Token ও Numeric Chat ID এনক্রিপ্টেড ভল্টে সংরক্ষিত হয়েছে", "Telegram")
                                    Toast.makeText(context, "Bot Token ও Chat ID নিরাপদে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, TelegramSecretConfig.lastTelegramDeliveryStatus.value, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("telegram_save_credentials_button")
                        ) {
                            Text("টোকেন ও ID সংরক্ষণ", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                TelegramSecretConfig.verifyBotConnectionAsync(context, actorEmail) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("telegram_verify_bot_button")
                        ) {
                            Text("বট যাচাই (getMe)", fontSize = 11.5.sp, color = AdminTeal)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            TelegramSecretConfig.sendTestMessageFromVault(
                                context = context,
                                actorEmail = actorEmail
                            ) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_test_message_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = AdminPrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("টেস্ট মেসেজ পাঠান (Send Test Message)", fontSize = 12.sp, color = AdminPrimaryGreen)
                    }

                    HorizontalDivider(color = AdminDivider)

                    Text("ভল্ট পাসওয়ার্ড পরিবর্তন করুন:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminPrimaryGreen)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newVaultPassword,
                            onValueChange = { newVaultPassword = it },
                            label = { Text("নতুন শক্তিশালী পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("telegram_new_password_input")
                        )
                        Button(
                            onClick = {
                                val currentPass = passwordInput.ifBlank { currentVaultPassForChange }
                                val (validStrength, reason) = TelegramSecretConfig.validatePasswordStrength(newVaultPassword)
                                if (!validStrength) {
                                    Toast.makeText(context, reason, Toast.LENGTH_LONG).show()
                                } else {
                                    val updated = TelegramSecretConfig.updateFilePassword(
                                        context = context,
                                        currentPassword = currentPass,
                                        newPassword = newVaultPassword,
                                        actorEmail = actorEmail
                                    )
                                    if (updated) {
                                        passwordInput = newVaultPassword.trim()
                                        currentVaultPassForChange = newVaultPassword.trim()
                                        newVaultPassword = ""
                                        Toast.makeText(context, "ভল্ট পাসওয়ার্ড সফলভাবে পরিবর্তন হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "পাসওয়ার্ড পরিবর্তন ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminTeal),
                            modifier = Modifier.testTag("telegram_change_password_button")
                        ) {
                            Text("পাসওয়ার্ড সেভ")
                        }
                    }
                }
            }
        }

        // 2. Telegram Event Toggles & Approved Numeric User IDs Card
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0088CC), modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Telegram Bot / Channel ও ইভেন্ট ট্রিগার", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AdminPrimaryGreen)
                        Text(currentSettings.username, fontSize = 13.sp, color = AdminTeal, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = AdminDivider)

                Text("Telegram Notifications (ইভেন্ট অ্যালার্ট):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)

                TelegramToggleRow("নতুন Admin যোগ হলে", notifyAdmin) { notifyAdmin = it }
                TelegramToggleRow("Boost অনুরোধ এলে", notifyBoost) { notifyBoost = it }
                TelegramToggleRow("গুরুত্বপূর্ণ পরিবর্তনে", notifyChanges) { notifyChanges = it }
                TelegramToggleRow("নিরাপত্তা সতর্কতা", notifySecurity) { notifySecurity = it }

                HorizontalDivider(color = AdminDivider)

                Text("অনুমোদিত Telegram Numeric User/Chat IDs:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                approvedIds.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• $item", fontSize = 13.sp, color = AdminTextDark)
                        IconButton(onClick = { approvedIds = approvedIds - item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove ID", tint = AdminLogoutRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newNumericId,
                        onValueChange = { newNumericId = it.filter { ch -> ch.isDigit() || ch == '-' } },
                        label = { Text("Numeric User/Chat ID (যেমন: 123456789)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("telegram_add_numeric_id_input")
                    )
                    Button(
                        onClick = {
                            if (TelegramSecretConfig.isValidNumericChatId(newNumericId)) {
                                approvedIds = approvedIds + "${newNumericId.trim()} ($newAdminLabel)"
                                newNumericId = ""
                                Toast.makeText(context, "Numeric Telegram ID তালিকায় যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "বৈধ সংখ্যার (Numeric) Telegram ID দিন (কমপক্ষে ৫ সংখ্যা)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminTeal),
                        modifier = Modifier.testTag("telegram_add_numeric_id_button")
                    ) {
                        Text("+ যোগ")
                    }
                }

                Button(
                    onClick = {
                        AdminRepository.updateTelegramSettings(
                            TelegramSettingsConfig(
                                username = "@Q_kormosongsthan_Bangladesh",
                                notifyNewAdmin = notifyAdmin,
                                notifyBoostRequest = notifyBoost,
                                notifyImportantChanges = notifyChanges,
                                notifySecurityAlert = notifySecurity,
                                approvedUserIds = approvedIds
                            ),
                            actorEmail = actorEmail
                        )
                        Toast.makeText(context, "Telegram সেটিংস Firebase ও লোকাল স্টোরেজে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("telegram_save_settings_button")
                ) {
                    Text("সেটিংস সংরক্ষণ")
                }
            }
        }

        // 3. Telegram Delivery History & Event Logs Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AdminDivider)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "সাম্প্রতিক Telegram ডেলিভারি লগ (Delivery History)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = AdminPrimaryGreen
                )
                if (deliveryLogs.isEmpty()) {
                    Text(
                        text = "এখনও কোনো Telegram অ্যালার্ট বা টেস্ট মেসেজ পাঠানো হয়নি।",
                        fontSize = 12.sp,
                        color = AdminTextMuted
                    )
                } else {
                    deliveryLogs.take(8).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AdminCreamWhite, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.eventTitle, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = AdminPrimaryGreen)
                                Text("${log.time} • ${log.details}", fontSize = 11.sp, color = AdminTextMuted)
                            }
                            Surface(
                                color = if (log.status == "সফল") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = log.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.status == "সফল") Color(0xFF2E7D32) else AdminLogoutRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelegramToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = AdminTextDark)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ============================================================================
// 11. REPORTS & COMPLAINTS SECTION
// ============================================================================
@Composable
fun AdminSupportSection(actorEmail: String) {
    val tickets by AdminRepository.supportTickets.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_support_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("অভিযোগ ও সহায়তা (Reports & Complaints)", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tickets.forEachIndexed { idx, ticket ->
                    var replyText by remember(ticket.id, ticket.adminReply) { mutableStateOf(ticket.adminReply) }
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = "${DateUtils.toBengaliDigits(idx + 1)}. ${ticket.subject} (${ticket.status})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AdminPrimaryGreen
                        )
                        Text(
                            text = "প্রেরক: ${ticket.reporterName} (${ticket.reporterContact}) • তারিখ: ${ticket.date}",
                            fontSize = 11.5.sp,
                            color = AdminTextMuted
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(ticket.description, fontSize = 12.5.sp, color = AdminTextDark)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            label = { Text("Admin উত্তর / সমাধান নোট") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    AdminRepository.resolveSupportTicket(ticket.id, replyText, "সমাধানকৃত", actorEmail)
                                    Toast.makeText(context, "অভিযোগ সমাধানকৃত হিসেবে চিহ্নিত হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("সমাধান সম্পন্ন", fontSize = 11.5.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    AdminRepository.resolveSupportTicket(ticket.id, replyText, "প্রক্রিয়াধীন", actorEmail)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("প্রক্রিয়াধীন রাখুন", fontSize = 11.5.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }
}

// ============================================================================
// 12. ADMIN ROLES & PERMISSIONS SECTION
// ============================================================================
@Composable
fun AdminRolesSection(actorEmail: String) {
    val admins by AdminRepository.adminUsers.collectAsState()
    val context = LocalContext.current
    val currentPerms = AdminRepository.permissions

    var manageAdmins by remember(currentPerms) { mutableStateOf(currentPerms.manageAdmins) }
    var manageApps by remember(currentPerms) { mutableStateOf(currentPerms.manageApplications) }
    var manageTg by remember(currentPerms) { mutableStateOf(currentPerms.manageTelegram) }
    var manageBoost by remember(currentPerms) { mutableStateOf(currentPerms.manageBoost) }
    var approveBoost by remember(currentPerms) { mutableStateOf(currentPerms.approveBoost) }
    var manageContent by remember(currentPerms) { mutableStateOf(currentPerms.manageContentAndLogo) }

    var showAddAdminDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_roles_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Admin ব্যবস্থাপনা ও Role/Permission", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
            Button(
                onClick = { showAddAdminDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ নতুন Admin", fontSize = 12.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                admins.forEach { admin ->
                    val isMainSuper = admin.email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(admin.email, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = AdminPrimaryGreen)
                            Text("ভূমিকা: ${admin.role.displayName} • স্ট্যাটাস: ${admin.status}", fontSize = 12.sp, color = AdminTextMuted)
                        }
                        Surface(
                            color = if (isMainSuper) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isMainSuper) "লকড (প্রধান Admin)" else admin.status,
                                color = if (isMainSuper) Color(0xFFE65100) else Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }

                Spacer(Modifier.height(14.dp))
                Text("Permission Settings:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                Spacer(Modifier.height(6.dp))

                PermissionCheckboxRow("Admin ব্যবস্থাপনা", manageAdmins) { manageAdmins = it }
                PermissionCheckboxRow("আবেদনের ব্যবস্থাপনা", manageApps) { manageApps = it }
                PermissionCheckboxRow("Telegram সেটিংস", manageTg) { manageTg = it }
                PermissionCheckboxRow("Boost ব্যবস্থাপনা", manageBoost) { manageBoost = it }
                PermissionCheckboxRow("Boost অনুমোদন", approveBoost) { approveBoost = it }
                PermissionCheckboxRow("কনটেন্ট ও লোগো সম্পাদনা", manageContent) { manageContent = it }

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        AdminRepository.updatePermissions(
                            AdminPermissions(
                                manageAdmins = manageAdmins,
                                manageApplications = manageApps,
                                manageTelegram = manageTg,
                                manageBoost = manageBoost,
                                approveBoost = approveBoost,
                                manageContentAndLogo = manageContent
                            ),
                            actorEmail = actorEmail
                        )
                        Toast.makeText(context, "Permission সেটিংস সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সংরক্ষণ")
                }
            }
        }
    }

    if (showAddAdminDialog) {
        var emailInput by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf(AdminRole.LIMITED_ADMIN) }

        AlertDialog(
            onDismissRequest = { showAddAdminDialog = false },
            title = { Text("নতুন Admin যোগ বা রোল পরিবর্তন", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Admin ইমেইল") },
                        singleLine = true
                    )
                    Text("ভূমিকা নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                    AdminRole.entries.forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRole = role }
                        ) {
                            RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                            Text(role.displayName, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = AdminRepository.addOrUpdateAdminUser(emailInput, selectedRole, actorEmail)
                        if (ok) {
                            showAddAdminDialog = false
                            Toast.makeText(context, "Admin ভূমিকা সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "বৈধ ইমেইল ও উপযুক্ত Admin অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAdminDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun PermissionCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, fontSize = 13.sp, color = AdminTextDark)
    }
}

// ============================================================================
// 13. AUDIT LOG & BACKUP/RECOVERY SECTION
// ============================================================================
@Composable
fun AdminAuditLogSection(actorEmail: String) {
    val logs by AdminRepository.auditLogs.collectAsState()
    val backups by AdminRepository.configBackups.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("admin_audit_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("অডিট লগ ও ব্যাকআপ", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
            Button(
                onClick = {
                    AdminRepository.createConfigBackup(actorEmail)
                    Toast.makeText(context, "নিরাপদ কনফিগারেশন ব্যাকআপ তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ ব্যাকআপ তৈরি করুন", fontSize = 12.sp)
            }
        }

        if (backups.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("কনফিগারেশন ভার্সন হিস্টোরি ও ব্যাকআপ:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                    Spacer(Modifier.height(8.dp))
                    backups.forEach { bkp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bkp.summary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AdminPrimaryGreen)
                                Text("${bkp.timestamp} • ${bkp.createdBy}", fontSize = 11.sp, color = AdminTextMuted)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (AdminRepository.restoreConfigBackup(bkp.id, actorEmail)) {
                                        Toast.makeText(context, "ব্যাকআপ সফলভাবে পুনরুদ্ধার হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("পুনরুদ্ধার (Restore)", fontSize = 11.sp)
                            }
                        }
                        HorizontalDivider(color = AdminDivider)
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("কার্যক্রম অডিট লগ (${DateUtils.toBengaliDigits(logs.size)} টি):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                Spacer(Modifier.height(8.dp))
                logs.forEach { log ->
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Text(log.action, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AdminPrimaryGreen)
                        Text("সম্পাদনকারী: ${log.user} • সময়: ${log.time} • বিভাগ: ${log.category}", fontSize = 11.sp, color = AdminTextMuted)
                    }
                    HorizontalDivider(color = AdminDivider.copy(alpha = 0.5f))
                }
            }
        }
    }
}
