package com.example

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Global App Settings Manager for Dark Mode and Language Selection ("bn" / "en").
 * Works across the entire application and persists in SharedPreferences + syncs with Firebase.
 */
object AppSettingsManager {
    private const val PREFS_NAME = "qawmi_app_settings_prefs"
    private const val KEY_DARK_MODE = "app_dark_mode_enabled"
    private const val KEY_LANGUAGE = "app_language_code" // "bn" or "en"

    val isDarkMode = MutableStateFlow(false)
    val currentLanguage = MutableStateFlow("bn")

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isDarkMode.value = prefs.getBoolean(KEY_DARK_MODE, false)
        currentLanguage.value = prefs.getString(KEY_LANGUAGE, "bn") ?: "bn"
    }

    fun setDarkMode(context: Context?, enabled: Boolean) {
        isDarkMode.value = enabled
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean(KEY_DARK_MODE, enabled)
            ?.apply()
    }

    fun setLanguage(context: Context?, langCode: String) {
        val normalized = if (langCode.lowercase().startsWith("en")) "en" else "bn"
        currentLanguage.value = normalized
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_LANGUAGE, normalized)
            ?.apply()
    }

    fun tr(bn: String, en: String): String {
        return if (currentLanguage.value == "en") en else bn
    }

    fun formatNumber(value: Int): String {
        return if (currentLanguage.value == "en") {
            value.toString()
        } else {
            DateUtils.toBengaliDigits(value.toString())
        }
    }
}

data class UserProfileData(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "ঢাকা, ঢাকা বিভাগ",
    val district: String = "ঢাকা",
    val division: String = "ঢাকা বিভাগ",
    val bio: String = "দাওরায়ে হাদীস (তাকমীল) ও ইফতা সম্পন্ন। দ্বীনি খেদমতে আগ্রহী।",
    val madrasahName: String = "আল-জামিয়া ইসলামিয়া, ঢাকা",
    val qualification: String = "দাওরায়ে হাদিস",
    val teachingSubjects: String = "আরবি, ফিকহ, ইসলামিক স্টাডিজ",
    val skills: String = "দারস প্রদান, তারবিয়াত ও ছাত্র পরিচালনা",
    val experience: String = "৩ বছর",
    val preferredKhedmotType: String = "শিক্ষকতা, দাওয়াহ",
    val roleLabel: String = "খেদমতপ্রার্থী",
    val designation: String = "মাদরাসা শিক্ষক",
    val photoUri: String = "",
    val avatarPreset: String = "scholar_green", // scholar_green, scholar_gold, madrasah_emerald, no_photo
    val isVerified: Boolean = true,
    val isOnline: Boolean = true,
    val lastActiveMillis: Long = System.currentTimeMillis(),
    val showOnlineStatus: Boolean = true,
    val allowInboxMessages: Boolean = true
) {
    fun completionPercentage(): Int {
        val checks = listOf(
            fullName.isNotBlank(),
            email.isNotBlank(),
            phone.isNotBlank(),
            location.isNotBlank(),
            district.isNotBlank(),
            division.isNotBlank(),
            bio.isNotBlank(),
            madrasahName.isNotBlank(),
            qualification.isNotBlank(),
            teachingSubjects.isNotBlank() && skills.isNotBlank() && experience.isNotBlank() && preferredKhedmotType.isNotBlank(),
            photoUri.isNotBlank() || avatarPreset != "no_photo"
        )
        val completed = checks.count { it }
        if (photoUri.isBlank() && avatarPreset == "scholar_green" && completed >= 8) {
            return 85
        }
        return ((completed.toFloat() / checks.size.toFloat()) * 100f).roundToInt().coerceIn(20, 100)
    }

    fun getEffectiveOnlineState(viewerIsSelf: Boolean = false): Boolean {
        return if (!showOnlineStatus && !viewerIsSelf) false else isOnline
    }

    fun getFormattedLastSeen(viewerIsSelf: Boolean = false): String {
        if (!showOnlineStatus && !viewerIsSelf) {
            return AppSettingsManager.tr("স্ট্যাটাস গোপন রাখা হয়েছে", "Status Hidden")
        }
        if (isOnline) {
            return AppSettingsManager.tr("বর্তমানে সক্রিয়", "Active Now")
        }
        val diffMillis = (System.currentTimeMillis() - lastActiveMillis).coerceAtLeast(0L)
        val minutes = diffMillis / (60 * 1000L)
        return when {
            minutes < 1 -> AppSettingsManager.tr("এইমাত্র সক্রিয় ছিলেন", "Active just now")
            minutes < 60 -> AppSettingsManager.tr(
                "শেষ সক্রিয়: ${DateUtils.toBengaliDigits(minutes.toString())} মিনিট আগে",
                "Last seen ${minutes}m ago"
            )
            minutes < 1440 -> {
                val hours = minutes / 60
                AppSettingsManager.tr(
                    "শেষ সক্রিয়: ${DateUtils.toBengaliDigits(hours.toString())} ঘণ্টা আগে",
                    "Last seen ${hours}h ago"
                )
            }
            else -> {
                val days = minutes / 1440
                AppSettingsManager.tr(
                    "শেষ সক্রিয়: ${DateUtils.toBengaliDigits(days.toString())} দিন আগে",
                    "Last seen ${days}d ago"
                )
            }
        }
    }
}

data class UserApplicationRecord(
    val id: String,
    val postId: String,
    val applicantUid: String,
    val applicantName: String,
    val applicantEmail: String,
    val applicantPhone: String,
    val applicantQualification: String,
    val roleTitle: String,
    val madrasahName: String,
    val location: String,
    val salaryInfo: String = "আলোচনা সাপেক্ষ",
    val deadline: String = "",
    val appliedDate: String,
    val appliedAtMillis: Long = System.currentTimeMillis(),
    val status: String = "পেন্ডিং", // পেন্ডিং, প্রক্রিয়াধীন, গৃহীত, প্রত্যাখ্যাত, প্রত্যাহারকৃত
    val notes: String = ""
)

