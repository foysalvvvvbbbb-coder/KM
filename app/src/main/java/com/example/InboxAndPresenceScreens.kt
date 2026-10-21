package com.example

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KaomiKormoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextOnlyInboxScreen(
    onBack: () -> Unit,
    onOpenChat: (ChatConversation) -> Unit,
    onViewPublicProfile: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val conversations by ProfileAndInboxRepository.conversations.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF3F6F3)
    val cardBg = if (isDark) Color(0xFF0E2E24) else Color.White
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = AppSettingsManager.tr("ইনবক্স", "Inbox"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                actions = { IconButton(onClick = { /* New Chat Logic */ }) { Icon(imageVector = Icons.Default.AddComment, contentDescription = "New Chat", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg
    ) { paddingValues ->
        if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = AppSettingsManager.tr("আপনার ইনবক্স খালি", "Your inbox is empty"), color = secondaryText)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(conversations, key = { it.id }) { conv ->
                    ConversationListItem(conv = conv, isDark = isDark, onClick = { onOpenChat(conv) }, onAvatarClick = { onViewPublicProfile(conv.participantId) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = if (isDark) Color(0xFF1F4E3F) else Color(0xFFEAEFE9))
                }
            }
        }
    }
}

@Composable
fun ConversationListItem(
    conv: ChatConversation,
    isDark: Boolean = false,
    onClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)
    val secondaryText = if (isDark) Color(0xFF9BC4B5) else Color(0xFF4A6358)

    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(52.dp).clickable { onAvatarClick() }) {
            ProfileAvatarCircle(avatarPreset = if (conv.participantName.contains("মাদ্রাসা")) "madrasah_emerald" else "scholar_green", size = 48.dp)
            Box(modifier = Modifier.size(12.dp).align(Alignment.BottomEnd).offset((-2).dp, (-2).dp).background(if (conv.isOnline) Color(0xFF4ADE80) else Color.Gray, CircleShape).border(2.dp, if (isDark) Color(0xFF071E17) else Color.White, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = conv.participantName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(text = conv.lastMessageTime, fontSize = 11.sp, color = secondaryText)
            }
            Text(text = conv.lastMessage, fontSize = 13.sp, color = secondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextOnlyChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onViewPublicProfile: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val conversations by ProfileAndInboxRepository.conversations.collectAsState()
    val allMessagesMap by ProfileAndInboxRepository.messagesByConversation.collectAsState()
    val isDark by AppSettingsManager.isDarkMode.collectAsState()

    val conversation = remember(conversations, conversationId) {
        conversations.find { it.id == conversationId } ?: ChatConversation(id = conversationId, participantId = conversationId, participantName = "ব্যবহারকারী", participantRole = "সদস্য", lastMessage = "", lastMessageTime = "এখন")
    }
    val messages = allMessagesMap[conversation.id] ?: emptyList()
    var messageInput by remember { mutableStateOf("") }

    val pageBg = if (isDark) Color(0xFF071E17) else Color(0xFFF3F6F3)
    val primaryText = if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onViewPublicProfile(conversation.participantId) }) { ProfileAvatarCircle(size = 38.dp); Spacer(Modifier.width(10.dp)); Column { Text(text = conversation.participantName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(text = if (conversation.isOnline) "অনলাইন" else "অফলাইন", color = Color(0xFFD7F3E3), fontSize = 11.5.sp) } } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3F31))
            )
        },
        containerColor = pageBg,
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = if (isDark) Color(0xFF0E2E24) else Color.White) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = messageInput, onValueChange = { messageInput = it }, placeholder = { Text("মেসেজ লিখুন...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), maxLines = 4)
                    IconButton(onClick = { if (messageInput.isNotBlank()) { ProfileAndInboxRepository.sendTextOnlyMessage(conversation.id, messageInput); messageInput = "" } }) { Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF0E6348)) }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(msg = msg, isDark = isDark)
            }
        }
    }
}

@Composable
fun ChatMessageBubble(msg: ChatMessage, isDark: Boolean) {
    val isMe = msg.isFromMe
    val bubbleColor = if (isMe) Color(0xFF0E6348) else if (isDark) Color(0xFF1C3A2D) else Color.White
    val textColor = if (isMe) Color.White else if (isDark) Color(0xFFE8F5EF) else Color(0xFF093E32)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(shape = RoundedCornerShape(12.dp), color = bubbleColor, border = if (!isMe && !isDark) BorderStroke(1.dp, Color(0xFFEAEFE9)) else null) {
            Column(modifier = Modifier.padding(10.dp, 6.dp)) {
                Text(text = msg.text, color = textColor, fontSize = 14.sp)
                Text(text = msg.timestampText, color = if (isMe) Color.White.copy(0.7f) else Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}
