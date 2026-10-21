package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameStringResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("কওমি কর্মসংস্থান বাংলাদেশ", appName)
    }

    @Test
    fun testAuthErrorMessageMapping() {
        val invalidUserEx = FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "User not found")
        val invalidCredEx = FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "Wrong password")
        val collisionEx = FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "Email in use")
        val weakPassEx = FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "Weak password", "Password must be stronger")
        val networkEx = FirebaseNetworkException("Network error")
        val tooManyRequestsEx = FirebaseTooManyRequestsException("Quota exceeded")

        assertEquals("অ্যাকাউন্টটি খুঁজে পাওয়া যায়নি বা মুছে ফেলা হয়েছে।", getAuthErrorMessage(invalidUserEx))
        assertEquals("ইমেইল বা পাসওয়ার্ড সঠিক নয়।", getAuthErrorMessage(invalidCredEx))
        assertEquals("এই ইমেইলটি দিয়ে ইতিমধ্যে অ্যাকাউন্ট খোলা আছে।", getAuthErrorMessage(collisionEx))
        assertEquals("পাসওয়ার্ডটি খুব দুর্বল। অন্তত ৬ অক্ষরের শক্তিশালী পাসওয়ার্ড দিন।", getAuthErrorMessage(weakPassEx))
        assertEquals("ইন্টারনেট সংযোগ নেই বা সার্ভারের সাথে যোগাযোগ করা যাচ্ছে না।", getAuthErrorMessage(networkEx))
        assertEquals("অতিরিক্ত ভুল চেষ্টার কারণে সাময়িকভাবে ব্লক করা হয়েছে। কিছুক্ষণ পর চেষ্টা করুন।", getAuthErrorMessage(tooManyRequestsEx))
        assertEquals("অজ্ঞাত সমস্যা দেখা দিয়েছে।", getAuthErrorMessage(null))
    }

    @Test
    fun testRegistrationValidationRules() {
        fun validateRegistration(name: String, email: String, pass: String, confirmPass: String): String? {
            val trimmedName = name.trim()
            val trimmedEmail = email.trim()
            val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            return when {
                trimmedName.isBlank() -> "আপনার নাম লিখুন।"
                trimmedEmail.isBlank() -> "আপনার ইমেইল লিখুন।"
                !trimmedEmail.matches(emailPattern.toRegex()) -> "সঠিক ইমেইল ফরম্যাট দিন (যেমন: name@example.com)।"
                pass.length < 6 -> "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।"
                pass != confirmPass -> "পাসওয়ার্ড ও নিশ্চিতকরণ পাসওয়ার্ড মেলেনি।"
                else -> null
            }
        }

        assertEquals("আপনার নাম লিখুন।", validateRegistration("", "test@example.com", "123456", "123456"))
        assertEquals("আপনার ইমেইল লিখুন।", validateRegistration("আব্দুল্লাহ", "", "123456", "123456"))
        assertEquals("সঠিক ইমেইল ফরম্যাট দিন (যেমন: name@example.com)।", validateRegistration("আব্দুল্লাহ", "invalid-email", "123456", "123456"))
        assertEquals("পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।", validateRegistration("আব্দুল্লাহ", "user@example.com", "12345", "12345"))
        assertEquals("পাসওয়ার্ড ও নিশ্চিতকরণ পাসওয়ার্ড মেলেনি।", validateRegistration("আব্দুল্লাহ", "user@example.com", "123456", "654321"))
        assertEquals(null, validateRegistration("আব্দুল্লাহ", "user@example.com", "123456", "123456"))
    }

    @Test
    fun testProfileDisplayNameResolution() {
        fun resolveProfileDisplayName(firebaseDisplayName: String?, cachedName: String?): String {
            return when {
                !firebaseDisplayName.isNullOrBlank() -> firebaseDisplayName
                !cachedName.isNullOrBlank() -> cachedName
                else -> "নাম প্রদান করা হয়নি"
            }
        }

        // When user provided name during registration and Firebase has it
        assertEquals("আব্দুল্লাহ আল মামুন", resolveProfileDisplayName("আব্দুল্লাহ আল মামুন", null))
        
        // When Firebase reload is pending but SharedPreferences cached it immediately
        assertEquals("মুহাম্মদ উমর", resolveProfileDisplayName(null, "মুহাম্মদ উমর"))
        
        // When both exist, Firebase display name takes precedence
        assertEquals("মুহাম্মদ উমর", resolveProfileDisplayName("মুহাম্মদ উমর", "পুরানো নাম"))
        
        // When user never provided a name or both are null/blank
        assertEquals("নাম প্রদান করা হয়নি", resolveProfileDisplayName(null, null))
        assertEquals("নাম প্রদান করা হয়নি", resolveProfileDisplayName("", ""))
        assertEquals("নাম প্রদান করা হয়নি", resolveProfileDisplayName("   ", "   "))
    }

    @Test
    fun testPasswordResetEmailValidation() {
        assertEquals("আপনার ইমেইল ঠিকানা লিখুন।", validatePasswordResetEmail(""))
        assertEquals("আপনার ইমেইল ঠিকানা লিখুন।", validatePasswordResetEmail("   "))
        assertEquals("সঠিক ইমেইল ফরম্যাট দিন (যেমন: name@example.com)।", validatePasswordResetEmail("not-an-email"))
        assertEquals("সঠিক ইমেইল ফরম্যাট দিন (যেমন: name@example.com)।", validatePasswordResetEmail("user@"))
        assertEquals(null, validatePasswordResetEmail("user@example.com"))
        assertEquals(null, validatePasswordResetEmail(" mdfoysalahamad202@gmail.com "))
    }

    @Test
    fun testPasswordResetErrorMessageMapping() {
        val invalidUserEx = FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "User not found")
        val invalidCredEx = FirebaseAuthInvalidCredentialsException("ERROR_INVALID_EMAIL", "Invalid email")
        val networkEx = FirebaseNetworkException("Network error")
        val tooManyRequestsEx = FirebaseTooManyRequestsException("Too many attempts")

        assertEquals("এই ইমেইল দিয়ে কোনো অ্যাকাউন্ট পাওয়া যায়নি।", getPasswordResetErrorMessage(invalidUserEx))
        assertEquals("ইমেইল ঠিকানাটি সঠিক নয়।", getPasswordResetErrorMessage(invalidCredEx))
        assertEquals("ইন্টারনেট সংযোগ নেই বা সার্ভারের সাথে যোগাযোগ করা যাচ্ছে না।", getPasswordResetErrorMessage(networkEx))
        assertEquals("অতিরিক্ত অনুরোধের কারণে সাময়িকভাবে ব্লক করা হয়েছে। কিছুক্ষণ পর চেষ্টা করুন।", getPasswordResetErrorMessage(tooManyRequestsEx))
        assertEquals("পাসওয়ার্ড রিসেট লিংক পাঠাতে ব্যর্থ হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।", getPasswordResetErrorMessage(null))
    }

    @Test
    fun testBengaliDigitsConversion() {
        assertEquals("০১২৩৪৫৬৭৮৯", DateUtils.toBengaliDigits("0123456789"))
        assertEquals("২০২৬", DateUtils.toBengaliDigits(2026))
        assertEquals("১৪৩৩", DateUtils.toBengaliDigits(1433L))
    }

    @Test
    fun testDateUtilsBengaliCalendar() {
        val cal = java.util.Calendar.getInstance()
        // Set to April 14, 2026 (1 Boishakh 1433)
        cal.set(2026, java.util.Calendar.APRIL, 14)
        val boishakhDate = DateUtils.getBengaliDate(cal)
        assertTrue("Expected 1 Boishakh: $boishakhDate", boishakhDate.contains("১ বৈশাখ ১৪৩৩ বঙ্গাব্দ"))

        // Set to October 20, 2026 (4 Kartik 1433)
        cal.set(2026, java.util.Calendar.OCTOBER, 20)
        val kartikDate = DateUtils.getBengaliDate(cal)
        assertTrue("Expected 4 Kartik: $kartikDate", kartikDate.contains("৪ কার্তিক ১৪৩৩ বঙ্গাব্দ"))
    }

    @Test
    fun testDateUtilsEnglishDateInBengali() {
        val cal = java.util.Calendar.getInstance()
        cal.set(2026, java.util.Calendar.OCTOBER, 20)
        val englishDate = DateUtils.getEnglishDate(cal)
        assertEquals("২০ অক্টোবর ২০২৬", englishDate)
    }

    @Test
    fun testDateUtilsWeekdayInBengali() {
        val cal = java.util.Calendar.getInstance()
        cal.set(2026, java.util.Calendar.OCTOBER, 20) // Tuesday
        val weekday = DateUtils.getWeekday(cal)
        assertEquals("মঙ্গলবার", weekday)
    }

    @Test
    fun testHomeWelcomeMessageResolution() {
        fun resolveWelcomeText(displayName: String?): String {
            return if (!displayName.isNullOrBlank()) "স্বাগতম, $displayName" else "স্বাগতম"
        }

        assertEquals("স্বাগতম, হাফেজ মাওলানা আব্দুল্লাহ", resolveWelcomeText("হাফেজ মাওলানা আব্দুল্লাহ"))
        assertEquals("স্বাগতম", resolveWelcomeText(null))
        assertEquals("স্বাগতম", resolveWelcomeText(""))
        assertEquals("স্বাগতম", resolveWelcomeText("   "))
    }

    @Test
    fun testCurrentDateInfoGeneration() {
        val dateInfo = DateUtils.getCurrentDateInfo()
        assertTrue(dateInfo.bengaliDate.isNotBlank())
        assertTrue(dateInfo.englishDate.isNotBlank())
        assertTrue(dateInfo.hijriDate.isNotBlank())
        assertTrue(dateInfo.weekday.isNotBlank())
    }

    @Test
    fun testFallbackPrayerTimes() {
        val prayers = PrayerTimeManager.getFallbackPrayerTimes()
        assertEquals(5, prayers.size)
        assertEquals("ফজর", prayers[0].name)
        assertEquals("04:42", prayers[0].start)
        assertEquals("05:58", prayers[0].end)

        assertEquals("যোহর", prayers[1].name)
        assertEquals("11:43", prayers[1].start)
        assertEquals("15:51", prayers[1].end) // Ends when Hanafi Asr starts

        assertEquals("আসর", prayers[2].name)
        assertEquals("15:51", prayers[2].start) // Hanafi Asr (2x shadow)
        assertEquals("17:28", prayers[2].end) // Sunset/Maghrib

        assertEquals("মাগরিব", prayers[3].name)
        assertEquals("17:28", prayers[3].start)
        assertEquals("18:44", prayers[3].end) // Isha start

        assertEquals("ইশা", prayers[4].name)
        assertEquals("18:44", prayers[4].start)
        assertEquals("04:42", prayers[4].end) // Subh Sadiq (Fajr start)

        for (prayer in prayers) {
            assertTrue(prayer.start.matches("^\\d{2}:\\d{2}$".toRegex()))
            assertTrue(prayer.end.matches("^\\d{2}:\\d{2}$".toRegex()))
        }
    }

    @Test
    fun testDhakaHanafiConstants() {
        assertEquals(23.8103, PrayerTimeManager.DHAKA_LATITUDE, 0.0001)
        assertEquals(90.4125, PrayerTimeManager.DHAKA_LONGITUDE, 0.0001)
        assertEquals(1, PrayerTimeManager.METHOD_KARACHI)
        assertEquals(1, PrayerTimeManager.SCHOOL_HANAFI)
    }

    @Test
    fun testTimeFormat12HourBengali() {
        assertEquals("০৪:৪২ সকাল", DateUtils.formatTimeTo12Hour("04:42"))
        assertEquals("০৫:৫৮ সকাল", DateUtils.formatTimeTo12Hour("05:58"))
        assertEquals("১১:৪৩ সকাল", DateUtils.formatTimeTo12Hour("11:43"))
        assertEquals("০৩:৫১ বিকাল", DateUtils.formatTimeTo12Hour("15:51"))
        assertEquals("০৫:২৮ বিকাল", DateUtils.formatTimeTo12Hour("17:28"))
        assertEquals("০৬:৪৪ সন্ধ্যা", DateUtils.formatTimeTo12Hour("18:44"))
    }

    @Test
    fun testPrayerIntervalCalculation() {
        fun calculateActivePrayer(currentMinutes: Int, fajr: Int, sunrise: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int): Int {
            return when {
                currentMinutes in fajr until sunrise -> 0 // ফজর
                currentMinutes in sunrise until dhuhr -> -1 // চাশত / যোহরের অপেক্ষা
                currentMinutes in dhuhr until asr -> 1 // যোহর
                currentMinutes in asr until maghrib -> 2 // আসর
                currentMinutes in maghrib until isha -> 3 // মাগরিব
                currentMinutes >= isha || currentMinutes < fajr -> 4 // ইশা
                else -> -1
            }
        }

        // Times in minutes for Hanafi Karachi Method:
        // Fajr: 04:42 (282), Sunrise: 05:58 (358)
        // Dhuhr: 11:43 (703), Asr: 15:51 (951)
        // Maghrib: 17:28 (1048), Isha: 18:44 (1124)
        val f = 282; val sr = 358; val d = 703; val a = 951; val m = 1048; val i = 1124

        // 5:00 AM (300 min) -> Fajr
        assertEquals(0, calculateActivePrayer(300, f, sr, d, a, m, i))

        // 10:00 AM (600 min) -> Pre-Dhuhr (-1)
        assertEquals(-1, calculateActivePrayer(600, f, sr, d, a, m, i))

        // 1:00 PM (780 min) -> Dhuhr
        assertEquals(1, calculateActivePrayer(780, f, sr, d, a, m, i))

        // 3:30 PM (930 min) -> Dhuhr still active under Hanafi (Asr starts at 15:51 / 951)
        assertEquals(1, calculateActivePrayer(930, f, sr, d, a, m, i))

        // 4:00 PM (960 min) -> Asr
        assertEquals(2, calculateActivePrayer(960, f, sr, d, a, m, i))

        // 6:00 PM (1080 min) -> Maghrib!
        assertEquals(3, calculateActivePrayer(1080, f, sr, d, a, m, i))

        // 7:30 PM (1170 min) -> Isha!
        assertEquals(4, calculateActivePrayer(1170, f, sr, d, a, m, i))

        // 11:30 PM (1410 min) -> Isha!
        assertEquals(4, calculateActivePrayer(1410, f, sr, d, a, m, i))

        // 3:00 AM (180 min) -> Night / Isha period before Fajr!
        assertEquals(4, calculateActivePrayer(180, f, sr, d, a, m, i))
    }

    @Test
    fun testAdminAccessControlAllowlist() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AdminRepository.init(context)

        // All 4 authorized initial admin accounts must be permitted
        assertTrue(AdminRepository.isUserAdmin("foysalahamad102@gmail.com"))
        assertTrue(AdminRepository.isUserAdmin("foysalahamad204@gmail.com"))
        assertTrue(AdminRepository.isUserAdmin("mdfoysalahamad202@gmail.com"))
        assertTrue(AdminRepository.isUserAdmin("sumuaktar01300448147@gmail.com"))

        // Case-insensitive and whitespace-tolerant check
        assertTrue(AdminRepository.isUserAdmin("  FOYSALAHAMAD204@GMAIL.COM  "))

        // Super Admin verification
        assertTrue(AdminRepository.isSuperAdmin("foysalahamad204@gmail.com"))
        assertEquals(AdminRole.SUPER_ADMIN, AdminRepository.getAdminRole("foysalahamad204@gmail.com"))

        // Logged-out and non-admin accounts must be denied
        org.junit.Assert.assertFalse(AdminRepository.isUserAdmin(null))
        org.junit.Assert.assertFalse(AdminRepository.isUserAdmin(""))
        org.junit.Assert.assertFalse(AdminRepository.isUserAdmin("   "))
        org.junit.Assert.assertFalse(AdminRepository.isUserAdmin("randomuser@gmail.com"))
        org.junit.Assert.assertFalse(AdminRepository.isUserAdmin("attacker@example.com"))
    }

    @Test
    fun testAdminDashboardChartActivityPoints() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AdminRepository.init(context)

        val points = AdminRepository.getLast7DaysActivity()
        assertEquals(7, points.size)
        points.forEach { pt ->
            assertTrue(pt.dateLabel.isNotBlank())
            assertTrue(pt.usersCount >= 1)
            assertTrue(pt.applicationsCount >= 1)
            assertTrue(pt.noticesCount >= 1)
        }
        // Last day should reflect full repository counts
        val lastPoint = points.last()
        assertEquals(AdminRepository.usersList.value.size, lastPoint.usersCount)
    }

    @Test
    fun testAdminPanelModulesOperationsAndPersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AdminRepository.init(context)

        // 1. User Management: Add & Toggle User Status
        val initialUsersCount = AdminRepository.usersList.value.size
        AdminRepository.addUser("মাওলানা তানভীর", "tanvir@qawmi.org", "01700112233", "আবেদনকারী")
        assertEquals(initialUsersCount + 1, AdminRepository.usersList.value.size)

        val addedUser = AdminRepository.usersList.value.first()
        assertEquals("সক্রিয়", addedUser.status)
        AdminRepository.toggleUserStatus(addedUser.id)
        val toggledUser = AdminRepository.usersList.value.first { it.id == addedUser.id }
        assertEquals("নিষ্ক্রিয়", toggledUser.status)

        // 2. Madrasah Management: Add & Update Verification Status
        val initialMadrasahCount = AdminRepository.madrasahsList.value.size
        AdminRepository.addMadrasah(
            name = "জামিয়া ইসলামিয়া ঢাকা",
            location = "মিরপুর, ঢাকা",
            muhtamim = "মুফতি আব্দুল্লাহ",
            departments = "দাওরায়ে হাদীস, ইফতা",
            phone = "01800112233",
            regNumber = "REG-2026-99"
        )
        assertEquals(initialMadrasahCount + 1, AdminRepository.madrasahsList.value.size)

        // 3. Dynamic Form Builder: Add & Remove Form Field
        val initialFieldsCount = AdminRepository.formFields.value.size
        val newField = DynamicFormField(
            id = "field_test_99",
            label = "সনদের কপি লিংক",
            placeholder = "লিংক দিন",
            type = "Short Text",
            isRequired = false,
            order = initialFieldsCount + 1
        )
        AdminRepository.addFormField(newField)
        assertEquals(initialFieldsCount + 1, AdminRepository.formFields.value.size)
        AdminRepository.removeFormField("field_test_99")
        assertEquals(initialFieldsCount, AdminRepository.formFields.value.size)

        // 4. Content Management & Backup/Restore
        val backup = AdminRepository.createConfigBackup()
        assertTrue(backup.id.startsWith("bkp_"))

        val updatedContent = AdminRepository.appContent.copy(heroSlogan = "নতুন পরীক্ষামূলক স্লোগান")
        AdminRepository.updateAppContent(updatedContent)
        assertEquals("নতুন পরীক্ষামূলক স্লোগান", AdminRepository.appContent.heroSlogan)

        val restored = AdminRepository.restoreConfigBackup(backup.id)
        assertTrue(restored)
        assertEquals("দ্বীনি খেদমতে সুযোগ, আল্লাহর সন্তুষ্টি", AdminRepository.appContent.heroSlogan)
    }

    @Test
    fun testNotificationSystemLifecycleTargetingAndSecurity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AdminRepository.init(context)

        // 1. Device token creation & logout deactivation
        val token = NotificationHelper.getOrCreateDeviceToken(context)
        assertTrue(token.startsWith("fcm_"))
        NotificationHelper.setNotificationsEnabled(context, true)
        assertTrue(NotificationHelper.isNotificationsEnabled(context))
        NotificationHelper.onUserLogout(context)
        assertEquals("", NotificationHelper.currentDeviceTokenFlow.value)
        val refreshedToken = NotificationHelper.getOrCreateDeviceToken(context)
        assertTrue(refreshedToken.startsWith("fcm_"))

        // 2. Audience targeting verification
        assertTrue(
            NotificationHelper.shouldReceiveForAudience(
                targetAudience = "সকল ব্যবহারকারী",
                currentUserEmail = "user@example.com",
                currentUserRole = "আবেদনকারী"
            )
        )
        assertTrue(
            NotificationHelper.shouldReceiveForAudience(
                targetAudience = "খেদমতপ্রার্থী",
                currentUserEmail = "candidate@example.com",
                currentUserRole = "আবেদনকারী"
            )
        )
        org.junit.Assert.assertFalse(
            NotificationHelper.shouldReceiveForAudience(
                targetAudience = "মাদ্রাসা প্রতিনিধি",
                currentUserEmail = "candidate@example.com",
                currentUserRole = "আবেদনকারী"
            )
        )
        assertTrue(
            NotificationHelper.shouldReceiveForAudience(
                targetAudience = "Admin",
                currentUserEmail = "foysalahamad204@gmail.com",
                currentUserRole = "Admin"
            )
        )
        org.junit.Assert.assertFalse(
            NotificationHelper.shouldReceiveForAudience(
                targetAudience = "Admin",
                currentUserEmail = "regularuser@gmail.com",
                currentUserRole = "আবেদনকারী"
            )
        )

        // 3. Broadcast security guard: non-admin cannot broadcast notifications
        val unauthorizedBroadcast = AdminRepository.sendBroadcastNotification(
            title = "ভুয়া ঘোষণা",
            body = "অননুমোদিত নোটিফিকেশন",
            targetAudience = "সকল ব্যবহারকারী",
            actorEmail = "attacker@example.com"
        )
        org.junit.Assert.assertFalse(unauthorizedBroadcast)

        // Authorized admin broadcast succeeds and deduplicates identical rapid clicks
        val authorizedBroadcast = AdminRepository.sendBroadcastNotification(
            title = "বিশেষ দ্বীনি ঘোষণা",
            body = "নতুন মাদ্রাসা ভেরিফিকেশন শুরু হয়েছে।",
            targetAudience = "সকল ব্যবহারকারী",
            actorEmail = "foysalahamad204@gmail.com",
            targetRoute = "madrasah"
        )
        assertTrue(authorizedBroadcast)
        assertEquals("বিশেষ দ্বীনি ঘোষণা", AdminRepository.sentNotifications.value.first().title)

        val duplicateBroadcast = AdminRepository.sendBroadcastNotification(
            title = "বিশেষ দ্বীনি ঘোষণা",
            body = "নতুন মাদ্রাসা ভেরিফিকেশন শুরু হয়েছে।",
            targetAudience = "সকল ব্যবহারকারী",
            actorEmail = "foysalahamad204@gmail.com",
            targetRoute = "madrasah"
        )
        org.junit.Assert.assertFalse(duplicateBroadcast)
    }

    @Test
    fun testEndToEndFeedBoostAndSupportTicketWorkflows() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AdminRepository.init(context)

        // 1. User submits a Support Ticket from Contact/Feedback dialog -> appears in Admin Panel
        val beforeTickets = AdminRepository.supportTickets.value.size
        AdminRepository.submitSupportTicket(
            reporterName = "মাওলানা মাহমুদ",
            reporterContact = "mahmud@qawmi.org",
            subject = "বিজ্ঞপ্তি বুস্ট সংক্রান্ত প্রশ্ন",
            description = "কীভাবে আমাদের মাদ্রাসার নিয়োগ বিজ্ঞপ্তি ফিচার্ড করব?"
        )
        assertEquals(beforeTickets + 1, AdminRepository.supportTickets.value.size)
        val createdTicket = AdminRepository.supportTickets.value.first()
        assertEquals("অপেক্ষমাণ", createdTicket.status)

        // Admin resolves the ticket
        AdminRepository.resolveSupportTicket(
            ticketId = createdTicket.id,
            reply = "Admin প্যানেল থেকে আপনার বিজ্ঞপ্তিটি ৭ দিনের জন্য Boost করা হয়েছে।",
            newStatus = "সমাধানকৃত",
            actorEmail = "foysalahamad204@gmail.com"
        )
        val resolvedTicket = AdminRepository.supportTickets.value.first { it.id == createdTicket.id }
        assertEquals("সমাধানকৃত", resolvedTicket.status)
        assertTrue(resolvedTicket.adminReply.contains("Boost"))

        // 2. Boost Approval & Expiry workflow
        val boostItem = AdminRepository.boostRequests.value.first()
        AdminRepository.updateBoostStatus(
            id = boostItem.id,
            newStatus = "Active",
            durationDays = 14,
            actorEmail = "foysalahamad204@gmail.com"
        )
        val activeBoost = AdminRepository.boostRequests.value.first { it.id == boostItem.id }
        assertEquals("Active", activeBoost.status)
        assertEquals(14, activeBoost.durationDays)
        assertTrue((activeBoost.expiresAtMillis ?: 0L) > System.currentTimeMillis())
    }

    @Test
    fun testTelegramSecretConfigPasswordProtectedVault() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AdminRepository.init(context)
        TelegramSecretConfig.resetVaultForTesting(context)

        // 1. Initially, no default password is active ("qawmi786" is NOT accepted as a default)
        org.junit.Assert.assertFalse(TelegramSecretConfig.isPasswordInitialized(context))
        org.junit.Assert.assertFalse(TelegramSecretConfig.isVaultUnlocked.value)
        org.junit.Assert.assertFalse(TelegramSecretConfig.unlockVault("qawmi786", context))

        // 2. Weak or example password ("qawmi786" or <6 chars) must be rejected during first-time setup
        val weakAttempt = TelegramSecretConfig.setupInitialPassword(
            context = context,
            newPassword = "qawmi786",
            confirmPassword = "qawmi786",
            actorEmail = "foysalahamad204@gmail.com"
        )
        org.junit.Assert.assertFalse(weakAttempt.first)

        val shortAttempt = TelegramSecretConfig.setupInitialPassword(
            context = context,
            newPassword = "1234",
            confirmPassword = "1234",
            actorEmail = "foysalahamad204@gmail.com"
        )
        org.junit.Assert.assertFalse(shortAttempt.first)

        // Non-admin cannot initialize the vault password
        val unauthorizedSetup = TelegramSecretConfig.setupInitialPassword(
            context = context,
            newPassword = "StrongAdmin#2026",
            confirmPassword = "StrongAdmin#2026",
            actorEmail = "intruder@example.com"
        )
        org.junit.Assert.assertFalse(unauthorizedSetup.first)

        // 3. Authorized Admin sets a strong first-time password -> succeeds and unlocks
        val validSetup = TelegramSecretConfig.setupInitialPassword(
            context = context,
            newPassword = "StrongAdmin#2026",
            confirmPassword = "StrongAdmin#2026",
            actorEmail = "foysalahamad204@gmail.com"
        )
        assertTrue(validSetup.first)
        assertTrue(TelegramSecretConfig.isPasswordInitialized(context))
        assertTrue(TelegramSecretConfig.isVaultUnlocked.value)

        // 4. Validate Bot Token & Numeric Chat ID formats
        assertTrue(TelegramSecretConfig.isValidBotTokenFormat("7123456789:AAH_test_bot_token_1234567890"))
        org.junit.Assert.assertFalse(TelegramSecretConfig.isValidBotTokenFormat("invalid_token"))
        assertTrue(TelegramSecretConfig.isValidNumericChatId("998877665"))
        assertTrue(TelegramSecretConfig.isValidNumericChatId("-1001234567890"))
        org.junit.Assert.assertFalse(TelegramSecretConfig.isValidNumericChatId("@not_numeric"))

        // 5. Save credentials with valid password while unlocked -> encrypted & active
        assertTrue(
            TelegramSecretConfig.saveCredentialsWithPassword(
                context = context,
                password = "StrongAdmin#2026",
                botToken = "7123456789:AAH_test_bot_token_1234567890",
                primaryChatId = "998877665",
                actorEmail = "foysalahamad204@gmail.com"
            )
        )
        assertTrue(TelegramSecretConfig.isBotConfigured(context))
        assertEquals("998877665", TelegramSecretConfig.getPrimaryChatIdForUi(context))
        assertTrue(TelegramSecretConfig.getMaskedBotToken(context).contains("••••••••"))

        // 6. Lock vault -> UI access to token/ID, saving, and test messages must be strictly blocked
        TelegramSecretConfig.lockVault()
        org.junit.Assert.assertFalse(TelegramSecretConfig.isVaultUnlocked.value)
        assertEquals("", TelegramSecretConfig.getBotTokenForUi(context))
        assertEquals("", TelegramSecretConfig.getPrimaryChatIdForUi(context))
        org.junit.Assert.assertFalse(
            TelegramSecretConfig.saveCredentialsWithPassword(
                context = context,
                password = "StrongAdmin#2026",
                botToken = "7123456789:AAH_other_token_1234567890",
                primaryChatId = "112233445",
                actorEmail = "foysalahamad204@gmail.com"
            )
        )
        var testMsgAllowedWhileLocked = true
        TelegramSecretConfig.sendTestMessageFromVault(context, "foysalahamad204@gmail.com") { ok, _ ->
            testMsgAllowedWhileLocked = ok
        }
        org.junit.Assert.assertFalse(testMsgAllowedWhileLocked)

        // 7. Wrong password fails to unlock and increments failed attempts
        org.junit.Assert.assertFalse(TelegramSecretConfig.unlockVault("wrong_password_123", context))
        assertEquals(1, TelegramSecretConfig.failedAttempts.value)

        // 8. Correct strong password unlocks vault and restores access to decrypted credentials
        assertTrue(TelegramSecretConfig.unlockVault("StrongAdmin#2026", context))
        assertEquals(0, TelegramSecretConfig.failedAttempts.value)
        assertEquals("998877665", TelegramSecretConfig.getPrimaryChatIdForUi(context))

        // 9. Updating vault password re-encrypts credentials and enforces the new password
        assertTrue(
            TelegramSecretConfig.updateFilePassword(
                context = context,
                currentPassword = "StrongAdmin#2026",
                newPassword = "UpdatedVaultPass#786",
                actorEmail = "foysalahamad204@gmail.com"
            )
        )
        org.junit.Assert.assertFalse(TelegramSecretConfig.verifyFilePassword("StrongAdmin#2026", context))
        assertTrue(TelegramSecretConfig.verifyFilePassword("UpdatedVaultPass#786", context))
        assertEquals("998877665", TelegramSecretConfig.getPrimaryChatIdForUi(context))
    }

    @Test
    fun testBoostingLifecycleAnalyticsAndNotificationCenterComplete() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FeedRepository.init(context)
        AdminRepository.init(context)

        val approvedNotice = AdminRepository.noticesList.value.first {
            it.status == "অনুমোদিত" && it.postId == "post_3"
        }

        // 1. Valid notice Boost request submission succeeds
        val submittedOk = AdminRepository.submitBoostRequest(
            title = "${approvedNotice.roleTitle} - ${approvedNotice.madrasahName}",
            requestedBy = approvedNotice.madrasahName,
            durationDays = 7,
            noticeId = approvedNotice.postId,
            targetScope = "সকল ব্যবহারকারী"
        )
        assertTrue(submittedOk)
        val createdBoost = AdminRepository.boostRequests.value.first()
        assertEquals("Pending", createdBoost.status)

        // 2. Rejecting a notice blocks Boost submission/activation for that notice
        val pendingOrRejectedNotice = AdminRepository.noticesList.value.last()
        AdminRepository.updateNoticeStatus(pendingOrRejectedNotice.postId, "প্রত্যাখ্যাত", "foysalahamad204@gmail.com")
        val invalidBoostAttempt = AdminRepository.submitBoostRequest(
            title = pendingOrRejectedNotice.roleTitle,
            requestedBy = pendingOrRejectedNotice.madrasahName,
            durationDays = 7,
            noticeId = pendingOrRejectedNotice.postId
        )
        org.junit.Assert.assertFalse(invalidBoostAttempt)

        // 3. Admin approves Boost -> Notice becomes isBoosted=true, Featured, and emits "Boost" notification
        AdminRepository.updateBoostStatus(
            id = createdBoost.id,
            newStatus = "Active",
            durationDays = 7,
            actorEmail = "foysalahamad204@gmail.com"
        )
        val activeBoost = AdminRepository.boostRequests.value.first { it.id == createdBoost.id }
        assertEquals("Active", activeBoost.status)
        val boostedNoticeAfterApprove = AdminRepository.noticesList.value.first { it.postId == approvedNotice.postId }
        assertTrue(boostedNoticeAfterApprove.isBoosted)
        assertTrue(AdminRepository.sentNotifications.value.any { it.category == "Boost" && it.title.contains("বিশেষভাবে প্রচারিত") })

        // 4. Analytics tracking: View, Click, and Apply increment counters on Notice and Active Boost
        val beforeViews = activeBoost.viewsCount
        val beforeClicks = activeBoost.clicksCount
        val beforeApps = activeBoost.applicationsCount
        AdminRepository.recordNoticeInteraction(approvedNotice.postId, "view")
        AdminRepository.recordNoticeInteraction(approvedNotice.postId, "click")
        AdminRepository.recordNoticeInteraction(approvedNotice.postId, "apply")

        val updatedBoostMetrics = AdminRepository.boostRequests.value.first { it.id == createdBoost.id }
        assertEquals(beforeViews + 1, updatedBoostMetrics.viewsCount)
        assertEquals(beforeClicks + 1, updatedBoostMetrics.clicksCount)
        assertEquals(beforeApps + 1, updatedBoostMetrics.applicationsCount)

        // 5. Pause -> removes from Featured (isBoosted=false), Resume -> restores Featured (isBoosted=true), Cancel -> returns to normal feed
        AdminRepository.updateBoostStatus(createdBoost.id, "Paused", 7, "foysalahamad204@gmail.com")
        assertEquals("Paused", AdminRepository.boostRequests.value.first { it.id == createdBoost.id }.status)
        org.junit.Assert.assertFalse(AdminRepository.noticesList.value.first { it.postId == approvedNotice.postId }.isBoosted)

        AdminRepository.updateBoostStatus(createdBoost.id, "Active", 7, "foysalahamad204@gmail.com")
        assertTrue(AdminRepository.noticesList.value.first { it.postId == approvedNotice.postId }.isBoosted)

        AdminRepository.updateBoostStatus(createdBoost.id, "Cancelled", 7, "foysalahamad204@gmail.com")
        assertEquals("Cancelled", AdminRepository.boostRequests.value.first { it.id == createdBoost.id }.status)
        org.junit.Assert.assertFalse(AdminRepository.noticesList.value.first { it.postId == approvedNotice.postId }.isBoosted)

        // 6. Notification Center Read/Unread, Mark All Read, Category Preferences, and Deep-Link Navigation
        val sampleNotifId = AdminRepository.sentNotifications.value.first().id
        NotificationHelper.markAsRead(context, sampleNotifId)
        assertTrue(NotificationHelper.readNotificationIdsFlow.value.contains(sampleNotifId))

        val allIds = AdminRepository.sentNotifications.value.map { it.id }
        NotificationHelper.markAllAsRead(context, allIds)
        assertTrue(NotificationHelper.readNotificationIdsFlow.value.containsAll(allIds))

        NotificationHelper.setCategoryEnabled(context, "Boost", false)
        org.junit.Assert.assertFalse(NotificationHelper.isCategoryEnabled(context, "Boost"))
        NotificationHelper.setCategoryEnabled(context, "Boost", true)
        assertTrue(NotificationHelper.isCategoryEnabled(context, "Boost"))

        NotificationHelper.triggerDirectNavigation("khedmot", approvedNotice.postId)
        assertEquals("khedmot", NotificationHelper.pendingDeepLinkRoute.value)
        NotificationHelper.consumeDeepLink()
        assertEquals(null, NotificationHelper.pendingDeepLinkRoute.value)
    }
}
