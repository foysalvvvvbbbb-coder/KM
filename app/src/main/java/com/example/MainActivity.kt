package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.theme.KaomiKormoTheme
import com.example.ui.theme.DarkGreenBox
import com.example.ui.theme.GoldBox
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.White

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
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                items.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == routes[index],
                        onClick = { navController.navigate(routes[index]) { popUpTo(0) } },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
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

