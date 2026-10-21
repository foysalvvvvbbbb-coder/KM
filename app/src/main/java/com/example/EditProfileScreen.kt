package com.example

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KaomiKormoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val profile by ProfileAndInboxRepository.currentUserProfile.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()

    var fullName by remember { mutableStateOf(profile.fullName) }
    var bio by remember { mutableStateOf(profile.bio) }
    var location by remember { mutableStateOf(profile.location) }
    var phone by remember { mutableStateOf(profile.phone) }
    var madrasahName by remember { mutableStateOf(profile.madrasahName) }
    var qualification by remember { mutableStateOf(profile.qualification) }
    var teachingSubjects by remember { mutableStateOf(profile.teachingSubjects) }
    var experience by remember { mutableStateOf(profile.experience) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            ProfileAndInboxRepository.updateProfile(profile.copy(photoUri = uri.toString()))
            Toast.makeText(context, AppSettingsManager.tr("প্রোফাইল ছবি আপডেট করা হয়েছে", "Profile photo updated"), Toast.LENGTH_SHORT).show()
        }
    }

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF5F7F2)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("প্রোফাইল সম্পাদনা", "Edit Profile"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp).padding(end = 12.dp))
                    } else {
                        TextButton(
                            onClick = {
                                isSaving = true
                                val updated = profile.copy(
                                    fullName = fullName, bio = bio, location = location,
                                    phone = phone, madrasahName = madrasahName,
                                    qualification = qualification, teachingSubjects = teachingSubjects,
                                    experience = experience
                                )
                                ProfileAndInboxRepository.updateProfile(updated) { success ->
                                    isSaving = false
                                    if (success) {
                                        Toast.makeText(context, AppSettingsManager.tr("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে", "Profile saved successfully"), Toast.LENGTH_SHORT).show()
                                        onSaved()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text(AppSettingsManager.tr("সংরক্ষণ", "Save"), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Profile Photo Section
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(100.dp)) {
                        ProfileAvatarCircle(avatarPreset = profile.avatarPreset, photoUri = profile.photoUri, size = 100.dp)
                        IconButton(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.align(Alignment.BottomEnd).background(Color(0xFF0E6348), RoundedCornerShape(12.dp)).size(32.dp)) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Change", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(text = AppSettingsManager.tr("প্রোফাইল ছবি পরিবর্তন করুন", "Change Profile Photo"), color = Color(0xFF0E6348), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Information Fields
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = AppSettingsManager.tr("ব্যক্তিগত তথ্য", "Personal Info"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text(AppSettingsManager.tr("পূর্ণ নাম", "Full Name")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(AppSettingsManager.tr("মোবাইল নম্বর", "Phone Number")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text(AppSettingsManager.tr("এলাকা (জেলা, বিভাগ)", "Location")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text(AppSettingsManager.tr("সংক্ষিপ্ত পরিচিতি", "Short Bio")) }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp))
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = AppSettingsManager.tr("খেদমত সংক্রান্ত তথ্য", "Khedmot Info"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    OutlinedTextField(value = madrasahName, onValueChange = { madrasahName = it }, label = { Text(AppSettingsManager.tr("বর্তমান/সর্বশেষ মাদ্রাসা", "Madrasah Name")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text(AppSettingsManager.tr("সর্বোচ্চ শিক্ষাগত যোগ্যতা", "Qualification")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = teachingSubjects, onValueChange = { teachingSubjects = it }, label = { Text(AppSettingsManager.tr("পড়ানোর বিষয় / পছন্দ", "Subjects")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text(AppSettingsManager.tr("অভিজ্ঞতা", "Experience")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            }
        }
    }
}
