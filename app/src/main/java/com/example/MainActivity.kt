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
val WarmOffWhite = Color(0xFFF7F5EE) // Keeping but not using for background
val MutedGold = Color(0xFFD9B66F)

object AppSession {
    // Light, elegant background colors (NO WHITE)
    val backgroundColor: Color = listOf(
        Color(0xFFE8F5E9), // Light Sage Green
        Color(0xFFE3F2FD), // Light Pastel Blue
        Color(0xFFF3E5F5), // Light Pastel Lavender
        Color(0xFFFFF3E0)  // Light Pastel Peach
    ).random()
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

    val items = listOf("হোম" to Icons.Default.Home, "খেদমত" to Icons.Default.Work, "আবেদন" to Icons.AutoMirrored.Filled.List, "প্রোফাইল" to Icons.Default.Person)
    val routes = listOf("home", "khedmot", "abedon", "profile")
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute in routes,
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
                    icon = { Icon(Icons.Default.Search, contentDescription = "খেদমত খুঁজুন", tint = ElegantGreen) },
                    label = { Text("খেদমত খুঁজুন", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "search_khedmot",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("search_khedmot") { popUpTo("home") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PostAdd, contentDescription = "নিয়োগ প্রকাশ", tint = ElegantGreen) },
                    label = { Text("নিয়োগ প্রকাশ", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "post_niyog",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("post_niyog") { popUpTo("home") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "আবেদন", tint = ElegantGreen) },
                    label = { Text("আবেদন", fontWeight = FontWeight.Medium) },
                    selected = currentRoute == "abedon",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("abedon") { popUpTo("home") }
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
                    NavigationBar(containerColor = Color(0xFF1E1E1E), tonalElevation = 8.dp) {
                        items.forEachIndexed { index, (label, icon) ->
                            NavigationBarItem(
                                selected = currentRoute == routes[index],
                                onClick = { navController.navigate(routes[index]) { popUpTo("home") } },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFFC9A44C),
                                    selectedTextColor = Color(0xFFC9A44C),
                                    indicatorColor = Color(0xFF333333)
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
                        navController = navController,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    ) 
                }
                composable("khedmot") { SimpleScreen("খেদমত") }
                composable("abedon") { SimpleScreen("আবেদন") }
                composable("profile") { ProfileScreen(navController) }
                composable("search_khedmot") { SimpleScreen("খেদমত খোঁজা") }
                composable("post_niyog") { SimpleScreen("নিয়োগ প্রকাশ") }
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
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(WarmOffWhite.copy(alpha = 0.8f))
                .border(1.dp, MutedGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "কওমি কর্মসংস্থান বাংলাদেশ", 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = ElegantGreen, 
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("ইমেইল") }, 
                    modifier = Modifier.fillMaxWidth().testTag("email_input"), 
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text("পাসওয়ার্ড") }, 
                    modifier = Modifier.fillMaxWidth().testTag("password_input"), 
                    shape = RoundedCornerShape(8.dp), 
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), 
                    trailingIcon = { 
                        IconButton(onClick = { passwordVisible = !passwordVisible }) { 
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle") 
                        } 
                    },
                    enabled = !isLoading
                )
                
                error?.let { 
                    Text(
                        it, 
                        color = if (it.contains("পাঠানো হয়েছে")) Color(0xFF075B45) else Color(0xFFB00020), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp).testTag("auth_error_text")
                    ) 
                }
                
                var showForgotDialog by remember { mutableStateOf(false) }
                
                TextButton(
                    onClick = { showForgotDialog = true }, 
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isLoading
                ) { Text("পাসওয়ার্ড ভুলে গেছেন?", color = ElegantGreen) }

                if (showForgotDialog) {
                    ForgotPasswordDialog(
                        auth = auth,
                        initialEmail = email,
                        onDismiss = { showForgotDialog = false }
                    )
                }
                
                Spacer(Modifier.height(16.dp))
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
                                            android.util.Log.d("AuthDebug", "Login Success for uid: ${user?.uid}")
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
                                            android.util.Log.e("AuthDebug", "Login Failed", ex)
                                            error = getAuthErrorMessage(ex)
                                        }
                                    }
                                    .addOnFailureListener { ex ->
                                        isLoading = false
                                        android.util.Log.e("AuthDebug", "Login Failure Listener", ex)
                                        error = getAuthErrorMessage(ex)
                                    }
                            } catch (e: Exception) {
                                isLoading = false
                                android.util.Log.e("AuthDebug", "Login Exception", e)
                                error = getAuthErrorMessage(e)
                            }
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_button"), 
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen), 
                    enabled = !isLoading
                ) { 
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = WarmOffWhite)
                        Spacer(Modifier.width(8.dp))
                        Text("লগইন হচ্ছে...", color = WarmOffWhite, fontSize = 16.sp)
                    } else {
                        Text("লগইন করুন", color = WarmOffWhite, fontSize = 16.sp) 
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = error ?: "",
                        color = Color(0xFFB00020),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                
                TextButton(
                    onClick = onRegister, 
                    modifier = Modifier.padding(top = 8.dp),
                    enabled = !isLoading
                ) { 
                    Text("নতুন অ্যাকাউন্ট তৈরি করুন", color = ElegantGreen, fontWeight = FontWeight.Bold) 
                }
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
fun InfoCard(text: String, containerColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text = text, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .graphicsLayer {
                shape = RoundedCornerShape(24.dp)
                clip = true
            }
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { Column(modifier = Modifier.padding(20.dp), content = content) }
    )
}

