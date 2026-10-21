package com.example

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

/**
 * পুশ নোটিফিকেশন ডেলিভারির প্রকৃত ফলাফল (শুধু ডাটাবেস রেকর্ড নয়, বাস্তব ডিভাইস ডেলিভারি স্ট্যাটাস)
 */
data class PushDeliveryResult(
    val isDeliveredToDevice: Boolean,
    val deliveryStatus: String,
    val failureReason: String = ""
)

object NotificationHelper {
    const val CHANNEL_ID = "qawmi_khedmot_notifications"
    const val CHANNEL_NAME = "কওমি কর্মসংস্থান বিজ্ঞপ্তি"
    const val EXTRA_TARGET_ROUTE = "extra_target_route"
    const val EXTRA_TARGET_ITEM_ID = "extra_target_item_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    private const val PREFS_NAME = "qawmi_notification_prefs"
    private const val KEY_NOTIF_ENABLED = "user_notifications_enabled"
    private const val KEY_BOOST_NOTIF_ENABLED = "user_boost_notifications_enabled"
    private const val KEY_APP_NOTIF_ENABLED = "user_app_notifications_enabled"
    private const val KEY_ANNOUNCE_NOTIF_ENABLED = "user_announce_notifications_enabled"
    private const val KEY_DEVICE_TOKEN = "device_push_token"
    private const val KEY_DEVICE_ID = "device_installation_id"
    private const val KEY_TOKEN_TIMESTAMP = "device_token_updated_at"
    private const val KEY_DELIVERED_IDS = "delivered_notification_keys"
    private const val KEY_READ_IDS = "read_notification_ids"
    private const val KEY_BOOST_FREQ_PREFIX = "boost_last_push_ms_"

    // Reactive state for deep-link navigation when a user taps a system notification or Notification Center item
    val pendingDeepLinkRoute = MutableStateFlow<String?>(null)
    val pendingDeepLinkItemId = MutableStateFlow<String?>(null)

    // Reactive state for user's notification toggle preferences
    val notificationsEnabledFlow = MutableStateFlow(true)
    val boostNotificationsEnabledFlow = MutableStateFlow(true)
    val applicationNotificationsEnabledFlow = MutableStateFlow(true)
    val announcementNotificationsEnabledFlow = MutableStateFlow(true)
    val currentDeviceTokenFlow = MutableStateFlow("")
    val readNotificationIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    fun init(context: Context) {
        createNotificationChannel(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        notificationsEnabledFlow.value = prefs.getBoolean(KEY_NOTIF_ENABLED, true)
        boostNotificationsEnabledFlow.value = prefs.getBoolean(KEY_BOOST_NOTIF_ENABLED, true)
        applicationNotificationsEnabledFlow.value = prefs.getBoolean(KEY_APP_NOTIF_ENABLED, true)
        announcementNotificationsEnabledFlow.value = prefs.getBoolean(KEY_ANNOUNCE_NOTIF_ENABLED, true)
        readNotificationIdsFlow.value = prefs.getStringSet(KEY_READ_IDS, emptySet()) ?: emptySet()
        currentDeviceTokenFlow.value = getOrCreateDeviceToken(context)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "নতুন খেদমত বিজ্ঞপ্তি, বিশেষভাবে প্রচারিত বুস্ট, আবেদন আপডেট ও গুরুত্বপূর্ণ ঘোষণা"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true)
        notificationsEnabledFlow.value = enabled
        return enabled
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply()
        notificationsEnabledFlow.value = enabled
        FirebaseSyncManager.syncCurrentUserNotificationPreference(enabled)
    }

    fun isCategoryEnabled(context: Context, category: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (category.trim()) {
            "Boost" -> prefs.getBoolean(KEY_BOOST_NOTIF_ENABLED, true)
            "Applications" -> prefs.getBoolean(KEY_APP_NOTIF_ENABLED, true)
            "Announcements" -> prefs.getBoolean(KEY_ANNOUNCE_NOTIF_ENABLED, true)
            "Security" -> true // Security alerts are critical
            else -> true
        }
    }

