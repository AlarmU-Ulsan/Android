package com.example.uou_alarm_it

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("/notice")
    fun getNotice(
        @Query("category") category: Int, // default: 2/ 0: 일반 공지/ 1: 주요 공지/ 2: 아무것도 아님
        @Query("page") page: Int // default: 0/ 가져올 페이지의 번호
    ): Call<GetNoticeRequest>

    @GET("/search")
    fun getSearch(
        @Query("keyWord") keyWord: String,
        @Query("page") page: Int // default: 0/ 가져올 페이지의 번호
    ): Call<GetNoticeRequest>

}