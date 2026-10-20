package com.example.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrayerResponse(
    val data: PrayerData
)

@JsonClass(generateAdapter = true)
data class PrayerData(
    val timings: Timings
)

@JsonClass(generateAdapter = true)
data class Timings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Sunset: String,
    val Isha: String,
    val Midnight: String
)