data class MyPostedNoticeItem(
    val postId: String,
    val roleTitle: String,
    val madrasahName: String,
    val location: String,
    val qualification: String = "দাওরায়ে হাদীস / সমমান",
    val salaryInfo: String = "আলোচনা সাপেক্ষ",
    val deadline: String = "",
    val phone: String = "",
    val viewsCount: Int = 0,
    val applicationsCount: Int = 0,
    val status: String = "Active", // Active, Pending, Rejected, Closed
    val isBoosted: Boolean = false,
    val ownerUid: String = "",
    val ownerEmail: String = ""
)

data class ChatConversation(
    val id: String,
    val participantId: String,
    val participantName: String,
    val participantRole: String,
    val participantEmail: String = "",
    val participantPhone: String = "",
    val participantMadrasah: String = "",
    val participantLocation: String = "ঢাকা, বাংলাদেশ",
    val isOnline: Boolean = true,
    val lastActiveText: String = "এখন",
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val lastMessageAtMillis: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val relatedPostId: String = "",
    val relatedPostTitle: String = "",
    val isBlocked: Boolean = false,
    val blockedByMe: Boolean = false,
    val isArchived: Boolean = false
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val text: String,
    val timestampText: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false,
    val isSeen: Boolean = true
)

data class MessageSendResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val message: ChatMessage? = null
)

object ProfileAndInboxRepository {
    private const val PREFS_NAME = "qawmi_profile_inbox_prefs_v2"
    private const val KEY_PROFILE_JSON = "current_user_profile_json"
    private const val KEY_USER_APPS_JSON = "user_applications_json"
    private const val KEY_MY_POSTS_JSON = "my_posted_notices_json"
    private const val KEY_CONVERSATIONS_JSON = "chat_conversations_json"
    private const val KEY_MESSAGES_PREFIX = "chat_messages_"
    const val MAX_MESSAGE_LENGTH = 1000

    private var appContext: Context? = null
    private var lastSentMessageText: String = ""
    private var lastSentMessageTimeMillis: Long = 0L

    val currentUserProfile = MutableStateFlow(UserProfileData())

    // Dynamic directory of real profiles resolved from Firestore / Feed / AdminRepository (no fake hardcoded demo profiles)
    val directoryProfiles = MutableStateFlow<Map<String, UserProfileData>>(emptyMap())

    // Real user applications, posted notices, conversations, and messages (no fake demo conversations)
    val userApplications = MutableStateFlow<List<UserApplicationRecord>>(emptyList())
    val myPostedNotices = MutableStateFlow<List<MyPostedNoticeItem>>(emptyList())
    val conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val messagesByConversation = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    fun init(context: Context) {
        appContext = context.applicationContext
        AppSettingsManager.init(context)
        loadFromPrefs(context)
        syncAuthIdentityToProfile(context)
        syncDirectoryFromRealFeed()
    }