    fun setCategoryEnabled(context: Context, category: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        when (category.trim()) {
            "Boost" -> {
                editor.putBoolean(KEY_BOOST_NOTIF_ENABLED, enabled)
                boostNotificationsEnabledFlow.value = enabled
            }
            "Applications" -> {
                editor.putBoolean(KEY_APP_NOTIF_ENABLED, enabled)
                applicationNotificationsEnabledFlow.value = enabled
            }
            "Announcements" -> {
                editor.putBoolean(KEY_ANNOUNCE_NOTIF_ENABLED, enabled)
                announcementNotificationsEnabledFlow.value = enabled
            }
        }
        editor.apply()
    }

    /**
     * একই বিজ্ঞপ্তির জন্য নির্ধারিত সময়ের (Frequency Limit) মধ্যে পুনরায় নোটিফিকেশন পাঠানো রোধ করে
     */
    fun canSendWithinFrequencyLimit(context: Context, itemKey: String, limitHours: Int): Boolean {
        if (itemKey.isBlank() || limitHours <= 0) return true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSent = prefs.getLong("$KEY_BOOST_FREQ_PREFIX$itemKey", 0L)
        if (lastSent == 0L) return true
        val elapsedMs = System.currentTimeMillis() - lastSent
        val limitMs = limitHours.toLong() * 3600L * 1000L
        return elapsedMs >= limitMs
    }

    fun recordFrequencyTimestamp(context: Context, itemKey: String) {
        if (itemKey.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong("$KEY_BOOST_FREQ_PREFIX$itemKey", System.currentTimeMillis()).apply()
    }

    fun isNotificationRead(notifId: String): Boolean {
        return readNotificationIdsFlow.value.contains(notifId)
    }

    /**
     * Computes the exact unread notification count from real Firebase/AdminRepository notifications,
     * deduplicating by notification ID and excluding read IDs. Never returns hardcoded or duplicate counts.
     */
    fun getUnreadNotificationCount(
        notifications: List<SentNotificationItem> = AdminRepository.sentNotifications.value,
        readIds: Set<String> = readNotificationIdsFlow.value
    ): Int {
        return notifications
            .filter { it.id.isNotBlank() }
            .distinctBy { it.id }
            .count { !readIds.contains(it.id) }
    }

    /**
     * Returns null when unreadCount <= 0 (so no badge/dot/0 is shown),
     * or the formatted count string ("১", "৫", etc.) when unreadCount > 0.
     */
    fun getBadgeText(unreadCount: Int): String? {
        if (unreadCount <= 0) return null
        return AppSettingsManager.formatNumber(unreadCount)
    }

    fun markAsRead(context: Context, notifId: String) {
        if (notifId.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val updated = readNotificationIdsFlow.value.toMutableSet().apply { add(notifId) }
        readNotificationIdsFlow.value = updated
        prefs.edit().putStringSet(KEY_READ_IDS, updated).apply()
    }

    fun markAllAsRead(context: Context, allIds: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val updated = readNotificationIdsFlow.value.toMutableSet().apply { addAll(allIds) }
        readNotificationIdsFlow.value = updated
        prefs.edit().putStringSet(KEY_READ_IDS, updated).apply()
    }

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var devId = prefs.getString(KEY_DEVICE_ID, null)
        if (devId.isNullOrBlank()) {
            devId = "dev_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            prefs.edit().putString(KEY_DEVICE_ID, devId).apply()
        }
        return devId
    }

    fun getOrCreateDeviceToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingToken = prefs.getString(KEY_DEVICE_TOKEN, null)
        val lastUpdated = prefs.getLong(KEY_TOKEN_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24L * 3600L * 1000L

        if (existingToken.isNullOrBlank() || (now - lastUpdated) > thirtyDaysMs) {
            return refreshDeviceToken(context)
        }
        currentDeviceTokenFlow.value = existingToken
        return existingToken
    }

    fun refreshDeviceToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val devId = getDeviceId(context)
        val newToken = "fcm_SyncToken_${devId}_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        prefs.edit()
            .putString(KEY_DEVICE_TOKEN, newToken)
            .putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
            .apply()
        currentDeviceTokenFlow.value = newToken
        FirebaseSyncManager.syncCurrentUserToken(newToken, devId)
        return newToken
    }

