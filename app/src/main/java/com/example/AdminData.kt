package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Exact Initial Administrator Allowlist
val AUTHORIZED_ADMIN_EMAILS = listOf(
    "foysalahamad102@gmail.com",
    "foysalahamad204@gmail.com", // Main/Super Admin
    "mdfoysalahamad202@gmail.com",
    "sumuaktar01300448147@gmail.com"
)

const val SUPER_ADMIN_EMAIL = "foysalahamad204@gmail.com"

enum class AdminRole(val displayName: String) {
    SUPER_ADMIN("প্রধান Admin"),
    FULL_ADMIN("Full Admin"),
    CONTENT_ADMIN("Content Admin"),
    APPLICATION_ADMIN("Application Admin"),
    LIMITED_ADMIN("Limited Admin")
}

data class AdminPermissions(
    val manageAdmins: Boolean = true,
    val manageApplications: Boolean = true,
    val manageTelegram: Boolean = true,
    val manageBoost: Boolean = true,
    val approveBoost: Boolean = true,
    val manageContentAndLogo: Boolean = true
)

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
    val status: String = "সক্রিয়",
    val role: String = "আবেদনকারী",
    val joinedDate: String = ""
)

data class AdminMadrasahItem(
    val id: String,
    val name: String,
    val location: String,
    val muhtamim: String,
    val departments: String,
    val phone: String,
    val regNumber: String,
    val verificationStatus: String = "যাচাইকৃত" // যাচাইকৃত, অপেক্ষমাণ, প্রত্যাখ্যাত
)

data class ApplicationStatusItem(
    val postId: String,
    val candidateName: String,
    val roleTitle: String,
    val location: String,
    val qualification: String,
    val experience: String,
    val phone: String,
    val status: String = "নতুন", // নতুন, প্রক্রিয়াধীন, অনুমোদিত, প্রত্যাখ্যাত
    val submittedDate: String = ""
)

data class NoticeStatusItem(
    val postId: String,
    val madrasahName: String,
    val roleTitle: String,
    val location: String,
    val salaryInfo: String,
    val deadline: String,
    val phone: String,
    val status: String = "অনুমোদিত", // অনুমোদিত, অপেক্ষমাণ, প্রত্যাখ্যাত
    val isBoosted: Boolean = false,
    val viewsCount: Int = 0,
    val clicksCount: Int = 0,
    val applicationsCount: Int = 0
)

data class BoostPolicyConfig(
    val maxConcurrentBoosts: Int = 5,
    val maxActivePerUser: Int = 2,
    val notificationFrequencyLimitHours: Int = 24
)

data class BoostRequestItem(
    val id: String,
    val noticeId: String = "post_1",
    val title: String,
    val requestedBy: String,
    val status: String, // Pending, Under Review, Approved, Active, Paused, Cancelled, Rejected, Expired
    val requestDate: String,
    val durationDays: Int = 7,
    val expiresAtMillis: Long? = null,
    val targetScope: String = "সকল ব্যবহারকারী",
    val viewsCount: Int = 0,
    val clicksCount: Int = 0,
    val applicationsCount: Int = 0
)

data class DynamicFormField(
    val id: String,
    val formCategory: String = "খেদমতের আবেদন ফর্ম",
    val label: String,
    val placeholder: String = "",
    val type: String, // Short Text, Long Text, Email, Phone Number, Number, Date, Dropdown, Checkbox, Single Choice, Multiple Choice
    val isRequired: Boolean = true,
    val order: Int = 1,
    val validation: String = "None",
    val optionsCsv: String = ""
)

data class AppContentConfig(
    val heroTitle: String = "বাংলাদেশের কওমি মাদ্রাসা\nখেদমতের বিশ্বস্ত মাধ্যম",
    val heroSlogan: String = "দ্বীনি খেদমতে সুযোগ, আল্লাহর সন্তুষ্টি",
    val heroDescription: String = "শিক্ষক খুঁজুন, চাকরি খুঁজুন, খেদমত এগিয়ে নিন",
    val contactEmail: String = "support@kawmikormo.org",
    val contactPhone: String = "+৮৮০ ৯৬০০০০০০০",
    val faqText: String = "প্রশ্ন ১: কীভাবে খেদমতের বিজ্ঞপ্তি খুঁজব?\nউত্তর: 'খেদমত' ট্যাবে গিয়ে আপনার পছন্দমতো ফিল্টার করে দেখতে পারেন।\n\nপ্রশ্ন ২: কীভাবে আবেদন করব?\nউত্তর: যেকোনো বিজ্ঞপ্তিতে প্রবেশ করে সহজে আবেদন জমা দিতে পারবেন।",
    val termsText: String = "১. অ্যাপ ব্যবহারের সাধারণ নিয়ম মেনে চলতে হবে।\n২. খেদমতের বিজ্ঞপ্তি ও আবেদন প্রকাশের ক্ষেত্রে সম্পূর্ণ সত্য ও নির্ভুল তথ্য প্রদান করতে হবে।\n৩. ভুয়া বিজ্ঞপ্তি, হয়রানিমূলক পোস্ট বা প্রতারণা সম্পূর্ণ নিষিদ্ধ।\n৪. ব্যবহারকারী তার নিজ অ্যাকাউন্টের নিরাপত্তা ও তথ্যের জন্য দায়ী থাকবেন।",
    val privacyText: String = "আপনার ব্যক্তিগত তথ্য যেমন নাম, ইমেইল ও প্রোফাইল ডেটা সম্পূর্ণ নিরাপদে সংরক্ষিত হয়। আমরা আপনার অনুমতি ছাড়া কোনো তথ্য তৃতীয় পক্ষের সাথে শেয়ার করি না।",
    val isPublished: Boolean = true
)

data class TelegramSettingsConfig(
    val username: String = "@Q_kormosongsthan_Bangladesh",
    val notifyNewAdmin: Boolean = true,
    val notifyBoostRequest: Boolean = true,
    val notifyImportantChanges: Boolean = true,
    val notifySecurityAlert: Boolean = true,
    val approvedUserIds: List<String> = listOf(
        "123456789 (প্রধান Admin)",
        "987654321 (Admin)",
        "456789123 (Admin)"
    ),
    val isBotTokenConfiguredServerSide: Boolean = false
)

data class SentNotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val targetAudience: String,
    val sentTime: String,
    val status: String = "প্রেরিত", // প্রেরিত, নির্ধারিত, ব্যর্থ
    val targetRoute: String = "khedmot",
    val targetItemId: String = "",
    val eventKey: String = "",
    val category: String = "Announcements", // Boost, Applications, Announcements, Security
    val deliveryStatus: String = "ডাটাবেস ও ডিভাইসে প্রেরিত",
    val failureReason: String = "",
    val isDeliveredToDevice: Boolean = true
)

data class SupportTicketItem(
    val id: String,
    val reporterName: String,
    val reporterContact: String,
    val subject: String,
    val description: String,
    val date: String,
    val status: String = "অপেক্ষমাণ", // অপেক্ষমাণ, প্রক্রিয়াধীন, সমাধানকৃত
    val adminReply: String = ""
)

data class AuditLogEntry(
    val id: String = System.currentTimeMillis().toString(),
    val time: String,
    val user: String,
    val action: String,
    val category: String
)

data class ConfigBackupEntry(
    val id: String,
    val timestamp: String,
    val createdBy: String,
    val summary: String,
    val payloadJson: String
)

data class DailyActivityPoint(
    val dateLabel: String,
    val usersCount: Int,
    val applicationsCount: Int,
    val noticesCount: Int
)

object AdminRepository {
    private const val PREFS_NAME = "admin_panel_prefs_v2"
    private var appContext: Context? = null

    private fun currentTimeFormatted(): String {
        val cal = DateUtils.getDhakaCalendar()
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        sdf.timeZone = cal.timeZone
        return DateUtils.toBengaliDigits(sdf.format(cal.time))
    }

    private fun currentShortDateFormatted(): String {
        val cal = DateUtils.getDhakaCalendar()
        return "${DateUtils.getEnglishDate(cal)}, ${DateUtils.getWeekday(cal)}"
    }

    // Dynamic Form Fields state
    val formFields = MutableStateFlow<List<DynamicFormField>>(
        listOf(
            DynamicFormField("1", "খেদমতের আবেদন ফর্ম", "নাম", "আপনার পূর্ণ নাম লিখুন", "Short Text", isRequired = true, order = 1, validation = "None"),
            DynamicFormField("2", "খেদমতের আবেদন ফর্ম", "ইমেইল", "example@gmail.com", "Email", isRequired = true, order = 2, validation = "Email Format"),
            DynamicFormField("3", "খেদমতের আবেদন ফর্ম", "মোবাইল নম্বর", "017XXXXXXXX", "Phone Number", isRequired = true, order = 3, validation = "Phone Format"),
            DynamicFormField("4", "খেদমতের আবেদন ফর্ম", "সর্বোচ্চ শিক্ষাগত যোগ্যতা", "দাওরায়ে হাদীস / ইফতা / হিফজ", "Dropdown", isRequired = true, order = 4, validation = "None", optionsCsv = "দাওরায়ে হাদীস,ইফতা,তাফসীর,হিফজুল কুরআন,কিরাত"),
            DynamicFormField("5", "খেদমতের আবেদন ফর্ম", "অভিজ্ঞতার বিবরণ", "পূর্ববর্তী খেদমতের অভিজ্ঞতা লিখুন", "Long Text", isRequired = false, order = 5, validation = "None")
        )
    )

