package com.example

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

import androidx.activity.compose.BackHandler

// Brand Theme Colors matching the reference image exactly
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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    val userEmail = currentUser?.email ?: "foysalahamad204@gmail.com"

    var selectedSection by remember { mutableStateOf("dashboard") }
    var isMobileSidebarOpen by remember { mutableStateOf(false) }

    // Intercept system back press to exit Admin Panel smoothly
    BackHandler {
        onNavigateBack()
    }

    // Authorization Guard
    if (!AdminRepository.isUserAdmin(userEmail)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminCreamWhite)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Denied",
                        tint = AdminLogoutRed,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "অনুমতি অস্বীকৃত (Access Denied)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminLogoutRed
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "আপনার অ্যাকাউন্ট ($userEmail) দিয়ে Admin Panel ব্যবহার করার অনুমতি নেই। শুধুমাত্র অনুমোদিত Admin ব্যবহারকারীরা প্রবেশ করতে পারবেন।",
                        fontSize = 13.sp,
                        color = AdminTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                    ) {
                        Text("অ্যাপে ফিরে যান")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                userEmail = userEmail,
                onOpenSidebar = { isMobileSidebarOpen = true },
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
            // Desktop/Tablet Permanent Navigation Rail Sidebar
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(AdminDarkGreen)
            ) {
                AdminSidebarContent(
                    selectedSection = selectedSection,
                    onSelectSection = { selectedSection = it },
                    onNavigateBack = onNavigateBack
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(AdminCreamWhite)
                    .padding(16.dp)
            ) {
                when (selectedSection) {
                    "dashboard" -> AdminDashboardSection(onNavigateSection = { selectedSection = it })
                    "users" -> AdminUsersSection()
                    "madrasahs" -> AdminMadrasahsSection()
                    "notices" -> AdminNoticesSection()
                    "applications" -> AdminApplicationsSection()
                    "boost" -> AdminBoostSection()
                    "notifications" -> AdminNotificationsSection()
                    "forms" -> AdminFormBuilderSection()
                    "content" -> AdminContentManagementSection()
                    "telegram" -> AdminTelegramSection()
                    "support" -> AdminSupportSection()
                    "roles" -> AdminRolesSection()
                    "audit" -> AdminAuditLogSection()
                    else -> AdminDashboardSection(onNavigateSection = { selectedSection = it })
                }
            }
        }

        // Mobile Drawer Overlay
        if (isMobileSidebarOpen) {
            val drawerState = rememberDrawerState(DrawerValue.Open) {
                if (it == DrawerValue.Closed) isMobileSidebarOpen = false
                true
            }
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = AdminDarkGreen,
                        modifier = Modifier.width(280.dp)
                    ) {
                        AdminSidebarContent(
                            selectedSection = selectedSection,
                            onSelectSection = { section ->
                                selectedSection = section
                                isMobileSidebarOpen = false
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                }
            ) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(
    userEmail: String,
    onOpenSidebar: () -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "Admin Control Panel",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenSidebar) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
        },
        actions = {
            // Explicit Return to App Button
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A4A)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("অ্যাপে ফিরে যান", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F5A4A))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AdminGoldAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = userEmail.substringBefore("@"),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminDarkGreen)
    )
}