    /**
     * Resolves whether a target userIdOrName belongs to the currently authenticated user
     * using the real Firebase Auth UID (and current user's profile UID).
     */
    fun isOwnProfile(targetUidOrName: String): Boolean {
        val clean = targetUidOrName.trim()
        if (clean.isEmpty() || clean.equals("self", ignoreCase = true) || clean.equals("me", ignoreCase = true)) {
            return true
        }
        val authUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null }
        val profileUid = currentUserProfile.value.uid
        if (!authUid.isNullOrBlank() && clean == authUid) return true
        if (profileUid.isNotBlank() && clean == profileUid) return true
        return false
    }

    fun getAuthenticatedUserId(): String {
        val authUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null }
        if (!authUid.isNullOrBlank()) return authUid
        return currentUserProfile.value.uid.ifBlank { "authenticated_user" }
    }

    fun syncDirectoryFromRealFeed() {
        val updatedDir = directoryProfiles.value.toMutableMap()
        FeedRepository.posts.forEach { post ->
            val pid = "post_owner_${post.id}"
            if (!updatedDir.containsKey(pid)) {
                val isMadrasah = post.type == FeedItemType.MADRASAH_NOTICE
                updatedDir[pid] = UserProfileData(
                    uid = pid,
                    fullName = post.title,
                    email = "",
                    phone = post.contactPhone,
                    location = post.location,
                    district = post.location.substringBefore(",").trim().ifBlank { "ঢাকা" },
                    division = post.location.substringAfter(",", "বাংলাদেশ").trim(),
                    bio = if (isMadrasah) "${post.title} — দ্বীনি শিক্ষাপ্রতিষ্ঠান।" else "${post.role} পদে খেদমতে আগ্রহী।",
                    madrasahName = if (isMadrasah) post.title else "আল-জামিয়া ইসলামিয়া",
                    qualification = post.qualification,
                    teachingSubjects = post.role,
                    skills = "দারস প্রদান ও তারবিয়াত",
                    experience = if (isMadrasah) "প্রতিষ্ঠিত" else post.extraInfo,
                    preferredKhedmotType = post.role,
                    roleLabel = if (isMadrasah) "মাদরাসা কর্তৃপক্ষ" else "মাদরাসা শিক্ষক",
                    designation = post.role,
                    avatarPreset = if (isMadrasah) "madrasah_emerald" else "scholar_green",
                    isVerified = post.isVerified,
                    isOnline = true,
                    lastActiveMillis = System.currentTimeMillis(),
                    showOnlineStatus = true,
                    allowInboxMessages = true
                )
            }
        }
        directoryProfiles.value = updatedDir
    }

    fun syncAuthIdentityToProfile(context: Context? = appContext) {
        val authUser = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
        val prefs = context?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (authUser != null) {
            val cachedName = prefs?.getString("name_${authUser.uid}", null)
            val resolvedName = when {
                !authUser.displayName.isNullOrBlank() -> authUser.displayName!!
                !cachedName.isNullOrBlank() -> cachedName
                currentUserProfile.value.fullName.isNotBlank() -> currentUserProfile.value.fullName
                !authUser.email.isNullOrBlank() -> authUser.email!!.substringBefore("@")
                else -> "ব্যবহারকারী"
            }
            val resolvedEmail = authUser.email?.takeIf { it.isNotBlank() } ?: currentUserProfile.value.email
            val updated = currentUserProfile.value.copy(
                uid = authUser.uid,
                fullName = resolvedName,
                email = resolvedEmail
            )
            currentUserProfile.value = updated
            saveToPrefs()
        } else {
            val current = currentUserProfile.value
            if (current.uid.isBlank() || current.fullName.isBlank()) {
                currentUserProfile.value = current.copy(
                    uid = current.uid.ifBlank { "local_authenticated_user" },
                    fullName = current.fullName.ifBlank { "মুহাম্মদ ফয়সাল" },
                    email = current.email.ifBlank { "user@qawmibd.org" },
                    phone = current.phone.ifBlank { "+880 1712 345678" }
                )
            }
        }
    }

    fun updateProfile(updated: UserProfileData, onComplete: (Boolean) -> Unit = {}) {
        val realUid = getAuthenticatedUserId()
        val finalProfile = updated.copy(
            uid = updated.uid.ifBlank { realUid },
            lastActiveMillis = System.currentTimeMillis()
        )
        currentUserProfile.value = finalProfile
        saveToPrefs()

        appContext?.let { ctx ->
            val uid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: finalProfile.uid
            ctx.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("name_$uid", finalProfile.fullName)
                .apply()
        }

        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && finalProfile.fullName.isNotBlank()) {
                val req = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(finalProfile.fullName)
                    .build()
                user.updateProfile(req)
            }
        } catch (_: Exception) {}

        val existingUser = AdminRepository.usersList.value.find {
            (finalProfile.email.isNotBlank() && it.email.equals(finalProfile.email, ignoreCase = true)) ||
                it.id == finalProfile.uid
        }
        val syncedUserItem = UserManagementItem(
            id = existingUser?.id ?: finalProfile.uid,
            name = finalProfile.fullName,
            email = finalProfile.email,
            phone = finalProfile.phone,
            status = existingUser?.status ?: "সক্রিয়",
            role = existingUser?.role ?: finalProfile.roleLabel,
            joinedDate = existingUser?.joinedDate ?: "২০২৬"
        )
        AdminRepository.usersList.value = AdminRepository.usersList.value.map {
            if (it.id == syncedUserItem.id || (syncedUserItem.email.isNotBlank() && it.email.equals(syncedUserItem.email, ignoreCase = true))) {
                syncedUserItem
            } else it
        }.let { list ->
            if (list.none { it.id == syncedUserItem.id }) list + syncedUserItem else list
        }
        FirebaseSyncManager.saveFullUserProfile(finalProfile)
        onComplete(true)
    }

    fun setOnlinePresence(isOnline: Boolean, showOnlineStatus: Boolean = currentUserProfile.value.showOnlineStatus) {
        val now = System.currentTimeMillis()
        val updated = currentUserProfile.value.copy(
            isOnline = isOnline,
            lastActiveMillis = now,
            showOnlineStatus = showOnlineStatus
        )
        currentUserProfile.value = updated
        saveToPrefs()
        FirebaseSyncManager.saveUserPresence(updated)
    }

    fun getPublicProfile(userIdOrName: String): UserProfileData {
        if (isOwnProfile(userIdOrName)) {
            return currentUserProfile.value
        }
        directoryProfiles.value[userIdOrName]?.let { return it }
        directoryProfiles.value.values.find {
            it.uid == userIdOrName ||
                it.fullName.equals(userIdOrName, ignoreCase = true) ||
                it.fullName.contains(userIdOrName, ignoreCase = true) ||
                userIdOrName.contains(it.fullName, ignoreCase = true) ||
                it.madrasahName.contains(userIdOrName, ignoreCase = true)
        }?.let { return it }

        val feedMatch = FeedRepository.posts.find {
            it.id == userIdOrName ||
                "post_owner_${it.id}" == userIdOrName ||
                it.title.contains(userIdOrName, ignoreCase = true) ||
                userIdOrName.contains(it.title, ignoreCase = true)
        }
        if (feedMatch != null) {
            val isMadrasah = feedMatch.type == FeedItemType.MADRASAH_NOTICE
            return UserProfileData(
                uid = "post_owner_${feedMatch.id}",
                fullName = feedMatch.title,
                email = "",
                phone = feedMatch.contactPhone,
                location = feedMatch.location,
                district = feedMatch.location.substringBefore(",").trim().ifBlank { "ঢাকা" },
                division = feedMatch.location.substringAfter(",", "বাংলাদেশ").trim(),
                bio = if (isMadrasah) "${feedMatch.title} — দ্বীনি শিক্ষাপ্রতিষ্ঠান।" else "${feedMatch.role} পদে খেদমতে আগ্রহী।",
                madrasahName = if (isMadrasah) feedMatch.title else "আল-জামিয়া ইসলামিয়া",
                qualification = feedMatch.qualification,
                teachingSubjects = "আরবি, ফিকহ, ইসলামিক স্টাডিজ",
                skills = "দারস প্রদান ও তারবিয়াত",
                experience = if (isMadrasah) "৫ বছর" else feedMatch.extraInfo,
                preferredKhedmotType = feedMatch.role,
                roleLabel = if (isMadrasah) "মাদরাসা কর্তৃপক্ষ" else "মাদরাসা শিক্ষক",
                designation = feedMatch.role,
                avatarPreset = if (isMadrasah) "madrasah_emerald" else "scholar_green",
                isVerified = feedMatch.isVerified,
                isOnline = true,
                lastActiveMillis = System.currentTimeMillis() - 180_000L,
                showOnlineStatus = true,
                allowInboxMessages = true
            )
        }

        val adminUserMatch = AdminRepository.usersList.value.find {
            it.id == userIdOrName || it.name.contains(userIdOrName, ignoreCase = true)
        }
        if (adminUserMatch != null) {
            return UserProfileData(
                uid = adminUserMatch.id,
                fullName = adminUserMatch.name,
                email = adminUserMatch.email,
                phone = adminUserMatch.phone,
                location = "ঢাকা, ঢাকা বিভাগ",
                district = "ঢাকা",
                division = "ঢাকা বিভাগ",
                madrasahName = "আল-জামিয়া ইসলামিয়া",
                qualification = "দাওরায়ে হাদিস",
                teachingSubjects = "আরবি, ফিকহ, ইসলামিক স্টাডিজ",
                experience = "৫ বছর",
                roleLabel = adminUserMatch.role.ifBlank { "মাদরাসা শিক্ষক" },
                designation = adminUserMatch.role.ifBlank { "মাদরাসা শিক্ষক" },
                isOnline = adminUserMatch.status == "সক্রিয়",
                showOnlineStatus = true,
                allowInboxMessages = true
            )
        }

        return UserProfileData(
            uid = userIdOrName.ifBlank { "external_user" },
            fullName = userIdOrName.ifBlank { "আবদুল করিম" },
            phone = "+880 1711 000000",
            location = "চট্টগ্রাম, চট্টগ্রাম বিভাগ",
            district = "চট্টগ্রাম",
            division = "চট্টগ্রাম বিভাগ",
            madrasahName = "আল-জামিয়া ইসলামিয়া, চট্টগ্রাম",
            qualification = "দাওরায়ে হাদিস",
            teachingSubjects = "আরবি, ফিকহ, ইসলামিক স্টাডিজ",
            experience = "৫ বছর",
            roleLabel = "মাদরাসা শিক্ষক",
            designation = "মাদরাসা শিক্ষক",
            isOnline = true,
            showOnlineStatus = true,
            allowInboxMessages = true
        )
    }

    // =========================================================================
    // Application History (আমার আবেদন)
    // =========================================================================
    fun hasAppliedToPost(postId: String): Boolean {
        return userApplications.value.any { it.postId == postId && it.status != "প্রত্যাহারকৃত" }
    }

    fun submitPostApplication(
        post: FeedItem,
        candidateName: String = currentUserProfile.value.fullName,
        candidatePhone: String = currentUserProfile.value.phone,
        candidateQual: String = currentUserProfile.value.qualification,
        notes: String = ""
    ): Boolean {
        if (hasAppliedToPost(post.id)) {
            return false // Prevent duplicate application
        }
        val profile = currentUserProfile.value
        val record = UserApplicationRecord(
            id = "uapp_${post.id}_${System.currentTimeMillis()}",
            postId = post.id,
            applicantUid = getAuthenticatedUserId(),
            applicantName = candidateName.ifBlank { profile.fullName },
            applicantEmail = profile.email,
            applicantPhone = candidatePhone.ifBlank { profile.phone },
            applicantQualification = candidateQual.ifBlank { profile.qualification },
            roleTitle = post.role,
            madrasahName = post.title,
            location = post.location,
            salaryInfo = post.extraInfo,
            deadline = post.secondaryInfo,
            appliedDate = "আজ, ${DateUtils.toBengaliDigits(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))}",
            appliedAtMillis = System.currentTimeMillis(),
            status = "পেন্ডিং",
            notes = notes
        )
        userApplications.value = listOf(record) + userApplications.value.filterNot { it.postId == post.id && it.status == "প্রত্যাহারকৃত" }

        myPostedNotices.value = myPostedNotices.value.map {
            if (it.postId == post.id) it.copy(applicationsCount = it.applicationsCount + 1) else it
        }
        saveToPrefs()
        FirebaseSyncManager.saveUserApplicationRecord(record)
        return true
    }

    fun withdrawApplication(applicationId: String): Boolean {
        val target = userApplications.value.find { it.id == applicationId } ?: return false
        if (target.status != "পেন্ডিং") return false
        userApplications.value = userApplications.value.filterNot { it.id == applicationId }
        saveToPrefs()
        FirebaseSyncManager.deleteUserApplicationRecord(applicationId)
        return true
    }

    fun updateApplicationStatusByOwnerOrAdmin(applicationId: String, newStatus: String): Boolean {
        val target = userApplications.value.find { it.id == applicationId } ?: return false
        val updated = target.copy(status = newStatus)
        userApplications.value = userApplications.value.map { if (it.id == applicationId) updated else it }
        saveToPrefs()
        FirebaseSyncManager.saveUserApplicationRecord(updated)

        AdminRepository.triggerEventNotification(
            title = "আবেদনের স্ট্যাটাস আপডেট: $newStatus",
            body = "${target.madrasahName}-এর '${target.roleTitle}' পদে আপনার আবেদনের বর্তমান অবস্থা: $newStatus",
            targetAudience = "খেদমতপ্রার্থী",
            targetRoute = "my_applications",
            targetItemId = target.postId,
            eventKey = "app_status_${applicationId}_$newStatus",
            category = "Applications"
        )
        return true
    }

    // =========================================================================
    // My Posted Notices (আমার বিজ্ঞপ্তি / পোস্ট)
    // =========================================================================
    fun createNewMyNotice(
        roleTitle: String,
        madrasahName: String,
        location: String,
        qualification: String,
        salaryInfo: String,
        deadline: String,
        phone: String
    ): MyPostedNoticeItem {
        val postId = "post_user_${System.currentTimeMillis()}"
        val profile = currentUserProfile.value
        val newNotice = MyPostedNoticeItem(
            postId = postId,
            roleTitle = roleTitle.trim(),
            madrasahName = madrasahName.trim(),
            location = location.trim(),
            qualification = qualification.trim().ifBlank { "দাওরায়ে হাদীস / সমমান" },
            salaryInfo = salaryInfo.trim().ifBlank { "আলোচনা সাপেক্ষ" },
            deadline = deadline.trim().ifBlank { "৩০ নভেম্বর, ২০২৬" },
            phone = phone.trim().ifBlank { profile.phone },
            viewsCount = 1,
            applicationsCount = 0,
            status = "Active",
            isBoosted = false,
            ownerUid = getAuthenticatedUserId(),
            ownerEmail = profile.email
        )
        myPostedNotices.value = listOf(newNotice) + myPostedNotices.value
        saveToPrefs()

        val feedItem = FeedItem(
            id = postId,
            type = FeedItemType.MADRASAH_NOTICE,
            title = newNotice.madrasahName,
            location = newNotice.location,
            role = newNotice.roleTitle,
            qualification = newNotice.qualification,
            extraInfo = newNotice.salaryInfo,
            secondaryInfo = newNotice.deadline,
            timeAgo = "এইমাত্র",
            isVerified = true,
            contactPhone = newNotice.phone,
            viewsCount = 1,
            clicksCount = 0,
            applicationsCount = 0
        )
        FeedRepository.addPost(feedItem)
        return newNotice
    }

    fun updateMyNotice(
        postId: String,
        roleTitle: String,
        madrasahName: String,
        location: String,
        salaryInfo: String,
        deadline: String,
        phone: String
    ): Boolean {
        val existing = myPostedNotices.value.find { it.postId == postId } ?: return false
        val updated = existing.copy(
            roleTitle = roleTitle.trim(),
            madrasahName = madrasahName.trim(),
            location = location.trim(),
            salaryInfo = salaryInfo.trim(),
            deadline = deadline.trim(),
            phone = phone.trim()
        )
        myPostedNotices.value = myPostedNotices.value.map { if (it.postId == postId) updated else it }
        saveToPrefs()

        val idx = FeedRepository.posts.indexOfFirst { it.id == postId }
        if (idx >= 0) {
            val oldFeed = FeedRepository.posts[idx]
            FeedRepository.posts[idx] = oldFeed.copy(
                title = updated.madrasahName,
                role = updated.roleTitle,
                location = updated.location,
                extraInfo = updated.salaryInfo,
                secondaryInfo = updated.deadline,
                contactPhone = updated.phone
            )
        }
        AdminRepository.noticesList.value = AdminRepository.noticesList.value.map {
            if (it.postId == postId) {
                it.copy(
                    madrasahName = updated.madrasahName,
                    roleTitle = updated.roleTitle,
                    location = updated.location,
                    salaryInfo = updated.salaryInfo,
                    deadline = updated.deadline,
                    phone = updated.phone
                )
            } else it
        }
        AdminRepository.noticesList.value.find { it.postId == postId }?.let {
            FirebaseSyncManager.saveNotice(it)
        }
        return true
    }

    fun toggleMyNoticeClosedStatus(postId: String): Boolean {
        val existing = myPostedNotices.value.find { it.postId == postId } ?: return false
        val nextStatus = if (existing.status == "Closed") "Active" else "Closed"
        myPostedNotices.value = myPostedNotices.value.map {
            if (it.postId == postId) it.copy(status = nextStatus) else it
        }
        saveToPrefs()
        val adminStatus = if (nextStatus == "Active") "অনুমোদিত" else "অপেক্ষমাণ"
        AdminRepository.updateNoticeStatus(postId, adminStatus)
        return true
    }

    fun deleteMyNotice(postId: String): Boolean {
        val existed = myPostedNotices.value.any { it.postId == postId }
        if (!existed) return false
        myPostedNotices.value = myPostedNotices.value.filterNot { it.postId == postId }
        FeedRepository.posts.removeAll { it.id == postId }
        AdminRepository.deleteNotice(postId)
        saveToPrefs()
        return true
    }

    // =========================================================================
    // Text-Only Inbox & Real-Time Chat (ইনবক্স — শুধুমাত্র টেক্সট মেসেজ)
    // =========================================================================
    fun getOrCreateConversationWith(
        participantId: String,
        participantName: String,
        participantRole: String = "মাদরাসা শিক্ষক",
        participantPhone: String = "",
        participantMadrasah: String = "",
        relatedPostId: String = "",
        relatedPostTitle: String = ""
    ): ChatConversation {
        val existing = conversations.value.find {
            it.participantId == participantId ||
                it.participantName.equals(participantName, ignoreCase = true) ||
                (participantName.isNotBlank() && it.participantName.startsWith(participantName, ignoreCase = true))
        }
        if (existing != null) return existing

        val cleanId = "conv_${participantId.ifBlank { participantName }.replace(Regex("[^a-zA-Z0-9_]"), "_").take(32)}_${System.currentTimeMillis() % 10000}"
        val newConv = ChatConversation(
            id = cleanId,
            participantId = participantId.ifBlank { cleanId },
            participantName = participantName,
            participantRole = participantRole,
            participantPhone = participantPhone,
            participantMadrasah = participantMadrasah.ifBlank { participantName },
            isOnline = true,
            lastActiveText = "এখন",
            lastMessage = "",
            lastMessageTime = "এইমাত্র",
            lastMessageAtMillis = System.currentTimeMillis(),
            unreadCount = 0,
            relatedPostId = relatedPostId,
            relatedPostTitle = relatedPostTitle
        )
        conversations.value = listOf(newConv) + conversations.value
        if (!messagesByConversation.value.containsKey(cleanId)) {
            messagesByConversation.value = messagesByConversation.value + (cleanId to emptyList())
        }
        saveToPrefs()
        FirebaseSyncManager.saveConversation(newConv)
        return newConv
    }

    /**
     * Strict Text-Only Validator:
     * Rejects empty/whitespace strings, file/image/video/audio attachment URIs or MIME payloads,
     * messages exceeding MAX_MESSAGE_LENGTH, blocked conversations, and duplicate spam bursts.
     */
    fun isStrictlyValidTextMessage(rawText: String): Pair<Boolean, String?> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return false to "খালি মেসেজ পাঠানো যাবে না। অনুগ্রহ করে টেক্সট লিখুন।"
        }
        if (trimmed.length > MAX_MESSAGE_LENGTH) {
            return false to "মেসেজ সর্বোচ্চ ${DateUtils.toBengaliDigits(MAX_MESSAGE_LENGTH.toString())} অক্ষরের হতে পারে।"
        }
        val lower = trimmed.lowercase()
        val forbiddenMediaPatterns = listOf(
            "content://",
            "file://",
            "data:image/",
            "data:video/",
            "data:audio/",
            "data:application/",
            "[image]",
            "[video]",
            "[audio]",
            "[voice_note]",
            "[attachment]"
        )
        if (forbiddenMediaPatterns.any { lower.contains(it) }) {
            return false to "শুধুমাত্র টেক্সট মেসেজ অনুমোদিত। ছবি, ভিডিও, অডিও বা ফাইল পাঠানো সম্পূর্ণ নিষিদ্ধ।"
        }
        return true to null
    }

    fun sendTextOnlyMessage(
        conversationId: String,
        rawText: String
    ): MessageSendResult {
        val conv = conversations.value.find { it.id == conversationId }
            ?: return MessageSendResult(false, "কথোপকথন খুঁজে পাওয়া যায়নি।")

        if (conv.isBlocked) {
            return MessageSendResult(false, "এই কথোপকথনটি ব্লক করা রয়েছে। মেসেজ পাঠানো যাবে না।")
        }

        val (isValid, validationError) = isStrictlyValidTextMessage(rawText)
        if (!isValid) {
            return MessageSendResult(false, validationError)
        }

        val sanitizedText = rawText.trim()
        val now = System.currentTimeMillis()
        if (sanitizedText == lastSentMessageText && (now - lastSentMessageTimeMillis) < 800L) {
            return MessageSendResult(false, "অনুগ্রহ করে একই মেসেজ বারবার পাঠাবেন না (Spam সুরক্ষা)।")
        }
        lastSentMessageText = sanitizedText
        lastSentMessageTimeMillis = now

        val profile = currentUserProfile.value
        val formattedTime = DateUtils.toBengaliDigits(
            SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(now))
        )
        val msg = ChatMessage(
            id = "msg_${now}_${(100..999).random()}",
            conversationId = conversationId,
            senderId = getAuthenticatedUserId(),
            senderName = profile.fullName,
            receiverId = conv.participantId,
            text = sanitizedText,
            timestampText = formattedTime,
            createdAtMillis = now,
            isFromMe = true,
            isSeen = true
        )

        val currentList = messagesByConversation.value[conversationId] ?: emptyList()
        messagesByConversation.value = messagesByConversation.value + (conversationId to (currentList + msg))

        val updatedConv = conv.copy(
            lastMessage = sanitizedText,
            lastMessageTime = formattedTime,
            lastMessageAtMillis = now
        )
        conversations.value = listOf(updatedConv) + conversations.value.filterNot { it.id == conversationId }
        saveToPrefs()

        FirebaseSyncManager.saveConversation(updatedConv)
        FirebaseSyncManager.saveChatMessage(msg)

        AdminRepository.triggerEventNotification(
            title = "নতুন মেসেজ: ${profile.fullName}",
            body = sanitizedText.take(80),
            targetAudience = conv.participantRole.ifBlank { "সকল ব্যবহারকারী" },
            targetRoute = "inbox",
            targetItemId = conversationId,
            eventKey = "chat_msg_${msg.id}",
            category = "Announcements"
        )

        return MessageSendResult(true, null, msg)
    }

    fun markConversationAsRead(conversationId: String) {
        val conv = conversations.value.find { it.id == conversationId } ?: return
        if (conv.unreadCount > 0) {
            conversations.value = conversations.value.map {
                if (it.id == conversationId) it.copy(unreadCount = 0) else it
            }
        }
        val list = messagesByConversation.value[conversationId] ?: return
        if (list.any { !it.isSeen }) {
            messagesByConversation.value = messagesByConversation.value + (
                conversationId to list.map { it.copy(isSeen = true) }
            )
        }
        saveToPrefs()
    }

    fun toggleArchiveConversation(conversationId: String): Boolean {
        val conv = conversations.value.find { it.id == conversationId } ?: return false
        val nextArchived = !conv.isArchived
        conversations.value = conversations.value.map {
            if (it.id == conversationId) it.copy(isArchived = nextArchived) else it
        }
        saveToPrefs()
        return nextArchived
    }

    fun toggleBlockConversation(conversationId: String): Boolean {
        val conv = conversations.value.find { it.id == conversationId } ?: return false
        val newBlockedState = !conv.isBlocked
        val updated = conv.copy(isBlocked = newBlockedState, blockedByMe = newBlockedState)
        conversations.value = conversations.value.map {
            if (it.id == conversationId) updated else it
        }
        saveToPrefs()
        FirebaseSyncManager.saveConversation(updated)
        return newBlockedState
    }

    fun reportConversationUser(conversationId: String, reason: String) {
        val conv = conversations.value.find { it.id == conversationId } ?: return
        val profile = currentUserProfile.value
        AdminRepository.submitSupportTicket(
            reporterName = profile.fullName,
            reporterContact = profile.email.ifBlank { profile.phone },
            subject = "ইনবক্স রিপোর্ট: ${conv.participantName}",
            description = "অভিযোগের কারণ: $reason (Conversation ID: $conversationId)"
        )
    }

    // =========================================================================
    // Cloud Sync Callbacks (from FirebaseSyncManager)
    // =========================================================================
    fun updateCurrentUserProfileFromCloud(cloudProfile: UserProfileData) {
        currentUserProfile.value = cloudProfile
        saveToPrefs()
    }

    fun updateUserApplicationsFromCloud(cloudApps: List<UserApplicationRecord>) {
        if (cloudApps.isNotEmpty()) {
            userApplications.value = cloudApps
            saveToPrefs()
        }
    }

    fun updateConversationsFromCloud(cloudConvs: List<ChatConversation>) {
        if (cloudConvs.isNotEmpty()) {
            conversations.value = cloudConvs
            saveToPrefs()
        }
    }

    fun updateMessagesFromCloud(conversationId: String, cloudMessages: List<ChatMessage>) {
        if (cloudMessages.isNotEmpty()) {
            messagesByConversation.value = messagesByConversation.value + (conversationId to cloudMessages)
            saveToPrefs()
        }
    }

    // =========================================================================
    // Local Persistence (SharedPreferences JSON)
    // =========================================================================
    private fun saveToPrefs() {
        val ctx = appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val p = currentUserProfile.value
            val pJson = JSONObject().apply {
                put("uid", p.uid)
                put("fullName", p.fullName)
                put("email", p.email)
                put("phone", p.phone)
                put("location", p.location)
                put("district", p.district)
                put("division", p.division)
                put("bio", p.bio)
                put("madrasahName", p.madrasahName)
                put("qualification", p.qualification)
                put("teachingSubjects", p.teachingSubjects)
                put("skills", p.skills)
                put("experience", p.experience)
                put("preferredKhedmotType", p.preferredKhedmotType)
                put("roleLabel", p.roleLabel)
                put("designation", p.designation)
                put("photoUri", p.photoUri)
                put("avatarPreset", p.avatarPreset)
                put("isVerified", p.isVerified)
                put("isOnline", p.isOnline)
                put("lastActiveMillis", p.lastActiveMillis)
                put("showOnlineStatus", p.showOnlineStatus)
                put("allowInboxMessages", p.allowInboxMessages)
            }

            val appsArray = JSONArray()
            userApplications.value.forEach { a ->
                appsArray.put(
                    JSONObject().apply {
                        put("id", a.id)
                        put("postId", a.postId)
                        put("applicantUid", a.applicantUid)
                        put("applicantName", a.applicantName)
                        put("applicantEmail", a.applicantEmail)
                        put("applicantPhone", a.applicantPhone)
                        put("applicantQualification", a.applicantQualification)
                        put("roleTitle", a.roleTitle)
                        put("madrasahName", a.madrasahName)
                        put("location", a.location)
                        put("salaryInfo", a.salaryInfo)
                        put("deadline", a.deadline)
                        put("appliedDate", a.appliedDate)
                        put("appliedAtMillis", a.appliedAtMillis)
                        put("status", a.status)
                        put("notes", a.notes)
                    }
                )
            }

            val postsArray = JSONArray()
            myPostedNotices.value.forEach { n ->
                postsArray.put(
                    JSONObject().apply {
                        put("postId", n.postId)
                        put("roleTitle", n.roleTitle)
                        put("madrasahName", n.madrasahName)
                        put("location", n.location)
                        put("qualification", n.qualification)
                        put("salaryInfo", n.salaryInfo)
                        put("deadline", n.deadline)
                        put("phone", n.phone)
                        put("viewsCount", n.viewsCount)
                        put("applicationsCount", n.applicationsCount)
                        put("status", n.status)
                        put("isBoosted", n.isBoosted)
                        put("ownerUid", n.ownerUid)
                        put("ownerEmail", n.ownerEmail)
                    }
                )
            }

            val convArray = JSONArray()
            conversations.value.forEach { c ->
                convArray.put(
                    JSONObject().apply {
                        put("id", c.id)
                        put("participantId", c.participantId)
                        put("participantName", c.participantName)
                        put("participantRole", c.participantRole)
                        put("participantEmail", c.participantEmail)
                        put("participantPhone", c.participantPhone)
                        put("participantMadrasah", c.participantMadrasah)
                        put("participantLocation", c.participantLocation)
                        put("isOnline", c.isOnline)
                        put("lastActiveText", c.lastActiveText)
                        put("lastMessage", c.lastMessage)
                        put("lastMessageTime", c.lastMessageTime)
                        put("lastMessageAtMillis", c.lastMessageAtMillis)
                        put("unreadCount", c.unreadCount)
                        put("relatedPostId", c.relatedPostId)
                        put("relatedPostTitle", c.relatedPostTitle)
                        put("isBlocked", c.isBlocked)
                        put("blockedByMe", c.blockedByMe)
                        put("isArchived", c.isArchived)
                    }
                )
            }

            val editor = prefs.edit()
                .putString(KEY_PROFILE_JSON, pJson.toString())
                .putString(KEY_USER_APPS_JSON, appsArray.toString())
                .putString(KEY_MY_POSTS_JSON, postsArray.toString())
                .putString(KEY_CONVERSATIONS_JSON, convArray.toString())

            messagesByConversation.value.forEach { (convId, msgs) ->
                val msgArr = JSONArray()
                msgs.forEach { m ->
                    msgArr.put(
                        JSONObject().apply {
                            put("id", m.id)
                            put("conversationId", m.conversationId)
                            put("senderId", m.senderId)
                            put("senderName", m.senderName)
                            put("receiverId", m.receiverId)
                            put("text", m.text)
                            put("timestampText", m.timestampText)
                            put("createdAtMillis", m.createdAtMillis)
                            put("isFromMe", m.isFromMe)
                            put("isSeen", m.isSeen)
                        }
                    )
                }
                editor.putString(KEY_MESSAGES_PREFIX + convId, msgArr.toString())
            }
            editor.apply()
        } catch (_: Exception) {}
    }

    private fun loadFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pRaw = prefs.getString(KEY_PROFILE_JSON, null)
            if (!pRaw.isNullOrBlank()) {
                val obj = JSONObject(pRaw)
                currentUserProfile.value = UserProfileData(
                    uid = obj.optString("uid", ""),
                    fullName = obj.optString("fullName", ""),
                    email = obj.optString("email", ""),
                    phone = obj.optString("phone", ""),
                    location = obj.optString("location", "ঢাকা, ঢাকা বিভাগ"),
                    district = obj.optString("district", "ঢাকা"),
                    division = obj.optString("division", "ঢাকা বিভাগ"),
                    bio = obj.optString("bio", currentUserProfile.value.bio),
                    madrasahName = obj.optString("madrasahName", currentUserProfile.value.madrasahName),
                    qualification = obj.optString("qualification", currentUserProfile.value.qualification),
                    teachingSubjects = obj.optString("teachingSubjects", currentUserProfile.value.teachingSubjects),
                    skills = obj.optString("skills", currentUserProfile.value.skills),
                    experience = obj.optString("experience", currentUserProfile.value.experience),
                    preferredKhedmotType = obj.optString("preferredKhedmotType", currentUserProfile.value.preferredKhedmotType),
                    roleLabel = obj.optString("roleLabel", currentUserProfile.value.roleLabel),
                    designation = obj.optString("designation", currentUserProfile.value.designation),
                    photoUri = obj.optString("photoUri", ""),
                    avatarPreset = obj.optString("avatarPreset", "scholar_green"),
                    isVerified = obj.optBoolean("isVerified", true),
                    isOnline = obj.optBoolean("isOnline", true),
                    lastActiveMillis = obj.optLong("lastActiveMillis", System.currentTimeMillis()),
                    showOnlineStatus = obj.optBoolean("showOnlineStatus", true),
                    allowInboxMessages = obj.optBoolean("allowInboxMessages", true)
                )
            }

            val appsRaw = prefs.getString(KEY_USER_APPS_JSON, null)
            if (!appsRaw.isNullOrBlank()) {
                val arr = JSONArray(appsRaw)
                val loadedApps = mutableListOf<UserApplicationRecord>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    loadedApps.add(
                        UserApplicationRecord(
                            id = o.optString("id"),
                            postId = o.optString("postId"),
                            applicantUid = o.optString("applicantUid", ""),
                            applicantName = o.optString("applicantName", ""),
                            applicantEmail = o.optString("applicantEmail", ""),
                            applicantPhone = o.optString("applicantPhone", ""),
                            applicantQualification = o.optString("applicantQualification", "দাওরায়ে হাদিস"),
                            roleTitle = o.optString("roleTitle"),
                            madrasahName = o.optString("madrasahName"),
                            location = o.optString("location"),
                            salaryInfo = o.optString("salaryInfo", "আলোচনা সাপেক্ষ"),
                            deadline = o.optString("deadline", ""),
                            appliedDate = o.optString("appliedDate"),
                            appliedAtMillis = o.optLong("appliedAtMillis", System.currentTimeMillis()),
                            status = o.optString("status", "পেন্ডিং"),
                            notes = o.optString("notes", "")
                        )
                    )
                }
                userApplications.value = loadedApps
            }

            val postsRaw = prefs.getString(KEY_MY_POSTS_JSON, null)
            if (!postsRaw.isNullOrBlank()) {
                val arr = JSONArray(postsRaw)
                val loadedPosts = mutableListOf<MyPostedNoticeItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    loadedPosts.add(
                        MyPostedNoticeItem(
                            postId = o.optString("postId"),
                            roleTitle = o.optString("roleTitle"),
                            madrasahName = o.optString("madrasahName"),
                            location = o.optString("location"),
                            qualification = o.optString("qualification"),
                            salaryInfo = o.optString("salaryInfo"),
                            deadline = o.optString("deadline"),
                            phone = o.optString("phone"),
                            viewsCount = o.optInt("viewsCount", 0),
                            applicationsCount = o.optInt("applicationsCount", 0),
                            status = o.optString("status", "Active"),
                            isBoosted = o.optBoolean("isBoosted", false),
                            ownerUid = o.optString("ownerUid", ""),
                            ownerEmail = o.optString("ownerEmail", "")
                        )
                    )
                }
                myPostedNotices.value = loadedPosts
            }

            val convRaw = prefs.getString(KEY_CONVERSATIONS_JSON, null)
            if (!convRaw.isNullOrBlank()) {
                val arr = JSONArray(convRaw)
                val loadedConvs = mutableListOf<ChatConversation>()
                val loadedMsgs = mutableMapOf<String, List<ChatMessage>>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val cid = o.optString("id")
                    loadedConvs.add(
                        ChatConversation(
                            id = cid,
                            participantId = o.optString("participantId"),
                            participantName = o.optString("participantName"),
                            participantRole = o.optString("participantRole"),
                            participantEmail = o.optString("participantEmail"),
                            participantPhone = o.optString("participantPhone"),
                            participantMadrasah = o.optString("participantMadrasah"),
                            participantLocation = o.optString("participantLocation", "ঢাকা, বাংলাদেশ"),
                            isOnline = o.optBoolean("isOnline", true),
                            lastActiveText = o.optString("lastActiveText", "এখন"),
                            lastMessage = o.optString("lastMessage"),
                            lastMessageTime = o.optString("lastMessageTime"),
                            lastMessageAtMillis = o.optLong("lastMessageAtMillis", System.currentTimeMillis()),
                            unreadCount = o.optInt("unreadCount", 0),
                            relatedPostId = o.optString("relatedPostId"),
                            relatedPostTitle = o.optString("relatedPostTitle"),
                            isBlocked = o.optBoolean("isBlocked", false),
                            blockedByMe = o.optBoolean("blockedByMe", false),
                            isArchived = o.optBoolean("isArchived", false)
                        )
                    )
                    val msgRaw = prefs.getString(KEY_MESSAGES_PREFIX + cid, null)
                    if (!msgRaw.isNullOrBlank()) {
                        val mArr = JSONArray(msgRaw)
                        val mList = mutableListOf<ChatMessage>()
                        for (j in 0 until mArr.length()) {
                            val mo = mArr.getJSONObject(j)
                            mList.add(
                                ChatMessage(
                                    id = mo.optString("id"),
                                    conversationId = mo.optString("conversationId", cid),
                                    senderId = mo.optString("senderId"),
                                    senderName = mo.optString("senderName"),
                                    receiverId = mo.optString("receiverId"),
                                    text = mo.optString("text"),
                                    timestampText = mo.optString("timestampText"),
                                    createdAtMillis = mo.optLong("createdAtMillis", System.currentTimeMillis()),
                                    isFromMe = mo.optBoolean("isFromMe", false),
                                    isSeen = mo.optBoolean("isSeen", true)
                                )
                            )
                        }
                        loadedMsgs[cid] = mList
                    }
                }
                conversations.value = loadedConvs
                messagesByConversation.value = loadedMsgs
            }
        } catch (_: Exception) {}
    }
}
