package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// Color palette matching the reference image exactly
val QawmiDarkGreen = Color(0xFF093F33)
val QawmiCardBg = Color(0xFFFFFFFF)
val QawmiMintPill = Color(0xFFE2F4EC)
val QawmiMintText = Color(0xFF0C4E3F)
val QawmiCandidateBadgeBg = Color(0xFFE3EFF9)
val QawmiCandidateBadgeText = Color(0xFF145688)
val QawmiGold = Color(0xFFE2C98F)
val QawmiTextDark = Color(0xFF093E32)
val QawmiTextMuted = Color(0xFF4C6B61)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onOpenDrawer: () -> Unit = {},
    initialFilter: String = "সব পোস্ট"
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        FeedRepository.init(context)
    }

    var selectedFilter by remember(initialFilter) { mutableStateOf(initialFilter) }
    LaunchedEffect(initialFilter) {
        selectedFilter = initialFilter
    }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    var showDistrictDialog by remember { mutableStateOf(false) }

    // Dialog state
    var applyingPost by remember { mutableStateOf<FeedItem?>(null) }
    var viewingCandidate by remember { mutableStateOf<FeedItem?>(null) }

    val allPosts = FeedRepository.posts

    val filteredPosts = remember(selectedFilter, selectedDistrict, allPosts.size) {
        allPosts.filter { post ->
            val matchesType = when (selectedFilter) {
                "নিয়োগ বিজ্ঞপ্তি", "খেদমতের বিজ্ঞপ্তি" -> post.type == FeedItemType.MADRASAH_NOTICE
                "আবেদন", "খেদমতের আবেদন" -> post.type == FeedItemType.CANDIDATE_APPLICATION
                "পছন্দের খেদমত" -> FeedRepository.isBookmarked(post.id)
                else -> true
            }
            val matchesDistrict = if (selectedDistrict != null) {
                post.location.contains(selectedDistrict!!) || post.secondaryInfo.contains(selectedDistrict!!)
            } else true

            matchesType && matchesDistrict
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
                            text = "۞  ফিড  ۞",
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
                    Box(modifier = Modifier.padding(end = 6.dp)) {
                        IconButton(onClick = { 
                            Toast.makeText(context, "নতুন কোনো নোটিফিকেশন নেই", Toast.LENGTH_SHORT).show() 
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "বিজ্ঞপ্তি", tint = Color.White)
                        }
                        // Red notification badge dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFE53935), CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                        )
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
            // Horizontal Filter Chips row
            FilterChipsRow(
                selectedFilter = selectedFilter,
                selectedDistrict = selectedDistrict,
                onFilterSelected = { selectedFilter = it },
                onDistrictClick = { showDistrictDialog = true }
            )

            // Posts List
            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = QawmiTextMuted, modifier = Modifier.size(52.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "কোনো পোস্ট পাওয়া যায়নি",
                            fontWeight = FontWeight.SemiBold,
                            color = QawmiTextDark,
                            fontSize = 16.sp
                        )
                        if (selectedDistrict != null) {
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = { selectedDistrict = null }) {
                                Text("জেলা ফিল্টার মুছুন", color = QawmiMintText)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        if (post.type == FeedItemType.MADRASAH_NOTICE) {
                            MadrasahNoticeCard(
                                post = post,
                                onApply = { applyingPost = post },
                                onBookmarkToggle = {
                                    val isBookmarked = FeedRepository.toggleBookmark(context, post.id)
                                    val msg = if (isBookmarked) "পছন্দের খেদমতে যোগ করা হয়েছে" else "পছন্দের তালিকা থেকে সরানো হয়েছে"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            CandidateApplicationCard(
                                post = post,
                                onViewProfile = { viewingCandidate = post },
                                onBookmarkToggle = {
                                    val isBookmarked = FeedRepository.toggleBookmark(context, post.id)
                                    val msg = if (isBookmarked) "সংরক্ষণ করা হয়েছে" else "সংরক্ষণ তালিকা থেকে সরানো হয়েছে"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // Bottom Motivational Quote Banner
                    item {
                        BottomMotivationCard()
                    }
                }
            }
        }
    }

    // Apply Confirmation Dialog
    applyingPost?.let { post ->
        ApplyDialog(
            post = post,
            onDismiss = { applyingPost = null },
            onConfirm = {
                FeedRepository.applyToPost(context, post.id)
                applyingPost = null
                Toast.makeText(context, "“${post.title}”-এ আপনার খেদমতের আবেদন সফলভাবে দাখিল হয়েছে।", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Candidate Profile View Dialog
    viewingCandidate?.let { post ->
        CandidateProfileDialog(
            candidate = post,
            onDismiss = { viewingCandidate = null }
        )
    }

    // District Selector Dialog
    if (showDistrictDialog) {
        DistrictFilterDialog(
            selectedDistrict = selectedDistrict,
            onSelect = {
                selectedDistrict = it
                showDistrictDialog = false
            },
            onDismiss = { showDistrictDialog = false }
        )
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    selectedDistrict: String?,
    onFilterSelected: (String) -> Unit,
    onDistrictClick: () -> Unit
) {
    val filters = listOf("সব পোস্ট", "নিয়োগ বিজ্ঞপ্তি", "আবেদন")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) QawmiDarkGreen else Color(0xFFF0F5F2),
                modifier = Modifier.clickable { onFilterSelected(filter) }
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) Color.White else QawmiTextDark,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }

        // District Filter Chip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (selectedDistrict != null) QawmiMintPill else Color(0xFFF0F5F2),
            border = if (selectedDistrict != null) BorderStroke(1.dp, QawmiMintText) else null,
            modifier = Modifier.clickable(onClick = onDistrictClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDistrict ?: "জেলা",
                    color = if (selectedDistrict != null) QawmiMintText else QawmiTextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (selectedDistrict != null) QawmiMintText else QawmiTextDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Tune/Settings icon button
        Surface(
            shape = CircleShape,
            color = Color(0xFFF0F5F2),
            modifier = Modifier.clickable(onClick = onDistrictClick)
        ) {
            Icon(
                Icons.Default.Tune,
                contentDescription = "ফিল্টার",
                tint = QawmiDarkGreen,
                modifier = Modifier.padding(8.dp).size(18.dp)
            )
        }
    }
}

@Composable
fun MadrasahNoticeCard(
    post: FeedItem,
    onApply: () -> Unit,
    onBookmarkToggle: () -> Unit
) {
    val isBookmarked = remember(post.id) { FeedRepository.isBookmarked(post.id) }
    val isApplied = remember(post.id) { FeedRepository.isApplied(post.id) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QawmiCardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Badge & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = QawmiMintPill
                ) {
                    Text(
                        text = "নিয়োগ বিজ্ঞপ্তি",
                        color = QawmiMintText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = post.timeAgo,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Body: Thumbnail + Info
            Row(verticalAlignment = Alignment.Top) {
                // Madrasah Thumbnail
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2F1E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_madrasah_thumb),
                        contentDescription = post.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = QawmiTextDark
                    )
                    Spacer(Modifier.height(4.dp))

                    FeedInfoRow(icon = Icons.Default.LocationOn, text = post.location)
                    FeedInfoRow(icon = Icons.Default.School, text = "পদ: ${post.role}")
                    FeedInfoRow(icon = Icons.Default.WorkspacePremium, text = "শিক্ষাগত যোগ্যতা: ${post.qualification}")
                    FeedInfoRow(icon = Icons.Default.Payments, text = "বেতন: ${post.extraInfo}")
                    FeedInfoRow(icon = Icons.Default.CalendarToday, text = "আবেদনের শেষ তারিখ: ${post.secondaryInfo}")
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
                    onClick = onApply,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApplied) Color(0xFF2E7D32) else QawmiDarkGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isApplied) "আবেদন করা হয়েছে ✓" else "আবেদন করুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "বুকমার্ক",
                        tint = if (isBookmarked) QawmiMintText else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CandidateApplicationCard(
    post: FeedItem,
    onViewProfile: () -> Unit,
    onBookmarkToggle: () -> Unit
) {
    val isBookmarked = remember(post.id) { FeedRepository.isBookmarked(post.id) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QawmiCardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Badge & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = QawmiCandidateBadgeBg
                ) {
                    Text(
                        text = "চাকরিপ্রার্থীর আবেদন",
                        color = QawmiCandidateBadgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = post.timeAgo,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Body: Candidate Avatar + Info
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0EFE8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = post.title,
                        tint = QawmiDarkGreen,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = QawmiTextDark
                    )
                    Spacer(Modifier.height(4.dp))

                    FeedInfoRow(icon = Icons.Default.LocationOn, text = post.location)
                    FeedInfoRow(icon = Icons.Default.Person, text = "পদ: ${post.role}")
                    FeedInfoRow(icon = Icons.Default.School, text = "শিক্ষাগত যোগ্যতা: ${post.qualification}")
                    FeedInfoRow(icon = Icons.Default.WorkOutline, text = "অভিজ্ঞতা: ${post.extraInfo}")
                    FeedInfoRow(icon = Icons.Default.Explore, text = "পছন্দের জেলা: ${post.secondaryInfo}")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, QawmiDarkGreen),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "প্রোফাইল দেখুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = QawmiDarkGreen
                    )
                }

                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "বুকমার্ক",
                        tint = if (isBookmarked) QawmiMintText else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeedInfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 1.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = QawmiTextMuted,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = QawmiTextMuted,
            maxLines = 1
        )
    }
}

@Composable
fun BottomMotivationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4EC)),
        border = BorderStroke(0.8.dp, Color(0xFFC7E5D5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = QawmiDarkGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "“কওমি মাদ্রাসা খেদমতেই\nআখেরাতের সফলতার পথ”",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = QawmiDarkGreen,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ApplyDialog(
    post: FeedItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var candidateName by remember { mutableStateOf("") }
    var candidatePhone by remember { mutableStateOf("") }
    var candidateQual by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "খেদমতের আবেদন",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${post.title} — ${post.role}",
                    fontSize = 13.sp,
                    color = QawmiTextMuted
                )
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = candidateName,
                    onValueChange = { candidateName = it },
                    label = { Text("আপনার নাম") },
                    placeholder = { Text("যেমন: মাওলানা আব্দুল্লাহ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = candidatePhone,
                    onValueChange = { candidatePhone = it },
                    label = { Text("মোবাইল নম্বর") },
                    placeholder = { Text("017XXXXXXXX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = candidateQual,
                    onValueChange = { candidateQual = it },
                    label = { Text("ইলমি যোগ্যতা / জামাত") },
                    placeholder = { Text("যেমন: দাওরায়ে হাদীস, ইফতা ইত্যাদি") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen)
                    ) {
                        Text("আবেদন দাখিল করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateProfileDialog(
    candidate: FeedItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0EFE8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = candidate.title,
                        tint = QawmiDarkGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = candidate.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiTextDark
                )
                Text(
                    text = "খেদমতপ্রার্থী • ${candidate.location}",
                    fontSize = 13.sp,
                    color = QawmiTextMuted
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileFieldRow(label = "আকাঙ্ক্ষিত পদ:", value = candidate.role)
                    ProfileFieldRow(label = "ইলমি যোগ্যতা:", value = candidate.qualification)
                    ProfileFieldRow(label = "খেদমতের অভিজ্ঞতা:", value = candidate.extraInfo)
                    ProfileFieldRow(label = "পছন্দের জেলা:", value = candidate.secondaryInfo)
                    ProfileFieldRow(label = "যোগাযোগ নম্বর:", value = candidate.contactPhone)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QawmiDarkGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("বন্ধ করুন", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProfileFieldRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = QawmiTextDark)
    }
}

@Composable
fun DistrictFilterDialog(
    selectedDistrict: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val districts = listOf(
        "সকল জেলা", "ঢাকা", "চট্টগ্রাম", "কুমিল্লা", "সিলেট", 
        "রাজশাহী", "বরিশাল", "খুলনা", "ময়মনসিংহ", "রংপুর", "নোয়াখালী", "বগুড়া"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "জেলা নির্বাচন করুন",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = QawmiDarkGreen
                )
                Spacer(Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(districts) { district ->
                        val isSelected = if (district == "সকল জেলা") selectedDistrict == null else selectedDistrict == district
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (district == "সকল জেলা") onSelect(null) else onSelect(district)
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = district,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) QawmiMintText else QawmiTextDark
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = QawmiMintText)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("বন্ধ করুন", color = Color.Gray)
                }
            }
        }
    }
}
