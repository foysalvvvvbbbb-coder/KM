package com.example

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * প্রতিটি Telegram নোটিফিকেশন বা টেস্ট মেসেজ পাঠানোর লগ এন্ট্রি (টোকেন ছাড়া নিরাপদ তথ্য)
 */
data class TelegramDeliveryLogEntry(
    val id: String,
    val time: String,
    val eventTitle: String,
    val targetCount: Int,
    val status: String, // "সফল", "ব্যর্থ", "কনফিগারেশন নেই"
    val details: String
)

/**
 * ====================================================================================
 *   কওমি কর্মসংস্থান বাংলাদেশ — TELEGRAM SECURE VAULT ও BOT কনফিগারেশন ফাইল
 * ====================================================================================
 *
 * নির্দেশনা (কিভাবে নিজ হাতে অথবা Admin Panel থেকে বসাবেন):
 * ১. নিরাপত্তার কারণে কোনো দুর্বল ডিফল্ট পাসওয়ার্ড (যেমন উদাহরণস্বরূপ "qawmi786") সক্রিয় রাখা হয়নি।
 *    প্রথমবার ব্যবহারের সময় Admin Panel-এর Telegram সেকশনে গিয়ে আপনার নিজস্ব শক্তিশালী পাসওয়ার্ড
 *    (কমপক্ষে ৬ অক্ষরের) নির্ধারণ করা বাধ্যতামূলক।
 * ২. আপনি চাইলে নিচের `MANUAL_FILE_PASSWORD`, `MANUAL_BOT_TOKEN` এবং `MANUAL_ADMIN_CHAT_ID`-এর
 *    ভেতরেও নিজ হাতে তথ্য বসাতে পারেন অথবা Google AI Studio-এর **Secrets Panel**-এ গিয়ে:
 *    - TELEGRAM_FILE_PASSWORD
 *    - TELEGRAM_BOT_TOKEN
 *    - TELEGRAM_ADMIN_CHAT_ID
 *    সেট করতে পারেন।
 * ৩. সঠিক পাসওয়ার্ড দিয়ে Vault আনলক না করা পর্যন্ত Bot Token ও Numeric Chat ID দেখা, পরিবর্তন,
 *    সংরক্ষণ বা টেস্ট মেসেজ পাঠানো সম্পূর্ণরূপে ব্লক থাকবে।
 */
object TelegramSecretConfig {

    // ================================================================================
    // ১. ম্যানুয়াল ফাইল কনফিগারেশন (ঐচ্ছিক — খালি থাকলে Admin Panel Vault ব্যবহৃত হবে)
    // ================================================================================

    /**
     * ফাইল আনলক করার পাসওয়ার্ড (File Password):
     * নোট: নিরাপত্তার জন্য এখানে কোনো ডিফল্ট পাসওয়ার্ড দেওয়া হয়নি। (উদাহরণস্বরূপ "qawmi786" ব্যবহার করবেন না;
     * কমপক্ষে ৬ অক্ষরের শক্তিশালী পাসওয়ার্ড দিন অথবা Admin Panel-এ প্রথমবার ব্যবহারের সময় সেট করুন)।
     */
    const val MANUAL_FILE_PASSWORD: String = ""

    /**
     * আপনার Telegram Bot Token (@BotFather থেকে প্রাপ্ত):
     * উদাহরণ ফরম্যাট: "7123456789:AAHxyz1234567890abcdefghijklmnop"
     */
    const val MANUAL_BOT_TOKEN: String = ""

    /**
     * আপনার প্রধান Telegram Numeric User ID বা Group/Channel Chat ID:
     * উদাহরণ ফরম্যাট: "123456789" অথবা চ্যানেল/গ্রুপের ক্ষেত্রে "-1001234567890"
     */
    const val MANUAL_ADMIN_CHAT_ID: String = ""

    /**
     * অতিরিক্ত অনুমোদিত Admin Telegram Numeric IDs:
     */
    val MANUAL_EXTRA_ADMIN_IDS: List<String> = listOf()

    /**
     * অফিসিয়াল বট বা চ্যানেলের ইউজারনেম:
     */
    const val BOT_USERNAME: String = "@Q_kormosongsthan_Bangladesh"

