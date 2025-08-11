package com.example.adproject.api

import com.example.adproject.model.AnnouncementResponse
import com.example.adproject.model.AnswerResponse
import com.example.adproject.model.DashboardResponse
import com.example.adproject.model.JoinClassResponse
import com.example.adproject.model.QsInform
import com.example.adproject.model.QsResultDTO
import com.example.adproject.model.Result
import com.example.adproject.model.SelectQuestionDTO
import com.example.adproject.model.ViewClassResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    // ====== 题库 ======
    @GET("viewQuestion")
    suspend fun viewQuestion(
        @Query("keyword") keyword: String = "",
        @Query("questionName") questionName: String = "",
        @Query("grade") grade: String = "",
        @Query("subject") subject: String = "",
        @Query("topic") topic: String = "",
        @Query("category") category: String = "",
        @Query("page") page: Int,
        @Query("questionIndex") questionIndex: Int = -1
    ): Response<QsResultDTO<QsInform>>

    @GET("doquestion")
    suspend fun getQuestionById(@Query("id") id: Int): Response<Result<SelectQuestionDTO>>

    // 可选：如果某些页面仍想拿原始 JSON
    @GET("doquestion")
    suspend fun getQuestionDetailRaw(@Query("id") id: Int): Response<JsonObject>

    @GET("answerQuestion")
    suspend fun answerQuestion(
        @Query("id") id: Int,
        @Query("correct") correct: Int,
        @Query("param") param: Int
    ): Response<AnswerResponse>

    // ====== Dashboard ======
    @GET("dashboard")
    suspend fun dashboard(): Response<DashboardResponse>

    // ====== 推荐（把 DashboardApi 的两个接口并进来）======
    // 原来是 PUT student/recommend / GET student/getRecommend
    // baseUrl 已含 /student/，这里写成 "recommend" / "getRecommend"
    @PUT("recommend")
    suspend fun triggerRecommend(): Response<JsonObject>

    @GET("getRecommend")
    suspend fun getRecommendIds(): Response<JsonObject>

    // ====== 班级 & 公告 ======
    @GET("viewClass")
    suspend fun viewClass(): Response<ViewClassResponse>

    @GET("selectAnnouncement")
    suspend fun selectAnnouncement(@Query("classId") classId: Int): Response<AnnouncementResponse>

    @GET("joinClass")
    suspend fun joinClass(
        @Query("accessType") accessType: String, // "byName" / "byLink"
        @Query("key") key: String                // name 或 token
    ): Response<JoinClassResponse>
}
