package top.monkeysxu.planet.feature_exam.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import top.monkeysxu.planet.core.model.ApiResponse
import top.monkeysxu.planet.feature_exam.model.Bank
import top.monkeysxu.planet.feature_exam.model.Category
import top.monkeysxu.planet.feature_exam.model.PracticeRecordPage
import top.monkeysxu.planet.feature_exam.model.Question
import top.monkeysxu.planet.feature_exam.model.StartPracticeResult

interface ExamApiService {

    @GET("mp/exam/banks")
    suspend fun getBanks(): ApiResponse<List<Bank>>

    @GET("mp/exam/category/tree")
    suspend fun getCategoryTree(@Query("bankId") bankId: Int): ApiResponse<List<Category>>

    @GET("mp/exam/question/{id}")
    suspend fun getQuestion(@Path("id") id: Long): ApiResponse<Question>

    @GET("mp/exam/practice/start")
    suspend fun startPractice(
        @Query("bankId") bankId: Int,
        @Query("categoryId") categoryId: Int? = null,
        @Query("practiceType") practiceType: Int = 1,
        @Query("limit") limit: Int = 999
    ): ApiResponse<StartPracticeResult>

    @POST("mp/exam/practice/submit")
    suspend fun submitAnswer(@Body request: SubmitRequest): ApiResponse<Any>

    @POST("mp/exam/practice/record")
    suspend fun saveRecord(@Body request: RecordRequest): ApiResponse<Any>

    @GET("mp/exam/practice/records")
    suspend fun getRecords(
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): ApiResponse<PracticeRecordPage>

    @POST("mp/exam/favorite")
    suspend fun toggleFavorite(@Body request: Map<String, Long>): ApiResponse<Boolean>

    @GET("mp/exam/favorite/check")
    suspend fun checkFavorite(@Query("questionId") questionId: Long): ApiResponse<Boolean>

    @GET("mp/exam/favorites")
    suspend fun getFavorites(): ApiResponse<List<Question>>

    @GET("mp/exam/wrongs")
    suspend fun getWrongs(): ApiResponse<List<Question>>

    @POST("mp/exam/wrongs/remove")
    suspend fun removeWrong(@Body request: Map<String, Long>): ApiResponse<Any>

    @POST("mp/exam/question/report")
    suspend fun reportQuestion(@Body request: ReportRequest): ApiResponse<Any>
}

data class SubmitRequest(
    val questionId: Long,
    val answer: String
)

data class RecordRequest(
    val bankId: Int,
    val categoryId: Int?,
    val correctCount: Int,
    val wrongCount: Int,
    val totalCount: Int,
    val duration: Int
)

data class ReportRequest(
    val questionId: Long,
    val type: String,
    val content: String
)
