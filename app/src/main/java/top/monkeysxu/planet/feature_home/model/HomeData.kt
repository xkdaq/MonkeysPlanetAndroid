package top.monkeysxu.planet.feature_home.model

import com.google.gson.annotations.SerializedName

data class HomeData(
    @SerializedName("bannerList")
    val bannerList: List<Banner>?,
    @SerializedName("noticeList")
    val noticeList: List<Notice>?,
    @SerializedName("articleList")
    val articleList: List<ArticleItem>?,
    @SerializedName("panArticleList")
    val panArticleList: List<PanArticle>?
)

data class Banner(
    @SerializedName("id")
    val id: Int,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("linkUrl")
    val linkUrl: String?
)

data class Notice(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String?
)

data class ArticleItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("date")
    val date: String?,
    @SerializedName("isTop")
    val isTop: Int?,
    @SerializedName("type")
    val type: Int?,
    @SerializedName("content")
    val content: String?
)

data class PanArticle(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("isTop")
    val isTop: Int?,
    @SerializedName("accessType")
    val accessType: Int?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("categoryList")
    val categoryList: List<String>?
)