    // নিষিদ্ধ/দুর্বল উদাহরণ পাসওয়ার্ড তালিকা যা কখনোই নিরাপদ পাসওয়ার্ড হিসেবে গ্রহণ করা হবে না
    private val FORBIDDEN_WEAK_PASSWORDS = setOf(
        "qawmi786",
        "123456",
        "password",
        "admin123",
        "12345678"
    )

    // ================================================================================
    // ২. পাসওয়ার্ড হ্যাশিং (SHA-256 + Salt), AES-GCM এনক্রিপশন ও ভল্ট লক/আনলক স্টেট
    // ================================================================================

    private const val VAULT_PREFS = "telegram_secret_vault_prefs"
    private const val KEY_PASSWORD_HASH = "vault_password_sha256_hash"
    private const val KEY_PASSWORD_SALT = "vault_password_salt"
    private const val KEY_ENCRYPTED_TOKEN = "vault_enc_bot_token"
    private const val KEY_ENCRYPTED_CHAT_ID = "vault_enc_chat_id"
    private const val KEY_LEGACY_RUNTIME_TOKEN = "vault_runtime_bot_token"
    private const val KEY_LEGACY_RUNTIME_CHAT_ID = "vault_runtime_chat_id"

    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30_000L

    // সেশন আনলক স্টেট (ডيفল্টভাবে লক করা থাকে)
    val isVaultUnlocked = MutableStateFlow(false)
    val lastTelegramDeliveryStatus = MutableStateFlow("পাসওয়ার্ড দিয়ে ভল্ট আনলক করুন")
    val connectedBotInfo = MutableStateFlow("যাচাই করা হয়নি")
    val failedAttempts = MutableStateFlow(0)
    val lockoutUntilMillis = MutableStateFlow(0L)

    val deliveryLogs = MutableStateFlow<List<TelegramDeliveryLogEntry>>(emptyList())

