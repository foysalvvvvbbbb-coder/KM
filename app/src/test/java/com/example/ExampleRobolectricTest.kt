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

}

