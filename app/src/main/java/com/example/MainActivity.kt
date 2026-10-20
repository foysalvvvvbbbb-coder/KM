package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns
import com.example.ui.theme.KaomiKormoTheme
import com.example.ui.theme.DarkGreenBox
import com.example.ui.theme.GoldBox
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.White

// Theme Colors
val DeepEmerald = Color(0xFF0B3028)
val ElegantGreen = Color(0xFF14523F)
val WarmOffWhite = Color(0xFFF7F5EE)
val MutedGold = Color(0xFFD9B66F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val items = listOf("হোম" to Icons.Default.Home, "খেদমত" to Icons.Default.Work, "আবেদন" to Icons.Default.List, "প্রোফাইল" to Icons.Default.Person)
    val routes = listOf("home", "khedmot", "abedon", "profile")
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "home" || currentRoute == "khedmot" || currentRoute == "abedon" || currentRoute == "profile") {
                NavigationBar(containerColor = Color(0xFF1E1E1E), tonalElevation = 8.dp) {
                    items.forEachIndexed { index, (label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == routes[index],
                            onClick = { navController.navigate(routes[index]) { popUpTo(0) } },
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
            startDestination = "auth", 
            Modifier.padding(innerPadding)
        ) {
            composable("auth") { AuthScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("khedmot") { SimpleScreen("খেদমত") }
            composable("abedon") { SimpleScreen("আবেদন") }
            composable("profile") { SimpleScreen("প্রোফাইল") }
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

fun getAuthErrorMessage(exception: Exception?): String {
    return when (exception) {
        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "অ্যাকাউন্টটি খুঁজে পাওয়া যায়নি।"
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "ইমেইল বা পাসওয়ার্ড ভুল।"
        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "এই ইমেইলটি ইতিমধ্যে ব্যবহৃত হয়েছে।"
        is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "পাসওয়ার্ডটি খুব সহজ, আরও শক্তিশালী পাসওয়ার্ড ব্যবহার করুন।"
        else -> "কিছু ভুল হয়েছে, পুনরায় চেষ্টা করুন।"
    }
}

@Composable
fun IslamicLoginScreen(navController: NavController, onRegister: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).border(1.dp, MutedGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmOffWhite)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("কওমি কর্মসংস্থান", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ElegantGreen)
                Text("লগইন", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ElegantGreen)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(email, { email = it }, label = { Text("ইমেইল") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(password, { password = it }, label = { Text("পাসওয়ার্ড") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle") } })
                error?.let { Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                TextButton(onClick = { /* Handle Forgot */ }, modifier = Modifier.align(Alignment.End)) { Text("পাসওয়ার্ড ভুলে গেছেন?", color = ElegantGreen) }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (email.isBlank() || password.isBlank()) error = "সব ফিল্ড পূরণ করুন"
                    else auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { if (it.isSuccessful) navController.navigate("home") { popUpTo(0) } else error = getAuthErrorMessage(it.exception) }
                }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen)) { Text("লগইন করুন", color = WarmOffWhite, fontSize = 16.sp) }
                TextButton(onClick = onRegister) { Text("নতুন অ্যাকাউন্ট তৈরি করুন", color = MutedGold) }
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
    var error by remember { mutableStateOf<String?>(null) }
    
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).border(1.dp, MutedGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmOffWhite)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("নতুন নিবন্ধন", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ElegantGreen)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(name, { name = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(email, { email = it }, label = { Text("ইমেইল") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(password, { password = it }, label = { Text("পাসওয়ার্ড") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), visualTransformation = PasswordVisualTransformation())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(confirmPassword, { confirmPassword = it }, label = { Text("পাসওয়ার্ড নিশ্চিত করুন") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), visualTransformation = PasswordVisualTransformation())
                error?.let { Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || password != confirmPassword) error = "সব তথ্য সঠিকভাবে দিন"
                    else auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { if (it.isSuccessful) navController.navigate("home") { popUpTo(0) } else error = getAuthErrorMessage(it.exception) }
                }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen)) { Text("নিবন্ধন করুন", color = WarmOffWhite, fontSize = 16.sp) }
                TextButton(onClick = onLogin) { Text("লগইন করুন", color = MutedGold) }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(text = "কওমি কর্মসংস্থান বাংলাদেশ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "আসসালামু আলাইকুম", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(text = "আপনার উপযুক্ত খেদমতের সুযোগ খুঁজে নিন।", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
fun SimpleScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(title) }
}