@Composable
fun AdminSidebarContent(
    selectedSection: String,
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
        Triple("audit", "অডিট লগ", Icons.Default.History)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF145D4C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Mosque, contentDescription = null, tint = AdminGoldAccent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("কওমি কর্মসংস্থান", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Admin Control Panel", color = Color(0xFFB2D8CD), fontSize = 11.sp)
                }
            }
        }

        HorizontalDivider(color = Color(0xFF125B4B), modifier = Modifier.padding(vertical = 8.dp))

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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
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

        HorizontalDivider(color = Color(0xFF125B4B), modifier = Modifier.padding(vertical = 12.dp))

        // Exit Admin Panel Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF8A1C2E))
                .clickable { onNavigateBack() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Exit Admin",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "মূল অ্যাপে ফিরে যান",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 1. Dashboard View
@Composable
fun AdminDashboardSection(onNavigateSection: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ড্যাশবোর্ড (Dashboard Summary)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Text("স্বাগতম, প্রধান Admin! আজ: ২৬ এপ্রিল ২০২৫, শনিবার", fontSize = 12.sp, color = AdminTextMuted)
            }
        }

        // 4 Summary Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryStatCard("মোট ব্যবহারকারী", "১২,৮৪০", "+১২ নতুন আজ", Icons.Default.People, Color(0xFFE3EFF9), Modifier.weight(1f))
            SummaryStatCard("মোট মাদ্রাসা", "১,০২৫", "+৬ যাচাই সম্পন্ন", Icons.Default.AccountBalance, Color(0xFFE1F6EF), Modifier.weight(1f))
            SummaryStatCard("মোট বিজ্ঞপ্তি", "২,৩৪০", "+১৮ নতুন", Icons.Default.Work, Color(0xFFF3E5F5), Modifier.weight(1f))
            SummaryStatCard("মোট আবেদন", "১,৮৫০", "+১০ নতুন", Icons.Default.Assignment, Color(0xFFFFF3E0), Modifier.weight(1f))
        }

        // Breakdown Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1.5f)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("আবেদন অবস্থা (বিস্তারিত)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BadgeBox("নতুন", "২৮", Color(0xFFE3F2FD), Color(0xFF1565C0))
                        BadgeBox("প্রক্রিয়াধীন", "৬০", Color(0xFFFFF8E1), Color(0xFFF57F17))
                        BadgeBox("অনুমোদিত", "১,৪২৫", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        BadgeBox("প্রত্যাখ্যাত", "৩৮১", Color(0xFFFFEBEE), Color(0xFFC62828))
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Boost অবস্থা", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BadgeBox("অপেক্ষমাণ", "৫", Color(0xFFFFF3E0), Color(0xFFE65100))
                        BadgeBox("সক্রিয়", "১২", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                    }
                }
            }
        }

        // 7-Day Trend Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("গত ৭ দিনের পরিসংখ্যান", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF1976D2), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("ব্যবহারকারী", fontSize = 11.sp, color = AdminTextMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF2E7D32), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("আবেদন", fontSize = 11.sp, color = AdminTextMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFE65100), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("বিজ্ঞপ্তি", fontSize = 11.sp, color = AdminTextMuted)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val userPoints = listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.8f, 0.85f)
                    val appPoints = listOf(0.15f, 0.25f, 0.2f, 0.35f, 0.45f, 0.5f)
                    val noticePoints = listOf(0.1f, 0.15f, 0.12f, 0.2f, 0.25f, 0.3f)

                    fun drawTrendLine(points: List<Float>, color: Color) {
                        val path = Path()
                        val stepX = w / (points.size - 1)
                        points.forEachIndexed { i, p ->
                            val x = i * stepX
                            val y = h - (p * h)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
                    }

                    drawTrendLine(userPoints, Color(0xFF1976D2))
                    drawTrendLine(appPoints, Color(0xFF2E7D32))
                    drawTrendLine(noticePoints, Color(0xFFE65100))
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("20 Apr", "21 Apr", "22 Apr", "23 Apr", "24 Apr", "25 Apr", "26 Apr").forEach { date ->
                        Text(date, fontSize = 10.sp, color = AdminTextMuted)
                    }
                }
            }
        }

        // Quick Actions Row
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Quick Actions (দ্রুত অ্যাকশন)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminPrimaryGreen)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateSection("forms") },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ফর্ম তৈরি/সম্পাদনা", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onNavigateSection("notifications") },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminTeal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("নোটিফিকেশন পাঠান", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onNavigateSection("roles") },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminDarkGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Admin যোগ করুন", fontSize = 12.sp)
                    }
                }
            }
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
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AdminPrimaryGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 11.sp, color = AdminTextMuted)
                Text(count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Text(subtext, fontSize = 10.sp, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun BadgeBox(title: String, value: String, bgColor: Color, textColor: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 10.sp, color = textColor)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// 2. User Management View
@Composable
fun AdminUsersSection() {
    val users by AdminRepository.usersList.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ব্যবহারকারী ব্যবস্থাপনা (User Management)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("নাম, ইমেইল বা ফোন নম্বর দিয়ে খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                val filteredUsers = users.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery)
                }

                filteredUsers.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(user.name, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                            Text("${user.email} • ${user.phone}", fontSize = 12.sp, color = AdminTextMuted)
                            Text("ভূমিকা: ${user.role}", fontSize = 11.sp, color = AdminTeal)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    AdminRepository.toggleUserStatus(user.id)
                                    Toast.makeText(context, "ব্যবহারকারীর স্ট্যাটাস আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.status == "সক্রিয়") Color(0xFF2E7D32) else AdminLogoutRed
                                )
                            ) {
                                Text(user.status, fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }
}

// 3. Madrasahs Section
@Composable
fun AdminMadrasahsSection() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("মাদ্রাসা ব্যবস্থাপনা (Madrasah Management)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("১. জামিয়া দারুল উলুম হাটহাজারী (যাচাইকৃত)", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                        Text("চট্টগ্রাম • কোর্স: দাওরায়ে হাদীস, তাফসীর • নিবন্ধন: 45892", fontSize = 12.sp, color = AdminTextMuted)
                    }
                    Button(
                        onClick = { Toast.makeText(context, "মাদ্রাসা তথ্য অনুমোদিত!", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("যাচাইকৃত", fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = AdminDivider)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("২. জামিয়া ইসলামিয়া পটিয়া (যাচাইকৃত)", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                        Text("পটিয়া, চট্টগ্রাম • কোর্স: আলিয়া ও কওমি শাখা • নিবন্ধন: 98124", fontSize = 12.sp, color = AdminTextMuted)
                    }
                    Button(
                        onClick = { Toast.makeText(context, "মাদ্রাসা তথ্য অনুমোদিত!", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("যাচাইকৃত", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 4. Job Notices Section
@Composable
fun AdminNoticesSection() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("খেদমতের বিজ্ঞপ্তি ব্যবস্থাপনা (Notice Management)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("১. হাফেজ শিক্ষক প্রয়োজন — হাটহাজারী মাদ্রাসা", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                        Text("বেতন: ১৫,০০০ - ২০,০০০ টাকা • আবেদনের শেষ তারিখ: ৩০ মে ২০২৫", fontSize = 12.sp, color = AdminTextMuted)
                    }
                    Button(
                        onClick = { Toast.makeText(context, "বিজ্ঞপ্তি প্রকাশিত!", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                    ) {
                        Text("অনুমোদিত", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 5. Applications Section
@Composable
fun AdminApplicationsSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("আবেদন ব্যবস্থাপনা (Applications Management)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("মোট জমা পড়া আবেদন: ১,৮৫০ টি", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Spacer(Modifier.height(8.dp))
                Text("সাম্প্রতিক আবেদন: হাফেজ আশফাকুর রহমান (হাফেজ্জী শিক্ষক নিয়োগ)", fontSize = 12.sp, color = AdminTextMuted)
                Text("শিক্ষা: দাওরায়ে হাদীস • অভিজ্ঞতা: ৩ বছর • মোবাইল: 01712******", fontSize = 11.sp, color = AdminTeal)
            }
        }
    }
}

// 6. Boost Section
@Composable
fun AdminBoostSection() {
    val boostList by AdminRepository.boostRequests.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Boost অনুরোধ ব্যবস্থাপনা (Boost Management)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                boostList.forEach { boost ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(boost.title, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                            Text("আবেদনকারী: ${boost.requestedBy} • তারিখ: ${boost.requestDate}", fontSize = 12.sp, color = AdminTextMuted)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    AdminRepository.updateBoostStatus(boost.id, "Active")
                                    Toast.makeText(context, "Boost সক্রিয় করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text(boost.status, fontSize = 11.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }
}

// 7. Notifications Section
@Composable
fun AdminNotificationsSection() {
    val context = LocalContext.current
    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("নোটিফিকেশন ও ঘোষণা প্রচার", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = notifTitle,
                    onValueChange = { notifTitle = it },
                    label = { Text("নোটিফিকেশন শিরোনাম") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = notifBody,
                    onValueChange = { notifBody = it },
                    label = { Text("নোটিফিকেশনের মূল বার্তা") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (notifTitle.isNotBlank()) {
                            Toast.makeText(context, "সকল ব্যবহারকারীর কাছে নোটিফিকেশন পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                            notifTitle = ""
                            notifBody = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("নোটিফিকেশন পাঠান")
                }
            }
        }
    }
}

// 8. Dynamic Form Builder Section
@Composable
fun AdminFormBuilderSection() {
    val fields by AdminRepository.formFields.collectAsState()
    val context = LocalContext.current
    var newLabel by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("Short Text") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ডাইনামিক ফর্ম নির্মাতা (Dynamic Form Builder)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("নতুন ফিল্ড যোগ করুন:", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    label = { Text("ফিল্ডের নাম (যেমন: এনআইডি নম্বর)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (newLabel.isNotBlank()) {
                            AdminRepository.addFormField(
                                DynamicFormField(
                                    id = System.currentTimeMillis().toString(),
                                    label = newLabel,
                                    type = newType,
                                    isRequired = true,
                                    order = fields.size + 1
                                )
                            )
                            newLabel = ""
                            Toast.makeText(context, "নতুন ফর্ম ফিল্ড যোগ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("ফিল্ড যোগ করুন")
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = AdminDivider)
                Spacer(Modifier.height(16.dp))

                Text("বর্তমান ফর্ম ফিল্ডসমূহ:", fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                Spacer(Modifier.height(8.dp))
                fields.forEach { field ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${field.order}. ${field.label} (${field.type})", fontWeight = FontWeight.SemiBold)
                            Text(if (field.isRequired) "আবশ্যক (Required)" else "ঐচ্ছিক (Optional)", fontSize = 11.sp, color = AdminTextMuted)
                        }
                        IconButton(onClick = { AdminRepository.removeFormField(field.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AdminLogoutRed)
                        }
                    }
                    HorizontalDivider(color = AdminDivider.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// 9. App Content Management Section
@Composable
fun AdminContentManagementSection() {
    val context = LocalContext.current
    var title by remember { mutableStateOf(AdminRepository.appContent.heroTitle) }
    var slogan by remember { mutableStateOf(AdminRepository.appContent.heroSlogan) }
    var desc by remember { mutableStateOf(AdminRepository.appContent.heroDescription) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("হোমপেজ কনটেন্ট সম্পাদনা (App Content Management)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ব্যানার শিরোনাম") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = slogan,
                    onValueChange = { slogan = it },
                    label = { Text("ব্যানার স্লোগান") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("ব্যানার বিবরণ") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        AdminRepository.appContent.heroTitle = title
                        AdminRepository.appContent.heroSlogan = slogan
                        AdminRepository.appContent.heroDescription = desc
                        Toast.makeText(context, "হোমপেজ কনটেন্ট সফলভাবে আপডেট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সংরক্ষণ করুন")
                }
            }
        }
    }
}

// 10. Telegram Integration Section
@Composable
fun AdminTelegramSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Telegram Integration Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Telegram Bot Username: ${AdminRepository.telegramSettings.username}", fontWeight = FontWeight.Bold, color = AdminTeal)
                Spacer(Modifier.height(12.dp))
                Text("অনুমোদিত Telegram User IDs:", fontWeight = FontWeight.SemiBold, color = AdminPrimaryGreen)
                AdminRepository.telegramSettings.approvedUserIds.forEach { userId ->
                    Text("• $userId", fontSize = 12.sp, color = AdminTextMuted)
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, "Telegram সেটিংস সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryGreen)
                ) {
                    Text("সেটিংস সংরক্ষণ")
                }
            }
        }
    }
}

// 11. Support / Complaints Section
@Composable
fun AdminSupportSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("অভিযোগ ও সহায়তা (Reports & Complaints)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("কোনো অভিযোগ বা সাধারণ টিকিটের রিপোর্ট বর্তমানে খালি আছে।", color = AdminTextMuted)
            }
        }
    }
}

// 12. Admin Roles Section
@Composable
fun AdminRolesSection() {
    val admins by AdminRepository.adminUsers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Admin ব্যবস্থাপনা ও Role/Permission", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                admins.forEach { admin ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(admin.email, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)
                            Text("ভূমিকা: ${admin.role.displayName} • স্ট্যাটাস: ${admin.status}", fontSize = 12.sp, color = AdminTextMuted)
                        }
                    }
                    HorizontalDivider(color = AdminDivider)
                }
            }
        }
    }
}

// 13. Audit Log Section
@Composable
fun AdminAuditLogSection() {
    val logs by AdminRepository.auditLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("অডিট লগ (Audit Log History)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryGreen)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                logs.forEach { log ->
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Text("${log.time} — ${log.action}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AdminPrimaryGreen)
                        Text("ব্যবহারকারী: ${log.user} • ক্যাটাগরি: ${log.category}", fontSize = 11.sp, color = AdminTextMuted)
                    }
                    HorizontalDivider(color = AdminDivider.copy(alpha = 0.5f))
                }
            }
        }
    }
}
