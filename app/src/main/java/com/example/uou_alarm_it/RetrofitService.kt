package com.example.uou_alarm_it

import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    @GET("/notice")
    fun getNotice(
        @Query("category") category: Int,
        @Query("page") page: Int
    ): Call<Notice>

}