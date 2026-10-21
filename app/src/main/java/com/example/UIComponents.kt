package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ProfileAvatarCircle(
    avatarPreset: String = "scholar_green",
    photoUri: String = "",
    size: Dp = 40.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                when (avatarPreset) {
                    "scholar_green" -> Color(0xFF14523F)
                    "scholar_gold" -> Color(0xFFD9B66F)
                    "madrasah_emerald" -> Color(0xFF0B3028)
                    else -> Color.LightGray
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri.isNotBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Profile Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

@Composable
fun ProfileDetailItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0E6348),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.5.sp,
                color = secondaryTextColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value.ifBlank { "তথ্য প্রদান করা হয়নি" },
                fontSize = 14.sp,
                color = primaryTextColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

val QawmiDarkGreen = Color(0xFF093F33)
val DeepEmerald = Color(0xFF0B3028)
val ElegantGreen = Color(0xFF14523F)
val WarmOffWhite = Color(0xFFF7F5EE)
val MutedGold = Color(0xFFD9B66F)