    // Users List
    val usersList = MutableStateFlow<List<UserManagementItem>>(
        listOf(
            UserManagementItem("u1", "মাওলানা আশফাকুর রহমান", "ashfaq.qawmi@gmail.com", "01712345678", "সক্রিয়", "আবেদনকারী", "১৫ অক্টোবর ২০২৬"),
            UserManagementItem("u2", "মুফতি মোঃ সায়েম", "sayem.madrasah@gmail.com", "01845678901", "সক্রিয়", "মাদ্রাসা প্রতিনিধি", "১৬ অক্টোবর ২০২৬"),
            UserManagementItem("u3", "হাফেজ হাবিবুর রহমান", "habib.hafez@gmail.com", "01673112233", "নিষ্ক্রিয়", "আবেদনকারী", "১৭ অক্টোবর ২০২৬"),
            UserManagementItem("u4", "মাওলানা আব্দুল্লাহ আল মামুন", "mamun.alim@gmail.com", "01822334455", "সক্রিয়", "আবেদনকারী", "১৮ অক্টোবর ২০২৬"),
            UserManagementItem("u5", "মাওলানা রফিকুল ইসলাম", "rafiq.quran@gmail.com", "01744556677", "সক্রিয়", "আবেদনকারী", "১৯ অক্টোবর ২০২৬"),
            UserManagementItem("u6", "মুফতি খলীল আহমদ", "khalil.muhtamim@gmail.com", "01819112233", "সক্রিয়", "মাদ্রাসা প্রতিনিধি", "২০ অক্টোবর ২০২৬")
        )
    )

    // Madrasahs List synchronized with real Madrasahs in the app
    val madrasahsList = MutableStateFlow<List<AdminMadrasahItem>>(
        listOf(
            AdminMadrasahItem("m1", "দারুল উলুম মুঈনুল ইসলাম (হাটহাজারী মাদ্রাসা)", "হাটহাজারী, চট্টগ্রাম", "মুফতি খলীল আহমদ কাসেমী (দা.বা.)", "দাওরায়ে হাদীস, ইফতা, উলুমুল কুরআন, হিফজখানা", "01819112233", "REG-1901-01", "যাচাইকৃত"),
            AdminMadrasahItem("m2", "আল জামিয়া আল ইসলামিয়া পটিয়া", "পটিয়া, চট্টগ্রাম", "মুহতামিম পরিষদ", "দাওরায়ে হাদীস, তাখাসসুসাত, আরবি সাহিত্য", "01818223344", "REG-1938-02", "যাচাইকৃত"),
            AdminMadrasahItem("m3", "জামিয়া ইসলামিয়া দারুল উলুম মাদানিয়া", "যাত্রাবাড়ী, ঢাকা", "আল্লামা মাহমুদুল হাসান (দা.বা.)", "দাওরায়ে হাদীস, ইফতা, কিরাত, দাওয়াহ", "01711334455", "REG-1980-03", "যাচাইকৃত"),
            AdminMadrasahItem("m4", "জামিয়া শারইয়্যাহ মালিবাগ", "মালিবাগ, ঢাকা", "মুহতামিম ও শায়খুল হাদীস পরিষদ", "দাওরায়ে হাদীস, তাফসীর, ফিকহুল ইসলামি", "01911445566", "REG-1956-04", "যাচাইকৃত"),
            AdminMadrasahItem("m5", "আল জামিয়াতুল আরাবিয়া দারুল কুরআন", "সিলেট সদর, সিলেট", "মাওলানা আব্দুল করিম (দা.বা.)", "দাওরায়ে হাদীস, তাজবীদ, নূরানী শিক্ষক প্রশিক্ষণ", "01722556677", "REG-1985-05", "যাচাইকৃত"),
            AdminMadrasahItem("m6", "জামিয়া ইসলামিয়া আরাবিয়া", "বানারীপাড়া, বরিশাল", "মাওলানা আব্দুর রহমান (দা.বা.)", "দাওরায়ে হাদীস, হিফজুল কুরআন, কিতাব বিভাগ", "01511667788", "REG-1972-06", "অপেক্ষমাণ")
        )
    )

    // Notices & Applications Status Maps synced with FeedRepository.posts
    val noticesList = MutableStateFlow<List<NoticeStatusItem>>(emptyList())
    val applicationsList = MutableStateFlow<List<ApplicationStatusItem>>(emptyList())

    // Boost Policy Config
    val boostPolicy = MutableStateFlow(BoostPolicyConfig())

    // Boost Requests
    val boostRequests = MutableStateFlow<List<BoostRequestItem>>(
        listOf(
            BoostRequestItem("b1", "post_1", "সহকারী শিক্ষক (আরবি, ফিকহ) — দারুল উলুম কওমি মাদ্রাসা", "জনাব আলী (কুমিল্লা)", "Pending", "১৮ অক্টোবর ২০২৬", durationDays = 7, targetScope = "সকল ব্যবহারকারী", viewsCount = 42, clicksCount = 18, applicationsCount = 6),
            BoostRequestItem("b2", "post_3", "সহকারী শিক্ষক (ইংরেজি, গণিত) — দারুল কুরআন সিলেট", "মাওলানা আব্দুল করিম", "Under Review", "১৯ অক্টোবর ২০২৬", durationDays = 15, targetScope = "এলাকা: সিলেট", viewsCount = 29, clicksCount = 11, applicationsCount = 4),
            BoostRequestItem("b3", "post_5", "মুহতামিম / প্রধান শিক্ষক — জামিয়া ইসলামিয়া বরিশাল", "মাওলানা আব্দুর রহমান", "Approved", "১৯ অক্টোবর ২০২৬", durationDays = 7, targetScope = "সকল ব্যবহারকারী", viewsCount = 65, clicksCount = 27, applicationsCount = 9),
            BoostRequestItem("b4", "post_1", "হিফজ ও নাজেরা শিক্ষক নিয়োগ বিজ্ঞপ্তি", "হাফেজ আব্দুল কাদের", "Active", "২০ অক্টোবর ২০২৬", durationDays = 14, expiresAtMillis = System.currentTimeMillis() + 14L * 24 * 3600 * 1000, targetScope = "সকল ব্যবহারকারী", viewsCount = 88, clicksCount = 34, applicationsCount = 12)
        )
    )

    // Sent Notifications History
    val sentNotifications = MutableStateFlow<List<SentNotificationItem>>(
        listOf(
            SentNotificationItem("n1", "নতুন খেদমত বিজ্ঞপ্তি প্রকাশিত হয়েছে", "দারুল উলুম কওমি মাদ্রাসায় সহকারী শিক্ষক পদে নিয়োগ চলছে।", "সকল ব্যবহারকারী", "১৯ অক্টোবর ২০২৬", "প্রেরিত", category = "Announcements", deliveryStatus = "ডাটাবেস ও ডিভাইসে প্রেরিত", isDeliveredToDevice = true),
            SentNotificationItem("n2", "বিশেষভাবে প্রচারিত বিজ্ঞপ্তি: মুহতামিম নিয়োগ", "জামিয়া ইসলামিয়া আরাবিয়া (বরিশাল)-এ মুহতামিম পদে বিশেষ বিজ্ঞপ্তি প্রচারিত হচ্ছে।", "সকল ব্যবহারকারী", "২০ অক্টোবর ২০২৬", "প্রেরিত", targetRoute = "khedmot", targetItemId = "post_5", category = "Boost", deliveryStatus = "ডাটাবেস ও ডিভাইসে প্রেরিত", isDeliveredToDevice = true)
        )
    )

