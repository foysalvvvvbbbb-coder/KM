package com.example

import android.os.Bundle
import android.util.Patterns
import java.util.Calendar
import java.time.LocalTime
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.DarkGreenBox
import com.example.ui.theme.GoldBox
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.KaomiKormoTheme
import com.example.ui.theme.White
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Theme Colors
val DeepEmerald = Color(0xFF0B3028)
val ElegantGreen = Color(0xFF14523F)
val WarmOffWhite = Color(0xFFF7F5EE)
val MutedGold = Color(0xFFD9B66F)

object AppSession {
    // Calming, soft Islamic mist-sage background (soothing, zero eye-strain, non-glare)
    val backgroundColor: Color = Color(0xFFF3F6F3)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("AuthDebug", "Firebase init error: ${e.message}")
        }
        enableEdgeToEdge()
        setContent {
            KaomiKormoTheme(darkTheme = false) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    val items = listOf(
        "হোম" to Icons.Default.Home,
        "ফিড" to Icons.Default.Article,
        "খেদমত" to Icons.Default.Work,
        "আবেদন" to Icons.Default.Assignment,
        "মাদ্রাসা" to Icons.Default.Person
    )
    val routes = listOf("home", "feed", "khedmot", "abedon", "madrasah")
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute in routes || currentRoute == "profile",
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = WarmOffWhite,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(DeepEmerald, ElegantGreen))
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar",
                            tint = MutedGold,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "কওমি কর্মসংস্থান বাংলাদেশ",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = currentUser?.displayName?.takeIf { it.isNotBlank() }
                                ?: currentUser?.email
                                ?: "খেদমতের বিশ্বস্ত প্ল্যাটফর্ম",
                            color = MutedGold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "হোম", tint = ElegantGreen) },
                    label = { Text("হোম", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "home",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        if (currentRoute != "home") {
                            navController.navigate("home") { popUpTo("home") { inclusive = false } }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Article, contentDescription = "ফিড", tint = ElegantGreen) },
                    label = { Text("ফিড", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "feed",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("feed") { popUpTo("home") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Work, contentDescription = "খেদমত", tint = ElegantGreen) },
                    label = { Text("খেদমত", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "khedmot",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("khedmot") { popUpTo("home") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "আবেদন", tint = ElegantGreen) },
                    label = { Text("আবেদন", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "abedon",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("abedon") { popUpTo("home") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "মাদ্রাসা", tint = ElegantGreen) },
                    label = { Text("মাদ্রাসা", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "madrasah",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("madrasah") { popUpTo("home") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "প্রোফাইল", tint = ElegantGreen) },
                    label = { Text("প্রোফাইল", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "profile",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("profile") { popUpTo("home") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "লগআউট", tint = Color(0xFFB00020)) },
                    label = { Text("লগআউট", color = Color(0xFFB00020), fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            auth.signOut()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (currentRoute in routes) {
                    NavigationBar(
                        containerColor = Color(0xFFEDF6F1),
                        tonalElevation = 4.dp
                    ) {
                        items.forEachIndexed { index, (label, icon) ->
                            val isSelected = currentRoute == routes[index]
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != routes[index]) {
                                        navController.navigate(routes[index]) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = QawmiDarkGreen,
                                    selectedTextColor = QawmiDarkGreen,
                                    indicatorColor = Color(0xFFC2E5D4),
                                    unselectedIconColor = Color(0xFF5A786E),
                                    unselectedTextColor = Color(0xFF5A786E)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController, 
                startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "auth", 
                Modifier.padding(innerPadding)
            ) {
                composable("auth") { AuthScreen(navController) }
                composable("home") { 
                    HomeScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onNavigateToFeed = { filter ->
                            when (filter) {
                                "আবেদন" -> navController.navigate("abedon") { popUpTo("home") }
                                "নিয়োগ বিজ্ঞপ্তি" -> navController.navigate("khedmot") { popUpTo("home") }
                                else -> navController.navigate("feed") { popUpTo("home") }
                            }
                        },
                        onNavigateToProfile = {
                            navController.navigate("profile") { popUpTo("home") }
                        },
                        onNavigateToMadrasah = {
                            navController.navigate("madrasah") { popUpTo("home") }
                        }
                    ) 
                }
                composable("feed") { 
                    FeedScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        initialFilter = "সব পোস্ট"
                    ) 
                }
                composable("khedmot") { 
                    FeedScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        initialFilter = "নিয়োগ বিজ্ঞপ্তি"
                    ) 
                }
                composable("abedon") { 
                    FeedScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        initialFilter = "আবেদন"
                    ) 
                }
                composable("madrasah") { 
                    MadrasahScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onNavigateToFeedWithFilter = { filter ->
                            navController.navigate("khedmot") { popUpTo("home") }
                        }
                    ) 
                }
                composable("profile") { ProfileScreen(navController) }
                composable("search_khedmot") { 
                    FeedScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        initialFilter = "নিয়োগ বিজ্ঞপ্তি"
                    ) 
                }
                composable("post_niyog") { 
                    FeedScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        initialFilter = "নিয়োগ বিজ্ঞপ্তি"
                    ) 
                }
            }
        }
    }
}