    fun hasDelivered(context: Context, dedupKey: String): Boolean {
        if (dedupKey.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_DELIVERED_IDS, emptySet()) ?: emptySet()
        return set.contains(dedupKey)
    }

    fun markDelivered(context: Context, dedupKey: String) {
        if (dedupKey.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = (prefs.getStringSet(KEY_DELIVERED_IDS, emptySet()) ?: emptySet()).toMutableSet()
        current.add(dedupKey)
        val trimmed = if (current.size > 300) current.toList().takeLast(300).toSet() else current
        prefs.edit().putStringSet(KEY_DELIVERED_IDS, trimmed).apply()
    }

    fun clearDeliveredKeyForRetry(context: Context, dedupKey: String) {
        if (dedupKey.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = (prefs.getStringSet(KEY_DELIVERED_IDS, emptySet()) ?: emptySet()).toMutableSet()
        current.remove(dedupKey)
        prefs.edit().putStringSet(KEY_DELIVERED_IDS, current).apply()
    }

    fun matchesAudience(
        targetAudience: String,
        currentUserEmail: String?,
        currentUserRole: String?,
        currentUserLocation: String? = null
    ): Boolean {
        val cleanTarget = targetAudience.trim()
        if (cleanTarget.isBlank() || cleanTarget == "সকল ব্যবহারকারী") return true
        val email = currentUserEmail?.trim()?.lowercase() ?: ""
        if (cleanTarget.contains("@")) {
            return email.isNotEmpty() && cleanTarget.lowercase() == email
        }
        if (cleanTarget.startsWith("এলাকা:")) {
            val targetDistrict = cleanTarget.removePrefix("এলাকা:").trim()
            if (targetDistrict.isBlank() || targetDistrict == "সারাদেশ") return true
            if (currentUserLocation.isNullOrBlank()) return true
            return currentUserLocation.contains(targetDistrict, ignoreCase = true)
        }
        if (cleanTarget == "খেদমতপ্রার্থী") {
            return currentUserRole == null || currentUserRole == "আবেদনকারী" || currentUserRole == "খেদমতপ্রার্থী"
        }
        if (cleanTarget == "মাদ্রাসা প্রতিনিধি") {
            return currentUserRole == null || currentUserRole == "মাদ্রাসা প্রতিনিধি"
        }
        if (cleanTarget == "Admin" || cleanTarget == "সকল Admin") {
            return AdminRepository.isUserAdmin(email)
        }
        return true
    }

    /**
     * বিস্তারিত ফলাফল ও ব্যর্থতার কারণসহ প্রকৃত ডিভাইস পুশ নোটিফিকেশন প্রেরণ করে
     */
    fun deliverSystemNotificationWithResult(
        context: Context,
        notifId: String,
        title: String,
        body: String,
        targetAudience: String = "সকল ব্যবহারকারী",
        targetRoute: String = "khedmot",
        targetItemId: String = "",
        eventKey: String = notifId,
        category: String = "Announcements",
        currentUserEmail: String? = null,
        currentUserRole: String? = null,
        currentUserLocation: String? = null,
        bypassDedupForRetry: Boolean = false
    ): PushDeliveryResult {
        val dedup = eventKey.ifBlank { notifId }
        if (!bypassDedupForRetry && hasDelivered(context, dedup)) {
            return PushDeliveryResult(
                isDeliveredToDevice = false,
                deliveryStatus = "ডুপ্লিকেট রোধকৃত (Duplicate Prevented)",
                failureReason = "একই ইভেন্টের নোটিফিকেশন পূর্বে পাঠানো হয়েছে"
            )
        }
        if (!isNotificationsEnabled(context)) {
            return PushDeliveryResult(
                isDeliveredToDevice = false,
                deliveryStatus = "নোটিফিকেশন বন্ধ (User Opted Out)",
                failureReason = "ব্যবহারকারী পুশ নোটিফিকেশন বন্ধ রেখেছেন"
            )
        }
        if (!isCategoryEnabled(context, category)) {
            return PushDeliveryResult(
                isDeliveredToDevice = false,
                deliveryStatus = "বিভাগ আনসাবস্ক্রাইবড ($category Disabled)",
                failureReason = "ব্যবহারকারী '$category' বিভাগের নোটিফিকেশন বন্ধ রেখেছেন"
            )
        }
        if (!matchesAudience(targetAudience, currentUserEmail, currentUserRole, currentUserLocation)) {
            return PushDeliveryResult(
                isDeliveredToDevice = false,
                deliveryStatus = "টার্গেট প্রাপকের বাইরে (Audience Filtered)",
                failureReason = "প্রাপক পরিধি '$targetAudience'-এর সাথে ব্যবহারকারী মিলছে না"
            )
        }
        if (!hasNotificationPermission(context)) {
            return PushDeliveryResult(
                isDeliveredToDevice = false,
                deliveryStatus = "পুশ ব্যর্থ: ডিভাইস অনুমতি নেই (Permission Denied)",
                failureReason = "Android POST_NOTIFICATIONS runtime permission অনুমোদিত নয়"
            )
        }

        return try {
            createNotificationChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TARGET_ROUTE, targetRoute)
                putExtra(EXTRA_TARGET_ITEM_ID, targetItemId)
                putExtra(EXTRA_NOTIFICATION_ID, notifId)
            }
            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                dedup.hashCode(),
                intent,
                pendingFlags
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(dedup.hashCode(), notification)
            markDelivered(context, dedup)
            PushDeliveryResult(
                isDeliveredToDevice = true,
                deliveryStatus = "ডিভাইসে পুশ প্রেরিত (Push Delivered)",
                failureReason = ""
            )
        } catch (e: SecurityException) {
            PushDeliveryResult(
                isDeliveredToDevice = false,
                deliveryStatus = "পুশ ব্যর্থ (SecurityException)",
                failureReason = e.localizedMessage ?: "SecurityException while posting notification"
            )
        } catch (e: Exception) {
            PushDeliveryResult(
                isDeliveredToDevice = false,
                deliveryStatus = "পুশ ব্যর্থ (Delivery Exception)",
                failureReason = e.localizedMessage ?: "Unknown notification error"
            )
        }
    }