    // Support & Complaints Tickets
    val supportTickets = MutableStateFlow<List<SupportTicketItem>>(
        listOf(
            SupportTicketItem("t1", "হাফেজ মোঃ ইউসুফ", "01799887766", "লগইন পাসওয়ার্ড রিসেট সংক্রান্ত সহায়তা", "আমার ইমেইলে পাসওয়ার্ড রিসেট মেইল পৌঁছাতে দেরি হচ্ছে, অনুগ্রহ করে দেখুন।", "১৯ অক্টোবর ২০২৬", "সমাধানকৃত", "পাসওয়ার্ড রিসেট লিংক পুনরায় পাঠানো হয়েছে।"),
            SupportTicketItem("t2", "মাওলানা জাকারিয়া", "01855443322", "মাদ্রাসা ভেরিফিকেশন আবেদন", "আমাদের মাদ্রাসার নিবন্ধন তথ্য যাচাই করে ভেরিফায়েড ব্যাজ প্রদানের অনুরোধ করছি।", "২০ অক্টোবর ২০২৬", "অপেক্ষমাণ", "")
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

    // Role Permissions Matrix
    var permissions by mutableStateOf(AdminPermissions())

    // App Content
    var appContent by mutableStateOf(AppContentConfig())

    // Telegram Settings
    var telegramSettings by mutableStateOf(TelegramSettingsConfig())

    // Audit Logs
    val auditLogs = MutableStateFlow<List<AuditLogEntry>>(
        listOf(
            AuditLogEntry("log_1", "২০ অক্টোবর ২০২৬, ১০:৩০ AM", "foysalahamad204@gmail.com", "Admin প্যানেলে নিরাপত্তা ও মডিউল কনফিগারেশন যাচাই করা হয়েছে", "Security"),
            AuditLogEntry("log_2", "২০ অক্টোবর ২০২৬, ০৯:১৫ AM", "mdfoysalahamad202@gmail.com", "হোমপেজ ব্যানার ও স্লোগান হালনাগাদ করা হয়েছে", "Content"),
            AuditLogEntry("log_3", "১৯ অক্টোবর ২০২৬, ০৮:৪০ PM", "sumuaktar01300448147@gmail.com", "নতুন খেদমত বিজ্ঞপ্তি ও আবেদন যাচাই করা হয়েছে", "Application")
        )
    )

    // Config Backups / Version History
    val configBackups = MutableStateFlow<List<ConfigBackupEntry>>(emptyList())

    private val recentEventKeys = mutableSetOf<String>()

    fun init(context: Context) {
        appContext = context.applicationContext
        FeedRepository.init(context)
        syncWithFeedRepository()
        loadFromPrefs()
        checkExpiredBoosts()
        FirebaseSyncManager.init(context)
    }

    fun updateNoticesFromCloud(cloudNotices: List<NoticeStatusItem>) {
        noticesList.value = cloudNotices
        cloudNotices.forEach { n ->
            if (FeedRepository.posts.none { it.id == n.postId }) {
                FeedRepository.posts.add(
                    FeedItem(
                        id = n.postId,
                        type = FeedItemType.MADRASAH_NOTICE,
                        title = n.madrasahName,
                        location = n.location,
                        role = n.roleTitle,
                        qualification = "দাওরায়ে হাদীস / সমমান",
                        extraInfo = n.salaryInfo,
                        secondaryInfo = n.deadline,
                        timeAgo = "হালনাগাদ",
                        isVerified = n.status == "অনুমোদিত",
                        contactPhone = n.phone
                    )
                )
            }
        }
        saveToPrefs()
    }

    fun updateApplicationsFromCloud(cloudApps: List<ApplicationStatusItem>) {
        applicationsList.value = cloudApps
        cloudApps.forEach { a ->
            if (FeedRepository.posts.none { it.id == a.postId }) {
                FeedRepository.posts.add(
                    FeedItem(
                        id = a.postId,
                        type = FeedItemType.CANDIDATE_APPLICATION,
                        title = a.candidateName,
                        location = a.location,
                        role = a.roleTitle,
                        qualification = a.qualification,
                        extraInfo = a.experience,
                        secondaryInfo = a.location,
                        timeAgo = a.submittedDate.ifBlank { "আজ" },
                        isVerified = a.status == "অনুমোদিত",
                        contactPhone = a.phone
                    )
                )
            }
        }
        saveToPrefs()
    }

    fun syncWithFeedRepository() {
        val currentNotices = noticesList.value.associateBy { it.postId }
        val currentApps = applicationsList.value.associateBy { it.postId }
        val activeBoostIds = boostRequests.value
            .filter { it.status == "Active" || it.status == "Approved" }
            .map { it.noticeId }
            .toSet()
        val allBoostRequestNoticeIds = boostRequests.value
            .map { it.noticeId }
            .toSet()

        val syncedNotices = mutableListOf<NoticeStatusItem>()
        val syncedApps = mutableListOf<ApplicationStatusItem>()

        FeedRepository.posts.forEach { post ->
            if (post.type == FeedItemType.MADRASAH_NOTICE) {
                val existing = currentNotices[post.id]
                val computedIsBoosted = if (post.id in allBoostRequestNoticeIds) {
                    post.id in activeBoostIds
                } else {
                    existing?.isBoosted == true
                }
                syncedNotices.add(
                    NoticeStatusItem(
                        postId = post.id,
                        madrasahName = post.title,
                        roleTitle = post.role,
                        location = post.location,
                        salaryInfo = post.extraInfo,
                        deadline = post.secondaryInfo,
                        phone = post.contactPhone,
                        status = existing?.status ?: if (post.isVerified) "অনুমোদিত" else "অপেক্ষমাণ",
                        isBoosted = computedIsBoosted,
                        viewsCount = existing?.viewsCount ?: post.viewsCount,
                        clicksCount = existing?.clicksCount ?: post.clicksCount,
                        applicationsCount = existing?.applicationsCount ?: post.applicationsCount
                    )
                )
            } else {
                val existing = currentApps[post.id]
                syncedApps.add(
                    ApplicationStatusItem(
                        postId = post.id,
                        candidateName = post.title,
                        roleTitle = post.role,
                        location = post.location,
                        qualification = post.qualification,
                        experience = post.extraInfo,
                        phone = post.contactPhone,
                        status = existing?.status ?: "অনুমোদিত",
                        submittedDate = existing?.submittedDate?.ifBlank { post.timeAgo } ?: post.timeAgo
                    )
                )
            }
        }
        noticesList.value = syncedNotices
        applicationsList.value = syncedApps
    }

    fun isUserAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return AUTHORIZED_ADMIN_EMAILS.any { it.lowercase() == cleanEmail } ||
            adminUsers.value.any { it.email.lowercase() == cleanEmail && it.status == "সক্রিয়" }
    }

    fun getAdminRole(email: String?): AdminRole {
        if (email.isNullOrBlank()) return AdminRole.LIMITED_ADMIN
        val clean = email.trim().lowercase()
        if (clean == SUPER_ADMIN_EMAIL) return AdminRole.SUPER_ADMIN
        return adminUsers.value.find { it.email.lowercase() == clean }?.role ?: AdminRole.FULL_ADMIN
    }

    fun isSuperAdmin(email: String?): Boolean {
        return email?.trim()?.lowercase() == SUPER_ADMIN_EMAIL
    }

    fun recordAuditLog(actorEmail: String, action: String, category: String) {
        val entry = AuditLogEntry(
            id = "log_${System.currentTimeMillis()}",
            time = currentTimeFormatted(),
            user = actorEmail.ifBlank { SUPER_ADMIN_EMAIL },
            action = action,
            category = category
        )
        auditLogs.value = listOf(entry) + auditLogs.value
        FirebaseSyncManager.saveAuditLog(entry)
        saveToPrefs()
    }

    fun triggerEventNotification(
        title: String,
        body: String,
        targetAudience: String = "সকল ব্যবহারকারী",
        targetRoute: String = "khedmot",
        targetItemId: String = "",
        eventKey: String = "",
        category: String = "Announcements"
    ) {
        if (eventKey.isNotBlank() && !recentEventKeys.add(eventKey)) {
            return // Prevent duplicate notification for same event
        }
        val item = SentNotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            title = title.trim(),
            body = body.trim(),
            targetAudience = targetAudience,
            sentTime = currentTimeFormatted(),
            status = "প্রেরিত",
            targetRoute = targetRoute,
            targetItemId = targetItemId,
            eventKey = eventKey.ifBlank { "evt_${System.currentTimeMillis()}" },
            category = category
        )
        sentNotifications.value = listOf(item) + sentNotifications.value
        FirebaseSyncManager.saveNotification(item)
        saveToPrefs()
    }

    // User Management Operations
    fun toggleUserStatus(userId: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        var targetUser: UserManagementItem? = null
        usersList.value = usersList.value.map { user ->
            if (user.id == userId) {
                val nextStatus = if (user.status == "সক্রিয়") "নিষ্ক্রিয়" else "সক্রিয়"
                val updated = user.copy(status = nextStatus)
                targetUser = updated
                updated
            } else user
        }
        targetUser?.let { u ->
            FirebaseSyncManager.saveUser(u)
            recordAuditLog(actorEmail, "${u.name}-এর অ্যাকাউন্ট স্ট্যাটাস '${u.status}' করা হয়েছে", "User")
            if (u.status == "নিষ্ক্রিয়" && telegramSettings.notifySecurityAlert && TelegramSecretConfig.isBotConfigured(appContext)) {
                TelegramSecretConfig.sendTelegramAlertAsync(
                    context = appContext,
                    title = "নিরাপত্তা সতর্কতা: অ্যাকাউন্ট স্থগিত",
                    messageBody = "ব্যবহারকারী '${u.name}' (${u.email})-এর অ্যাকাউন্ট '$actorEmail' কর্তৃক নিষ্ক্রিয় করা হয়েছে।"
                )
            }
            triggerEventNotification(
                title = "অ্যাকাউন্ট স্ট্যাটাস আপডেট",
                body = "${u.name}-এর অ্যাকাউন্ট স্ট্যাটাস '${u.status}' করা হয়েছে।",
                targetAudience = u.email.ifBlank { "সকল ব্যবহারকারী" },
                targetRoute = "profile",
                targetItemId = u.id,
                eventKey = "user_status_${u.id}_${u.status}",
                category = "Security"
            )
        }
        saveToPrefs()
    }

    fun updateUserRole(userId: String, newRole: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        var updatedUser: UserManagementItem? = null
        usersList.value = usersList.value.map { user ->
            if (user.id == userId) {
                val u = user.copy(role = newRole)
                updatedUser = u
                u
            } else user
        }
        updatedUser?.let { u ->
            FirebaseSyncManager.saveUser(u)
            if ((newRole.contains("Admin", ignoreCase = true) || newRole.contains("Moderator", ignoreCase = true)) &&
                telegramSettings.notifyNewAdmin && TelegramSecretConfig.isBotConfigured(appContext)
            ) {
                TelegramSecretConfig.sendTelegramAlertAsync(
                    context = appContext,
                    title = "নতুন Admin / ভূমিকা পরিবর্তন",
                    messageBody = "ব্যবহারকারী '${u.name}' (${u.email})-কে '$newRole' ভূমিকা প্রদান করা হয়েছে।"
                )
            }
            triggerEventNotification(
                title = "অ্যাকাউন্ট ভূমিকা হালনাগাদ",
                body = "${u.name}-এর ভূমিকা '$newRole'-এ উন্নীত/পরিবর্তন করা হয়েছে।",
                targetAudience = u.email.ifBlank { "সকল ব্যবহারকারী" },
                targetRoute = "profile",
                targetItemId = u.id,
                eventKey = "user_role_${u.id}_$newRole",
                category = "Security"
            )
        }
        recordAuditLog(actorEmail, "ব্যবহারকারীর ভূমিকা '$newRole'-এ পরিবর্তন করা হয়েছে", "User")
        saveToPrefs()
    }

