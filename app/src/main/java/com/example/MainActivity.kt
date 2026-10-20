package com.example

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactDateHeader(modifier: Modifier = Modifier) {
    val dateInfo = remember { DateUtils.getCurrentDateInfo() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        color = WarmOffWhite.copy(alpha = 0.6f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center
            ) {
                // বাংলা তারিখ
                Text(
                    text = dateInfo.bengaliDate,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = DeepEmerald.copy(alpha = 0.85f)
                )

                Text(
                    text = " • ",
                    fontSize = 11.5.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                // ইংরেজি তারিখ
                Text(
                    text = dateInfo.englishDate,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = DeepEmerald.copy(alpha = 0.85f)
                )

                Text(
                    text = " • ",
                    fontSize = 11.5.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                // হিজরি তারিখ
                Text(
                    text = dateInfo.hijriDate,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = DeepEmerald.copy(alpha = 0.85f)
                )

                Text(
                    text = " • ",
                    fontSize = 11.5.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                // সপ্তাহের বার
                Text(
                    text = dateInfo.weekday,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = DeepEmerald.copy(alpha = 0.85f)
                )
            }

            Text(
                text = "*হিজরি তারিখ স্থানীয় চাঁদ দেখার ওপর পরিবর্তনশীল",
                fontSize = 9.5.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Top Header with Hamburger menu on left and App Title
        // (Separate profile icon on top-right is removed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("drawer_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "মেনু খুলুন",
                    tint = ElegantGreen,
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = "কওমি কর্মসংস্থান বাংলাদেশ",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 40.dp) // Balances left hamburger icon for optical centering
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Compact Date Header
        CompactDateHeader()

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Centered Home Page Welcome Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "কওমি কর্মসংস্থান",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ElegantGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            val welcomeText = if (!greetingName.isNullOrBlank()) {
                "স্বাগতম, $greetingName"
            } else {
                "স্বাগতম"
            }

            Text(
                text = welcomeText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepEmerald,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "আপনার উপযুক্ত খেদমতের সুযোগ খুঁজে নিন।",
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        var query by remember { mutableStateOf("") }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("খেদমত বা প্রতিষ্ঠানের নাম লিখুন") },
            modifier = Modifier.fillMaxWidth().testTag("search_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            trailingIcon = {
                IconButton(onClick = { /* Handle Search */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

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

