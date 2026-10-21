package com.example

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.KaomiKormoTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    deepLinkRoute: String? = null,
    onConsumeDeepLink: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    val userProfile by ProfileAndInboxRepository.currentUserProfile.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()
    val lang by AppSettingsManager.currentLanguage.collectAsState()

    val notifPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        NotificationHelper.setNotificationsEnabled(context, granted)
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            ProfileAndInboxRepository.syncAuthIdentityToProfile(context)
            FirebaseSyncManager.onAuthStateChanged()
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        AppSettingsManager.init(context)
        AdminRepository.init(context)
        ProfileAndInboxRepository.init(context)
        ProfileAndInboxRepository.syncAuthIdentityToProfile(context)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasNotificationPermission(context)
        ) {
            try {
                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } catch (_: Exception) {}
        }
    }

    val inAppDeepLinkRoute by NotificationHelper.pendingDeepLinkRoute.collectAsState()
    LaunchedEffect(deepLinkRoute, inAppDeepLinkRoute, currentUser) {
        val target = deepLinkRoute ?: inAppDeepLinkRoute
        if (!target.isNullOrBlank() && currentUser != null) {
            val validRoutes = setOf(
                "home", "feed", "khedmot", "abedon", "madrasah", "profile",
                "edit_profile", "my_applications", "my_posts", "inbox",
                "online_status", "notifications_center", "saved_posts", "admin_panel"
            )
            if (target in validRoutes) {
                navController.navigate(target) {
                    launchSingleTop = true
                }
            }
            onConsumeDeepLink()
            NotificationHelper.consumeDeepLink()
        }
    }

    val adminUsers by AdminRepository.adminUsers.collectAsState()
    val isAdminUser = remember(currentUser?.email, adminUsers) {
        AdminRepository.isUserAdmin(currentUser?.email)
    }

    val items = listOf(
        AppSettingsManager.tr("হোম", "Home") to Icons.Default.Home,
        AppSettingsManager.tr("বিজ্ঞপ্তি", "Notices") to Icons.Default.Article,
        AppSettingsManager.tr("আবেদন", "Apply") to Icons.Default.Assignment,
        AppSettingsManager.tr("ইনবক্স", "Inbox") to Icons.Default.Email,
        AppSettingsManager.tr("প্রোফাইল", "Profile") to Icons.Default.Person
    )
    val routes = listOf("home", "feed", "my_applications", "inbox", "profile")
    val bottomBarVisibleRoutes = setOf(
        "home", "feed", "khedmot", "abedon", "madrasah", "profile",
        "my_applications", "my_posts", "inbox", "saved_posts"
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Dialog states
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotifSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute in bottomBarVisibleRoutes,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDark) Color(0xFF06261E) else Color(0xFF0A3C2F),
                modifier = Modifier.widthIn(max = 315.dp).testTag("hamburger_drawer_sheet")
            ) {
                Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF072D23)).padding(horizontal = 18.dp, vertical = 20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            ProfileAvatarCircle(avatarPreset = userProfile.avatarPreset, photoUri = userProfile.photoUri, size = 54.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: userProfile.fullName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(if (userProfile.isOnline) Color(0xFF4ADE80) else Color.LightGray, CircleShape))
                                    Spacer(Modifier.width(5.dp))
                                    Text(text = if (userProfile.isOnline) AppSettingsManager.tr("অনলাইন", "Online") else AppSettingsManager.tr("অফলাইন", "Offline"), color = if (userProfile.isOnline) Color(0xFF4ADE80) else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Spacer(Modifier.height(6.dp))

                    val primaryNavMenuItems = listOf(
                        Triple(AppSettingsManager.tr("হোম", "Home"), Icons.Default.Home) { navController.navigate("home") { popUpTo("home") { inclusive = true }; launchSingleTop = true } },
                        Triple(AppSettingsManager.tr("খেদমত বিজ্ঞপ্তি", "Khedmot Notices"), Icons.Default.Description) { navController.navigate("khedmot") { launchSingleTop = true } },
                        Triple(AppSettingsManager.tr("মাদরাসা", "Madrasahs"), Icons.Default.AccountBalance) { navController.navigate("madrasah") { launchSingleTop = true } },
                        Triple(AppSettingsManager.tr("আবেদন", "Applications"), Icons.Default.Assignment) { navController.navigate("my_applications") { launchSingleTop = true } },
                        Triple(AppSettingsManager.tr("বুস্ট", "Boost"), Icons.Default.RocketLaunch) { navController.navigate("khedmot") { launchSingleTop = true } },
                        Triple(AppSettingsManager.tr("নোটিফিকেশন", "Notifications"), Icons.Default.Notifications) { navController.navigate("notifications_center") { launchSingleTop = true } },
                        Triple(AppSettingsManager.tr("সেটিংস", "Settings"), Icons.Default.Settings) { showSettingsDialog = true },
                        Triple(AppSettingsManager.tr("ভাষা পরিবর্তন", "Language Selection"), Icons.Default.Translate) { showLanguageDialog = true }
                    )

                    primaryNavMenuItems.forEach { (title, icon, onClickAction) ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { coroutineScope.launch { drawerState.close() }; onClickAction() }.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFD7F3E3), modifier = Modifier.size(21.dp))
                            Spacer(Modifier.width(14.dp))
                            Text(text = title, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DarkMode, contentDescription = AppSettingsManager.tr("ডার্ক মোড", "Dark Mode"), tint = Color(0xFFD7F3E3), modifier = Modifier.size(21.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(text = AppSettingsManager.tr("ডার্ক মোড", "Dark Mode"), color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Switch(checked = isDark, onCheckedChange = { AppSettingsManager.setDarkMode(context, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1E8E3E), uncheckedThumbColor = Color(0xFFD7F3E3), uncheckedTrackColor = Color(0xFF164F3E)), modifier = Modifier.testTag("drawer_dark_mode_switch"))
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp), color = Color.White.copy(alpha = 0.14f))

                    val secondaryMenuItems = buildList {
                        add(Triple(AppSettingsManager.tr("আমার বিজ্ঞপ্তি", "My Posts"), Icons.Default.Article) { navController.navigate("my_posts") { launchSingleTop = true } })
                        add(Triple(AppSettingsManager.tr("ইনবক্স", "Inbox"), Icons.Default.Email) { navController.navigate("inbox") { launchSingleTop = true } })
                        add(Triple(AppSettingsManager.tr("পছন্দের তালিকা", "Saved Posts"), Icons.Default.Favorite) { navController.navigate("saved_posts") { launchSingleTop = true } })
                        add(Triple(AppSettingsManager.tr("অনলাইন / অফলাইন স্ট্যাটাস", "Online Status & Privacy"), Icons.Default.VerifiedUser) { navController.navigate("online_status") { launchSingleTop = true } })
                        add(Triple(AppSettingsManager.tr("নোটিফিকেশন সেটিংস", "Notification Settings"), Icons.Default.Tune) { showNotifSettingsDialog = true })
                        add(Triple(AppSettingsManager.tr("অ্যাপ সম্পর্কে", "About App"), Icons.Default.Info) { showAboutDialog = true })
                        add(Triple(AppSettingsManager.tr("নিয়মাবলি ও শর্তাবলি", "Terms & Conditions"), Icons.Default.MenuBook) { showTermsDialog = true })
                        add(Triple(AppSettingsManager.tr("গোপনীয়তা নীতি", "Privacy Policy"), Icons.Default.Policy) { showPrivacyDialog = true })
                        add(Triple(AppSettingsManager.tr("সাহায্য ও সাধারণ প্রশ্নোত্তর", "Help & FAQ"), Icons.Default.HelpOutline) { showHelpDialog = true })
                        add(Triple(AppSettingsManager.tr("যোগাযোগ ও সমস্যা জানান", "Contact & Support"), Icons.Default.Chat) { showContactDialog = true })
                        add(Triple(AppSettingsManager.tr("অ্যাপ শেয়ার করুন", "Share App"), Icons.Default.Share) {
                            val sendIntent = android.content.Intent().apply { action = android.content.Intent.ACTION_SEND; putExtra(android.content.Intent.EXTRA_TEXT, "কওমি কর্মসংস্থান বাংলাদেশ অ্যাপ: দ্বীনি খেদমতের বিশ্বস্ত প্ল্যাটফর্ম।"); type = "text/plain" }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                        })
                        add(Triple(AppSettingsManager.tr("মতামত দিন", "Feedback"), Icons.Default.Star) { showFeedbackDialog = true })
                        if (isAdminUser) { add(Triple(AppSettingsManager.tr("এডমিন প্যানেল", "Admin Panel"), Icons.Default.AdminPanelSettings) { navController.navigate("admin_panel") }) }
                    }

                    secondaryMenuItems.forEach { (title, icon, onClickAction) ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { coroutineScope.launch { drawerState.close() }; onClickAction() }.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFBFE6DA), modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(14.dp))
                            Text(text = title, color = Color(0xFFE8F5EF), fontSize = 13.5.sp, fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp), color = Color.White.copy(alpha = 0.18f))

                    Row(modifier = Modifier.fillMaxWidth().testTag("drawer_logout_button").clickable { coroutineScope.launch { drawerState.close() }; showLogoutConfirmDialog = true }.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = AppSettingsManager.tr("লগআউট", "Logout"), tint = Color(0xFFFF8A80), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(text = AppSettingsManager.tr("লগআউট", "Logout"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (currentRoute in bottomBarVisibleRoutes) {
                    NavigationBar(containerColor = if (isDark) Color(0xFF0A281F) else Color(0xFFEDF6F1), tonalElevation = 4.dp) {
                        items.forEachIndexed { index, (label, icon) ->
                            val isSelected = currentRoute == routes[index] || (routes[index] == "feed" && currentRoute in setOf("khedmot", "abedon", "saved_posts"))
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (routes[index] == "home") { navController.navigate("home") { popUpTo("home") { inclusive = true }; launchSingleTop = true } }
                                    else if (currentRoute != routes[index]) { navController.navigate(routes[index]) { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                                },
                                icon = { Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                                label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = if (isDark) Color(0xFFE8F5EF) else QawmiDarkGreen, selectedTextColor = if (isDark) Color(0xFFE8F5EF) else QawmiDarkGreen, indicatorColor = if (isDark) Color(0xFF194B3B) else Color(0xFFC2E5D4), unselectedIconColor = if (isDark) Color(0xFF8DB5A6) else Color(0xFF5A786E), unselectedTextColor = if (isDark) Color(0xFF8DB5A6) else Color(0xFF5A786E)),
                                modifier = Modifier.testTag("bottom_nav_${routes[index]}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "auth", Modifier.padding(innerPadding)) {
                composable("auth") { AuthScreen(navController) }
                composable("home") { HomeScreen(onOpenDrawer = { coroutineScope.launch { drawerState.open() } }, onNavigateToFeed = { filter -> when (filter) { "আবেদন" -> navController.navigate("abedon") { popUpTo("home") }; "নিয়োগ বিজ্ঞপ্তি" -> navController.navigate("khedmot") { popUpTo("home") }; "পছন্দের খেদমত" -> navController.navigate("saved_posts") { popUpTo("home") }; else -> navController.navigate("feed") { popUpTo("home") } } }, onNavigateToProfile = { navController.navigate("profile") { popUpTo("home") } }, onNavigateToMadrasah = { navController.navigate("madrasah") { popUpTo("home") } }) }
                composable("feed") { FeedScreen(onOpenDrawer = { coroutineScope.launch { drawerState.open() } }, initialFilter = "সব পোস্ট", onViewPublicProfile = { target -> navController.navigate("public_profile/${android.net.Uri.encode(target)}") }, onOpenChat = { conv -> navController.navigate("chat/${conv.id}") }) }
                composable("khedmot") { FeedScreen(onOpenDrawer = { coroutineScope.launch { drawerState.open() } }, initialFilter = "নিয়োগ বিজ্ঞপ্তি", onViewPublicProfile = { target -> navController.navigate("public_profile/${android.net.Uri.encode(target)}") }, onOpenChat = { conv -> navController.navigate("chat/${conv.id}") }) }
                composable("abedon") { FeedScreen(onOpenDrawer = { coroutineScope.launch { drawerState.open() } }, initialFilter = "আবেদন", onViewPublicProfile = { target -> navController.navigate("public_profile/${android.net.Uri.encode(target)}") }, onOpenChat = { conv -> navController.navigate("chat/${conv.id}") }) }
                composable("saved_posts") { FeedScreen(onOpenDrawer = { coroutineScope.launch { drawerState.open() } }, initialFilter = "পছন্দের খেদমত", onViewPublicProfile = { target -> navController.navigate("public_profile/${android.net.Uri.encode(target)}") }, onOpenChat = { conv -> navController.navigate("chat/${conv.id}") }) }
                composable("madrasah") { MadrasahScreen(onOpenDrawer = { coroutineScope.launch { drawerState.open() } }, onNavigateToFeedWithFilter = { _ -> navController.navigate("khedmot") { popUpTo("home") } }) }
                composable("profile") { 
                    ProfessionalProfileScreen(
                        onBack = { navController.popBackStack() },
                        onEditProfile = { navController.navigate("edit_profile") },
                        onNavigateToMyApplications = { navController.navigate("my_applications") },
                        onNavigateToMyPosts = { navController.navigate("my_posts") },
                        onNavigateToInbox = { navController.navigate("inbox") },
                        onNavigateToOnlineStatus = { navController.navigate("online_status") }
                    )
                }
                composable("edit_profile") { EditProfileScreen(onBack = { navController.popBackStack() }, onSaved = { navController.popBackStack() }) }
                composable("my_applications") { MyApplicationsScreen(onBack = { if (!navController.popBackStack()) { navController.navigate("home") } }, onViewPublicProfile = { target -> navController.navigate("public_profile/${android.net.Uri.encode(target)}") }, onOpenChat = { conv -> navController.navigate("chat/${conv.id}") }) }
                composable("my_posts") { MyPostsScreen(onBack = { if (!navController.popBackStack()) { navController.navigate("profile") } }, onOpenChat = { conv -> navController.navigate("chat/${conv.id}") }) }
                composable("inbox") { TextOnlyInboxScreen(onBack = { if (!navController.popBackStack()) { navController.navigate("home") } }, onOpenChat = { conv -> navController.navigate("chat/${conv.id}") }, onViewPublicProfile = { target -> navController.navigate("public_profile/${android.net.Uri.encode(target)}") }) }
                composable("chat/{conversationId}") { backStackEntry -> val convId = backStackEntry.arguments?.getString("conversationId") ?: "conv_rashed"; TextOnlyChatScreen(conversationId = convId, onBack = { navController.popBackStack() }, onViewPublicProfile = { target -> navController.navigate("public_profile/${android.net.Uri.encode(target)}") }) }
                composable("public_profile/{userIdOrName}") { backStackEntry -> val rawArg = backStackEntry.arguments?.getString("userIdOrName") ?: "user_rashed"; val decoded = android.net.Uri.decode(rawArg); PublicUserProfileScreen(userIdOrName = decoded, onBack = { navController.popBackStack() }, onSendMessage = { conv -> navController.navigate("chat/${conv.id}") }, onEditOwnProfile = { navController.navigate("edit_profile") }, onNavigateToMyApplications = { navController.navigate("my_applications") }, onNavigateToMyPosts = { navController.navigate("my_posts") }, onNavigateToOnlineStatus = { navController.navigate("online_status") }) }
                composable("online_status") { OnlineStatusAndPrivacyScreen(onBack = { navController.popBackStack() }) }
                composable("notifications_center") { NotificationCenterScreen(onBack = { navController.popBackStack() }, onNavigateToRoute = { route -> navController.navigate(route) { launchSingleTop = true } }) }
                composable("admin_panel") { AdminPanelScreen(onNavigateBack = { navController.popBackStack() }) }
            }
        }
    }

    // Move all dialogs to separate small composables or keep them here if few
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("লগআউট নিশ্চিতকরণ", fontWeight = FontWeight.Bold, color = Color(0xFFB4233A)) },
            text = { Text("আপনি কি নিশ্চিত যে লগআউট করতে চান?", fontSize = 15.sp, color = Color(0xFF075747)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        NotificationHelper.onUserLogout(context)
                        FirebaseSyncManager.stopRealtimeSync()
                        auth.signOut()
                        navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4233A))
                ) { Text("লগআউট করুন", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirmDialog = false }) { Text("বাতিল", color = Color(0xFF075747)) } }
        )
    }
    
    // Add other dialog implementations similarly... (About, Terms, Privacy, Help, Contact, Feedback)
    if (showFeedbackDialog) {
        var feedbackText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("মতামত দিন", fontWeight = FontWeight.Bold, color = Color(0xFF075747)) },
            text = { OutlinedTextField(value = feedbackText, onValueChange = { feedbackText = it }, label = { Text("আপনার মূল্যবান মতামত") }, modifier = Modifier.fillMaxWidth().height(120.dp)) },
            confirmButton = {
                Button(onClick = {
                    if (feedbackText.isNotBlank()) AdminRepository.submitSupportTicket(reporterName = currentUser?.displayName ?: "ব্যবহারকারী", reporterContact = currentUser?.email ?: "", subject = "অ্যাপ ব্যবহারকারীর মতামত", description = feedbackText)
                    showFeedbackDialog = false
                    Toast.makeText(context, "মতামত পাঠানোর জন্য জাযাকাল্লাহ খায়ের!", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF075747))) { Text("জমা দিন", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showFeedbackDialog = false }) { Text("বাতিল") } }
        )
    }
}