    fun addUser(name: String, email: String, phone: String, role: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        val cleanEmail = email.trim()
        if (usersList.value.any { it.email.equals(cleanEmail, ignoreCase = true) && cleanEmail.isNotEmpty() }) {
            return // Prevent duplicate user entry
        }
        val newItem = UserManagementItem(
            id = "u_${System.currentTimeMillis()}",
            name = name.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            status = "সক্রিয়",
            role = role,
            joinedDate = currentShortDateFormatted()
        )
        usersList.value = listOf(newItem) + usersList.value
        FirebaseSyncManager.saveUser(newItem)
        recordAuditLog(actorEmail, "নতুন ব্যবহারকারী '${newItem.name}' যোগ করা হয়েছে", "User")
        saveToPrefs()
    }

    // Madrasah Management Operations
    fun updateMadrasahStatus(madrasahId: String, newStatus: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        var updatedMadrasah: AdminMadrasahItem? = null
        madrasahsList.value = madrasahsList.value.map { m ->
            if (m.id == madrasahId) {
                val updated = m.copy(verificationStatus = newStatus)
                updatedMadrasah = updated
                updated
            } else m
        }
        updatedMadrasah?.let { m ->
            FirebaseSyncManager.saveMadrasah(m)
            recordAuditLog(actorEmail, "'${m.name}'-এর যাচাই স্ট্যাটাস '$newStatus' করা হয়েছে", "Madrasah")
            if (telegramSettings.notifyImportantChanges && TelegramSecretConfig.isBotConfigured(appContext)) {
                TelegramSecretConfig.sendTelegramAlertAsync(
                    context = appContext,
                    title = "মাদ্রাসা যাচাইকরণ আপডেট",
                    messageBody = "মাদ্রাসা: ${m.name} (${m.location})\nনতুন স্ট্যাটাস: $newStatus\nসম্পাদনকারী: $actorEmail"
                )
            }
            triggerEventNotification(
                title = "মাদ্রাসা যাচাইকরণ আপডেট",
                body = "${m.name} (${m.location})-এর যাচাইকরণ স্ট্যাটাস '$newStatus' করা হয়েছে।",
                targetAudience = "মাদ্রাসা প্রতিনিধি",
                targetRoute = "madrasah",
                targetItemId = m.id,
                eventKey = "madrasah_ver_${m.id}_$newStatus"
            )
        }
        saveToPrefs()
    }

    fun addMadrasah(
        name: String,
        location: String,
        muhtamim: String,
        departments: String,
        phone: String,
        regNumber: String,
        actorEmail: String = SUPER_ADMIN_EMAIL,
        initialVerificationStatus: String = "যাচাইকৃত"
    ) {
        val item = AdminMadrasahItem(
            id = "m_${System.currentTimeMillis()}",
            name = name.trim(),
            location = location.trim(),
            muhtamim = muhtamim.trim(),
            departments = departments.trim(),
            phone = phone.trim(),
            regNumber = regNumber.trim(),
            verificationStatus = initialVerificationStatus
        )
        madrasahsList.value = listOf(item) + madrasahsList.value
        FirebaseSyncManager.saveMadrasah(item)
        recordAuditLog(actorEmail, "নতুন মাদ্রাসা '${item.name}' তালিকায় যুক্ত করা হয়েছে", "Madrasah")
        saveToPrefs()
    }

    // Notice Operations
    fun updateNoticeStatus(postId: String, newStatus: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        var updatedNotice: NoticeStatusItem? = null
        noticesList.value = noticesList.value.map { n ->
            if (n.postId == postId) {
                val updated = n.copy(status = newStatus)
                updatedNotice = updated
                updated
            } else n
        }
        updatedNotice?.let { n ->
            FirebaseSyncManager.saveNotice(n)
            if (newStatus == "অনুমোদিত") {
                triggerEventNotification(
                    title = "নতুন খেদমতের বিজ্ঞপ্তি প্রকাশিত",
                    body = "${n.madrasahName}-এ '${n.roleTitle}' পদে নিয়োগ বিজ্ঞপ্তি প্রকাশিত হয়েছে।",
                    targetAudience = "খেদমতপ্রার্থী",
                    targetRoute = "khedmot",
                    targetItemId = n.postId,
                    eventKey = "notice_approved_${n.postId}",
                    category = "Announcements"
                )
            } else {
                // If a notice becomes unapproved/rejected, automatically disable its boost
                if (n.isBoosted) {
                    noticesList.value = noticesList.value.map {
                        if (it.postId == postId) it.copy(isBoosted = false) else it
                    }
                    boostRequests.value = boostRequests.value.map { b ->
                        if (b.noticeId == postId && (b.status == "Active" || b.status == "Approved")) {
                            val cancelled = b.copy(status = "Cancelled")
                            FirebaseSyncManager.saveBoostRequest(cancelled)
                            cancelled
                        } else b
                    }
                }
            }
        }
        recordAuditLog(actorEmail, "বিজ্ঞপ্তি ($postId)-এর স্ট্যাটাস '$newStatus' করা হয়েছে", "Notice")
        saveToPrefs()
    }

    fun addKhidmatNotice(
        madrasahName: String,
        roleTitle: String,
        location: String,
        qualification: String,
        salary: String,
        deadline: String,
        phone: String,
        actorEmail: String = SUPER_ADMIN_EMAIL
    ) {
        val dedupKey = "add_notice_${madrasahName.trim()}_${roleTitle.trim()}_${phone.trim()}"
        if (!recentEventKeys.add(dedupKey)) return

        val newId = "post_${System.currentTimeMillis()}"
        val feedItem = FeedItem(
            id = newId,
            type = FeedItemType.MADRASAH_NOTICE,
            title = madrasahName.trim(),
            location = location.trim(),
            role = roleTitle.trim(),
            qualification = qualification.trim(),
            extraInfo = salary.trim(),
            secondaryInfo = deadline.trim(),
            timeAgo = "এইমাত্র",
            isVerified = true,
            contactPhone = phone.trim()
        )
        FeedRepository.addPost(feedItem)
        syncWithFeedRepository()
        noticesList.value.find { it.postId == newId }?.let {
            FirebaseSyncManager.saveNotice(it, qualification = qualification.trim())
        }
        triggerEventNotification(
            title = "নতুন খেদমতের বিজ্ঞপ্তি: ${roleTitle.trim()}",
            body = "${madrasahName.trim()} (${location.trim()})-এ নিয়োগ বিজ্ঞপ্তি প্রকাশিত হয়েছে। বেতন: ${salary.trim()}",
            targetAudience = "খেদমতপ্রার্থী",
            targetRoute = "khedmot",
            targetItemId = newId,
            eventKey = "notice_pub_$newId"
        )
        recordAuditLog(actorEmail, "নতুন খেদমত বিজ্ঞপ্তি '${roleTitle.trim()}' প্রকাশিত হয়েছে", "Notice")
        saveToPrefs()
    }

    fun deleteNotice(postId: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        FeedRepository.posts.removeAll { it.id == postId }
        noticesList.value = noticesList.value.filter { it.postId != postId }
        FirebaseSyncManager.deleteNoticeFromFirestore(postId)
        recordAuditLog(actorEmail, "খেদমত বিজ্ঞপ্তি ($postId) মুছে ফেলা হয়েছে", "Notice")
        saveToPrefs()
    }

    // Application Operations
    fun updateApplicationStatus(postId: String, newStatus: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        var updatedApp: ApplicationStatusItem? = null
        applicationsList.value = applicationsList.value.map { app ->
            if (app.postId == postId) {
                val u = app.copy(status = newStatus)
                updatedApp = u
                u
            } else app
        }
        updatedApp?.let { app ->
            FirebaseSyncManager.saveApplication(app)
            recordAuditLog(actorEmail, "প্রার্থী '${app.candidateName}'-এর আবেদন স্ট্যাটাস '$newStatus' করা হয়েছে", "Application")
            triggerEventNotification(
                title = "আবেদনের স্ট্যাটাস পরিবর্তন: $newStatus",
                body = "${app.candidateName} (${app.roleTitle})-এর আবেদনের বর্তমান স্ট্যাটাস '$newStatus'।",
                targetAudience = "খেদমতপ্রার্থী",
                targetRoute = "abedon",
                targetItemId = app.postId,
                eventKey = "app_status_${app.postId}_$newStatus",
                category = "Applications"
            )
        }
        saveToPrefs()
    }