@Composable
fun DateCard(modifier: Modifier = Modifier) {
    val dateInfo = remember { DateUtils.getCurrentDateInfo() }
    
    // Premium distinct Card for Date section with rich Islamic green styling for crystal-clear readability
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F4C3A)),
        border = BorderStroke(1.dp, MutedGold.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MutedGold.copy(alpha = 0.2f),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    Icons.Outlined.DateRange, 
                    contentDescription = null, 
                    tint = MutedGold, 
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // 1. Arabic / Hijri Date (First)
                Text(
                    text = dateInfo.hijriDate, 
                    style = MaterialTheme.typography.titleSmall,
                    color = MutedGold,
                    fontWeight = FontWeight.Bold
                )
                // 2. Bengali Date (Second)
                Text(
                    text = dateInfo.bengaliDate, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                // 3. English Date & Weekday (Third)
                Text(
                    text = "${dateInfo.englishDate} • ${dateInfo.weekday}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB2DFDB)
                )
            }
        }
    }
}

@Composable
fun PrayerTimesSection(modifier: Modifier = Modifier) {
    var prayerTimes by remember { mutableStateOf<List<PrayerTime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                prayerTimes = PrayerTimeManager.getPrayerTimes("Dhaka")
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "সময়সূচী লোড করা যায়নি"
                isLoading = false
            }
        }
    }
    
    // 3D Depth Card for Prayer Times with rich dark green gradient/container and strong shadow
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF082B22)),
        border = BorderStroke(1.5.dp, MutedGold.copy(alpha = 0.6f))
    ) {
        val cal = remember { Calendar.getInstance() }
        val currentMinutesTotal = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        
        fun parseMinutes(timeStr: String): Int {
            return try {
                val parts = timeStr.trim().take(5).split(":")
                val h = parts[0].toInt()
                val m = parts[1].toInt()
                h * 60 + m
            } catch (_: Exception) {
                0
            }
        }

        val activePrayerIndex = prayerTimes.indexOfLast { 
            parseMinutes(it.start) <= currentMinutesTotal 
        }
        val nextPrayer = if (activePrayerIndex != -1 && activePrayerIndex < prayerTimes.size - 1) prayerTimes[activePrayerIndex + 1] else prayerTimes.firstOrNull()

        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = MutedGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নামাজের সময়সূচী", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
                nextPrayer?.let {
                    Surface(shape = RoundedCornerShape(6.dp), color = MutedGold) {
                        Text("পরবর্তী: ${it.name}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp, 2.dp), color = Color(0xFF082B22), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp), color = MutedGold)
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    prayerTimes.forEachIndexed { index, prayer ->
                        val isActive = index == activePrayerIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isActive) MutedGold.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    BorderStroke(0.5.dp, if (isActive) MutedGold else Color.Transparent),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                prayer.name, 
                                style = MaterialTheme.typography.bodyMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = if (isActive) Color(0xFFFFD700) else Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                 // Start time chip
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        "শুরু: ${DateUtils.formatTimeTo12Hour(prayer.start)}", 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = Color(0xFFE0F2F1),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                // End time chip with high-contrast background so yellow pops out perfectly
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        "শেষ: ${DateUtils.formatTimeTo12Hour(prayer.end)}", 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = Color(0xFFFFD700), // Brilliant Gold/Yellow that pops out
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AajkerShoronCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), // Soft botanical light green tint
        border = BorderStroke(1.2.dp, MutedGold.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floral decorated header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32), // Floral green
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "আজকের স্মরণ", 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold, 
                    color = DeepEmerald,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            // Main inspiring text
            Text(
                "আপনি কি আজ প্রিয় নবী হযরত মুহাম্মদ সাল্লাল্লাহু আলাইহি ওয়াসাল্লাম-এর ওপর দরুদ পাঠ করেছেন?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1B382B),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Darood box with floral/gold styling
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(0.8.dp, MutedGold.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "“আল্লাহুম্মা সাল্লি আলা মুহাম্মাদিও ওয়া আলা আলি মুহাম্মাদ, সাল্লাল্লাহু আলাইহি ওয়াসাল্লাম”",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = ElegantGreen,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingCard(greetingName: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14523F)), // ElegantGreen
        border = BorderStroke(1.dp, MutedGold.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "আসসালামু আলাইকুম, ",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFD9B66F) // MutedGold
                )
                Text(
                    text = greetingName ?: "সম্মানিত ব্যবহারকারী",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(
                text = "কওমি কর্মসংস্থান প্ল্যাটফর্মে আপনাকে স্বাগতম।",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB2DFDB)
            )
        }
    }
}

