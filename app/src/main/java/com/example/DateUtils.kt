package com.example

import android.os.Build
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.TimeZone

data class DateInfo(
    val bengaliDate: String,
    val englishDate: String,
    val hijriDate: String,
    val weekday: String
)

object DateUtils {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(number: Long): String = toBengaliDigits(number.toString())
    fun toBengaliDigits(number: Int): String = toBengaliDigits(number.toString())

    fun toBengaliDigits(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private val englishMonthsBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val hijriMonthsBn = listOf(
        "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    private val weekdaysBn = mapOf(
        Calendar.SUNDAY to "রবিবার",
        Calendar.MONDAY to "সোমবার",
        Calendar.TUESDAY to "মঙ্গলবার",
        Calendar.WEDNESDAY to "বুধবার",
        Calendar.THURSDAY to "বৃহস্পতিবার",
        Calendar.FRIDAY to "শুক্রবার",
        Calendar.SATURDAY to "শনিবার"
    )

    fun getDhakaCalendar(): Calendar {
        val tz = try {
            TimeZone.getTimeZone("Asia/Dhaka")
        } catch (_: Exception) {
            TimeZone.getDefault()
        }
        return Calendar.getInstance(tz)
    }

    /**
     * Calculates the Bengali calendar date based on the official Bangla Academy (Bangladesh)
     * revised calendar (effective since 2019).
     */
    fun getBengaliDate(cal: Calendar): String {
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1 // 1..12
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val isLeapYear = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)

        val (bDay, bMonth, bYear) = when (gMonth) {
            1 -> { // January
                if (gDay < 15) Triple(gDay + 16, "পৌষ", gYear - 594)
                else Triple(gDay - 14, "মাঘ", gYear - 594)
            }
            2 -> { // February
                if (gDay < 14) Triple(gDay + 17, "মাঘ", gYear - 594)
                else Triple(gDay - 13, "ফাল্গুন", gYear - 594)
            }
            3 -> { // March
                if (gDay < 15) {
                    val offset = if (isLeapYear) 16 else 15
                    Triple(gDay + offset, "ফাল্গুন", gYear - 594)
                } else {
                    Triple(gDay - 14, "চৈত্র", gYear - 594)
                }
            }
            4 -> { // April
                if (gDay < 14) Triple(gDay + 17, "চৈত্র", gYear - 594)
                else Triple(gDay - 13, "বৈশাখ", gYear - 593)
            }
            5 -> { // May
                if (gDay < 15) Triple(gDay + 17, "বৈশাখ", gYear - 593)
                else Triple(gDay - 14, "জ্যৈষ্ঠ", gYear - 593)
            }
            6 -> { // June
                if (gDay < 15) Triple(gDay + 17, "জ্যৈষ্ঠ", gYear - 593)
                else Triple(gDay - 14, "আষাঢ়", gYear - 593)
            }
            7 -> { // July
                if (gDay < 16) Triple(gDay + 16, "আষাঢ়", gYear - 593)
                else Triple(gDay - 15, "শ্রাবণ", gYear - 593)
            }
            8 -> { // August
                if (gDay < 16) Triple(gDay + 16, "শ্রাবণ", gYear - 593)
                else Triple(gDay - 15, "ভাদ্র", gYear - 593)
            }
            9 -> { // September
                if (gDay < 16) Triple(gDay + 16, "ভাদ্র", gYear - 593)
                else Triple(gDay - 15, "আশ্বিন", gYear - 593)
            }
            10 -> { // October
                if (gDay < 17) Triple(gDay + 15, "আশ্বিন", gYear - 593)
                else Triple(gDay - 16, "কার্তিক", gYear - 593)
            }
            11 -> { // November
                if (gDay < 16) Triple(gDay + 15, "কার্তিক", gYear - 593)
                else Triple(gDay - 15, "অগ্রহায়ণ", gYear - 593)
            }
            12 -> { // December
                if (gDay < 16) Triple(gDay + 15, "অগ্রহায়ণ", gYear - 593)
                else Triple(gDay - 15, "পৌষ", gYear - 593)
            }
            else -> Triple(1, "বৈশাখ", gYear - 593)
        }

        return "${toBengaliDigits(bDay)} $bMonth ${toBengaliDigits(bYear)} বঙ্গাব্দ"
    }

    /**
     * Formats the Gregorian (English) date in Bengali numerals and month name.
     */
    fun getEnglishDate(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val monthName = englishMonthsBn.getOrElse(monthIdx) { "" }
        return "${toBengaliDigits(day)} $monthName ${toBengaliDigits(year)}"
    }

    /**
     * Weekday name in Bengali.
     */
    fun getWeekday(cal: Calendar): String {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return weekdaysBn[dayOfWeek] ?: ""
    }

    /**
     * Astronomical conversion calculation as fallback.
     */
    private fun getAstronomicalHijri(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045

        val l = jd - 1948440 + 10632
        val n = (l - 1) / 10631
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        var hMonth = (24 * l3) / 709
        var hDay = l3 - (709 * hMonth) / 24
        val hYear = 30 * n + j - 30

        if (hMonth < 1) hMonth = 1
        if (hMonth > 12) hMonth = 12
        if (hDay < 1) hDay = 1
        if (hDay > 30) hDay = 30

        return Triple(hDay, hMonth, hYear)
    }

    /**
     * Calculates the Hijri date using java.time.chrono.HijrahDate (Umm al-Qura) when
     * available (Android 8.0+), falling back to standard astronomical tabular algorithm.
     * Clearly indicates reliance on local moon sighting.
     */
    fun getHijriDate(cal: Calendar): String {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val dhakaZone = try {
                    ZoneId.of("Asia/Dhaka")
                } catch (_: Exception) {
                    ZoneId.systemDefault()
                }
                val instant = cal.toInstant()
                val zdt = instant.atZone(dhakaZone)
                val hijrahDate = HijrahDate.from(zdt)
                val hDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)
                val hMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
                val hYear = hijrahDate.get(ChronoField.YEAR)
                val monthName = hijriMonthsBn.getOrElse(hMonth - 1) { "হিজরি" }
                return "${toBengaliDigits(hDay)} $monthName ${toBengaliDigits(hYear)} হিজরি*"
            }
        } catch (_: Throwable) {
            // fallback to astronomical formula below
        }

        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val (hDay, hMonth, hYear) = getAstronomicalHijri(y, m, d)
        val monthName = hijriMonthsBn.getOrElse(hMonth - 1) { "হিজরি" }
        return "${toBengaliDigits(hDay)} $monthName ${toBengaliDigits(hYear)} হিজরি*"
    }

    /**
     * Returns all 4 current date fields using Bangladesh Standard Time.
     */
    fun getCurrentDateInfo(): DateInfo {
        val cal = getDhakaCalendar()
        return DateInfo(
            bengaliDate = getBengaliDate(cal),
            englishDate = getEnglishDate(cal),
            hijriDate = getHijriDate(cal),
            weekday = getWeekday(cal)
        )
    }
}
