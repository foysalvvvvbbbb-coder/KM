package com.example

import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

fun getAuthErrorMessage(exception: Exception?): String {
    if (exception == null) return "অজ্ঞাত সমস্যা দেখা দিয়েছে।"
    return when (exception) {
        is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "পাসওয়ার্ডটি খুব দুর্বল। অন্তত ৬ অক্ষরের শক্তিশালী পাসওয়ার্ড দিন।"
        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "অ্যাকাউন্টটি খুঁজে পাওয়া যায়নি বা মুছে ফেলা হয়েছে।"
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "ইমেইল বা পাসওয়ার্ড সঠিক নয়।"
        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "এই ইমেইলটি দিয়ে ইতিমধ্যে অ্যাকাউন্ট খোলা আছে।"
        is com.google.firebase.FirebaseNetworkException -> "ইন্টারনেট সংযোগ নেই বা সার্ভারের সাথে যোগাযোগ করা যাচ্ছে না।"
        is com.google.firebase.FirebaseTooManyRequestsException -> "অতিরিক্ত ভুল চেষ্টার কারণে সাময়িকভাবে ব্লক করা হয়েছে। কিছুক্ষণ পর চেষ্টা করুন।"
        is com.google.firebase.auth.FirebaseAuthException -> {
            when (exception.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Firebase Console-এ Google Sign-In সক্রিয় করা নেই। Firebase Console > Authentication > Sign-in method-এ গিয়ে Google সক্রিয় করুন।"
                "ERROR_INVALID_CREDENTIAL" ->
                    "গুগল প্রমাণপত্রটি সঠিক নয় বা মেয়াদ শেষ হয়ে গেছে।"
                "ERROR_USER_DISABLED" ->
                    "এই অ্যাকাউন্টটি নিষ্ক্রিয় করা হয়েছে।"
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                    "এই ইমেইল দিয়ে ইতিমধ্যে অন্য পদ্ধতিতে অ্যাকাউন্ট খোলা রয়েছে।"
                else -> "প্রমাণীকরণ ত্রুটি (${exception.errorCode}): ${exception.localizedMessage ?: ""}"
            }
        }
        else -> "সমস্যা: ${exception.localizedMessage ?: exception.message ?: "অজ্ঞাত ত্রুটি"}"
    }
}

fun validatePasswordResetEmail(email: String): String? {
    val trimmed = email.trim()
    return when {
        trimmed.isBlank() -> "আপনার ইমেইল ঠিকানা লিখুন।"
        !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> "সঠিক ইমেইল ফরম্যাট দিন (যেমন: name@example.com)।"
        else -> null
    }
}

fun getPasswordResetErrorMessage(exception: Exception?): String {
    if (exception == null) return "পাসওয়ার্ড রিসেট লিংক পাঠাতে ব্যর্থ হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
    return when (exception) {
        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "এই ইমেইল দিয়ে কোনো অ্যাকাউন্ট পাওয়া যায়নি।"
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "ইমেইল ঠিকানাটি সঠিক নয়।"
        is com.google.firebase.FirebaseNetworkException -> "ইন্টারনেট সংযোগ নেই বা সার্ভারের সাথে যোগাযোগ করা যাচ্ছে না।"
        is com.google.firebase.FirebaseTooManyRequestsException -> "অতিরিক্ত অনুরোধের কারণে সাময়িকভাবে ব্লক করা হয়েছে। কিছুক্ষণ পর চেষ্টা করুন।"
        is com.google.firebase.auth.FirebaseAuthException -> "প্রমাণীকরণ ত্রুটি: ${exception.localizedMessage ?: exception.errorCode}"
        else -> "ইমেইল পাঠাতে ব্যর্থ: ${exception.localizedMessage ?: exception.message ?: "অজ্ঞাত ত্রুটি"}"
    }
}