@Composable
fun AnimatedHeaderBanner(onOpenDrawer: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_live_glow")
    
    val primaryColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFD9B66F), // Gold
        targetValue = Color(0xFF4CAF50), // Green
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "primary_color"
    )

    val secondaryColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFFFEB3B), // Bright Yellow
        targetValue = Color(0xFF00E676), // Vibrant Mint Green
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "secondary_color"
    )

    val borderWidth by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_width"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF082B22)),
        border = BorderStroke(borderWidth.dp, secondaryColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "মেনু", tint = Color.White)
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🌸 কওমি কর্মসংস্থান 🌸",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = { /* TODO: Notifications */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "নোটিফিকেশন", tint = Color.White)
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val initialUser = auth.currentUser
    val cachedName = remember(initialUser?.uid) {
        val uid = initialUser?.uid
        if (uid != null) {
            val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            prefs.getString("name_$uid", null)
        } else null
    }
    var greetingName by remember {
        mutableStateOf(
            initialUser?.displayName?.takeIf { it.isNotBlank() } ?: cachedName?.takeIf { it.isNotBlank() }
        )
    }

    LaunchedEffect(Unit) {
        initialUser?.reload()?.addOnCompleteListener {
            val updatedUser = auth.currentUser
            val freshName = updatedUser?.displayName?.takeIf { it.isNotBlank() }
            if (freshName != null) {
                greetingName = freshName
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppSession.backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AnimatedHeaderBanner(onOpenDrawer)

        Spacer(modifier = Modifier.height(10.dp))
        DateCard()
        Spacer(modifier = Modifier.height(16.dp))
        GreetingCard(greetingName)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard(
                text = "খেদমত খুঁজুন",
                containerColor = DarkGreenBox,
                textColor = White,
                modifier = Modifier.weight(1f).clickable { navController.navigate("search_khedmot") }
            )
            InfoCard(
                text = "নিয়োগ প্রকাশ",
                containerColor = GoldBox,
                textColor = IslamicGreen,
                modifier = Modifier.weight(1f).clickable { navController.navigate("post_niyog") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PrayerTimesSection()
        Spacer(modifier = Modifier.height(16.dp))
        AajkerShoronCard()
        Spacer(modifier = Modifier.height(16.dp))
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

