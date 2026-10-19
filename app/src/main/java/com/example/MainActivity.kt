package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = { AppNavigationBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Text(text = "কওমি কর্মসংস্থান বাংলাদেশ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "আসসালামু আলাইকুম", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(text = "আপনার উপযুক্ত খেদমতের সুযোগ খুঁজে নিন।", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("খেদমত বা প্রতিষ্ঠানের নাম লিখুন") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(text = "খেদমত খুঁজুন", containerColor = DarkGreenBox, textColor = White, modifier = Modifier.weight(1f))
                InfoCard(text = "নিয়োগ প্রকাশ", containerColor = GoldBox, textColor = IslamicGreen, modifier = Modifier.weight(1f))
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
fun AppNavigationBar() {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        val items = listOf("হোম" to Icons.Default.Home, "খেদমত" to Icons.Default.Work, "আবেদন" to Icons.Default.List, "প্রোফাইল" to Icons.Default.Person)
        items.forEachIndexed { index, (label, icon) ->
            NavigationBarItem(
                selected = index == 0, 
                onClick = {}, 
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