fun getGoogleSignInErrorMessage(exception: Exception?): String {
    if (exception == null) return "গুগল সাইন-ইন সম্পন্ন করা যায়নি।"
    if (exception is GetCredentialCancellationException) {
        val msg = exception.message ?: ""
        if (msg.contains("Activity was cancelled", ignoreCase = true)) {
            return "গুগল সাইন-ইন বন্ধ হয়ে গেছে (Activity Cancelled)। Firebase Console-এ SHA-1 যুক্ত থাকা এবং Support Email সেভ থাকা আবশ্যক।"
        }
        return ""
    }
    if (exception is NoCredentialException) {
        return "ডিভাইসে কোনো গুগল অ্যাকাউন্ট পাওয়া যায়নি। অনুগ্রহ করে ডিভাইসে গুগল অ্যাকাউন্ট যোগ করুন।"
    }
    val msg = exception.localizedMessage ?: exception.message ?: ""
    val type = if (exception is GetCredentialException) exception.type else ""

    // Only treat as silent cancellation if user intentionally cancelled or closed the picker
    if (msg.equals("User canceled", ignoreCase = true) ||
        type.contains("USER_CANCELED", ignoreCase = true)
    ) {
        return ""
    }

    if (msg.contains("Activity was cancelled", ignoreCase = true)) {
        return "গুগল সাইন-ইন বন্ধ হয়ে গেছে (Activity Cancelled)। Firebase Console-এ SHA-1 ফিঙ্গারপ্রিন্ট এবং Google Provider কনফিগারেশন যাচাই করুন।"
    }

    return when {
        msg.contains("10:") || msg.contains("DEVELOPER_ERROR") ->
            "গুগল কনফিগারেশন মিসম্যাচ (DEVELOPER_ERROR 10)। Firebase Console-এ SHA-1 ও SHA-256 ফিঙ্গারপ্রিন্ট যোগ করুন এবং Google Sign-In সক্রিয় করুন।"
        msg.contains("7:") || msg.contains("NETWORK_ERROR") ->
            "ইন্টারনেট সংযোগ নেই বা সার্ভারের সাথে যোগাযোগ করা যাচ্ছে না।"
        msg.contains("12500") || msg.contains("SIGN_IN_FAILED") ->
            "গুগল সাইন-ইন ব্যর্থ হয়েছে (Code 12500)। Firebase Console-এ Support Email ও Google Provider সক্রিয় করুন।"
        msg.contains("16:") ->
            "গুগল সাইন-ইন সম্পন্ন হয়নি (Status 16)। Firebase Console-এ SHA-1 ও SHA-256 যোগ করুন এবং OAuth Consent Screen যাচাই করুন।"
        else ->
            "গুগল লগইন সম্পন্ন করা যায়নি: ${msg.ifBlank { "অজ্ঞাত ত্রুটি ($type)" }}"
    }
}

fun extractGoogleIdToken(credential: androidx.credentials.Credential): String? {
    if (credential is GoogleIdTokenCredential) {
        val token = credential.idToken
        if (!token.isNullOrBlank()) {
            return token
        }
    }
    if (credential is CustomCredential) {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL ||
            credential.type.contains("google", ignoreCase = true)
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val token = googleIdTokenCredential.idToken
                if (!token.isNullOrBlank()) {
                    return token
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthDebug", "Error in GoogleIdTokenCredential.createFrom", e)
            }
        }
        val data = credential.data
        val candidates = listOf(
            "com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN",
            "id_token",
            "idToken",
            "token"
        )
        for (key in candidates) {
            val token = data.getString(key)
            if (!token.isNullOrBlank()) {
                android.util.Log.d("AuthDebug", "ID token extracted from bundle key: $key")
                return token
            }
        }
        for (key in data.keySet()) {
            val value = data.getString(key)
            if (value != null && value.count { it == '.' } == 2 && value.length > 50) {
                android.util.Log.d("AuthDebug", "ID token recovered via JWT heuristics from key: $key")
                return value
            }
        }
    }
    return null
}

fun Context.findActivity(): ComponentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun resolveGoogleWebClientId(context: Context): String {
    // 1. Try google-services generated default_web_client_id
    val defaultResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (defaultResId != 0) {
        try {
            val id = context.getString(defaultResId).trim()
            if (id.isNotBlank() && !id.startsWith("YOUR_") && !id.contains("example")) {
                return id
            }
        } catch (_: Exception) {}
    }
    // 2. Try custom_web_client_id from strings.xml
    val customResId = context.resources.getIdentifier("custom_web_client_id", "string", context.packageName)
    if (customResId != 0) {
        try {
            val id = context.getString(customResId).trim()
            if (id.isNotBlank() && !id.startsWith("YOUR_") && !id.contains("example")) {
                return id
            }
        } catch (_: Exception) {}
    }
    return ""
}
