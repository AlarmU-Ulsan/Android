package com.alarmit.uou_alarm_it

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

    @POST("/fcm/fcm_token")
    fun postFCMToken(
        @Body request: PostFCMTokenRequest
    ): Call<PostFCMResponse>

    @POST("/fcm/subscribe")
    fun postFCMRegister(
        @Body request: FCMSubscribeRequest
    ): Call<PostFCMSubscribeResponse>

    @HTTP(method = "DELETE", path = "/fcm/subscribe", hasBody = true) // DELETE에서 Body를 사용하려면 이렇게 해야 함.
    fun deleteFCMUnregister(
        @Body request: FCMSubscribeRequest
    ): Call<PostFCMResponse>

    @GET("/api/version/android")
    fun getVersion(): Call<GetVersionResponse>
}