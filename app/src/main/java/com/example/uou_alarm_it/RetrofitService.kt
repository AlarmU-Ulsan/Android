package com.example.uou_alarm_it

import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    @GET("/notice")
    fun getNotice(
        @Query("type") type: Int, // default: 2/ 0: 일반 공지/ 1: 주요 공지/ 2: 아무것도 아님
        @Query("page") page: Int, // default: 0/ 가져올 페이지의 번호
        @Query("major") major : String // default: ICT융합힉부/ IT융합전공, AI융합전공, ICT융합학부
    ): Call<GetNoticeResponse>

    @GET("/search")
    fun getSearch(
        @Query("keyWord") keyWord: String,
        @Query("major") major: String,
        @Query("page") page: Int // default: 0/ 가져올 페이지의 번호
    ): Call<GetNoticeResponse>

    @POST("/fcm/register")
    fun postFCMRegister(
        @Query("token") token: String,
        @Query("major") major: String
    ): Call<PostFCMRegisterResponse>

    @DELETE("/fcm/unregister")
    fun deleteFCMUnregister(
        @Query("token") token: String,
        @Query("major") major: String
    ): Call<PostFCMRegisterResponse>
}