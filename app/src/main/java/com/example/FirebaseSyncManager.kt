package com.example

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class FirestoreOpType {
    GET, LIST, CREATE, UPDATE, DELETE, WRITE
}

object FirebaseSyncManager {
    private var appContext: Context? = null
    private var firestoreInstance: FirebaseFirestore? = null
    private val activeListeners = mutableListOf<ListenerRegistration>()
    private var isListening = false

    val isConnectedToFirestore = MutableStateFlow(false)
    val lastSyncStatus = MutableStateFlow("সংযুক্ত হচ্ছে...")
    val lastErrorMessage = MutableStateFlow<String?>(null)

    private fun getDb(): FirebaseFirestore? {
        val ctx = appContext ?: return null
        if (firestoreInstance == null) {
            try {
                val dbId = ctx.getString(R.string.firestore_database_id)
                firestoreInstance = FirebaseFirestore.getInstance(dbId)
            } catch (e: Exception) {
                Log.w("FirestoreSync", "Firestore init fallback: ${e.message}")
            }
        }
        return firestoreInstance
    }

    fun handleFirestoreError(exception: Exception, opType: FirestoreOpType, path: String) {
        val uid = try { FirebaseAuth.getInstance().currentUser?.uid ?: "unauthenticated" } catch (_: Exception) { "unauthenticated" }
        val errJson = JSONObject().apply {
            put("operation", opType.name)
            put("path", path)
            put("uid", uid)
            put("message", exception.localizedMessage ?: "Unknown Firestore error")
        }
        Log.e("FirestoreError", errJson.toString())
        lastErrorMessage.value = exception.localizedMessage
        lastSyncStatus.value = "অফলাইন/লোকাল সিঙ্ক সক্রিয় (${opType.name})"
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        NotificationHelper.init(context)
        ProfileAndInboxRepository.init(context)

        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                ProfileAndInboxRepository.syncAuthIdentityToProfile(context)
                ProfileAndInboxRepository.setOnlinePresence(true)
                startRealtimeSync()
                syncAuthenticatedUserRecord()
            } else {
                lastSyncStatus.value = "লগইন পর ক্লাউড সিঙ্ক সক্রিয় হবে"
            }
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Auth not initialized in test environment: ${e.message}")
        }
    }

    fun onAuthStateChanged() {
        val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
        if (user != null) {
            ProfileAndInboxRepository.syncAuthIdentityToProfile(appContext)
            ProfileAndInboxRepository.setOnlinePresence(true)
            startRealtimeSync()
            syncAuthenticatedUserRecord()
        } else {
            ProfileAndInboxRepository.setOnlinePresence(false)
            stopRealtimeSync()
            lastSyncStatus.value = "লগআউট অবস্থা (লোকাল ক্যাশ)"
        }
    }

    fun stopRealtimeSync() {
        activeListeners.forEach {
            try { it.remove() } catch (_: Exception) {}
        }
        activeListeners.clear()
        isListening = false
        isConnectedToFirestore.value = false
    }

    fun startRealtimeSync() {
        if (isListening) return
        val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null } ?: return
        val db = getDb() ?: return
        val ctx = appContext ?: return
        isListening = true

        try {
            // 1. Notices Listener
            val noticesReg = db.collection("notices")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "notices")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        isConnectedToFirestore.value = true
                        lastSyncStatus.value = "Firebase লাইভ সংযুক্ত"
                        if (snapshot.isEmpty) {
                            // Seed existing notices non-destructively
                            AdminRepository.noticesList.value.forEach { saveNotice(it) }
                        } else {
                            val loaded = snapshot.documents.mapNotNull { doc ->
                                val postId = doc.getString("postId") ?: doc.id
                                val madrasahName = doc.getString("madrasahName") ?: return@mapNotNull null
                                val roleTitle = doc.getString("roleTitle") ?: ""
                                val location = doc.getString("location") ?: "বাংলাদেশ"
                                val salaryInfo = doc.getString("salaryInfo") ?: "আলোচনা সাপেক্ষ"
                                val deadline = doc.getString("deadline") ?: "চলমান"
                                val phone = doc.getString("phone") ?: ""
                                val status = doc.getString("status") ?: "অনুমোদিত"
                                val isBoosted = doc.getBoolean("isBoosted") ?: false
                                val viewsCount = (doc.getLong("viewsCount") ?: 0L).toInt()
                                val clicksCount = (doc.getLong("clicksCount") ?: 0L).toInt()
                                val applicationsCount = (doc.getLong("applicationsCount") ?: 0L).toInt()
                                NoticeStatusItem(
                                    postId = postId,
                                    madrasahName = madrasahName,
                                    roleTitle = roleTitle,
                                    location = location,
                                    salaryInfo = salaryInfo,
                                    deadline = deadline,
                                    phone = phone,
                                    status = status,
                                    isBoosted = isBoosted,
                                    viewsCount = viewsCount,
                                    clicksCount = clicksCount,
                                    applicationsCount = applicationsCount
                                )
                            }
                            if (loaded.isNotEmpty()) {
                                AdminRepository.updateNoticesFromCloud(loaded)
                            }
                        }
                    }
                }
            activeListeners.add(noticesReg)

            // 2. Applications Listener
            val appsReg = db.collection("applications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "applications")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        if (snapshot.isEmpty) {
                            AdminRepository.applicationsList.value.forEach { saveApplication(it) }
                        } else {
                            val loaded = snapshot.documents.mapNotNull { doc ->
                                val postId = doc.getString("postId") ?: doc.id
                                val candidateName = doc.getString("candidateName") ?: return@mapNotNull null
                                val roleTitle = doc.getString("roleTitle") ?: ""
                                val location = doc.getString("location") ?: "বাংলাদেশ"
                                val qualification = doc.getString("qualification") ?: "দাওরায়ে হাদীস"
                                val experience = doc.getString("experience") ?: "নতুন"
                                val phone = doc.getString("phone") ?: ""
                                val status = doc.getString("status") ?: "নতুন"
                                val submittedDate = doc.getString("submittedDate") ?: "আজ"
                                ApplicationStatusItem(
                                    postId = postId,
                                    candidateName = candidateName,
                                    roleTitle = roleTitle,
                                    location = location,
                                    qualification = qualification,
                                    experience = experience,
                                    phone = phone,
                                    status = status,
                                    submittedDate = submittedDate
                                )
                            }
                            if (loaded.isNotEmpty()) {
                                AdminRepository.updateApplicationsFromCloud(loaded)
                            }
                        }
                    }
                }
            activeListeners.add(appsReg)

            // 2.5 User Applications Listener (Real-time sync of user job applications)
            val userAppsReg = db.collection("user_applications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val currentUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null }
                        val loaded = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val postId = doc.getString("postId") ?: return@mapNotNull null
                            val applicantUid = doc.getString("applicantUid") ?: ""
                            val candidateName = doc.getString("applicantName") ?: ""
                            val roleTitle = doc.getString("roleTitle") ?: ""
                            val madrasahName = doc.getString("madrasahName") ?: ""
                            val location = doc.getString("location") ?: "বাংলাদেশ"
                            val salaryInfo = doc.getString("salaryInfo") ?: "আলোচনা সাপেক্ষ"
                            val deadline = doc.getString("deadline") ?: "চলমান"
                            val appliedDate = doc.getString("appliedDate") ?: "আজ"
                            val appliedAtMillis = doc.getLong("appliedAtMillis") ?: System.currentTimeMillis()
                            val status = doc.getString("status") ?: "পেন্ডিং"
                            val notes = doc.getString("notes") ?: ""
                            UserApplicationRecord(
                                id = id,
                                postId = postId,
                                applicantUid = applicantUid,
                                applicantName = candidateName,
                                applicantEmail = doc.getString("applicantEmail") ?: "",
                                applicantPhone = doc.getString("applicantPhone") ?: "",
                                applicantQualification = doc.getString("applicantQualification") ?: "",
                                roleTitle = roleTitle,
                                madrasahName = madrasahName,
                                location = location,
                                salaryInfo = salaryInfo,
                                deadline = deadline,
                                appliedDate = appliedDate,
                                appliedAtMillis = appliedAtMillis,
                                status = status,
                                notes = notes
                            )
                        }
                        val myApps = if (!currentUid.isNullOrBlank()) {
                            loaded.filter { it.applicantUid == currentUid || it.applicantUid.isBlank() }
                        } else {
                            loaded
                        }
                        if (myApps.isNotEmpty()) {
                            ProfileAndInboxRepository.updateUserApplicationsFromCloud(myApps)
                        }
                    }
                }
            activeListeners.add(userAppsReg)

            // 3. Madrasahs Listener
            val madrasahsReg = db.collection("madrasahs")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "madrasahs")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        if (snapshot.isEmpty) {
                            AdminRepository.madrasahsList.value.forEach { saveMadrasah(it) }
                        } else {
                            val loaded = snapshot.documents.mapNotNull { doc ->
                                val id = doc.getString("id") ?: doc.id
                                val name = doc.getString("name") ?: return@mapNotNull null
                                AdminMadrasahItem(
                                    id = id,
                                    name = name,
                                    location = doc.getString("location") ?: "",
                                    muhtamim = doc.getString("muhtamim") ?: "",
                                    departments = doc.getString("departments") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    regNumber = doc.getString("regNumber") ?: "",
                                    verificationStatus = doc.getString("verificationStatus") ?: "যাচাইকৃত"
                                )
                            }
                            if (loaded.isNotEmpty()) {
                                AdminRepository.madrasahsList.value = loaded
                            }
                        }
                    }
                }
            activeListeners.add(madrasahsReg)

            // 4. Users Listener (for Admin User Management & Current User Profile Sync)
            val usersReg = db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "users")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val loaded = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: return@mapNotNull null
                            UserManagementItem(
                                id = id,
                                name = name,
                                email = doc.getString("email") ?: "",
                                phone = doc.getString("phone") ?: "",
                                status = doc.getString("status") ?: "সক্রিয়",
                                role = doc.getString("role") ?: "আবেদনকারী",
                                joinedDate = doc.getString("joinedDate") ?: ""
                            )
                        }
                        if (loaded.isNotEmpty()) {
                            val merged = (loaded + AdminRepository.usersList.value)
                                .distinctBy { it.email.lowercase().ifBlank { it.id } }
                            AdminRepository.usersList.value = merged
                        }
                    }
                }
            activeListeners.add(usersReg)

            // 5. Boost Requests Listener
            val boostReg = db.collection("boost_requests")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "boost_requests")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        if (snapshot.isEmpty) {
                            AdminRepository.boostRequests.value.forEach { saveBoostRequest(it) }
                        } else {
                            val loaded = snapshot.documents.mapNotNull { doc ->
                                val id = doc.getString("id") ?: doc.id
                                val title = doc.getString("title") ?: return@mapNotNull null
                                BoostRequestItem(
                                    id = id,
                                    noticeId = doc.getString("noticeId") ?: "post_1",
                                    title = title,
                                    requestedBy = doc.getString("requestedBy") ?: "",
                                    status = doc.getString("status") ?: "Pending",
                                    requestDate = doc.getString("requestDate") ?: "",
                                    durationDays = (doc.getLong("durationDays") ?: 7L).toInt(),
                                    expiresAtMillis = doc.getLong("expiresAtMillis"),
                                    targetScope = doc.getString("targetScope") ?: "সকল ব্যবহারকারী",
                                    viewsCount = (doc.getLong("viewsCount") ?: 0L).toInt(),
                                    clicksCount = (doc.getLong("clicksCount") ?: 0L).toInt(),
                                    applicationsCount = (doc.getLong("applicationsCount") ?: 0L).toInt()
                                )
                            }
                            if (loaded.isNotEmpty()) {
                                AdminRepository.boostRequests.value = loaded
                                AdminRepository.checkExpiredBoosts()
                            }
                        }
                    }
                }
            activeListeners.add(boostReg)

            // 6. Notifications Listener (Real-time Push Notification Delivery + History)
            val notifReg = db.collection("notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "notifications")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val currentUserEmail = user.email
                        val currentUserRole = AdminRepository.usersList.value
                            .find { it.email.equals(currentUserEmail, ignoreCase = true) }?.role

                        val loaded = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: return@mapNotNull null
                            val body = doc.getString("body") ?: ""
                            val targetAudience = doc.getString("targetAudience") ?: "সকল ব্যবহারকারী"
                            val targetRoute = doc.getString("targetRoute") ?: "khedmot"
                            val targetItemId = doc.getString("targetItemId") ?: ""
                            val eventKey = doc.getString("eventKey") ?: id
                            val sentTime = doc.getString("sentTime") ?: ""
                            val status = doc.getString("status") ?: "প্রেরিত"
                            val category = doc.getString("category") ?: "Announcements"

                            // Deliver real Android System Notification for new unread events
                            val pushRes = NotificationHelper.deliverSystemNotificationWithResult(
                                context = ctx,
                                notifId = id,
                                title = title,
                                body = body,
                                targetAudience = targetAudience,
                                targetRoute = targetRoute,
                                targetItemId = targetItemId,
                                eventKey = eventKey,
                                category = category,
                                currentUserEmail = currentUserEmail,
                                currentUserRole = currentUserRole
                            )

                            SentNotificationItem(
                                id = id,
                                title = title,
                                body = body,
                                targetAudience = targetAudience,
                                sentTime = sentTime,
                                status = status,
                                targetRoute = targetRoute,
                                targetItemId = targetItemId,
                                eventKey = eventKey,
                                category = category,
                                deliveryStatus = doc.getString("deliveryStatus") ?: pushRes.deliveryStatus,
                                failureReason = doc.getString("failureReason") ?: pushRes.failureReason,
                                isDeliveredToDevice = doc.getBoolean("isDeliveredToDevice") ?: pushRes.isDeliveredToDevice
                            )
                        }
                        if (loaded.isNotEmpty()) {
                            AdminRepository.sentNotifications.value = loaded
                        }
                    }
                }
            activeListeners.add(notifReg)

            // 7. Dynamic Form Configs Listener
            val formsReg = db.collection("form_configs")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "form_configs")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val loaded = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val label = doc.getString("label") ?: return@mapNotNull null
                            DynamicFormField(
                                id = id,
                                formCategory = doc.getString("formCategory") ?: "খেদমতের আবেদন ফর্ম",
                                label = label,
                                placeholder = doc.getString("placeholder") ?: "",
                                type = doc.getString("type") ?: "Short Text",
                                isRequired = doc.getBoolean("isRequired") ?: true,
                                order = (doc.getLong("order") ?: 1L).toInt(),
                                validation = doc.getString("validation") ?: "None",
                                optionsCsv = doc.getString("optionsCsv") ?: ""
                            )
                        }.sortedBy { it.order }
                        if (loaded.isNotEmpty()) {
                            AdminRepository.formFields.value = loaded
                        }
                    }
                }
            activeListeners.add(formsReg)

            // 8. App Config Listener (Content Management, Telegram, Permissions)
            val configReg = db.collection("app_config")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "app_config")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        snapshot.documents.forEach { doc ->
                            when (doc.id) {
                                "main_content" -> {
                                    AdminRepository.appContent = AppContentConfig(
                                        heroTitle = doc.getString("heroTitle") ?: AdminRepository.appContent.heroTitle,
                                        heroSlogan = doc.getString("heroSlogan") ?: AdminRepository.appContent.heroSlogan,
                                        heroDescription = doc.getString("heroDescription") ?: AdminRepository.appContent.heroDescription,
                                        contactEmail = doc.getString("contactEmail") ?: AdminRepository.appContent.contactEmail,
                                        contactPhone = doc.getString("contactPhone") ?: AdminRepository.appContent.contactPhone,
                                        faqText = doc.getString("faqText") ?: AdminRepository.appContent.faqText,
                                        termsText = doc.getString("termsText") ?: AdminRepository.appContent.termsText,
                                        privacyText = doc.getString("privacyText") ?: AdminRepository.appContent.privacyText,
                                        isPublished = doc.getBoolean("isPublished") ?: true
                                    )
                                }
                                "telegram_settings" -> {
                                    val idsRaw = doc.getString("approvedUserIdsJson")
                                    val parsedIds = mutableListOf<String>()
                                    if (!idsRaw.isNullOrBlank()) {
                                        try {
                                            val arr = JSONArray(idsRaw)
                                            for (i in 0 until arr.length()) parsedIds.add(arr.getString(i))
                                        } catch (_: Exception) {}
                                    }
                                    AdminRepository.telegramSettings = TelegramSettingsConfig(
                                        username = doc.getString("username") ?: AdminRepository.telegramSettings.username,
                                        notifyNewAdmin = doc.getBoolean("notifyNewAdmin") ?: true,
                                        notifyBoostRequest = doc.getBoolean("notifyBoostRequest") ?: true,
                                        notifyImportantChanges = doc.getBoolean("notifyImportantChanges") ?: true,
                                        notifySecurityAlert = doc.getBoolean("notifySecurityAlert") ?: true,
                                        approvedUserIds = if (parsedIds.isNotEmpty()) parsedIds else AdminRepository.telegramSettings.approvedUserIds
                                    )
                                }
                                "boost_policy" -> {
                                    AdminRepository.boostPolicy.value = BoostPolicyConfig(
                                        maxConcurrentBoosts = (doc.getLong("maxConcurrentBoosts") ?: 5L).toInt(),
                                        maxActivePerUser = (doc.getLong("maxActivePerUser") ?: 2L).toInt(),
                                        notificationFrequencyLimitHours = (doc.getLong("notificationFrequencyLimitHours") ?: 24L).toInt()
                                    )
                                }
                            }
                        }
                    }
                }
            activeListeners.add(configReg)

            // 9. Support Tickets Listener
            val ticketsReg = db.collection("support_tickets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, FirestoreOpType.LIST, "support_tickets")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val loaded = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val subject = doc.getString("subject") ?: return@mapNotNull null
                            SupportTicketItem(
                                id = id,
                                reporterName = doc.getString("reporterName") ?: "ব্যবহারকারী",
                                reporterContact = doc.getString("reporterContact") ?: "",
                                subject = subject,
                                description = doc.getString("description") ?: "",
                                date = doc.getString("date") ?: "",
                                status = doc.getString("status") ?: "অপেক্ষমাণ",
                                adminReply = doc.getString("adminReply") ?: ""
                            )
                        }
                        if (loaded.isNotEmpty()) {
                            AdminRepository.supportTickets.value = loaded
                        }
                    }
                }
            activeListeners.add(ticketsReg)
        } catch (e: Exception) {
            handleFirestoreError(e, FirestoreOpType.LIST, "root_listeners")
        }
    }

    fun syncAuthenticatedUserRecord() {
        val ctx = appContext ?: return
        val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null } ?: return
        val email = user.email ?: ""
        val cachedName = ctx.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("name_${user.uid}", null)
        val displayName = when {
            !user.displayName.isNullOrBlank() -> user.displayName!!
            !cachedName.isNullOrBlank() -> cachedName
            email.isNotBlank() -> email.substringBefore("@")
            else -> "ব্যবহারকারী"
        }
        val token = NotificationHelper.getOrCreateDeviceToken(ctx)
        val deviceId = NotificationHelper.getDeviceId(ctx)
        val notifEnabled = NotificationHelper.isNotificationsEnabled(ctx)

        val db = getDb() ?: return
        val payload = mapOf(
            "id" to user.uid,
            "uid" to user.uid,
            "name" to displayName,
            "email" to email,
            "phone" to (user.phoneNumber ?: ""),
            "status" to "সক্রিয়",
            "role" to if (AdminRepository.isUserAdmin(email)) "Admin" else "আবেদনকারী",
            "notificationsEnabled" to notifEnabled,
            "fcmToken" to token,
            "deviceId" to deviceId,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(user.uid)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "users/${user.uid}")
            }
    }

    fun syncCurrentUserNotificationPreference(enabled: Boolean) {
        val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null } ?: return
        val db = getDb() ?: return
        db.collection("users").document(user.uid)
            .set(
                mapOf(
                    "notificationsEnabled" to enabled,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.UPDATE, "users/${user.uid}")
            }
    }

    fun syncCurrentUserToken(token: String, deviceId: String) {
        val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null } ?: return
        val db = getDb() ?: return
        db.collection("users").document(user.uid)
            .set(
                mapOf(
                    "fcmToken" to token,
                    "deviceId" to deviceId,
                    "tokenUpdatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.UPDATE, "users/${user.uid}")
            }
    }

    fun saveUser(item: UserManagementItem, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val db = getDb() ?: run { onComplete(true, null); return }
        val payload = mapOf(
            "id" to item.id,
            "name" to item.name,
            "email" to item.email,
            "phone" to item.phone,
            "status" to item.status,
            "role" to item.role,
            "joinedDate" to item.joinedDate,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(item.id)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "users/${item.id}")
                onComplete(false, e.localizedMessage)
            }
    }

    fun saveMadrasah(item: AdminMadrasahItem, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val db = getDb() ?: run { onComplete(true, null); return }
        val payload = mapOf(
            "id" to item.id,
            "name" to item.name,
            "location" to item.location,
            "muhtamim" to item.muhtamim,
            "departments" to item.departments,
            "phone" to item.phone,
            "regNumber" to item.regNumber,
            "verificationStatus" to item.verificationStatus,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("madrasahs").document(item.id)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "madrasahs/${item.id}")
                onComplete(false, e.localizedMessage)
            }
    }

    fun saveNotice(item: NoticeStatusItem, qualification: String = "দাওরায়ে হাদীস", onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val db = getDb() ?: run { onComplete(true, null); return }
        val payload = mapOf(
            "postId" to item.postId,
            "madrasahName" to item.madrasahName,
            "roleTitle" to item.roleTitle,
            "location" to item.location,
            "qualification" to qualification,
            "salaryInfo" to item.salaryInfo,
            "deadline" to item.deadline,
            "phone" to item.phone,
            "status" to item.status,
            "isBoosted" to item.isBoosted,
            "viewsCount" to item.viewsCount,
            "clicksCount" to item.clicksCount,
            "applicationsCount" to item.applicationsCount,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("notices").document(item.postId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "notices/${item.postId}")
                onComplete(false, e.localizedMessage)
            }
    }

    fun deleteNoticeFromFirestore(postId: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val db = getDb() ?: run { onComplete(true, null); return }
        db.collection("notices").document(postId)
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.DELETE, "notices/$postId")
                onComplete(false, e.localizedMessage)
            }
    }

    fun saveApplication(item: ApplicationStatusItem, preferredDistricts: String = "সারাদেশ", onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val db = getDb() ?: run { onComplete(true, null); return }
        val payload = mapOf(
            "postId" to item.postId,
            "candidateName" to item.candidateName,
            "roleTitle" to item.roleTitle,
            "location" to item.location,
            "qualification" to item.qualification,
            "experience" to item.experience,
            "preferredDistricts" to preferredDistricts,
            "phone" to item.phone,
            "status" to item.status,
            "submittedDate" to item.submittedDate,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("applications").document(item.postId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "applications/${item.postId}")
                onComplete(false, e.localizedMessage)
            }
    }

    fun saveBoostPolicy(policy: BoostPolicyConfig) {
        val db = getDb() ?: return
        val payload = mapOf(
            "maxConcurrentBoosts" to policy.maxConcurrentBoosts,
            "maxActivePerUser" to policy.maxActivePerUser,
            "notificationFrequencyLimitHours" to policy.notificationFrequencyLimitHours,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("app_config").document("boost_policy")
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "app_config/boost_policy")
            }
    }

    fun saveBoostRequest(item: BoostRequestItem, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val db = getDb() ?: run { onComplete(true, null); return }
        val payload = mutableMapOf<String, Any>(
            "id" to item.id,
            "noticeId" to item.noticeId,
            "title" to item.title,
            "requestedBy" to item.requestedBy,
            "status" to item.status,
            "requestDate" to item.requestDate,
            "durationDays" to item.durationDays,
            "targetScope" to item.targetScope,
            "viewsCount" to item.viewsCount,
            "clicksCount" to item.clicksCount,
            "applicationsCount" to item.applicationsCount,
            "updatedAt" to System.currentTimeMillis()
        )
        if (item.expiresAtMillis != null) {
            payload["expiresAtMillis"] = item.expiresAtMillis
        }
        db.collection("boost_requests").document(item.id)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "boost_requests/${item.id}")
                onComplete(false, e.localizedMessage)
            }
    }

    fun saveNotification(item: SentNotificationItem, senderEmail: String = SUPER_ADMIN_EMAIL, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val ctx = appContext
        var finalDeliveryStatus = item.deliveryStatus
        var finalFailureReason = item.failureReason
        var finalDeliveredFlag = item.isDeliveredToDevice
        if (ctx != null) {
            val currentUserEmail = try { FirebaseAuth.getInstance().currentUser?.email } catch (_: Exception) { null }
            val res = NotificationHelper.deliverSystemNotificationWithResult(
                context = ctx,
                notifId = item.id,
                title = item.title,
                body = item.body,
                targetAudience = item.targetAudience,
                targetRoute = item.targetRoute,
                targetItemId = item.targetItemId,
                eventKey = item.eventKey.ifBlank { item.id },
                category = item.category,
                currentUserEmail = currentUserEmail
            )
            finalDeliveryStatus = res.deliveryStatus
            finalFailureReason = res.failureReason
            finalDeliveredFlag = res.isDeliveredToDevice
            // Update in-memory list with real delivery outcome
            AdminRepository.sentNotifications.value = AdminRepository.sentNotifications.value.map {
                if (it.id == item.id) {
                    it.copy(
                        deliveryStatus = finalDeliveryStatus,
                        failureReason = finalFailureReason,
                        isDeliveredToDevice = finalDeliveredFlag
                    )
                } else it
            }
        }
        val db = getDb() ?: run { onComplete(true, null); return }
        val payload = mapOf(
            "id" to item.id,
            "title" to item.title,
            "body" to item.body,
            "targetAudience" to item.targetAudience,
            "targetRoute" to item.targetRoute,
            "targetItemId" to item.targetItemId,
            "eventKey" to item.eventKey.ifBlank { item.id },
            "category" to item.category,
            "sentTime" to item.sentTime,
            "status" to item.status,
            "deliveryStatus" to finalDeliveryStatus,
            "failureReason" to finalFailureReason,
            "isDeliveredToDevice" to finalDeliveredFlag,
            "senderEmail" to senderEmail,
            "createdAtMillis" to System.currentTimeMillis()
        )
        db.collection("notifications").document(item.id)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.CREATE, "notifications/${item.id}")
                onComplete(false, e.localizedMessage)
            }
    }

    fun saveFormField(field: DynamicFormField) {
        val db = getDb() ?: return
        val payload = mapOf(
            "id" to field.id,
            "formCategory" to field.formCategory,
            "label" to field.label,
            "placeholder" to field.placeholder,
            "type" to field.type,
            "isRequired" to field.isRequired,
            "order" to field.order,
            "validation" to field.validation,
            "optionsCsv" to field.optionsCsv,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("form_configs").document(field.id)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "form_configs/${field.id}")
            }
    }

    fun deleteFormField(fieldId: String) {
        val db = getDb() ?: return
        db.collection("form_configs").document(fieldId)
            .delete()
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.DELETE, "form_configs/$fieldId")
            }
    }

    fun saveAppContent(config: AppContentConfig) {
        val db = getDb() ?: return
        val payload = mapOf(
            "heroTitle" to config.heroTitle,
            "heroSlogan" to config.heroSlogan,
            "heroDescription" to config.heroDescription,
            "contactEmail" to config.contactEmail,
            "contactPhone" to config.contactPhone,
            "faqText" to config.faqText,
            "termsText" to config.termsText,
            "privacyText" to config.privacyText,
            "isPublished" to config.isPublished,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("app_config").document("main_content")
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "app_config/main_content")
            }
    }

    fun saveTelegramSettings(config: TelegramSettingsConfig) {
        val db = getDb() ?: return
        val idsArr = JSONArray()
        config.approvedUserIds.forEach { idsArr.put(it) }
        val payload = mapOf(
            "username" to config.username,
            "notifyNewAdmin" to config.notifyNewAdmin,
            "notifyBoostRequest" to config.notifyBoostRequest,
            "notifyImportantChanges" to config.notifyImportantChanges,
            "notifySecurityAlert" to config.notifySecurityAlert,
            "approvedUserIdsJson" to idsArr.toString(),
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("app_config").document("telegram_settings")
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "app_config/telegram_settings")
            }
    }

    fun saveSupportTicket(ticket: SupportTicketItem) {
        val db = getDb() ?: return
        val payload = mapOf(
            "id" to ticket.id,
            "reporterName" to ticket.reporterName,
            "reporterContact" to ticket.reporterContact,
            "subject" to ticket.subject,
            "description" to ticket.description,
            "date" to ticket.date,
            "status" to ticket.status,
            "adminReply" to ticket.adminReply,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("support_tickets").document(ticket.id)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "support_tickets/${ticket.id}")
            }
    }

    fun saveAuditLog(entry: AuditLogEntry) {
        val db = getDb() ?: return
        val payload = mapOf(
            "id" to entry.id,
            "time" to entry.time,
            "user" to entry.user,
            "action" to entry.action,
            "category" to entry.category,
            "createdAtMillis" to System.currentTimeMillis()
        )
        db.collection("audit_logs").document(entry.id)
            .set(payload)
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.CREATE, "audit_logs/${entry.id}")
            }
    }

    fun saveTelegramVaultMeta(
        isPasswordConfigured: Boolean,
        maskedToken: String,
        updatedBy: String
    ) {
        val db = getDb() ?: return
        val payload = mapOf(
            "isPasswordConfigured" to isPasswordConfigured,
            "maskedToken" to maskedToken,
            "updatedBy" to updatedBy,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("telegram_vault").document("security_meta")
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "telegram_vault/security_meta")
            }
    }

    fun saveTelegramDeliveryLog(entry: TelegramDeliveryLogEntry) {
        val db = getDb() ?: return
        val payload = mapOf(
            "id" to entry.id,
            "time" to entry.time,
            "eventTitle" to entry.eventTitle,
            "targetCount" to entry.targetCount,
            "status" to entry.status,
            "details" to entry.details,
            "createdAtMillis" to System.currentTimeMillis()
        )
        db.collection("telegram_logs").document(entry.id)
            .set(payload)
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.CREATE, "telegram_logs/${entry.id}")
            }
    }

    fun saveFullUserProfile(profile: UserProfileData, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val db = getDb() ?: run { onComplete(true, null); return }
        val docId = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: profile.uid
        val payload = mapOf(
            "id" to docId,
            "uid" to docId,
            "name" to profile.fullName,
            "fullName" to profile.fullName,
            "email" to profile.email,
            "phone" to profile.phone,
            "location" to profile.location,
            "district" to profile.district,
            "division" to profile.division,
            "bio" to profile.bio,
            "madrasahName" to profile.madrasahName,
            "qualification" to profile.qualification,
            "teachingSubjects" to profile.teachingSubjects,
            "skills" to profile.skills,
            "experience" to profile.experience,
            "preferredKhedmotType" to profile.preferredKhedmotType,
            "roleLabel" to profile.roleLabel,
            "designation" to profile.designation,
            "photoUri" to profile.photoUri,
            "avatarPreset" to profile.avatarPreset,
            "isVerified" to profile.isVerified,
            "isOnline" to profile.isOnline,
            "lastActiveMillis" to profile.lastActiveMillis,
            "showOnlineStatus" to profile.showOnlineStatus,
            "completionPercentage" to profile.completionPercentage(),
            "status" to "সক্রিয়",
            "role" to if (AdminRepository.isUserAdmin(profile.email)) "Admin" else "আবেদনকারী",
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(docId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "users/$docId")
                onComplete(false, e.localizedMessage)
            }
    }

    fun saveUserPresence(profile: UserProfileData) {
        val db = getDb() ?: return
        val docId = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: return
        val payload = mapOf(
            "isOnline" to profile.isOnline,
            "lastActiveMillis" to profile.lastActiveMillis,
            "showOnlineStatus" to profile.showOnlineStatus,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(docId)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.UPDATE, "users/$docId")
            }
    }

    fun saveUserApplicationRecord(record: UserApplicationRecord) {
        val db = getDb() ?: return
        val payload = mapOf(
            "id" to record.id,
            "postId" to record.postId,
            "applicantUid" to record.applicantUid,
            "applicantName" to record.applicantName,
            "applicantEmail" to record.applicantEmail,
            "applicantPhone" to record.applicantPhone,
            "applicantQualification" to record.applicantQualification,
            "roleTitle" to record.roleTitle,
            "madrasahName" to record.madrasahName,
            "location" to record.location,
            "salaryInfo" to record.salaryInfo,
            "deadline" to record.deadline,
            "appliedDate" to record.appliedDate,
            "appliedAtMillis" to record.appliedAtMillis,
            "status" to record.status,
            "notes" to record.notes,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("user_applications").document(record.id)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "user_applications/${record.id}")
            }
    }

    fun deleteUserApplicationRecord(applicationId: String) {
        val db = getDb() ?: return
        db.collection("user_applications").document(applicationId)
            .delete()
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.DELETE, "user_applications/$applicationId")
            }
    }

    fun saveConversation(conv: ChatConversation) {
        val db = getDb() ?: return
        val senderUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null } ?: "user_default"
        val payload = mapOf(
            "id" to conv.id,
            "participantIds" to listOf(senderUid, conv.participantId),
            "participantId" to conv.participantId,
            "participantName" to conv.participantName,
            "participantRole" to conv.participantRole,
            "participantEmail" to conv.participantEmail,
            "participantPhone" to conv.participantPhone,
            "participantMadrasah" to conv.participantMadrasah,
            "participantLocation" to conv.participantLocation,
            "isOnline" to conv.isOnline,
            "lastActiveText" to conv.lastActiveText,
            "lastMessage" to conv.lastMessage,
            "lastMessageTime" to conv.lastMessageTime,
            "lastMessageAtMillis" to conv.lastMessageAtMillis,
            "unreadCount" to conv.unreadCount,
            "relatedPostId" to conv.relatedPostId,
            "relatedPostTitle" to conv.relatedPostTitle,
            "isBlocked" to conv.isBlocked,
            "blockedByMe" to conv.blockedByMe,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("conversations").document(conv.id)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.WRITE, "conversations/${conv.id}")
            }
    }

    fun saveChatMessage(msg: ChatMessage) {
        val db = getDb() ?: return
        val payload = mapOf(
            "id" to msg.id,
            "conversationId" to msg.conversationId,
            "senderId" to msg.senderId,
            "senderName" to msg.senderName,
            "receiverId" to msg.receiverId,
            "text" to msg.text,
            "messageType" to "TEXT_ONLY",
            "timestampText" to msg.timestampText,
            "createdAtMillis" to msg.createdAtMillis,
            "isSeen" to msg.isSeen
        )
        db.collection("conversations").document(msg.conversationId)
            .collection("messages").document(msg.id)
            .set(payload)
            .addOnFailureListener { e ->
                handleFirestoreError(e, FirestoreOpType.CREATE, "conversations/${msg.conversationId}/messages/${msg.id}")
            }
    }
}