@Composable
fun AuthScreen(navController: NavController) {
    var isRegister by remember { mutableStateOf(false) }
    
    Box(Modifier.fillMaxSize().background(DeepEmerald)) {
        // Geometric pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 1f
            for (i in 0..size.width.toInt() step 60) {
                drawLine(MutedGold.copy(alpha = 0.05f), androidx.compose.ui.geometry.Offset(i.toFloat(), 0f), androidx.compose.ui.geometry.Offset(i.toFloat(), size.height), stroke)
            }
            for (i in 0..size.height.toInt() step 60) {
                drawLine(MutedGold.copy(alpha = 0.05f), androidx.compose.ui.geometry.Offset(0f, i.toFloat()), androidx.compose.ui.geometry.Offset(size.width, i.toFloat()), stroke)
            }
        }
        
        if (isRegister) {
            IslamicRegisterScreen(navController) { isRegister = false }
        } else {
            IslamicLoginScreen(navController) { isRegister = true }
        }
    }
}

@Composable
fun StyledButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB21414), contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IslamicLoginScreen(navController: NavController, onRegister: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF227357), // Soft glowing mint-emerald at top matching reference
                        Color(0xFF0F4232), // Deep emerald
                        Color(0xFF0B3A2D)  // Dark emerald at bottom of header
                    )
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Header Section with Hanging Lanterns and Central Mosque Emblem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Background Mosque Silhouette Art & Gradient Glow via Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val paintFar = androidx.compose.ui.graphics.Color(0xFF14533F).copy(alpha = 0.6f)
                    val pathFar = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height - 30f)
                        quadraticTo(size.width * 0.25f, size.height - 85f, size.width * 0.5f, size.height - 55f)
                        quadraticTo(size.width * 0.75f, size.height - 85f, size.width, size.height - 30f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(pathFar, paintFar)

                    val paintNear = androidx.compose.ui.graphics.Color(0xFF104434)
                    val pathNear = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height - 45f)
                        cubicTo(
                            size.width * 0.3f, size.height - 45f,
                            size.width * 0.4f, size.height - 100f,
                            size.width * 0.5f, size.height - 100f
                        )
                        cubicTo(
                            size.width * 0.6f, size.height - 100f,
                            size.width * 0.7f, size.height - 45f,
                            size.width, size.height - 30f
                        )
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(pathNear, paintNear)
                }

                // Hanging Lanterns on Left and Right (Ornate Fanoos style with chain)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Lantern
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(40.dp)
                                .background(Color(0xFFE2C98F))
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF14523F))
                                .border(1.5.dp, Color(0xFFE2C98F), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Lantern",
                                tint = Color(0xFFE2C98F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Right Lantern
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(40.dp)
                                .background(Color(0xFFE2C98F))
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF14523F))
                                .border(1.5.dp, Color(0xFFE2C98F), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Lantern",
                                tint = Color(0xFFE2C98F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Central Content: Circular Mosque Emblem, Title, and Slogan
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Mosque Emblem (Rich Design with Mosque / Dome Icon matching reference)
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C382E))
                            .border(3.dp, Color(0xFFE2C98F), CircleShape)
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = "Mosque Emblem",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "কওমি কর্মসংস্থান",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "দ্বীনি খেদমতের মাধ্যমে আলোকিত সমাজ গঠন",
                        fontSize = 13.sp,
                        color = Color(0xFFC7E2D8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            // Large Curved Login Card (Light Mint/Cream background)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F8F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "লগইন করুন",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B3A2D)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "আপনার ইমেইল ও পাসওয়ার্ড দিয়ে প্রবেশ করুন",
                        fontSize = 13.sp,
                        color = Color(0xFF5A786E)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        placeholder = { Text("ইমেইল ঠিকানা", color = Color(0xFF8FA89D), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = Color(0xFF14523F)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF14523F),
                            unfocusedBorderColor = Color(0xFFD0E2D8),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        placeholder = { Text("পাসওয়ার্ড", color = Color(0xFF8FA89D), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Color(0xFF14523F)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = Color(0xFF14523F)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF14523F),
                            unfocusedBorderColor = Color(0xFFD0E2D8),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(8.dp))

                    // Forgot Password link aligned right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showForgotDialog = true },
                            enabled = !isLoading,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF14523F),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "পাসওয়ার্ড ভুলে গেছেন?",
                                    color = Color(0xFF14523F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (showForgotDialog) {
                        ForgotPasswordDialog(
                            auth = auth,
                            initialEmail = email,
                            onDismiss = { showForgotDialog = false }
                        )
                    }

                    error?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = it,
                            color = if (it.contains("পাঠানো")) Color(0xFF075B45) else Color(0xFFB00020),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("auth_error_text")
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Login Button (Dark Green with arrow)
                    Button(
                        onClick = {
                            val trimmedEmail = email.trim()
                            if (trimmedEmail.isBlank()) {
                                error = "আপনার ইমেইল লিখুন।"
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                error = "সঠিক ইমেইল ফরম্যাট দিন (যেমন: name@example.com)।"
                            } else if (password.isBlank()) {
                                error = "আপনার পাসওয়ার্ড লিখুন।"
                            } else if (password.length < 6) {
                                error = "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।"
                            } else {
                                isLoading = true
                                error = null
                                try {
                                    auth.signInWithEmailAndPassword(trimmedEmail, password)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful && auth.currentUser != null) {
                                                val user = auth.currentUser
                                                val userDisplayName = user?.displayName
                                                if (!userDisplayName.isNullOrBlank()) {
                                                    try {
                                                        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                                        user.uid.let { uid ->
                                                            prefs.edit().putString("name_$uid", userDisplayName).apply()
                                                        }
                                                    } catch (_: Exception) {}
                                                }
                                                navController.navigate("home") {
                                                    popUpTo("auth") { inclusive = true }
                                                }
                                            } else {
                                                val ex = task.exception
                                                error = getAuthErrorMessage(ex)
                                            }
                                        }
                                        .addOnFailureListener { ex ->
                                            isLoading = false
                                            error = getAuthErrorMessage(ex)
                                        }
                                } catch (e: Exception) {
                                    isLoading = false
                                    error = getAuthErrorMessage(e)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A4232)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("লগইন হচ্ছে...", color = Color.White, fontSize = 15.sp)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("লগইন করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Divider with "অথবা"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD0E2D8))
                        Text(
                            text = "  অথবা  ",
                            color = Color(0xFF708C80),
                            fontSize = 12.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD0E2D8))
                    }

                    Spacer(Modifier.height(18.dp))

                    // Register Button (Outlined with icon and arrow)
                    OutlinedButton(
                        onClick = onRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF14523F)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF14523F)),
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color(0xFF14523F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "নতুন অ্যাকাউন্ট খুলুন",
                                color = Color(0xFF14523F),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF14523F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Footer Motif and Motto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "۞  ",
                    color = Color(0xFFD4AF37),
                    fontSize = 14.sp
                )
                Text(
                    text = "দ্বীনের খেদমতে একসাথে",
                    color = Color(0xFFB5D4CA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "  ۞",
                    color = Color(0xFFD4AF37),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun IslamicRegisterScreen(navController: NavController, onLogin: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).border(1.dp, MutedGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmOffWhite)
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("নতুন নিবন্ধন", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ElegantGreen)
                Spacer(Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("পূর্ণ নাম") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(14.dp))
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("ইমেইল") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(Modifier.height(14.dp))
                
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text("পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(8.dp), 
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { 
                        IconButton(onClick = { passwordVisible = !passwordVisible }) { 
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle") 
                        } 
                    },
                    singleLine = true
                )
                Spacer(Modifier.height(14.dp))
                
                OutlinedTextField(
                    value = confirmPassword, 
                    onValueChange = { confirmPassword = it }, 
                    label = { Text("পাসওয়ার্ড নিশ্চিত করুন") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(8.dp), 
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { 
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { 
                            Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle") 
                        } 
                    },
                    singleLine = true
                )
                
                error?.let { 
                    Text(
                        it, 
                        color = Color(0xFFB00020), 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 10.dp)
                    ) 
                }
                
                Spacer(Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedEmail = email.trim()
                        
                        if (trimmedName.isBlank()) {
                            error = "আপনার নাম লিখুন।"
                        } else if (trimmedEmail.isBlank()) {
                            error = "আপনার ইমেইল লিখুন।"
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                            error = "সঠিক ইমেইল ফরম্যাট দিন (যেমন: name@example.com)।"
                        } else if (password.length < 6) {
                            error = "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।"
                        } else if (password != confirmPassword) {
                            error = "পাসওয়ার্ড ও নিশ্চিতকরণ পাসওয়ার্ড মেলেনি।"
                        } else {
                            isLoading = true
                            error = null
                            try {
                                auth.createUserWithEmailAndPassword(trimmedEmail, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful && auth.currentUser != null) {
                                            val user = auth.currentUser
                                            android.util.Log.d("AuthDebug", "Registration Success for uid: ${user?.uid}")
                                            
                                            // Immediately cache display name locally as fallback
                                            try {
                                                val context = navController.context
                                                val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                                user?.uid?.let { uid ->
                                                    prefs.edit().putString("name_$uid", trimmedName).apply()
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.w("AuthDebug", "Local name caching error", e)
                                            }

                                            // Update profile in Firebase Authentication and wait for completion
                                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(trimmedName)
                                                .build()

                                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                                if (profileTask.isSuccessful) {
                                                    android.util.Log.d("AuthDebug", "Firebase updateProfile success: $trimmedName")
                                                } else {
                                                    android.util.Log.w("AuthDebug", "Firebase updateProfile failed: ${profileTask.exception?.message}")
                                                }

                                                // Reload currentUser to guarantee displayName is populated immediately
                                                user.reload().addOnCompleteListener {
                                                    isLoading = false
                                                    navController.navigate("home") {
                                                        popUpTo("auth") { inclusive = true }
                                                    }
                                                }
                                            }?.addOnFailureListener { ex ->
                                                android.util.Log.e("AuthDebug", "Firebase updateProfile failure listener", ex)
                                                user.reload().addOnCompleteListener {
                                                    isLoading = false
                                                    navController.navigate("home") {
                                                        popUpTo("auth") { inclusive = true }
                                                    }
                                                }
                                            } ?: run {
                                                isLoading = false
                                                navController.navigate("home") {
                                                    popUpTo("auth") { inclusive = true }
                                                }
                                            }
                                        } else {
                                            isLoading = false
                                            val ex = task.exception
                                            android.util.Log.e("AuthDebug", "Registration Failed", ex)
                                            error = getAuthErrorMessage(ex)
                                        }
                                    }
                                    .addOnFailureListener { ex ->
                                        isLoading = false
                                        android.util.Log.e("AuthDebug", "Registration Failure Listener", ex)
                                        error = getAuthErrorMessage(ex)
                                    }
                            } catch (e: Exception) {
                                isLoading = false
                                android.util.Log.e("AuthDebug", "Registration Exception", e)
                                error = getAuthErrorMessage(e)
                            }
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(50.dp), 
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen), 
                    enabled = !isLoading
                ) { 
                    Text(if (isLoading) "নিবন্ধন হচ্ছে..." else "নিবন্ধন করুন", color = WarmOffWhite, fontSize = 16.sp) 
                }
                
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onLogin) { 
                    Text("ইতিমধ্যে অ্যাকাউন্ট আছে? লগইন করুন", color = ElegantGreen, fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}




@Composable
fun ProfileScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val initialUser = auth.currentUser

    // 1. Check cached name as immediate safeguard
    val cachedName = remember(initialUser?.uid) {
        val uid = initialUser?.uid
        if (uid != null) {
            val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            prefs.getString("name_$uid", null)
        } else null
    }

    var displayName by remember { 
        mutableStateOf(
            when {
                !initialUser?.displayName.isNullOrBlank() -> initialUser?.displayName!!
                !cachedName.isNullOrBlank() -> cachedName
                else -> "নাম পাওয়া যায়নি"
            }
        ) 
    }
    var email by remember { mutableStateOf(initialUser?.email ?: "লগইন করা নেই") }
    var uid by remember { mutableStateOf(initialUser?.uid ?: "নাই") }
    var isReloading by remember { mutableStateOf(false) }

    // 2. Fetch fresh user data with reload() to ensure latest profile data is displayed
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            isReloading = true
            user.reload().addOnCompleteListener { task ->
                isReloading = false
                val refreshedUser = auth.currentUser
                if (refreshedUser != null) {
                    val freshName = refreshedUser.displayName
                    if (!freshName.isNullOrBlank()) {
                        displayName = freshName
                        try {
                            val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putString("name_${refreshedUser.uid}", freshName).apply()
                        } catch (e: Exception) {
                            // ignore
                        }
                    } else if (!cachedName.isNullOrBlank()) {
                        displayName = cachedName
                    } else {
                        displayName = "নাম পাওয়া যায়নি"
                    }
                    email = refreshedUser.email ?: "লগইন করা নেই"
                    uid = refreshedUser.uid
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ব্যবহারকারী প্রোফাইল", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ElegantGreen)
        Spacer(Modifier.height(24.dp))
        
        Icon(
            imageVector = Icons.Default.AccountCircle, 
            contentDescription = "User Icon", 
            tint = ElegantGreen, 
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WarmOffWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "নাম: $displayName", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 16.sp, 
                        color = DeepEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    if (isReloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp), 
                            strokeWidth = 2.dp, 
                            color = ElegantGreen
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("ইমেইল: $email", fontSize = 14.sp, color = DeepEmerald)
                Spacer(Modifier.height(8.dp))
                Text("UID: $uid", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text("প্রমাণীকরণ: Email / Password (Firebase)", fontSize = 12.sp, color = Color(0xFF075B45))
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("লগআউট করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() },
    initialEmail: String = "",
    onDismiss: () -> Unit
) {
    var emailInput by remember { mutableStateOf(initialEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = "পাসওয়ার্ড রিসেট",
                fontWeight = FontWeight.Bold,
                color = ElegantGreen,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (successMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "সফল",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "ইমেইল পাঠানো হয়েছে",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = successMessage!!,
                                color = Color(0xFF1B5E20),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "আপনার নিবন্ধিত ইমেইল ঠিকানাটি লিখুন। আমরা পাসওয়ার্ড রিসেট করার একটি লিংক পাঠাব।",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text("আপনার ইমেইল") },
                        placeholder = { Text("example@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        isError = errorMessage != null
                    )

                    if (isLoading) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ElegantGreen
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "লিংক পাঠানো হচ্ছে...",
                                fontSize = 12.sp,
                                color = ElegantGreen
                            )
                        }
                    }

                    errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = Color(0xFFB00020),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (successMessage != null) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ঠিক আছে", color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        val validationError = validatePasswordResetEmail(emailInput)
                        if (validationError != null) {
                            errorMessage = validationError
                        } else {
                            val trimmedEmail = emailInput.trim()
                            isLoading = true
                            errorMessage = null
                            try {
                                auth.sendPasswordResetEmail(trimmedEmail)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            successMessage = "পাসওয়ার্ড রিসেট করার লিংক আপনার ইমেইলে পাঠানো হয়েছে। ইনবক্স ও স্প্যাম ফোল্ডার চেক করুন।"
                                        } else {
                                            errorMessage = getPasswordResetErrorMessage(task.exception)
                                        }
                                    }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = getPasswordResetErrorMessage(e)
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isLoading) "অপেক্ষা করুন..." else "রিসেট লিংক পাঠান", color = Color.White)
                }
            }
        },
        dismissButton = {
            if (successMessage != null) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("বন্ধ করুন", color = Color.Gray)
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("বাতিল", color = Color.Gray)
                }
            }
        }
    )
}

@Composable
fun SimpleScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(title) }
}

