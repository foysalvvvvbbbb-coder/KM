package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MadrasahDetail(
    val id: String,
    val name: String,
    val location: String,
    val established: String,
    val muhtamim: String,
    val departments: String,
    val phone: String,
    val students: String,
    val isVerified: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MadrasahScreen(
    onOpenDrawer: () -> Unit = {},
    onNavigateToFeedWithFilter: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        AdminRepository.init(context)
    }
    val adminMadrasahs by AdminRepository.madrasahsList.collectAsState()

    val madrasahList = remember(adminMadrasahs) {
        adminMadrasahs.filter { it.verificationStatus != "প্রত্যাখ্যাত" }.map { m ->
            val est = when (m.id) {
                "m1" -> "১৯০১ খ্রিষ্টাব্দ"
                "m2" -> "১৯৩৮ খ্রিষ্টাব্দ"
                "m3" -> "১৯৮০ খ্রিষ্টাব্দ"
                "m4" -> "১৯৫৬ খ্রিষ্টাব্দ"
                "m5" -> "১৯৮৫ খ্রিষ্টাব্দ"
                "m6" -> "১৯৭২ খ্রিষ্টাব্দ"
                else -> "নিবন্ধিত (${m.regNumber.ifBlank { "কওমি মাদ্রাসা" }})"
            }
            val stu = when (m.id) {
                "m1" -> "৭,০০০+ তালিবুল ইলম"
                "m2" -> "৫,০০০+ তালিবুল ইলম"
                "m3" -> "৪,০০০+ তালিবুল ইলম"
                "m4" -> "২,৫০০+ তালিবুল ইলম"
                "m5" -> "২,০০০+ তালিবুল ইলম"
                "m6" -> "১,৫০০+ তালিবুল ইলম"
                else -> "সক্রিয় শিক্ষা বিভাগ"
            }
            MadrasahDetail(
                id = m.id,
                name = m.name,
                location = m.location,
                established = est,
                muhtamim = m.muhtamim,
                departments = m.departments,
                phone = m.phone,
                students = stu,
                isVerified = m.verificationStatus == "যাচাইকৃত"
            )
        }
    }

    val filteredList = remember(searchQuery, madrasahList) {
        if (searchQuery.isBlank()) madrasahList
        else madrasahList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.departments.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "۞  মাদ্রাসা পরিচিতি  ۞",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "মেনু", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "যাচাইকৃত কওমি মাদ্রাসার তালিকা", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Verified, contentDescription = "যাচাইকৃত", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = QawmiDarkGreen)
            )
        },
        containerColor = Color(0xFFF3F6F3)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("মাদ্রাসার নাম বা জেলা দিয়ে খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = QawmiDarkGreen)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QawmiDarkGreen,
                        unfocusedBorderColor = Color(0xFFD4E5DC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // List of Madrasahs
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { madrasah ->
                    MadrasahDetailCard(
                        madrasah = madrasah,
                        onViewCirculars = {
                            onNavigateToFeedWithFilter("নিয়োগ বিজ্ঞপ্তি")
                        },
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${madrasah.phone}"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "নম্বর: ${madrasah.phone}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MadrasahDetailCard(
    madrasah: MadrasahDetail,
    onViewCirculars: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.8.dp, Color(0xFFE2EBE5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE2F4EC)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF0C4E3F),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "যাচাইকৃত মাদ্রাসা",
                            color = Color(0xFF0C4E3F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "স্থাপিত: ${madrasah.established}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(10.dp))

            // Body
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2F1E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_madrasah_thumb),
                        contentDescription = madrasah.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = madrasah.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = QawmiDarkGreen
                    )
                    Spacer(Modifier.height(4.dp))
                    MadrasahInfoRow(Icons.Default.LocationOn, madrasah.location)
                    MadrasahInfoRow(Icons.Default.Person, "মুহতামিম: ${madrasah.muhtamim}")
                    MadrasahInfoRow(Icons.Default.School, "বিভাগ: ${madrasah.departments}")
                    MadrasahInfoRow(Icons.Default.Groups, "শিক্ষার্থী: ${madrasah.students}")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onViewCirculars,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("নিয়োগ বিজ্ঞপ্তি দেখুন", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, QawmiDarkGreen),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = QawmiDarkGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("যোগাযোগ", fontSize = 12.5.sp, color = QawmiDarkGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MadrasahInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 1.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF5A786E),
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF5A786E),
            maxLines = 1
        )
    }
}