    private fun currentTimeStr(): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.forLanguageTag("bn-BD"))
        return DateUtils.toBengaliDigits(sdf.format(Date()))
    }

    private fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun deriveAesKey(secretSeed: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("qawmi_vault_aes_key::$secretSeed".toByteArray(Charsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }

    private fun encryptValue(plainText: String, secretSeed: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
            cipher.init(Cipher.ENCRYPT_MODE, deriveAesKey(secretSeed), GCMParameterSpec(128, iv))
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    private fun decryptValue(encoded: String, secretSeed: String): String {
        if (encoded.isBlank()) return ""
        return try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size <= 12) return ""
            val iv = combined.copyOfRange(0, 12)
            val cipherBytes = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, deriveAesKey(secretSeed), GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }

    private fun getEncryptionSeed(context: Context?): String {
        val prefs = context?.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val salt = prefs?.getString(KEY_PASSWORD_SALT, null) ?: "default_qawmi_salt"
        val hash = prefs?.getString(KEY_PASSWORD_HASH, null) ?: "default_qawmi_hash"
        return "$salt::$hash"
    }

    /**
     * পাসওয়ার্ড শক্তিশালী ও বৈধ কি না যাচাই করে ("qawmi786" বা ৬ অক্ষরের কম হলে বাতিল করে)
     */
    fun validatePasswordStrength(password: String): Pair<Boolean, String> {
        val clean = password.trim()
        if (clean.length < 6) {
            return false to "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
        }
        if (clean.lowercase() in FORBIDDEN_WEAK_PASSWORDS) {
            return false to "'$clean' একটি উদাহরণ বা দুর্বল পাসওয়ার্ড। অনুগ্রহ করে নিজস্ব শক্তিশালী পাসওয়ার্ড দিন।"
        }
        return true to "বৈধ পাসওয়ার্ড"
    }

    /**
     * প্রথমবার ভল্ট পাসওয়ার্ড সেট করা হয়েছে কি না তা পরীক্ষা করে
     */
    fun isPasswordInitialized(context: Context? = null): Boolean {
        val prefs = context?.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val savedHash = prefs?.getString(KEY_PASSWORD_HASH, null)
        if (!savedHash.isNullOrBlank()) return true

        val envPass = try { BuildConfig.TELEGRAM_FILE_PASSWORD } catch (_: Exception) { "" }
        if (envPass.isNotBlank() &&
            envPass != "YOUR_TELEGRAM_FILE_PASSWORD_HERE" &&
            envPass.trim().lowercase() !in FORBIDDEN_WEAK_PASSWORDS
        ) {
            return true
        }

        if (MANUAL_FILE_PASSWORD.isNotBlank() &&
            MANUAL_FILE_PASSWORD.trim().lowercase() !in FORBIDDEN_WEAK_PASSWORDS
        ) {
            return true
        }
        return false
    }

    /**
     * প্রথমবার ব্যবহারের সময় Admin কর্তৃক নতুন শক্তিশালী পাসওয়ার্ড নির্ধারণ
     */
    fun setupInitialPassword(
        context: Context,
        newPassword: String,
        confirmPassword: String,
        actorEmail: String = SUPER_ADMIN_EMAIL
    ): Pair<Boolean, String> {
        if (!AdminRepository.isUserAdmin(actorEmail)) {
            return false to "শুধুমাত্র অনুমোদিত Admin ভল্ট পাসওয়ার্ড নির্ধারণ করতে পারবেন।"
        }
        if (isPasswordInitialized(context)) {
            return false to "ভল্ট পাসওয়ার্ড ইতিমধ্যে নির্ধারিত আছে। পরিবর্তন করতে পাসওয়ার্ড পরিবর্তন অপশন ব্যবহার করুন।"
        }
        val cleanNew = newPassword.trim()
        val cleanConfirm = confirmPassword.trim()
        if (cleanNew != cleanConfirm) {
            return false to "উভয় ঘরে দেওয়া পাসওয়ার্ড মিলছে না!"
        }
        val (isValid, reason) = validatePasswordStrength(cleanNew)
        if (!isValid) {
            return false to reason
        }

        val salt = generateSalt()
        val passwordHash = sha256Hex("$salt::$cleanNew")
        context.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PASSWORD_SALT, salt)
            .putString(KEY_PASSWORD_HASH, passwordHash)
            .apply()

        isVaultUnlocked.value = true
        failedAttempts.value = 0
        lockoutUntilMillis.value = 0L
        lastTelegramDeliveryStatus.value = "ভল্ট পাসওয়ার্ড নির্ধারিত ও আনলককৃত • এখন Bot Token ও ID বসান"

        FirebaseSyncManager.saveTelegramVaultMeta(
            isPasswordConfigured = true,
            maskedToken = getMaskedBotToken(context),
            updatedBy = actorEmail
        )
        AdminRepository.recordAuditLog(actorEmail, "Telegram Vault-এর প্রাথমিক নিরাপত্তা পাসওয়ার্ড নির্ধারণ করা হয়েছে", "Telegram")
        return true to "আলহামদুলিল্লাহ! ভল্ট পাসওয়ার্ড সফলভাবে নির্ধারণ ও আনলক হয়েছে।"
    }

    /**
     * ব্যবহারকারীর দেওয়া পাসওয়ার্ড সঠিক কি না যাচাই করে
     */
    fun verifyFilePassword(inputPassword: String, context: Context? = null): Boolean {
        val cleanInput = inputPassword.trim()
        if (cleanInput.isBlank()) return false
        if (cleanInput.lowercase() in FORBIDDEN_WEAK_PASSWORDS) return false

        val prefs = context?.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val savedHash = prefs?.getString(KEY_PASSWORD_HASH, null)
        val savedSalt = prefs?.getString(KEY_PASSWORD_SALT, null)
        if (!savedHash.isNullOrBlank() && !savedSalt.isNullOrBlank()) {
            return sha256Hex("$savedSalt::$cleanInput") == savedHash
        }

        val envPass = try { BuildConfig.TELEGRAM_FILE_PASSWORD } catch (_: Exception) { "" }
        if (envPass.isNotBlank() &&
            envPass != "YOUR_TELEGRAM_FILE_PASSWORD_HERE" &&
            envPass.trim().lowercase() !in FORBIDDEN_WEAK_PASSWORDS
        ) {
            return cleanInput == envPass.trim()
        }

        if (MANUAL_FILE_PASSWORD.isNotBlank() &&
            MANUAL_FILE_PASSWORD.trim().lowercase() !in FORBIDDEN_WEAK_PASSWORDS
        ) {
            return cleanInput == MANUAL_FILE_PASSWORD.trim()
        }

        return false
    }

    /**
     * ভল্ট লকআউট অবস্থায় আছে কি না পরীক্ষা করে
     */
    fun isCurrentlyLockedOut(): Boolean {
        val until = lockoutUntilMillis.value
        return until > System.currentTimeMillis()
    }

    /**
     * পাসওয়ার্ড দিয়ে ফাইল/ভল্ট আনলক করে (বারবার ভুল পাসওয়ার্ড দিলে নিরাপত্তা সতর্কতা পাঠায়)
     */
    fun unlockVault(
        inputPassword: String,
        context: Context? = null,
        actorEmail: String = SUPER_ADMIN_EMAIL
    ): Boolean {
        if (!isPasswordInitialized(context)) {
            isVaultUnlocked.value = false
            lastTelegramDeliveryStatus.value = "প্রথমে আপনার নতুন ভল্ট পাসওয়ার্ড সেট করুন"
            return false
        }
        if (isCurrentlyLockedOut()) {
            val remainingSec = ((lockoutUntilMillis.value - System.currentTimeMillis()) / 1000L).coerceAtLeast(1L)
            lastTelegramDeliveryStatus.value = "বারবার ভুল চেষ্টার কারণে ভল্ট সাময়িক লক ($remainingSec সেকেন্ড অপেক্ষা করুন)"
            return false
        }

        val matched = verifyFilePassword(inputPassword, context)
        if (matched) {
            isVaultUnlocked.value = true
            failedAttempts.value = 0
            lockoutUntilMillis.value = 0L
            lastTelegramDeliveryStatus.value = if (isBotConfigured(context)) {
                "আনলককৃত • Bot Token ও Numeric Chat ID সক্রিয় আছে"
            } else {
                "আনলককৃত • অনুগ্রহ করে Bot Token ও Numeric Chat ID বসান"
            }
            return true
        } else {
            isVaultUnlocked.value = false
            val attempts = failedAttempts.value + 1
            failedAttempts.value = attempts
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockoutUntilMillis.value = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            }
            if (attempts >= 3) {
                AdminRepository.recordAuditLog(
                    actorEmail,
                    "Telegram Vault-এ বারবার ভুল পাসওয়ার্ড প্রচেষ্টা ($attempts বার)",
                    "Security"
                )
                if (AdminRepository.telegramSettings.notifySecurityAlert && isBotConfigured(context)) {
                    sendTelegramAlertAsync(
                        context = context,
                        title = "নিরাপত্তা সতর্কতা: Telegram Vault আনলক ব্যর্থ",
                        messageBody = "ব্যবহারকারী ($actorEmail) পরপর $attempts বার ভুল পাসওয়ার্ড দিয়ে Telegram Vault খোলার চেষ্টা করেছেন।"
                    )
                }
            }
            lastTelegramDeliveryStatus.value = "ভুল পাসওয়ার্ড! ($attempts/$MAX_FAILED_ATTEMPTS ব্যর্থ চেষ্টা)"
            return false
        }
    }

    /**
     * ভল্ট পুনরায় লক করে
     */
    fun lockVault() {
        isVaultUnlocked.value = false
        lastTelegramDeliveryStatus.value = "ভল্ট লক করা হয়েছে"
    }

    /**
     * টেস্ট বা রিসেট প্রয়োজনে ভল্টের স্টোরেজ ক্লিয়ার করার সহায়ক মেথড
     */
    fun resetVaultForTesting(context: Context) {
        context.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        isVaultUnlocked.value = false
        failedAttempts.value = 0
        lockoutUntilMillis.value = 0L
        connectedBotInfo.value = "যাচাই করা হয়নি"
        lastTelegramDeliveryStatus.value = "পাসওয়ার্ড দিয়ে ভল্ট আনলক করুন"
    }

    /**
     * পুরনো পাসওয়ার্ড যাচাই করে নতুন শক্তিশালী ফাইল পাসওয়ার্ড সেট করে এবং টোকেন পুনরায় এনক্রিপ্ট করে
     */
    fun updateFilePassword(
        context: Context,
        currentPassword: String,
        newPassword: String,
        actorEmail: String = SUPER_ADMIN_EMAIL
    ): Boolean {
        if (!verifyFilePassword(currentPassword, context)) return false
        val (isValid, _) = validatePasswordStrength(newPassword)
        if (!isValid) return false

        // বর্তমান টোকেন ও আইডি ডিক্রিপ্ট করে নতুন কী দিয়ে পুনরায় এনক্রিপ্ট করা
        val existingToken = getActiveBotToken(context)
        val existingChatId = getActivePrimaryChatId(context)

        val newSalt = generateSalt()
        val newHash = sha256Hex("$newSalt::${newPassword.trim()}")
        val newSeed = "$newSalt::$newHash"

        val prefs = context.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString(KEY_PASSWORD_SALT, newSalt)
            .putString(KEY_PASSWORD_HASH, newHash)

        if (existingToken.isNotBlank()) {
            editor.putString(KEY_ENCRYPTED_TOKEN, encryptValue(existingToken, newSeed))
        }
        if (existingChatId.isNotBlank()) {
            editor.putString(KEY_ENCRYPTED_CHAT_ID, encryptValue(existingChatId, newSeed))
        }
        editor.apply()

        AdminRepository.recordAuditLog(actorEmail, "Telegram Vault পাসওয়ার্ড পরিবর্তন করা হয়েছে", "Telegram")
        return true
    }

    /**
     * Bot Token ফরম্যাট বৈধ কি না পরীক্ষা করে (@BotFather স্ট্যান্ডার্ড ফরম্যাট)
     */
    fun isValidBotTokenFormat(token: String): Boolean {
        val clean = token.trim()
        if (clean.length < 20 || !clean.contains(":")) return false
        val parts = clean.split(":", limit = 2)
        if (parts.size != 2) return false
        val botId = parts[0]
        val secret = parts[1]
        return botId.length in 5..16 &&
            botId.all { it.isDigit() } &&
            secret.length >= 10 &&
            secret.all { it.isLetterOrDigit() || it == '_' || it == '-' }
    }

    /**
     * Telegram Numeric User ID বা Group/Channel Chat ID বৈধ সংখ্যার ফরম্যাটে আছে কি না পরীক্ষা করে
     */
    fun isValidNumericChatId(chatId: String): Boolean {
        val clean = chatId.trim()
        if (clean.length < 5 || clean.length > 18) return false
        val digitsPart = if (clean.startsWith("-")) clean.substring(1) else clean
        return digitsPart.isNotEmpty() && digitsPart.all { it.isDigit() }
    }

    /**
     * পাসওয়ার্ড যাচাই ও ভল্ট আনলক থাকা সাপেক্ষে Bot Token ও Numeric Chat ID এনক্রিপ্ট করে সংরক্ষণ
     */
    fun saveCredentialsWithPassword(
        context: Context,
        password: String,
        botToken: String,
        primaryChatId: String,
        actorEmail: String = SUPER_ADMIN_EMAIL
    ): Boolean {
        if (!AdminRepository.isUserAdmin(actorEmail)) return false
        if (!isVaultUnlocked.value) return false
        if (!verifyFilePassword(password, context)) return false

        val cleanToken = botToken.trim()
        val cleanChatId = primaryChatId.trim()

        if (cleanToken.isNotEmpty() && !isValidBotTokenFormat(cleanToken)) {
            lastTelegramDeliveryStatus.value = "অবৈধ Bot Token ফরম্যাট! @BotFather-এর সঠিক টোকেন দিন।"
            return false
        }
        if (cleanChatId.isNotEmpty() && !isValidNumericChatId(cleanChatId)) {
            lastTelegramDeliveryStatus.value = "অবৈধ Chat ID! শুধুমাত্র সংখ্যার (Numeric) Telegram ID দিন।"
            return false
        }

        val seed = getEncryptionSeed(context)
        context.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENCRYPTED_TOKEN, encryptValue(cleanToken, seed))
            .putString(KEY_ENCRYPTED_CHAT_ID, encryptValue(cleanChatId, seed))
            .remove(KEY_LEGACY_RUNTIME_TOKEN)
            .remove(KEY_LEGACY_RUNTIME_CHAT_ID)
            .apply()

        FirebaseSyncManager.saveTelegramVaultMeta(
            isPasswordConfigured = isPasswordInitialized(context),
            maskedToken = getMaskedBotToken(context),
            updatedBy = actorEmail
        )

        lastTelegramDeliveryStatus.value = if (isBotConfigured(context)) {
            "Bot Token ও Numeric Chat ID এনক্রিপ্টেড ভল্টে সংরক্ষিত ও সক্রিয়!"
        } else {
            "ভল্ট কনফিগারেশন হালনাগাদ হয়েছে"
        }
        return true
    }

    /**
     * শুধুমাত্র ভল্ট আনলক থাকলে UI-তে দেখানোর জন্য Bot Token রিটার্ন করে (লক থাকলে খালি স্ট্রিং)
     */
    fun getBotTokenForUi(context: Context? = null): String {
        if (!isVaultUnlocked.value) return ""
        return getActiveBotToken(context)
    }

    /**
     * শুধুমাত্র ভল্ট আনলক থাকলে UI-তে দেখানোর জন্য Primary Chat ID রিটার্ন করে (লক থাকলে খালি স্ট্রিং)
     */
    fun getPrimaryChatIdForUi(context: Context? = null): String {
        if (!isVaultUnlocked.value) return ""
        return getActivePrimaryChatId(context)
    }

    /**
     * অভ্যন্তরীণ সিস্টেম নোটিফিকেশন পাঠানোর জন্য সক্রিয় Bot Token রিটার্ন করে
     */
    internal fun getActiveBotToken(context: Context? = null): String {
        if (MANUAL_BOT_TOKEN.isNotBlank() && isValidBotTokenFormat(MANUAL_BOT_TOKEN)) {
            return MANUAL_BOT_TOKEN.trim()
        }
        val prefs = context?.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val encrypted = prefs?.getString(KEY_ENCRYPTED_TOKEN, null)
        if (!encrypted.isNullOrBlank()) {
            val decrypted = decryptValue(encrypted, getEncryptionSeed(context))
            if (decrypted.isNotBlank()) return decrypted.trim()
        }
        val envToken = try { BuildConfig.TELEGRAM_BOT_TOKEN } catch (_: Exception) { "" }
        if (envToken.isNotBlank() && envToken != "YOUR_TELEGRAM_BOT_TOKEN_HERE" && isValidBotTokenFormat(envToken)) {
            return envToken.trim()
        }
        return ""
    }

    /**
     * অভ্যন্তরীণ সিস্টেম নোটিফিকেশন পাঠানোর জন্য সক্রিয় Primary Chat ID রিটার্ন করে
     */
    internal fun getActivePrimaryChatId(context: Context? = null): String {
        if (MANUAL_ADMIN_CHAT_ID.isNotBlank() && isValidNumericChatId(MANUAL_ADMIN_CHAT_ID)) {
            return MANUAL_ADMIN_CHAT_ID.trim()
        }
        val prefs = context?.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val encrypted = prefs?.getString(KEY_ENCRYPTED_CHAT_ID, null)
        if (!encrypted.isNullOrBlank()) {
            val decrypted = decryptValue(encrypted, getEncryptionSeed(context))
            if (decrypted.isNotBlank()) return decrypted.trim()
        }
        val envChatId = try { BuildConfig.TELEGRAM_ADMIN_CHAT_ID } catch (_: Exception) { "" }
        if (envChatId.isNotBlank() && envChatId != "YOUR_TELEGRAM_ADMIN_CHAT_ID_HERE" && isValidNumericChatId(envChatId)) {
            return envChatId.trim()
        }
        return ""
    }

    /**
     * সকল অনুমোদিত Numeric Chat ID একত্রে রিটার্ন করে
     */
    fun getAllTargetChatIds(context: Context? = null): List<String> {
        val ids = mutableSetOf<String>()
        val primary = getActivePrimaryChatId(context)
        if (isValidNumericChatId(primary)) ids.add(primary)

        MANUAL_EXTRA_ADMIN_IDS.forEach { raw ->
            val clean = raw.trim()
            if (isValidNumericChatId(clean)) ids.add(clean)
        }

        AdminRepository.telegramSettings.approvedUserIds.forEach { entry ->
            val numericPart = entry.trim().substringBefore(" ").trim()
            if (isValidNumericChatId(numericPart)) {
                // উদাহরণ/ডেমো আইডি বাদে সকল বাস্তব নিউমেরিক আইডি গ্রহণ
                if (numericPart !in setOf("123456789", "987654321", "456789123")) {
                    ids.add(numericPart)
                }
            }
        }
        return ids.toList()
    }

    /**
     * Bot Token এবং অন্তত একটি বৈধ Numeric Chat ID সংযুক্ত আছে কি না তা পরীক্ষা করে
     */
    fun isBotConfigured(context: Context? = null): Boolean {
        val token = getActiveBotToken(context)
        return isValidBotTokenFormat(token) && getAllTargetChatIds(context).isNotEmpty()
    }

    /**
     * নিরাপত্তার জন্য Bot Token মাস্ক করে দেখায় (যাতে পুরো টোকেন কখনো স্ক্রিনে বা লগে ফাঁস না হয়)
     */
    fun getMaskedBotToken(context: Context? = null): String {
        val token = getActiveBotToken(context)
        if (token.isBlank()) return "বসানো হয়নি (Not Set)"
        if (token.length <= 12) return "••••••••"
        return "${token.take(6)}••••••••${token.takeLast(4)}"
    }

    /**
     * নিরাপত্তার জন্য Chat ID মাস্ক করে দেখায় (ভল্ট লক থাকা অবস্থায়)
     */
    fun getMaskedChatId(context: Context? = null): String {
        val chatId = getActivePrimaryChatId(context)
        if (chatId.isBlank()) return "বসানো হয়নি (Not Set)"
        if (chatId.length <= 4) return "••••"
        return "${chatId.take(2)}••••${chatId.takeLast(2)}"
    }

    private fun recordDeliveryLog(
        eventTitle: String,
        targetCount: Int,
        status: String,
        details: String
    ) {
        val entry = TelegramDeliveryLogEntry(
            id = "tg_log_${System.currentTimeMillis()}",
            time = currentTimeStr(),
            eventTitle = eventTitle,
            targetCount = targetCount,
            status = status,
            details = details
        )
        deliveryLogs.value = (listOf(entry) + deliveryLogs.value).take(25)
        FirebaseSyncManager.saveTelegramDeliveryLog(entry)
    }

    // ================================================================================
    // ৩. ভল্ট-সুরক্ষিত বট যাচাই (getMe) ও টেস্ট মেসেজ প্রেরণ
    // ================================================================================

    /**
     * শুধুমাত্র ভল্ট আনলক থাকা অবস্থায় টেস্ট মেসেজ পাঠানোর নিরাপদ মেথড
     */
    fun sendTestMessageFromVault(
        context: Context?,
        actorEmail: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (!AdminRepository.isUserAdmin(actorEmail)) {
            val msg = "অননুমোদিত অ্যাক্সেস! শুধুমাত্র Admin টেস্ট মেসেজ পাঠাতে পারবেন।"
            onResult(false, msg)
            return
        }
        if (!isVaultUnlocked.value) {
            val msg = "ভল্ট লক করা আছে! টেস্ট মেসেজ পাঠাতে প্রথমে সঠিক পাসওয়ার্ড দিয়ে ভল্ট আনলক করুন।"
            lastTelegramDeliveryStatus.value = msg
            onResult(false, msg)
            return
        }
        AdminRepository.recordAuditLog(actorEmail, "Telegram Bot টেস্ট মেসেজ প্রেরণের অনুরোধ করা হয়েছে", "Telegram")
        sendTelegramAlertAsync(
            context = context,
            title = "টেস্ট মেসেজ (Admin Secure Vault)",
            messageBody = "আলহামদুলিল্লাহ! কওমি কর্মসংস্থান বাংলাদেশ অ্যাপের পাসওয়ার্ড-সুরক্ষিত Telegram Vault থেকে বট সংযোগ সফলভাবে কাজ করছে।\nপ্রেরক: $actorEmail",
            onResult = onResult
        )
    }

    /**
     * শুধুমাত্র ভল্ট আনলক থাকা অবস্থায় Telegram getMe API কল করে Bot Token যাচাই করে
     */
    fun verifyBotConnectionAsync(
        context: Context?,
        actorEmail: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (!AdminRepository.isUserAdmin(actorEmail) || !isVaultUnlocked.value) {
            val msg = "প্রথমে সঠিক পাসওয়ার্ড দিয়ে Telegram Vault আনলক করুন।"
            onResult(false, msg)
            return
        }
        val token = getActiveBotToken(context)
        if (!isValidBotTokenFormat(token)) {
            val msg = "বৈধ Bot Token পাওয়া যায়নি। প্রথমে সঠিক Bot Token সংরক্ষণ করুন।"
            connectedBotInfo.value = "টোকেন অবৈধ বা খালি"
            onResult(false, msg)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var ok = false
            var infoMsg = ""
            try {
                val url = URL("https://api.telegram.org/bot$token/getMe")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                val code = conn.responseCode
                if (code in 200..299) {
                    val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val json = JSONObject(body)
                    if (json.optBoolean("ok", false)) {
                        val result = json.optJSONObject("result")
                        val botName = result?.optString("first_name", "Bot") ?: "Bot"
                        val username = result?.optString("username", "") ?: ""
                        ok = true
                        infoMsg = "সংযুক্ত বট: $botName (@$username)"
                    } else {
                        infoMsg = "টোকেন যাচাই ব্যর্থ (API Error)"
                    }
                } else {
                    infoMsg = "টোকেন যাচাই ব্যর্থ (HTTP $code)"
                }
                conn.disconnect()
            } catch (e: Exception) {
                infoMsg = "নেটওয়ার্ক ত্রুটি: ${e.localizedMessage ?: " সংযোগ পাওয়া যায়নি"}"
            }

            withContext(Dispatchers.Main) {
                connectedBotInfo.value = infoMsg
                lastTelegramDeliveryStatus.value = infoMsg
                onResult(ok, infoMsg)
            }
        }
    }

    // ================================================================================
    // ৪. বাস্তব TELEGRAM BOT API মেসেজ প্রেরণ (Real HTTP POST to api.telegram.org)
    // ================================================================================

    fun sendTelegramAlertAsync(
        context: Context?,
        title: String,
        messageBody: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val token = getActiveBotToken(context)
        val chatIds = getAllTargetChatIds(context)

        if (!isValidBotTokenFormat(token)) {
            val msg = "Telegram Bot Token বসানো হয়নি (ভল্ট আনলক করে বৈধ টোকেন দিন)"
            lastTelegramDeliveryStatus.value = msg
            recordDeliveryLog(title, 0, "কনফিগারেশন নেই", msg)
            onResult(false, msg)
            return
        }
        if (chatIds.isEmpty()) {
            val msg = "Telegram Numeric Chat ID বসানো হয়নি (ভল্ট আনলক করে নিউমেরিক ID দিন)"
            lastTelegramDeliveryStatus.value = msg
            recordDeliveryLog(title, 0, "কনফিগারেশন নেই", msg)
            onResult(false, msg)
            return
        }

        val formattedText = "🕌 *কওমি কর্মসংস্থান বাংলাদেশ*\n" +
            "📌 *$title*\n\n" +
            "$messageBody\n\n" +
            "🔗 $BOT_USERNAME"

        CoroutineScope(Dispatchers.IO).launch {
            var successCount = 0
            var lastError = ""

            for (chatId in chatIds) {
                try {
                    val url = URL("https://api.telegram.org/bot$token/sendMessage")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 10000
                        readTimeout = 10000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    }

                    val payload = JSONObject().apply {
                        put("chat_id", chatId)
                        put("text", formattedText)
                        put("parse_mode", "Markdown")
                    }

                    OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(payload.toString())
                        writer.flush()
                    }

                    val code = conn.responseCode
                    if (code in 200..299) {
                        successCount++
                    } else {
                        lastError = "HTTP $code"
                    }
                    conn.disconnect()
                } catch (e: Exception) {
                    lastError = e.localizedMessage ?: "Network error"
                }
            }

            withContext(Dispatchers.Main) {
                if (successCount > 0) {
                    val statusMsg = "Telegram মেসেজ সফলভাবে পাঠানো হয়েছে ($successCount/${chatIds.size} টি আইডিতে)"
                    lastTelegramDeliveryStatus.value = statusMsg
                    recordDeliveryLog(title, chatIds.size, "সফল", statusMsg)
                    onResult(true, statusMsg)
                } else {
                    val errMsg = "Telegram মেসেজ পাঠানো যায়নি: $lastError"
                    lastTelegramDeliveryStatus.value = errMsg
                    recordDeliveryLog(title, chatIds.size, "ব্যর্থ", errMsg)
                    onResult(false, errMsg)
                }
            }
        }
    }
}