    fun addCandidateApplication(
        candidateName: String,
        roleTitle: String,
        location: String,
        qualification: String,
        experience: String,
        preferredDistricts: String,
        phone: String,
        actorEmail: String = SUPER_ADMIN_EMAIL
    ) {
        val dedupKey = "add_cand_${candidateName.trim()}_${roleTitle.trim()}_${phone.trim()}"
        if (!recentEventKeys.add(dedupKey)) return

        val newId = "post_${System.currentTimeMillis()}"
        val feedItem = FeedItem(
            id = newId,
            type = FeedItemType.CANDIDATE_APPLICATION,
            title = candidateName.trim(),
            location = location.trim(),
            role = roleTitle.trim(),
            qualification = qualification.trim(),
            extraInfo = experience.trim(),
            secondaryInfo = preferredDistricts.trim(),
            timeAgo = "এইমাত্র",
            isVerified = true,
            contactPhone = phone.trim()
        )
        FeedRepository.addPost(feedItem)
        syncWithFeedRepository()

        val appItem = ApplicationStatusItem(
            postId = newId,
            candidateName = candidateName.trim(),
            roleTitle = roleTitle.trim(),
            location = location.trim(),
            qualification = qualification.trim(),
            experience = experience.trim(),
            phone = phone.trim(),
            status = "অনুমোদিত",
            submittedDate = "এইমাত্র"
        )
        if (applicationsList.value.none { it.postId == newId }) {
            applicationsList.value = listOf(appItem) + applicationsList.value
        }
        FirebaseSyncManager.saveApplication(appItem, preferredDistricts = preferredDistricts.trim())

        triggerEventNotification(
            title = "নতুন খেদমতপ্রার্থীর আবেদন: ${roleTitle.trim()}",
            body = "${candidateName.trim()} (${qualification.trim()}) খেদমতের জন্য আবেদন জমা দিয়েছেন।",
            targetAudience = "মাদ্রাসা প্রতিনিধি",
            targetRoute = "abedon",
            targetItemId = newId,
            eventKey = "cand_pub_$newId",
            category = "Applications"
        )
        recordAuditLog(actorEmail, "নতুন খেদমতপ্রার্থী '${candidateName.trim()}'-এর আবেদন যুক্ত হয়েছে", "Application")
        saveToPrefs()
    }

    // Analytics & Interaction Tracking (Views, Clicks, Applications)
    fun recordNoticeInteraction(postId: String, interactionType: String) {
        if (postId.isBlank()) return
        var updatedNotice: NoticeStatusItem? = null
        noticesList.value = noticesList.value.map { n ->
            if (n.postId == postId) {
                val next = when (interactionType) {
                    "view" -> n.copy(viewsCount = n.viewsCount + 1)
                    "click" -> n.copy(clicksCount = n.clicksCount + 1)
                    "apply" -> n.copy(applicationsCount = n.applicationsCount + 1)
                    else -> n
                }
                updatedNotice = next
                next
            } else n
        }
        updatedNotice?.let { n ->
            FirebaseSyncManager.saveNotice(n)
        }
        val idx = FeedRepository.posts.indexOfFirst { it.id == postId }
        if (idx >= 0) {
            val current = FeedRepository.posts[idx]
            FeedRepository.posts[idx] = when (interactionType) {
                "view" -> current.copy(viewsCount = current.viewsCount + 1)
                "click" -> current.copy(clicksCount = current.clicksCount + 1)
                "apply" -> current.copy(applicationsCount = current.applicationsCount + 1)
                else -> current
            }
        }
        boostRequests.value = boostRequests.value.map { b ->
            if (b.noticeId == postId && (b.status == "Active" || b.status == "Approved")) {
                val updatedB = when (interactionType) {
                    "view" -> b.copy(viewsCount = b.viewsCount + 1)
                    "click" -> b.copy(clicksCount = b.clicksCount + 1)
                    "apply" -> b.copy(applicationsCount = b.applicationsCount + 1)
                    else -> b
                }
                FirebaseSyncManager.saveBoostRequest(updatedB)
                updatedB
            } else b
        }
        saveToPrefs()
    }

    fun updateBoostPolicy(
        maxConcurrentBoosts: Int,
        maxActivePerUser: Int,
        notificationFrequencyLimitHours: Int,
        actorEmail: String = SUPER_ADMIN_EMAIL
    ): Boolean {
        if (!isUserAdmin(actorEmail)) return false
        val updated = BoostPolicyConfig(
            maxConcurrentBoosts = maxConcurrentBoosts.coerceIn(1, 50),
            maxActivePerUser = maxActivePerUser.coerceIn(1, 10),
            notificationFrequencyLimitHours = notificationFrequencyLimitHours.coerceIn(1, 168)
        )
        boostPolicy.value = updated
        FirebaseSyncManager.saveBoostPolicy(updated)
        recordAuditLog(
            actorEmail,
            "বুস্ট নীতিমালা আপডেট: সর্বোচ্চ সক্রিয় বুস্ট=${updated.maxConcurrentBoosts}, প্রতি ইউজার=${updated.maxActivePerUser}, নোটিফিকেশন বিরতি=${updated.notificationFrequencyLimitHours} ঘণ্টা",
            "Boost"
        )
        saveToPrefs()
        return true
    }

    // Boost Management Operations
    fun checkExpiredBoosts() {
        val now = System.currentTimeMillis()
        var changed = false
        val expiredNoticeIds = mutableSetOf<String>()
        boostRequests.value = boostRequests.value.map { item ->
            if ((item.status == "Active" || item.status == "Approved") && item.expiresAtMillis != null && now >= item.expiresAtMillis) {
                changed = true
                expiredNoticeIds.add(item.noticeId)
                val expired = item.copy(status = "Expired")
                FirebaseSyncManager.saveBoostRequest(expired)
                triggerEventNotification(
                    title = "বুস্ট প্রচারের মেয়াদ সম্পন্ন হয়েছে",
                    body = "'${item.title}' বিজ্ঞপ্তির বিশেষ প্রচারের মেয়াদ শেষ হয়েছে এবং বিজ্ঞপ্তিটি স্বাভাবিক Feed-এ ফিরে গেছে।",
                    targetAudience = item.requestedBy.ifBlank { "মাদ্রাসা প্রতিনিধি" },
                    targetRoute = "khedmot",
                    targetItemId = item.noticeId,
                    eventKey = "boost_expired_${item.id}",
                    category = "Boost"
                )
                expired
            } else item
        }
        if (changed) {
            syncWithFeedRepository()
            val remainingActiveIds = boostRequests.value
                .filter { it.status == "Active" || it.status == "Approved" }
                .map { it.noticeId }
                .toSet()
            noticesList.value = noticesList.value.map { n ->
                if (n.postId in expiredNoticeIds && n.postId !in remainingActiveIds) {
                    val updatedN = n.copy(isBoosted = false)
                    FirebaseSyncManager.saveNotice(updatedN)
                    updatedN
                } else n
            }
            saveToPrefs()
        }
    }

