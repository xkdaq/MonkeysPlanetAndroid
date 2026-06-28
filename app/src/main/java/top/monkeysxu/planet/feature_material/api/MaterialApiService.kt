package top.monkeysxu.planet.feature_material.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import top.monkeysxu.planet.core.model.ApiResponse
import top.monkeysxu.planet.feature_material.model.MaterialCategory
import top.monkeysxu.planet.feature_material.model.MaterialItem
import top.monkeysxu.planet.feature_material.model.MaterialSubject

interface MaterialApiService {

    @POST("api/material/subjects")
    suspend fun getSubjects(): ApiResponse<List<MaterialSubject>>

    @FormUrlEncoded
    @POST("api/material/categories")
    suspend fun getCategories(
        @Field("subjectId") subjectId: Int? = null
    ): ApiResponse<List<MaterialCategory>>

    @FormUrlEncoded
    @POST("api/material/list")
    suspend fun getMaterialList(
        @Field("subjectId") subjectId: Int? = null,
        @Field("categoryId") categoryId: Int? = null,
        @Field("pageNum") pageNum: Int = 1,
        @Field("pageSize") pageSize: Int = 20,
        @Field("keywords") keyword: String? = null
    ): ApiResponse<List<MaterialItem>>

    @FormUrlEncoded
    @POST("api/material/details")
    suspend fun getMaterialDetail(
        @Field("id") id: Int
    ): ApiResponse<MaterialItem>
}
