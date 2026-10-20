package com.example.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerService {
    @GET("v1/timings/{date}")
    suspend fun getPrayerTimesByDate(
        @Path("date") date: String,
        @Query("latitude") latitude: Double = 23.8103,
        @Query("longitude") longitude: Double = 90.4125,
        @Query("method") method: Int = 1, // University of Islamic Sciences, Karachi
        @Query("school") school: Int = 1  // Hanafi (Asr shadow 2x)
    ): PrayerResponse

    @GET("v1/timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double = 23.8103,
        @Query("longitude") longitude: Double = 90.4125,
        @Query("method") method: Int = 1,
        @Query("school") school: Int = 1
    ): PrayerResponse
}
