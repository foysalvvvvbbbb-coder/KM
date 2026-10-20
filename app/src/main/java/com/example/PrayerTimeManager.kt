package com.example

import com.example.api.PrayerService
import java.util.Calendar
import java.util.Locale
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class PrayerTime(val name: String, val start: String, val end: String)

object PrayerTimeManager {
    const val DHAKA_LATITUDE = 23.8103
    const val DHAKA_LONGITUDE = 90.4125
    const val METHOD_KARACHI = 1 // University of Islamic Sciences, Karachi
    const val SCHOOL_HANAFI = 1 // Hanafi (Asr shadow is 2x)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
        .build()

    private val service = retrofit.create(PrayerService::class.java)

    /**
     * Standard Karachi Method (Hanafi) prayer times fallback for Dhaka, Bangladesh (Asia/Dhaka)
     * according to authentic Hanafi Fiqh rules:
     * - ফজর: সুবহে সাদিক থেকে সূর্যোদয় পূর্ব পর্যন্ত
     * - যোহর: সূর্য ঢলে পড়া থেকে হানাফি আসর (দ্বিগুণ ছায়া) পূর্ব পর্যন্ত
     * - আসর: হানাফি আসর শুরু থেকে সূর্যাস্ত/মাগরিব পূর্ব পর্যন্ত
     * - মাগরিব: সূর্যাস্ত থেকে এশা শুরু পূর্ব পর্যন্ত
     * - ইশা: এশা শুরু থেকে সুবহে সাদিক (ফজর শুরু) পূর্ব পর্যন্ত
     */
    fun getFallbackPrayerTimes(): List<PrayerTime> {
        return listOf(
            PrayerTime("ফজর", "04:42", "05:58"),
            PrayerTime("যোহর", "11:43", "15:51"),
            PrayerTime("আসর", "15:51", "17:28"),
            PrayerTime("মাগরিব", "17:28", "18:44"),
            PrayerTime("ইশা", "18:44", "04:42")
        )
    }

    suspend fun getPrayerTimes(city: String = "Dhaka"): List<PrayerTime> {
        return try {
            val cal = DateUtils.getDhakaCalendar()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            val dateStr = String.format(Locale.US, "%02d-%02d-%04d", day, month, year)

            val response = service.getPrayerTimesByDate(
                date = dateStr,
                latitude = DHAKA_LATITUDE,
                longitude = DHAKA_LONGITUDE,
                method = METHOD_KARACHI,
                school = SCHOOL_HANAFI
            )
            val timings = response.data.timings
            listOf(
                // ফজর: শুরু সুবহে সাদিক, শেষ সূর্যোদয়
                PrayerTime("ফজর", timings.Fajr.take(5), timings.Sunrise.take(5)),
                // যোহর: শুরু জাওয়াল/যোহর, শেষ হানাফি আসরের ওয়াক্ত শুরু হওয়া পর্যন্ত
                PrayerTime("যোহর", timings.Dhuhr.take(5), timings.Asr.take(5)),
                // আসর: শুরু হানাফি আসর, শেষ সূর্যাস্ত/মাগরিব শুরু পর্যন্ত
                PrayerTime("আসর", timings.Asr.take(5), timings.Maghrib.take(5)),
                // মাগরিব: শুরু সূর্যাস্ত, শেষ এশা শুরু হওয়া পর্যন্ত
                PrayerTime("মাগরিব", timings.Maghrib.take(5), timings.Isha.take(5)),
                // ইশা: শুরু এশা, শেষ সুবহে সাদিক (পরবর্তী ফজর শুরু হওয়া পর্যন্ত)
                PrayerTime("ইশা", timings.Isha.take(5), timings.Fajr.take(5))
            )
        } catch (e: Exception) {
            getFallbackPrayerTimes()
        }
    }
}
