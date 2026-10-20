package com.example.api

import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerService {
    @GET("v1/timingsByCity")
    suspend fun getPrayerTimes(
        @Query("city") city: String,
        @Query("country") country: String = "Bangladesh",
        @Query("method") method: Int = 1 // Muslim World League
    ): PrayerResponse
}