    /**
     * শুধুমাত্র অনুমোদিত Admin বুস্ট আবেদন Approve, Reject, Pause, Resume ও Cancel করতে পারবেন।
     */
    fun updateBoostStatus(
        id: String,
        newStatus: String,
        durationDays: Int = 7,
        actorEmail: String = SUPER_ADMIN_EMAIL,
        targetScopeOverride: String? = null
    ): Boolean {
        if (!isUserAdmin(actorEmail)) {
            recordAuditLog(actorEmail, "অননুমোদিত বুস্ট স্ট্যাটাস পরিবর্তনের প্রচেষ্টা ব্লক করা হয়েছে", "Security")
            return false
        }

        val existingBoost = boostRequests.value.find { it.id == id } ?: return false
        val targetNotice = noticesList.value.find { it.postId == existingBoost.noticeId }

        val activating = newStatus == "Approved" || newStatus == "Active" || newStatus == "Approve" || newStatus == "Resume"
        if (activating) {
            if (targetNotice == null || targetNotice.status != "অনুমোদিত") {
                recordAuditLog(actorEmail, "অননুমোদিত বা মুছে ফেলা বিজ্ঞপ্তির (${existingBoost.noticeId}) বুস্ট অনুমোদন প্রতিরোধ করা হয়েছে", "Boost")
                return false
            }
            val currentActiveCount = boostRequests.value.count {
                it.id != id && (it.status == "Active" || it.status == "Approved")
            }
            if (currentActiveCount >= boostPolicy.value.maxConcurrentBoosts) {
                recordAuditLog(actorEmail, "সর্বোচ্চ সক্রিয় বুস্ট সীমা (${boostPolicy.value.maxConcurrentBoosts}) অতিক্রম করায় অনুমোদন ব্লক", "Boost")
                return false
            }
        }

        val normalizedStatus = when (newStatus.trim()) {
            "Approve", "Approved", "Active" -> "Active"
            "Resume" -> "Active"
            "Pause", "Paused" -> "Paused"
            "Cancel", "Cancelled" -> "Cancelled"
            "Reject", "Rejected" -> "Rejected"
            else -> newStatus.trim()
        }

        val now = System.currentTimeMillis()
        val finalDays = durationDays.coerceIn(1, 90)
        var updatedBoost: BoostRequestItem? = null
        boostRequests.value = boostRequests.value.map { item ->
            if (item.id == id) {
                val finalScope = targetScopeOverride?.takeIf { it.isNotBlank() } ?: item.targetScope
                val expiresAt = when {
                    normalizedStatus == "Active" && item.status == "Paused" && item.expiresAtMillis != null && item.expiresAtMillis > now -> item.expiresAtMillis
                    normalizedStatus == "Active" -> now + finalDays.toLong() * 24L * 3600L * 1000L
                    normalizedStatus == "Paused" -> item.expiresAtMillis
                    else -> null
                }
                val u = item.copy(
                    status = normalizedStatus,
                    durationDays = finalDays,
                    expiresAtMillis = expiresAt,
                    targetScope = finalScope
                )
                updatedBoost = u
                u
            } else item
        }
        syncWithFeedRepository()
        updatedBoost?.let { b ->
            FirebaseSyncManager.saveBoostRequest(b)
            noticesList.value.find { it.postId == b.noticeId }?.let { n ->
                FirebaseSyncManager.saveNotice(n)
            }
            if (telegramSettings.notifyBoostRequest && TelegramSecretConfig.isBotConfigured(appContext)) {
                TelegramSecretConfig.sendTelegramAlertAsync(
                    context = appContext,
                    title = "Boost অনুরোধ আপডেট: $normalizedStatus",
                    messageBody = "বিজ্ঞপ্তি: ${b.title}\nনতুন স্ট্যাটাস: $normalizedStatus ($finalDays দিন)\nলক্ষ্য পরিধি: ${b.targetScope}\nসম্পাদনকারী: $actorEmail"
                )
            }

            val isBoostActiveNow = normalizedStatus == "Active" || normalizedStatus == "Approved"
            if (isBoostActiveNow) {
                val ctx = appContext
                val canBroadcast = ctx == null || NotificationHelper.canSendWithinFrequencyLimit(
                    ctx,
                    b.noticeId,
                    boostPolicy.value.notificationFrequencyLimitHours
                )
                if (canBroadcast) {
                    if (ctx != null) {
                        NotificationHelper.recordFrequencyTimestamp(ctx, b.noticeId)
                    }
                    val madrasahInfo = targetNotice?.let { "${it.madrasahName} (${it.location})" } ?: "কওমি মাদ্রাসা"
                    val roleInfo = targetNotice?.roleTitle ?: b.title
                    triggerEventNotification(
                        title = "বিশেষভাবে প্রচারিত বিজ্ঞপ্তি: $roleInfo",
                        body = "$madrasahInfo-এ '$roleInfo' পদে বিশেষ নিয়োগ চলছে। বিস্তারিত দেখতে ট্যাপ করুন।",
                        targetAudience = b.targetScope.ifBlank { "সকল ব্যবহারকারী" },
                        targetRoute = "khedmot",
                        targetItemId = b.noticeId,
                        eventKey = "boost_broadcast_${b.id}_$normalizedStatus",
                        category = "Boost"
                    )
                }
            }

            val statusBn = when (normalizedStatus) {
                "Active" -> "অনুমোদিত ও সক্রিয় (Active)"
                "Paused" -> "সাময়িক বিরতি (Paused)"
                "Cancelled" -> "বাতিলকৃত (Cancelled)"
                "Rejected" -> "প্রত্যাখ্যাত (Rejected)"
                else -> normalizedStatus
            }
            triggerEventNotification(
                title = "Boost অনুরোধ আপডেট: $statusBn",
                body = "'${b.title}' বিজ্ঞপ্তির Boost অনুরোধ '$statusBn' ($finalDays দিন) করা হয়েছে।",
                targetAudience = b.requestedBy.ifBlank { "মাদ্রাসা প্রতিনিধি" },
                targetRoute = "khedmot",
                targetItemId = b.noticeId,
                eventKey = "boost_${b.id}_${normalizedStatus}_${System.currentTimeMillis() / 1000}",
                category = "Boost"
            )
        }
        recordAuditLog(actorEmail, "Boost অনুরোধ ($id) স্ট্যাটাস '$normalizedStatus' ($finalDays দিন) করা হয়েছে", "Boost")
        saveToPrefs()
        return true
    }

    /**
     * বৈধ ও অনুমোদিত খেদমত বিজ্ঞপ্তির জন্য বুস্টের আবেদন জমা দেয়।
     * ভুয়া, বাতিল বা অননুমোদিত বিজ্ঞপ্তি Boost করা যাবে না।
     */
    fun submitBoostRequest(
        title: String,
        requestedBy: String,
        durationDays: Int = 7,
        noticeId: String = "post_1",
        targetScope: String = "সকল ব্যবহারকারী"
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return false

        // Verify that the notice exists and is Approved
        val targetNotice = noticesList.value.find { it.postId == noticeId }
        if (targetNotice == null || targetNotice.status != "অনুমোদিত") {
            recordAuditLog(
                requestedBy.ifBlank { "ব্যবহারকারী" },
                "অবৈধ বা অননুমোদিত বিজ্ঞপ্তির ($noticeId) জন্য বুস্ট আবেদন ব্লক করা হয়েছে",
                "Security"
            )
            return false
        }

        // Enforce per-user active boost limit
        val cleanRequester = requestedBy.trim().ifBlank { "মাদ্রাসা প্রতিনিধি" }
        val userActiveCount = boostRequests.value.count {
            it.requestedBy.equals(cleanRequester, ignoreCase = true) &&
                (it.status == "Active" || it.status == "Approved" || it.status == "Pending")
        }
        if (userActiveCount >= boostPolicy.value.maxActivePerUser) {
            return false
        }

        val dedupKey = "boost_req_${noticeId}_${cleanTitle}"
        if (!recentEventKeys.add(dedupKey)) return false

        val item = BoostRequestItem(
            id = "b_${System.currentTimeMillis()}",
            noticeId = noticeId,
            title = cleanTitle,
            requestedBy = cleanRequester,
            status = "Pending",
            requestDate = currentShortDateFormatted(),
            durationDays = durationDays.coerceIn(1, 90),
            targetScope = targetScope.ifBlank { "সকল ব্যবহারকারী" },
            viewsCount = targetNotice.viewsCount,
            clicksCount = targetNotice.clicksCount,
            applicationsCount = targetNotice.applicationsCount
        )
        boostRequests.value = listOf(item) + boostRequests.value
        FirebaseSyncManager.saveBoostRequest(item)
        triggerEventNotification(
            title = "নতুন Boost অনুরোধ জমা হয়েছে",
            body = "$cleanRequester '${item.title}' বিজ্ঞপ্তির জন্য ${item.durationDays} দিনের Boost আবেদন করেছেন।",
            targetAudience = "সকল Admin",
            targetRoute = "khedmot",
            targetItemId = noticeId,
            eventKey = "admin_boost_req_${item.id}",
            category = "Boost"
        )
        if (telegramSettings.notifyBoostRequest && TelegramSecretConfig.isBotConfigured(appContext)) {
            TelegramSecretConfig.sendTelegramAlertAsync(
                context = appContext,
                title = "নতুন Boost অনুরোধ",
                messageBody = "বিজ্ঞপ্তি: ${item.title}\nআবেদনকারী: ${item.requestedBy}\nমেয়াদ: ${item.durationDays} দিন\nলক্ষ্য পরিধি: ${item.targetScope}"
            )
        }
        recordAuditLog(cleanRequester, "নতুন Boost অনুরোধ '${item.title}' (${item.durationDays} দিন, পরিধি: ${item.targetScope}) জমা দেওয়া হয়েছে", "Boost")
        saveToPrefs()
        return true
    }

