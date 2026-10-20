package com.example

import java.util.Calendar
import com.example.api.PrayerService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class PrayerTime(val name: String, val start: String, val end: String)

object PrayerTimeManager {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
        .build()

    private val service = retrofit.create(PrayerService::class.java)

    suspend fun getPrayerTimes(city: String): List<PrayerTime> {
        return try {
            val response = service.getPrayerTimes(city)
            val timings = response.data.timings
            listOf(
                PrayerTime("ফজর", timings.Fajr, timings.Sunrise),
                PrayerTime("যোহর", timings.Dhuhr, timings.Asr),
                PrayerTime("আসর", timings.Asr, timings.Maghrib),
                PrayerTime("মাগরিব", timings.Maghrib, timings.Isha),
                PrayerTime("ইশা", timings.Isha, timings.Midnight) // Isha end time is approx midnight
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}
