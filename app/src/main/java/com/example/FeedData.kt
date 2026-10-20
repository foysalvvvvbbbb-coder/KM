package com.example

import android.content.Context
import androidx.compose.runtime.mutableStateListOf

enum class FeedItemType {
    MADRASAH_NOTICE,
    CANDIDATE_APPLICATION
}

data class FeedItem(
    val id: String,
    val type: FeedItemType,
    val title: String, // মাদ্রাসার নাম অথবা প্রার্থীর নাম
    val location: String, // জেলা ও থানা
    val role: String, // পদের নাম
    val qualification: String, // শিক্ষাগত যোগ্যতা
    val extraInfo: String, // বেতন অথবা অভিজ্ঞতা
    val secondaryInfo: String, // আবেদনের শেষ তারিখ অথবা পছন্দের জেলা
    val timeAgo: String,
    val isVerified: Boolean = true,
    val contactPhone: String = "01712345678"
)

object FeedRepository {
    private const val PREFS_NAME = "qawmi_feed_prefs"
    private const val BOOKMARKS_KEY = "bookmarked_ids"
    private const val APPLIED_KEY = "applied_ids"

    // Default authentic Islamic Qawmi feed posts matching the reference image exactly
    val initialPosts = listOf(
        FeedItem(
            id = "post_1",
            type = FeedItemType.MADRASAH_NOTICE,
            title = "দারুল উলুম কওমি মাদ্রাসা",
            location = "কুমিল্লা, কুমিল্লা",
            role = "সহকারী শিক্ষক (আরবি, ফিকহ, তাফসীর)",
            qualification = "দাওরায়ে হাদীস / সমমান",
            extraInfo = "১২,০০০ - ১৮,০০০ টাকা",
            secondaryInfo = "২০ অক্টোবর ২০২৫",
            timeAgo = "২ ঘণ্টা আগে",
            isVerified = true,
            contactPhone = "01711223344"
        ),
        FeedItem(
            id = "post_2",
            type = FeedItemType.CANDIDATE_APPLICATION,
            title = "মোঃ আব্দুল্লাহ আল মামুন",
            location = "নোয়াখালী",
            role = "সহকারী শিক্ষক (বাংলা, আরবি)",
            qualification = "মাস্টার্স (ইসলামিক স্টাডিজ) / দাওরায়ে হাদীস",
            extraInfo = "২ বছর",
            secondaryInfo = "নোয়াখালী, লক্ষ্মীপুর, চট্টগ্রাম",
            timeAgo = "৪ ঘণ্টা আগে",
            isVerified = true,
            contactPhone = "01822334455"
        ),
        FeedItem(
            id = "post_3",
            type = FeedItemType.MADRASAH_NOTICE,
            title = "আল জামিয়াতুল আরাবিয়া দারুল কুরআন",
            location = "সিলেট, সিলেট",
            role = "সহকারী শিক্ষক (ইংরেজি, গণিত)",
            qualification = "মাস্টার্স / ইসলামি ডিগ্রি",
            extraInfo = "৮,০০০ - ১২,০০০ টাকা",
            secondaryInfo = "২৫ অক্টোবর ২০২৫",
            timeAgo = "৬ ঘণ্টা আগে",
            isVerified = true,
            contactPhone = "01933445566"
        ),
        FeedItem(
            id = "post_4",
            type = FeedItemType.CANDIDATE_APPLICATION,
            title = "মাওলানা রফিকুল ইসলাম",
            location = "রাজশাহী",
            role = "কুরআন শিক্ষক (কোরআন ও নাজেরা)",
            qualification = "দাওরায়ে হাদীস ও হাফেজে কুরআন",
            extraInfo = "৫ বছর",
            secondaryInfo = "রাজশাহী, নাটোর",
            timeAgo = "৮ ঘণ্টা আগে",
            isVerified = true,
            contactPhone = "01744556677"
        ),
        FeedItem(
            id = "post_5",
            type = FeedItemType.MADRASAH_NOTICE,
            title = "জামিয়া ইসলামিয়া আরাবিয়া",
            location = "বানারীপাড়া, বরিশাল",
            role = "মুহতামিম / প্রধান শিক্ষক",
            qualification = "দাওরায়ে হাদীস ও ইফতা",
            extraInfo = "আলোচনা সাপেক্ষ",
            secondaryInfo = "১৮ অক্টোবর ২০২৫",
            timeAgo = "১০ ঘণ্টা আগে",
            isVerified = true,
            contactPhone = "01555667788"
        )
    )

    val posts = mutableStateListOf<FeedItem>().apply {
        addAll(initialPosts)
    }

    private val bookmarkedIds = mutableSetOf<String>()
    private val appliedIds = mutableSetOf<String>()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedBookmarks = prefs.getStringSet(BOOKMARKS_KEY, emptySet()) ?: emptySet()
        bookmarkedIds.clear()
        bookmarkedIds.addAll(savedBookmarks)

        val savedApplied = prefs.getStringSet(APPLIED_KEY, emptySet()) ?: emptySet()
        appliedIds.clear()
        appliedIds.addAll(savedApplied)
    }

    fun isBookmarked(postId: String): Boolean = bookmarkedIds.contains(postId)

    fun toggleBookmark(context: Context, postId: String): Boolean {
        val isNowBookmarked = if (bookmarkedIds.contains(postId)) {
            bookmarkedIds.remove(postId)
            false
        } else {
            bookmarkedIds.add(postId)
            true
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(BOOKMARKS_KEY, bookmarkedIds.toSet()).apply()
        return isNowBookmarked
    }

    fun isApplied(postId: String): Boolean = appliedIds.contains(postId)

    fun applyToPost(context: Context, postId: String) {
        appliedIds.add(postId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(APPLIED_KEY, appliedIds.toSet()).apply()
    }

    fun addPost(item: FeedItem) {
        posts.add(0, item)
    }

    fun getBookmarkedPosts(): List<FeedItem> {
        return posts.filter { bookmarkedIds.contains(it.id) }
    }

    fun getAppliedPosts(): List<FeedItem> {
        return posts.filter { appliedIds.contains(it.id) }
    }
}
