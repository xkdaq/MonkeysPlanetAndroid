package top.monkeysxu.planet.feature_home.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import top.monkeysxu.planet.core.model.ApiResponse
import top.monkeysxu.planet.feature_home.model.ArticleItem
import top.monkeysxu.planet.feature_home.model.HomeData

interface HomeApiService {

    @FormUrlEncoded
    @POST("api/home/index")
    suspend fun getHomeData(
        @Field("bannerLimit") bannerLimit: Int = 3,
        @Field("noticeLimit") noticeLimit: Int = 3,
        @Field("articleLimit") articleLimit: Int = 10,
        @Field("panArticleLimit") panArticleLimit: Int = 0
    ): ApiResponse<HomeData>

    /** 文章列表（分页） */
    @FormUrlEncoded
    @POST("api/article/list")
    suspend fun getArticleList(
        @Field("pageNum") pageNum: Int = 1,
        @Field("pageSize") pageSize: Int = 12,
        @Field("keywords") keywords: String? = null
    ): ApiResponse<List<ArticleItem>>

    /** 文章详情（根据ID） */
    @FormUrlEncoded
    @POST("api/article/details")
    suspend fun getArticleDetail(
        @Field("id") id: Int
    ): ApiResponse<ArticleItem>
}
