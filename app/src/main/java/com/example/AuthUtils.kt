package com.example

import android.util.Patterns

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
                    "এই সাইন-ইন পদ্ধতি বর্তমানে সক্রিয় নেই।"
                "ERROR_INVALID_CREDENTIAL" ->
                    "প্রদত্ত ইমেইল বা পাসওয়ার্ড সঠিক নয়।"
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
