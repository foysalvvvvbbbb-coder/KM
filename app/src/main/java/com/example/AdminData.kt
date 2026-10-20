package com.example

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Admin List & Permissions
val AUTHORIZED_ADMIN_EMAILS = listOf(
    "foysalahamad102@gmail.com",
    "foysalahamad204@gmail.com",
    "mdfoysalahamad202@gmail.com",
    "sumuaktar01300448147@gmail.com"
)

enum class AdminRole(val displayName: String) {
    SUPER_ADMIN("প্রধান Admin"),
    FULL_ADMIN("Full Admin"),
    CONTENT_ADMIN("Content Admin"),
    APPLICATION_ADMIN("Application Admin")
}

data class AdminUserItem(
    val email: String,
    val role: AdminRole,
    val status: String = "সক্রিয়"
)

data class UserManagementItem(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    var status: String = "সক্রিয়",
    var role: String = "সাধারণ"
)

data class BoostRequestItem(
    val id: String,
    val title: String,
    val requestedBy: String,
    var status: String, // Pending, Under Review, Active, Rejected
    val requestDate: String
)

data class DynamicFormField(
    val id: String,
    var label: String,
    var type: String, // Short Text, Long Text, Email, Phone Number, Number, Date, Dropdown, Single Choice
    var isRequired: Boolean = true,
    var order: Int = 1,
    var validation: String = "None"
)

data class AppContentConfig(
    var heroTitle: String = "কওমি কর্মসংস্থান বাংলাদেশ",
    var heroSlogan: String = "দ্বীনি খেদমতে সুযোগ, আল্লাহর সন্তুষ্টি",
    var heroDescription: String = "কওমি মাদ্রাসা ও খেদমতপ্রার্থীদের মধ্যে দ্বীনি খেদমতের সুদৃঢ় মেলবন্ধন।",
    var isPublished: Boolean = true
)

data class TelegramSettingsConfig(
    val username: String = "@Q_kormosongsthan_Bangladesh",
    var notifyNewAdmin: Boolean = true,
    var notifyBoostRequest: Boolean = true,
    var notifyImportantChanges: Boolean = true,
    var notifySecurityAlert: Boolean = true,
    val approvedUserIds: MutableList<String> = mutableListOf("123456789 (Admin)", "987654321 (Admin)", "456789123 (Admin)")
)

data class AuditLogEntry(
    val time: String,
    val user: String,
    val action: String,
    val category: String
)

object AdminRepository {
    private const val PREFS_NAME = "admin_panel_prefs"
    
    // Dynamic Form Fields state
    val formFields = MutableStateFlow<List<DynamicFormField>>(
        listOf(
            DynamicFormField("1", "নাম", "Short Text", isRequired = true, order = 1, validation = "None"),
            DynamicFormField("2", "ইমেইল", "Email", isRequired = true, order = 2, validation = "Email Format"),
            DynamicFormField("3", "মোবাইল নম্বর", "Phone Number", isRequired = true, order = 3, validation = "Phone Format"),
            DynamicFormField("4", "সর্বোচ্চ শিক্ষাগত যোগ্যতা", "Dropdown", isRequired = true, order = 4, validation = "None"),
            DynamicFormField("5", "অভিজ্ঞতার বিবরণ", "Long Text", isRequired = false, order = 5, validation = "None")
        )
    )

    // Users List
    val usersList = MutableStateFlow<List<UserManagementItem>>(
        listOf(
            UserManagementItem("1", "আশফাকুর রহমান", "example1@gmail.com", "01712******", "সক্রিয়", "আবেদনকারী"),
            UserManagementItem("2", "মোঃ সায়েম", "example2@gmail.com", "01845******", "সক্রিয়", "মাদ্রাসা প্রতিনিধি"),
            UserManagementItem("3", "হাবিবুর রহমান", "example3@gmail.com", "01673******", "নিষ্ক্রিয়", "আবেদনকারী"),
            UserManagementItem("4", "আমিনা খাতুন", "example4@gmail.com", "01911******", "সক্রিয়", "আবেদনকারী")
        )
    )

    // Boost Requests
    val boostRequests = MutableStateFlow<List<BoostRequestItem>>(
        listOf(
            BoostRequestItem("1", "হাফেজ্জী শিক্ষক নিয়োগ...", "জনাব আলী", "Pending", "২৬ এপ্রিল ২০২৫"),
            BoostRequestItem("2", "মুহাদ্দিস খেদমত", "মোঃ হাসান", "Under Review", "২৫ এপ্রিল ২০২৫"),
            BoostRequestItem("3", "মাদ্রাসায় প্রশাসনিক পদ...", "রাফিড আক্তার", "Approved", "২২ এপ্রিল ২০২৫"),
            BoostRequestItem("4", "শিক্ষক নিয়োগ", "আব্দুল কাদের", "Active", "২০ এপ্রিল ২০২৫")
        )
    )

    // Admin Users List
    val adminUsers = MutableStateFlow<List<AdminUserItem>>(
        listOf(
            AdminUserItem("foysalahamad204@gmail.com", AdminRole.SUPER_ADMIN, "সক্রিয়"),
            AdminUserItem("foysalahamad102@gmail.com", AdminRole.FULL_ADMIN, "সক্রিয়"),
            AdminUserItem("mdfoysalahamad202@gmail.com", AdminRole.CONTENT_ADMIN, "সক্রিয়"),
            AdminUserItem("sumuaktar01300448147@gmail.com", AdminRole.APPLICATION_ADMIN, "সক্রিয়")
        )
    )

    // App Content
    var appContent by mutableStateOf(AppContentConfig())

    // Telegram Settings
    var telegramSettings by mutableStateOf(TelegramSettingsConfig())

    // Audit Logs
    val auditLogs = MutableStateFlow<List<AuditLogEntry>>(
        listOf(
            AuditLogEntry("২ মিনিট আগে", "System", "নতুন ব্যবহারকারী নিবন্ধিত হয়েছে", "User"),
            AuditLogEntry("১২ মিনিট আগে", "foysalahamad204@gmail.com", "একটি নতুন আবেদন অনুমোদিত হয়েছে", "Application"),
            AuditLogEntry("২৫ মিনিট আগে", "mdfoysalahamad202@gmail.com", "একটি নতুন নিয়োগ বিজ্ঞপ্তি প্রকাশিত হয়েছে", "Notice"),
            AuditLogEntry("৪০ মিনিট আগে", "System", "নতুন Boost অনুরোধ জমা হয়েছে", "Boost"),
            AuditLogEntry("১ ঘণ্টা আগে", "foysalahamad102@gmail.com", "Admin প্যানেলে সফলভাবে লগইন করেছেন", "Auth")
        )
    )

    fun isUserAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return AUTHORIZED_ADMIN_EMAILS.any { it.lowercase() == cleanEmail } ||
               adminUsers.value.any { it.email.lowercase() == cleanEmail }
    }

    fun updateBoostStatus(id: String, newStatus: String) {
        boostRequests.value = boostRequests.value.map { item ->
            if (item.id == id) item.copy(status = newStatus) else item
        }
    }

    fun toggleUserStatus(userId: String) {
        usersList.value = usersList.value.map { user ->
            if (user.id == userId) {
                val nextStatus = if (user.status == "সক্রিয়") "নিষ্ক্রিয়" else "সক্রিয়"
                user.copy(status = nextStatus)
            } else user
        }
    }

    fun addFormField(field: DynamicFormField) {
        formFields.value = formFields.value + field
    }

    fun removeFormField(id: String) {
        formFields.value = formFields.value.filter { it.id != id }
    }
}