    fun deliverSystemNotification(
        context: Context,
        notifId: String,
        title: String,
        body: String,
        targetAudience: String = "সকল ব্যবহারকারী",
        targetRoute: String = "khedmot",
        targetItemId: String = "",
        eventKey: String = notifId,
        currentUserEmail: String? = null,
        currentUserRole: String? = null
    ): Boolean {
        return deliverSystemNotificationWithResult(
            context = context,
            notifId = notifId,
            title = title,
            body = body,
            targetAudience = targetAudience,
            targetRoute = targetRoute,
            targetItemId = targetItemId,
            eventKey = eventKey,
            currentUserEmail = currentUserEmail,
            currentUserRole = currentUserRole
        ).isDeliveredToDevice
    }

    fun triggerDirectNavigation(route: String, itemId: String = "") {
        if (route.isNotBlank()) {
            pendingDeepLinkRoute.value = route
            pendingDeepLinkItemId.value = itemId.takeIf { it.isNotBlank() }
        }
    }

    fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val route = intent.getStringExtra(EXTRA_TARGET_ROUTE)
        val itemId = intent.getStringExtra(EXTRA_TARGET_ITEM_ID)
        if (!route.isNullOrBlank()) {
            pendingDeepLinkRoute.value = route
            pendingDeepLinkItemId.value = itemId
            intent.removeExtra(EXTRA_TARGET_ROUTE)
            intent.removeExtra(EXTRA_TARGET_ITEM_ID)
        }
    }

    fun shouldReceiveForAudience(
        targetAudience: String,
        currentUserEmail: String?,
        currentUserRole: String?,
        currentUserLocation: String? = null
    ): Boolean = matchesAudience(targetAudience, currentUserEmail, currentUserRole, currentUserLocation)

    fun onUserLogout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_DEVICE_TOKEN).remove(KEY_TOKEN_TIMESTAMP).apply()
        currentDeviceTokenFlow.value = ""
        FirebaseSyncManager.syncCurrentUserToken("", getDeviceId(context))
    }

    fun consumeDeepLink() {
        pendingDeepLinkRoute.value = null
        pendingDeepLinkItemId.value = null
    }
}
