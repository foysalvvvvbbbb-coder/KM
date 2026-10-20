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
}