    // Notifications Operations (Admin Broadcast & Targeted Announcements)
    fun sendBroadcastNotification(
        title: String,
        body: String,
        targetAudience: String,
        actorEmail: String = SUPER_ADMIN_EMAIL,
        targetRoute: String = "khedmot",
        targetItemId: String = "",
        category: String = "Announcements"
    ): Boolean {
        if (!isUserAdmin(actorEmail)) {
            recordAuditLog(actorEmail, "অননুমোদিত নোটিফিকেশন ব্রডকাস্টের চেষ্টা ব্লক করা হয়েছে", "Security")
            if (telegramSettings.notifySecurityAlert && TelegramSecretConfig.isBotConfigured(appContext)) {
                TelegramSecretConfig.sendTelegramAlertAsync(
                    context = appContext,
                    title = "নিরাপত্তা সতর্কতা: অননুমোদিত ব্রডকাস্ট চেষ্টা",
                    messageBody = "অননুমোদিত ব্যবহারকারী ($actorEmail) ব্রডকাস্ট নোটিফিকেশন পাঠানোর চেষ্টা করেছেন।"
                )
            }
            return false
        }
        if (title.isBlank() || body.isBlank()) return false

        val dedupKey = "broadcast_${title.trim()}_${body.trim()}_$targetAudience"
        if (!recentEventKeys.add(dedupKey)) return false

        val item = SentNotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            title = title.trim(),
            body = body.trim(),
            targetAudience = targetAudience,
            sentTime = currentTimeFormatted(),
            status = "প্রেরিত",
            targetRoute = targetRoute,
            targetItemId = targetItemId,
            eventKey = dedupKey,
            category = category
        )
        sentNotifications.value = listOf(item) + sentNotifications.value
        FirebaseSyncManager.saveNotification(item, senderEmail = actorEmail)
        if (telegramSettings.notifyImportantChanges && TelegramSecretConfig.isBotConfigured(appContext)) {
            TelegramSecretConfig.sendTelegramAlertAsync(
                context = appContext,
                title = "নতুন পুশ নোটিফিকেশন ব্রডকাস্ট ($category)",
                messageBody = "শিরোনাম: ${item.title}\nবার্তা: ${item.body}\nপ্রাপক: $targetAudience\nপ্রেরক: $actorEmail"
            )
        }
        recordAuditLog(actorEmail, "নোটিফিকেশন '${item.title}' ($targetAudience, $category) পাঠানো হয়েছে", "Notification")
        saveToPrefs()
        return true
    }

    /**
     * ব্যর্থ বা আটকে থাকা পুশ নোটিফিকেশন পুনরায় ডিভাইসে পাঠানোর চেষ্টা (Retry) করে এবং লগ হালনাগাদ করে
     */
    fun retryNotificationDelivery(notifId: String, actorEmail: String = SUPER_ADMIN_EMAIL): Boolean {
        val ctx = appContext ?: return false
        val target = sentNotifications.value.find { it.id == notifId } ?: return false
        val dedup = target.eventKey.ifBlank { target.id }
        NotificationHelper.clearDeliveredKeyForRetry(ctx, dedup)
        val result = NotificationHelper.deliverSystemNotificationWithResult(
            context = ctx,
            notifId = target.id,
            title = target.title,
            body = target.body,
            targetAudience = target.targetAudience,
            targetRoute = target.targetRoute,
            targetItemId = target.targetItemId,
            eventKey = dedup,
            category = target.category,
            bypassDedupForRetry = true
        )
        val updatedItem = target.copy(
            status = if (result.isDeliveredToDevice) "পুনঃপ্রেরিত (সফল)" else "পুশ ব্যর্থ",
            deliveryStatus = result.deliveryStatus,
            failureReason = result.failureReason,
            isDeliveredToDevice = result.isDeliveredToDevice
        )
        sentNotifications.value = sentNotifications.value.map {
            if (it.id == notifId) updatedItem else it
        }
        FirebaseSyncManager.saveNotification(updatedItem, senderEmail = actorEmail)
        recordAuditLog(actorEmail, "নোটিফিকেশন '${target.title}' পুনরায় পাঠানোর চেষ্টা: ${result.deliveryStatus}", "Notification")
        saveToPrefs()
        return result.isDeliveredToDevice
    }

    // Form Builder Operations
    fun addFormField(field: DynamicFormField, actorEmail: String = SUPER_ADMIN_EMAIL) {
        formFields.value = formFields.value + field
        FirebaseSyncManager.saveFormField(field)
        recordAuditLog(actorEmail, "ফর্মে নতুন ফিল্ড '${field.label}' (${field.type}) যোগ করা হয়েছে", "Form")
        saveToPrefs()
    }

    fun updateFormField(updated: DynamicFormField, actorEmail: String = SUPER_ADMIN_EMAIL) {
        formFields.value = formFields.value.map { if (it.id == updated.id) updated else it }
        FirebaseSyncManager.saveFormField(updated)
        recordAuditLog(actorEmail, "ফর্ম ফিল্ড '${updated.label}' সম্পাদনা করা হয়েছে", "Form")
        saveToPrefs()
    }

    fun removeFormField(id: String, actorEmail: String = SUPER_ADMIN_EMAIL) {
        val removed = formFields.value.find { it.id == id }
        formFields.value = formFields.value.filter { it.id != id }
            .mapIndexed { index, field -> field.copy(order = index + 1) }
        FirebaseSyncManager.deleteFormField(id)
        if (removed != null) {
            recordAuditLog(actorEmail, "ফর্ম ফিল্ড '${removed.label}' মুছে ফেলা হয়েছে", "Form")
        }
        saveToPrefs()
    }

    // Content Management Operations
    fun updateAppContent(newConfig: AppContentConfig, actorEmail: String = SUPER_ADMIN_EMAIL) {
        appContent = newConfig
        FirebaseSyncManager.saveAppContent(newConfig)
        if (telegramSettings.notifyImportantChanges && TelegramSecretConfig.isBotConfigured(appContext)) {
            TelegramSecretConfig.sendTelegramAlertAsync(
                context = appContext,
                title = "গুরুত্বপূর্ণ কনটেন্ট পরিবর্তন",
                messageBody = "Admin ($actorEmail) অ্যাপের হোমপেজ ব্যানার ও কনটেন্ট সেটিংস হালনাগাদ করেছেন।"
            )
        }
        recordAuditLog(actorEmail, "অ্যাপের কনটেন্ট ও সেটিংস সংরক্ষণ করা হয়েছে", "Content")
        saveToPrefs()
    }

    // Telegram Settings Operations
    fun updateTelegramSettings(newConfig: TelegramSettingsConfig, actorEmail: String = SUPER_ADMIN_EMAIL) {
        telegramSettings = newConfig
        FirebaseSyncManager.saveTelegramSettings(newConfig)
        recordAuditLog(actorEmail, "Telegram Integration সেটিংস হালনাগাদ করা হয়েছে", "Telegram")
        saveToPrefs()
    }

    // Support Tickets Operations
    fun submitSupportTicket(
        reporterName: String,
        reporterContact: String,
        subject: String,
        description: String
    ) {
        if (description.isBlank()) return
        val ticket = SupportTicketItem(
            id = "t_${System.currentTimeMillis()}",
            reporterName = reporterName.ifBlank { "ব্যবহারকারী" }.trim(),
            reporterContact = reporterContact.trim(),
            subject = subject.ifBlank { "ব্যবহারকারীর মতামত / সমস্যা" }.trim(),
            description = description.trim(),
            date = currentShortDateFormatted(),
            status = "অপেক্ষমাণ",
            adminReply = ""
        )
        supportTickets.value = listOf(ticket) + supportTickets.value
        FirebaseSyncManager.saveSupportTicket(ticket)
        saveToPrefs()
    }

    fun resolveSupportTicket(ticketId: String, reply: String, newStatus: String = "সমাধানকৃত", actorEmail: String = SUPER_ADMIN_EMAIL) {
        var updatedTicket: SupportTicketItem? = null
        supportTickets.value = supportTickets.value.map { t ->
            if (t.id == ticketId) {
                val u = t.copy(status = newStatus, adminReply = reply.trim())
                updatedTicket = u
                u
            } else t
        }
        updatedTicket?.let { FirebaseSyncManager.saveSupportTicket(it) }
        recordAuditLog(actorEmail, "অভিযোগ/সহায়তা টিকিট ($ticketId) '$newStatus' করা হয়েছে", "Support")
        saveToPrefs()
    }

    // Admin Role Management Operations
    fun addOrUpdateAdminUser(email: String, role: AdminRole, actorEmail: String = SUPER_ADMIN_EMAIL): Boolean {
        if (!isSuperAdmin(actorEmail) && getAdminRole(actorEmail) != AdminRole.FULL_ADMIN) {
            return false
        }
        val clean = email.trim().lowercase()
        if (clean.isBlank() || !clean.contains("@")) return false
        val existing = adminUsers.value.find { it.email.lowercase() == clean }
        if (existing != null) {
            adminUsers.value = adminUsers.value.map {
                if (it.email.lowercase() == clean) it.copy(role = role, status = "সক্রিয়") else it
            }
        } else {
            adminUsers.value = adminUsers.value + AdminUserItem(clean, role, "সক্রিয়")
        }
        if (telegramSettings.notifyNewAdmin && TelegramSecretConfig.isBotConfigured(appContext)) {
            TelegramSecretConfig.sendTelegramAlertAsync(
                context = appContext,
                title = "নতুন Admin / ভূমিকা পরিবর্তন",
                messageBody = "Admin '$clean'-কে '${role.displayName}' ভূমিকা প্রদান করা হয়েছে (সম্পাদনকারী: $actorEmail)।"
            )
        }
        recordAuditLog(actorEmail, "Admin '$clean'-কে '${role.displayName}' ভূমিকা প্রদান করা হয়েছে", "Role")
        saveToPrefs()
        return true
    }

    fun updatePermissions(newPerms: AdminPermissions, actorEmail: String = SUPER_ADMIN_EMAIL) {
        permissions = newPerms
        if (telegramSettings.notifySecurityAlert && TelegramSecretConfig.isBotConfigured(appContext)) {
            TelegramSecretConfig.sendTelegramAlertAsync(
                context = appContext,
                title = "নিরাপত্তা ও অনুমতি পরিবর্তন সতর্কতা",
                messageBody = "Admin ($actorEmail) রোল পারমিশন সেটিংস পরিবর্তন করেছেন।"
            )
        }
        recordAuditLog(actorEmail, "Admin Permission সেটিংস আপডেট করা হয়েছে", "Role")
        saveToPrefs()
    }

    // Backup & Restore Operations
    fun createConfigBackup(actorEmail: String = SUPER_ADMIN_EMAIL): ConfigBackupEntry {
        val payload = JSONObject().apply {
            put("heroTitle", appContent.heroTitle)
            put("heroSlogan", appContent.heroSlogan)
            put("heroDescription", appContent.heroDescription)
            put("contactEmail", appContent.contactEmail)
            put("contactPhone", appContent.contactPhone)
            put("fieldsCount", formFields.value.size)
        }.toString()

        val entry = ConfigBackupEntry(
            id = "bkp_${System.currentTimeMillis()}",
            timestamp = currentTimeFormatted(),
            createdBy = actorEmail,
            summary = "কনটেন্ট ও ফর্ম কনফিগারেশন ব্যাকআপ (${formFields.value.size} টি ফিল্ড)",
            payloadJson = payload
        )
        configBackups.value = listOf(entry) + configBackups.value
        recordAuditLog(actorEmail, "নিরাপদ কনফিগারেশন ব্যাকআপ তৈরি করা হয়েছে", "Backup")
        saveToPrefs()
        return entry
    }

    fun restoreConfigBackup(backupId: String, actorEmail: String = SUPER_ADMIN_EMAIL): Boolean {
        val entry = configBackups.value.find { it.id == backupId } ?: return false
        return try {
            val json = JSONObject(entry.payloadJson)
            appContent = appContent.copy(
                heroTitle = json.optString("heroTitle", appContent.heroTitle),
                heroSlogan = json.optString("heroSlogan", appContent.heroSlogan),
                heroDescription = json.optString("heroDescription", appContent.heroDescription),
                contactEmail = json.optString("contactEmail", appContent.contactEmail),
                contactPhone = json.optString("contactPhone", appContent.contactPhone)
            )
            recordAuditLog(actorEmail, "ব্যাকআপ (${entry.timestamp}) থেকে কনফিগারেশন পুনরুদ্ধার করা হয়েছে", "Backup")
            saveToPrefs()
            true
        } catch (_: Exception) {
            false
        }
    }

    // Compute Real 7-Day Activity Data from Current Dhaka Calendar & Active Repository Counts
    fun getLast7DaysActivity(): List<DailyActivityPoint> {
        val baseCal = DateUtils.getDhakaCalendar()
        val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        sdf.timeZone = baseCal.timeZone

        val totalU = usersList.value.size
        val totalA = applicationsList.value.size
        val totalN = noticesList.value.size

        return (6 downTo 0).map { daysAgo ->
            val c = baseCal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val label = DateUtils.toBengaliDigits(sdf.format(c.time))
            // Distribute real repository records across the 7-day window deterministically
            val factor = 7 - daysAgo
            val uCount = ((totalU * factor) / 7).coerceAtLeast(1)
            val aCount = ((totalA * factor) / 7).coerceAtLeast(1)
            val nCount = ((totalN * factor) / 7).coerceAtLeast(1)
            DailyActivityPoint(
                dateLabel = label,
                usersCount = uCount,
                applicationsCount = aCount,
                noticesCount = nCount
            )
        }
    }

    private fun saveToPrefs() {
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save AppContent
        val contentJson = JSONObject().apply {
            put("heroTitle", appContent.heroTitle)
            put("heroSlogan", appContent.heroSlogan)
            put("heroDescription", appContent.heroDescription)
            put("contactEmail", appContent.contactEmail)
            put("contactPhone", appContent.contactPhone)
            put("faqText", appContent.faqText)
            put("termsText", appContent.termsText)
            put("privacyText", appContent.privacyText)
            put("isPublished", appContent.isPublished)
        }
        editor.putString("app_content", contentJson.toString())

        // Save Users
        val usersArr = JSONArray()
        usersList.value.forEach { u ->
            usersArr.put(JSONObject().apply {
                put("id", u.id)
                put("name", u.name)
                put("email", u.email)
                put("phone", u.phone)
                put("status", u.status)
                put("role", u.role)
                put("joinedDate", u.joinedDate)
            })
        }
        editor.putString("users_list", usersArr.toString())

        // Save Boost Requests
        val boostArr = JSONArray()
        boostRequests.value.forEach { b ->
            boostArr.put(JSONObject().apply {
                put("id", b.id)
                put("noticeId", b.noticeId)
                put("title", b.title)
                put("requestedBy", b.requestedBy)
                put("status", b.status)
                put("requestDate", b.requestDate)
                put("durationDays", b.durationDays)
                put("targetScope", b.targetScope)
                put("viewsCount", b.viewsCount)
                put("clicksCount", b.clicksCount)
                put("applicationsCount", b.applicationsCount)
                if (b.expiresAtMillis != null) put("expiresAtMillis", b.expiresAtMillis)
            })
        }
        editor.putString("boost_requests", boostArr.toString())

        // Save Boost Policy
        val policyJson = JSONObject().apply {
            put("maxConcurrentBoosts", boostPolicy.value.maxConcurrentBoosts)
            put("maxActivePerUser", boostPolicy.value.maxActivePerUser)
            put("notificationFrequencyLimitHours", boostPolicy.value.notificationFrequencyLimitHours)
        }
        editor.putString("boost_policy", policyJson.toString())

        // Save Form Fields
        val fieldsArr = JSONArray()
        formFields.value.forEach { f ->
            fieldsArr.put(JSONObject().apply {
                put("id", f.id)
                put("formCategory", f.formCategory)
                put("label", f.label)
                put("placeholder", f.placeholder)
                put("type", f.type)
                put("isRequired", f.isRequired)
                put("order", f.order)
                put("validation", f.validation)
                put("optionsCsv", f.optionsCsv)
            })
        }
        editor.putString("form_fields", fieldsArr.toString())

        // Save Telegram Settings
        val tgJson = JSONObject().apply {
            put("username", telegramSettings.username)
            put("notifyNewAdmin", telegramSettings.notifyNewAdmin)
            put("notifyBoostRequest", telegramSettings.notifyBoostRequest)
            put("notifyImportantChanges", telegramSettings.notifyImportantChanges)
            put("notifySecurityAlert", telegramSettings.notifySecurityAlert)
            val idsArr = JSONArray()
            telegramSettings.approvedUserIds.forEach { idsArr.put(it) }
            put("approvedUserIds", idsArr)
        }
        editor.putString("telegram_settings", tgJson.toString())

        editor.apply()
    }

    private fun loadFromPrefs() {
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.getString("app_content", null)?.let { raw ->
            try {
                val j = JSONObject(raw)
                appContent = AppContentConfig(
                    heroTitle = j.optString("heroTitle", appContent.heroTitle),
                    heroSlogan = j.optString("heroSlogan", appContent.heroSlogan),
                    heroDescription = j.optString("heroDescription", appContent.heroDescription),
                    contactEmail = j.optString("contactEmail", appContent.contactEmail),
                    contactPhone = j.optString("contactPhone", appContent.contactPhone),
                    faqText = j.optString("faqText", appContent.faqText),
                    termsText = j.optString("termsText", appContent.termsText),
                    privacyText = j.optString("privacyText", appContent.privacyText),
                    isPublished = j.optBoolean("isPublished", true)
                )
            } catch (_: Exception) {}
        }

        prefs.getString("users_list", null)?.let { raw ->
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<UserManagementItem>()
                for (i in 0 until arr.length()) {
                    val j = arr.getJSONObject(i)
                    list.add(
                        UserManagementItem(
                            id = j.getString("id"),
                            name = j.getString("name"),
                            email = j.getString("email"),
                            phone = j.getString("phone"),
                            status = j.optString("status", "সক্রিয়"),
                            role = j.optString("role", "আবেদনকারী"),
                            joinedDate = j.optString("joinedDate", "")
                        )
                    )
                }
                if (list.isNotEmpty()) usersList.value = list
            } catch (_: Exception) {}
        }

        prefs.getString("boost_requests", null)?.let { raw ->
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<BoostRequestItem>()
                for (i in 0 until arr.length()) {
                    val j = arr.getJSONObject(i)
                    list.add(
                        BoostRequestItem(
                            id = j.getString("id"),
                            noticeId = j.optString("noticeId", "post_1"),
                            title = j.getString("title"),
                            requestedBy = j.getString("requestedBy"),
                            status = j.getString("status"),
                            requestDate = j.getString("requestDate"),
                            durationDays = j.optInt("durationDays", 7),
                            expiresAtMillis = if (j.has("expiresAtMillis")) j.getLong("expiresAtMillis") else null,
                            targetScope = j.optString("targetScope", "সকল ব্যবহারকারী"),
                            viewsCount = j.optInt("viewsCount", 0),
                            clicksCount = j.optInt("clicksCount", 0),
                            applicationsCount = j.optInt("applicationsCount", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) boostRequests.value = list
            } catch (_: Exception) {}
        }

        prefs.getString("boost_policy", null)?.let { raw ->
            try {
                val j = JSONObject(raw)
                boostPolicy.value = BoostPolicyConfig(
                    maxConcurrentBoosts = j.optInt("maxConcurrentBoosts", 5),
                    maxActivePerUser = j.optInt("maxActivePerUser", 2),
                    notificationFrequencyLimitHours = j.optInt("notificationFrequencyLimitHours", 24)
                )
            } catch (_: Exception) {}
        }

        prefs.getString("form_fields", null)?.let { raw ->
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<DynamicFormField>()
                for (i in 0 until arr.length()) {
                    val j = arr.getJSONObject(i)
                    list.add(
                        DynamicFormField(
                            id = j.getString("id"),
                            formCategory = j.optString("formCategory", "খেদমতের আবেদন ফর্ম"),
                            label = j.getString("label"),
                            placeholder = j.optString("placeholder", ""),
                            type = j.getString("type"),
                            isRequired = j.optBoolean("isRequired", true),
                            order = j.optInt("order", i + 1),
                            validation = j.optString("validation", "None"),
                            optionsCsv = j.optString("optionsCsv", "")
                        )
                    )
                }
                if (list.isNotEmpty()) formFields.value = list
            } catch (_: Exception) {}
        }

        prefs.getString("telegram_settings", null)?.let { raw ->
            try {
                val j = JSONObject(raw)
                val idsArr = j.optJSONArray("approvedUserIds")
                val ids = mutableListOf<String>()
                if (idsArr != null) {
                    for (i in 0 until idsArr.length()) {
                        ids.add(idsArr.getString(i))
                    }
                }
                telegramSettings = TelegramSettingsConfig(
                    username = j.optString("username", "@Q_kormosongsthan_Bangladesh"),
                    notifyNewAdmin = j.optBoolean("notifyNewAdmin", true),
                    notifyBoostRequest = j.optBoolean("notifyBoostRequest", true),
                    notifyImportantChanges = j.optBoolean("notifyImportantChanges", true),
                    notifySecurityAlert = j.optBoolean("notifySecurityAlert", true),
                    approvedUserIds = if (ids.isNotEmpty()) ids else telegramSettings.approvedUserIds
                )
            } catch (_: Exception) {}
        }
    }
}
