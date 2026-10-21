package com.example

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

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
fun IslamicLoginScreen(navController: NavController, onRegister: () -> Unit) {
    val context = LocalContext.current
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
                        Color(0xFF227357),
                        Color(0xFF0F4232),
                        Color(0xFF0B3A2D)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val paintFar = Color(0xFF14533F).copy(alpha = 0.6f)
                    val pathFar = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height - 30f)
                        quadraticTo(size.width * 0.25f, size.height - 85f, size.width * 0.5f, size.height - 55f)
                        quadraticTo(size.width * 0.75f, size.height - 85f, size.width, size.height - 30f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(pathFar, paintFar)

                    val paintNear = Color(0xFF104434)
                    val pathNear = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height - 45f)
                        cubicTo(size.width * 0.3f, size.height - 45f, size.width * 0.4f, size.height - 100f, size.width * 0.5f, size.height - 100f)
                        cubicTo(size.width * 0.6f, size.height - 100f, size.width * 0.7f, size.height - 45f, size.width, size.height - 30f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(pathNear, paintNear)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(1.5.dp).height(40.dp).background(Color(0xFFE2C98F)))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF14523F))
                                .border(1.5.dp, Color(0xFFE2C98F), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = "Lantern", tint = Color(0xFFE2C98F), modifier = Modifier.size(24.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(1.5.dp).height(40.dp).background(Color(0xFFE2C98F)))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF14523F))
                                .border(1.5.dp, Color(0xFFE2C98F), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = "Lantern", tint = Color(0xFFE2C98F), modifier = Modifier.size(24.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0xFF165A46))
                            .border(2.dp, Color(0xFFE2C98F), RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Logo", tint = Color(0xFFE2C98F), modifier = Modifier.size(60.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(text = "কওমি কর্মসংস্থান", color = WarmOffWhite, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = "বাংলাদেশ", color = MutedGold, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-30).dp),
                shape = RoundedCornerShape(24.dp),
                color = WarmOffWhite,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "লগইন করুন", color = ElegantGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(text = "আপনার একাউন্টে প্রবেশ করুন", color = Color.Gray, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))

                    if (error != null) {
                        Text(text = error!!, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = { Text("ইমেইল") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElegantGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("পাসওয়ার্ড") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ElegantGreen) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showForgotDialog = true }) {
                            Text("পাসওয়ার্ড ভুলে গেছেন?", color = ElegantGreen, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                error = "ইমেইল ও পাসওয়ার্ড উভয়ই প্রদান করুন"
                                return@Button
                            }
                            isLoading = true
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                                    } else {
                                        error = getAuthErrorMessage(task.exception)
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("লগইন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onRegister) {
                        Text("অ্যাকাউন্ট নেই? নতুন তৈরি করুন", color = ElegantGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showForgotDialog) {
        ForgotPasswordDialog(initialEmail = email) { showForgotDialog = false }
    }
}

@Composable
fun IslamicRegisterScreen(navController: NavController, onLogin: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF227357), Color(0xFF0F4232))))
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF165A46))
                    .border(1.5.dp, Color(0xFFE2C98F), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFFE2C98F), modifier = Modifier.size(45.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "নতুন অ্যাকাউন্ট", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(30.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = WarmOffWhite
            ) {
                Column(modifier = Modifier.padding(26.dp)) {
                    if (error != null) {
                        Text(text = error!!, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = { Text("পূর্ণ নাম") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElegantGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = { Text("ইমেইল") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElegantGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ElegantGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || email.isBlank() || password.length < 6) {
                                error = "সবগুলো ঘর সঠিকভাবে পূরণ করুন"
                                return@Button
                            }
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val trimmedName = name.trim()
                                        
                                        // Cache name locally
                                        try {
                                            val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                            user?.uid?.let { uid ->
                                                prefs.edit().putString("name_$uid", trimmedName).apply()
                                            }
                                        } catch (_: Exception) {}

                                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                            .setDisplayName(trimmedName)
                                            .build()
                                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                            user.reload().addOnCompleteListener {
                                                isLoading = false
                                                navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        error = getAuthErrorMessage(task.exception)
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text(if (isLoading) "নিবন্ধন হচ্ছে..." else "নিবন্ধন করুন", color = WarmOffWhite, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("ইতিমধ্যে অ্যাকাউন্ট আছে? লগইন করুন", color = ElegantGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    initialEmail: String = "",
    onDismiss: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var emailInput by remember { mutableStateOf(initialEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(text = "পাসওয়ার্ড রিসেট", fontWeight = FontWeight.Bold, color = ElegantGreen, fontSize = 18.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (successMessage != null) {
                    Text(text = successMessage!!, color = Color(0xFF1B5E20), fontSize = 13.sp)
                } else {
                    Text(text = "আপনার নিবন্ধিত ইমেইল ঠিকানাটি লিখুন। আমরা পাসওয়ার্ড রিসেট করার একটি লিংক পাঠাব।", fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(value = emailInput, onValueChange = { emailInput = it; errorMessage = null }, label = { Text("আপনার ইমেইল") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading, shape = RoundedCornerShape(8.dp))
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            if (successMessage != null) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen)) { Text("ঠিক আছে") }
            } else {
                Button(
                    onClick = {
                        val error = validatePasswordResetEmail(emailInput)
                        if (error != null) { errorMessage = error } else {
                            isLoading = true
                            auth.sendPasswordResetEmail(emailInput.trim()).addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) { successMessage = "পাসওয়ার্ড রিসেট লিংক আপনার ইমেইলে পাঠানো হয়েছে।" }
                                else { errorMessage = getPasswordResetErrorMessage(task.exception) }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen)
                ) { Text(if (isLoading) "অপেক্ষা করুন..." else "লিংক পাঠান") }
            }
        },
        dismissButton = {
            if (successMessage == null) {
                TextButton(onClick = onDismiss, enabled = !isLoading) { Text("বাতিল", color = Color.Gray) }
            }
        }
    )
}

