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
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ContextWrapper
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    val items = listOf("হোম" to Icons.Default.Home, "খেদমত" to Icons.Default.Work, "আবেদন" to Icons.AutoMirrored.Filled.List, "প্রোফাইল" to Icons.Default.Person)
    val routes = listOf("home", "khedmot", "abedon", "profile")
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
            composable("home") { HomeScreen(navController) }
            composable("khedmot") { SimpleScreen("খেদমত") }
            composable("abedon") { SimpleScreen("আবেদন") }
            composable("profile") { ProfileScreen(navController) }
            composable("search_khedmot") { SimpleScreen("খেদমত খোঁজা") }
            composable("post_niyog") { SimpleScreen("নিয়োগ প্রকাশ") }
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
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showGoogleHelpDialog by remember { mutableStateOf(false) }
    
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
                    enabled = !isLoading && !isGoogleLoading
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
                    enabled = !isLoading && !isGoogleLoading
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
                    enabled = !isLoading && !isGoogleLoading
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
                    enabled = !isLoading && !isGoogleLoading
                ) { 
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = WarmOffWhite)
                        Spacer(Modifier.width(8.dp))
                        Text("লগইন হচ্ছে...", color = WarmOffWhite, fontSize = 16.sp)
                    } else {
                        Text("লগইন করুন", color = WarmOffWhite, fontSize = 16.sp) 
                    }
                }

                // “অথবা” Divider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MutedGold.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Text(
                        text = "অথবা",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = ElegantGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MutedGold.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }

                // Google Sign-In Button
                OutlinedButton(
                    onClick = {
                        if (isLoading || isGoogleLoading) return@OutlinedButton
                        val webClientId = resolveGoogleWebClientId(context)
                        if (webClientId.isBlank()) {
                            val msg = "গুগল লগইন চালুর জন্য Firebase Console-এ Google Provider সক্রিয় করে Web Client ID সেট করুন।"
                            error = msg
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            showGoogleHelpDialog = true
                            return@OutlinedButton
                        }

                        isGoogleLoading = true
                        error = null

                        coroutineScope.launch {
                            try {
                                val targetContext = context.findActivity() ?: context
                                val credentialManager = CredentialManager.create(targetContext)
                                val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                android.util.Log.d("AuthDebug", "Requesting Google credentials via CredentialManager...")
                                val result = credentialManager.getCredential(
                                    context = targetContext,
                                    request = request
                                )

                                val credential = result.credential
                                android.util.Log.d("AuthDebug", "Credential received: ${credential::class.java.simpleName}, type: ${credential.type}")

                                val idToken = extractGoogleIdToken(credential)

                                if (idToken.isNullOrBlank()) {
                                    isGoogleLoading = false
                                    val tokenError = "গুগল আইডি টোকেন পাওয়া যায়নি। (টাইপ: ${credential.type})"
                                    android.util.Log.e("AuthDebug", "Failed to retrieve Google ID token. Class: ${credential::class.java.name}, type: ${credential.type}")
                                    error = tokenError
                                    Toast.makeText(context, tokenError, Toast.LENGTH_LONG).show()
                                    showGoogleHelpDialog = true
                                } else {
                                    android.util.Log.d("AuthDebug", "Google ID token obtained. Signing in with Firebase GoogleAuthProvider...")
                                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                                    auth.signInWithCredential(authCredential)
                                        .addOnCompleteListener { task ->
                                            isGoogleLoading = false
                                            if (task.isSuccessful && auth.currentUser != null) {
                                                val user = auth.currentUser
                                                android.util.Log.d("AuthDebug", "Firebase Google Sign-In SUCCESS: uid=${user?.uid}, email=${user?.email}")
                                                val userDisplayName = user?.displayName
                                                if (!userDisplayName.isNullOrBlank()) {
                                                    try {
                                                        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                                        user.uid.let { uid ->
                                                            prefs.edit().putString("name_$uid", userDisplayName).apply()
                                                        }
                                                    } catch (e: Exception) {
                                                        android.util.Log.w("AuthDebug", "Local user name caching failed", e)
                                                    }
                                                }
                                                user?.reload()?.addOnCompleteListener {
                                                    android.util.Log.d("AuthDebug", "Firebase user reload complete")
                                                }
                                                navController.navigate("home") {
                                                    popUpTo("auth") { inclusive = true }
                                                }
                                            } else {
                                                val ex = task.exception
                                                val errorCode = (ex as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: "UNKNOWN"
                                                android.util.Log.e("AuthDebug", "Firebase Google auth failed. Code: $errorCode, message: ${ex?.message}", ex)
                                                val errorMsg = "গুগল লগইন সম্পন্ন করা যায়নি: ${getAuthErrorMessage(ex)}"
                                                error = errorMsg
                                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                showGoogleHelpDialog = true
                                            }
                                        }
                                        .addOnFailureListener { ex ->
                                            if (isGoogleLoading) {
                                                isGoogleLoading = false
                                                val errorCode = (ex as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: "UNKNOWN"
                                                android.util.Log.e("AuthDebug", "Firebase Google auth onFailure. Code: $errorCode, message: ${ex.message}", ex)
                                                val errorMsg = "গুগল লগইন সম্পন্ন করা যায়নি: ${getAuthErrorMessage(ex)}"
                                                error = errorMsg
                                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                showGoogleHelpDialog = true
                                            }
                                        }
                                }
                            } catch (e: GetCredentialCancellationException) {
                                isGoogleLoading = false
                                val msg = e.message ?: ""
                                android.util.Log.d("AuthDebug", "Google Sign-In cancellation: $msg")
                                if (msg.contains("Activity was cancelled", ignoreCase = true) || msg.contains("16:") || msg.contains("10:")) {
                                    val err = "গুগল সাইন-ইন বন্ধ হয়েছে (Activity Cancelled)। Firebase Console-এ SHA-1 ফিঙ্গারপ্রিন্ট ও Support Email সেভ করা আছে কি না যাচাই করুন।"
                                    error = err
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    showGoogleHelpDialog = true
                                }
                            } catch (e: NoCredentialException) {
                                isGoogleLoading = false
                                android.util.Log.e("AuthDebug", "No Google account on device", e)
                                val msg = "ডিভাইসে কোনো গুগল অ্যাকাউন্ট পাওয়া যায়নি। অনুগ্রহ করে ডিভাইসে গুগল অ্যাকাউন্ট যোগ করুন।"
                                error = msg
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            } catch (e: GetCredentialException) {
                                isGoogleLoading = false
                                val errMsg = getGoogleSignInErrorMessage(e)
                                if (errMsg.isNotBlank()) {
                                    android.util.Log.e("AuthDebug", "Google Sign-In GetCredentialException: type=${e.type}, message=${e.message}", e)
                                    error = errMsg
                                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                                    showGoogleHelpDialog = true
                                } else {
                                    android.util.Log.d("AuthDebug", "Google Sign-In dismissed/cancelled by user (GetCredentialException)")
                                }
                            } catch (e: Exception) {
                                isGoogleLoading = false
                                val rawMsg = e.localizedMessage ?: e.message ?: ""
                                if (rawMsg.equals("User canceled", ignoreCase = true)) {
                                    android.util.Log.d("AuthDebug", "Google Sign-In cancelled by user: $rawMsg")
                                } else {
                                    android.util.Log.e("AuthDebug", "Google Sign-In unexpected error: ${e::class.java.simpleName}, message: $rawMsg", e)
                                    val errMsg = "গুগল লগইনে সমস্যা হয়েছে: ${rawMsg.ifBlank { "অনুগ্রহ করে আবার চেষ্টা করুন।" }}"
                                    error = errMsg
                                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                                    showGoogleHelpDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_login_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = DeepEmerald
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD3D3D3)),
                    enabled = !isLoading && !isGoogleLoading
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = ElegantGreen
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "গুগল সাইন-ইন হচ্ছে...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepEmerald
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Google দিয়ে লগইন করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepEmerald
                        )
                    }
                }

                // Always visible help button
                TextButton(
                    onClick = { showGoogleHelpDialog = true },
                    modifier = Modifier.padding(top = 2.dp),
                    enabled = !isLoading && !isGoogleLoading
                ) {
                    Text(
                        "Google লগইন সমস্যা? SHA কী ও সমাধান দেখুন",
                        color = ElegantGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error ?: "",
                        color = Color(0xFFB00020),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    TextButton(onClick = { showGoogleHelpDialog = true }) {
                        Text("Firebase কনফিগারেশন সহায়তা দেখুন", color = ElegantGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                TextButton(
                    onClick = onRegister, 
                    modifier = Modifier.padding(top = 8.dp),
                    enabled = !isLoading && !isGoogleLoading
                ) { 
                    Text("নতুন অ্যাকাউন্ট তৈরি করুন", color = ElegantGreen, fontWeight = FontWeight.Bold) 
                }
            }
        }

        if (showGoogleHelpDialog) {
            val clipboardManager = LocalClipboardManager.current
            val sha1Key = "29:81:AD:43:C4:7A:87:F0:1B:96:44:B9:C8:AD:56:26:65:BF:67:71"
            val sha256Key = "7C:C7:F3:99:14:E2:A2:2C:79:92:8F:3F:55:4A:9F:8E:80:11:A7:AE:A4:DD:3B:DD:A7:23:1A:32:7E:3E:2D:C9"
            val packageName = "bd.kaomikormo.app"
            val webClientIdStr = "606917405325-2irml4tp1qpet69ih8t7gicccr534t9n.apps.googleusercontent.com"

            AlertDialog(
                onDismissRequest = { showGoogleHelpDialog = false },
                title = {
                    Text("Google সাইন-ইন সমাধান গাইড", fontWeight = FontWeight.Bold, color = ElegantGreen)
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            "অ্যাকাউন্ট বাছাই করার পর লগইন না হওয়ার ৩টি মূল কারণ ও সমাধান:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepEmerald
                        )
                        Spacer(Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    "১. SHA-1 ফিঙ্গারপ্রিন্ট (বাধ্যতামূলক):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DeepEmerald
                                )
                                Text(
                                    "শুধু SHA-256 যথেষ্ট নয়। Google Sign-In ক্লায়েন্টের জন্য SHA-1 আবশ্যক!",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(Modifier.height(4.dp))
                                SelectionContainer {
                                    Text(sha1Key, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sha1Key))
                                        Toast.makeText(context, "SHA-1 কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("SHA-1 কপি করুন", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    "২. SHA-256 ফিঙ্গারপ্রিন্ট:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8D6E63)
                                )
                                Spacer(Modifier.height(4.dp))
                                SelectionContainer {
                                    Text(sha256Key, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sha256Key))
                                        Toast.makeText(context, "SHA-256 কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D4C41)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("SHA-256 কপি করুন", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            "৩. Firebase Authentication Support Email:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DeepEmerald
                        )
                        Text(
                            "Firebase Console > Authentication > Sign-in method > Google-এ গিয়ে 'Project support email' ড্রপডাউন থেকে আপনার ইমেইল নির্বাচন করে Save করুন। এটি খালি থাকলে অ্যাকাউন্ট পিক করার পরই বন্ধ হয়ে যায়।",
                            fontSize = 11.sp
                        )

                        Spacer(Modifier.height(6.dp))
                        Text(
                            "৪. OAuth Consent Screen (Google Cloud Console):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DeepEmerald
                        )
                        Text(
                            "Google Cloud Console-এ গিয়ে OAuth Consent Screen 'In production' করুন অথবা 'Test Users' তালিকায় আপনার জিমেইল যোগ করুন।",
                            fontSize = 11.sp
                        )

                        Spacer(Modifier.height(6.dp))
                        Text("প্যাকেজ নাম: $packageName", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGoogleHelpDialog = false }) {
                        Text("বুঝেছি", color = ElegantGreen, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = WarmOffWhite,
                shape = RoundedCornerShape(16.dp)
            )
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
fun HomeScreen(navController: NavController) {
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "কওমি কর্মসংস্থান বাংলাদেশ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = greetingName?.let { "স্বাগতম, $it" } ?: "আসসালামু আলাইকুম", 
                    fontSize = 16.sp, 
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = { navController.navigate("profile") }) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = ElegantGreen, modifier = Modifier.size(32.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "আপনার উপযুক্ত খেদমতের সুযোগ খুঁজে নিন।", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(20.dp))
        
        var query by remember { mutableStateOf("") }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("খেদমত বা প্রতিষ্ঠানের নাম লিখুন") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            trailingIcon = { IconButton(onClick = { /* Handle Search */ }) { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) } }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard(text = "খেদমত খুঁজুন", containerColor = DarkGreenBox, textColor = White, modifier = Modifier.weight(1f).clickable { navController.navigate("search_khedmot") })
            InfoCard(text = "নিয়োগ প্রকাশ", containerColor = GoldBox, textColor = IslamicGreen, modifier = Modifier.weight(1f).clickable { navController.navigate("post_niyog") })
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
                val isGoogleAccount = initialUser?.providerData?.any { it.providerId == "google.com" } == true
                val providerText = if (isGoogleAccount) "Google Sign-In (Firebase)" else "Email / Password (Firebase)"
                Text("প্রমাণীকরণ: $providerText", fontSize = 12.sp, color = Color(0xFF075B45))
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

