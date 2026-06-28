package top.monkeysxu.planet.feature_material.model

import com.google.gson.annotations.SerializedName

data class MaterialSubject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String?,
    @SerializedName("sort")
    val sort: Int?
)

data class MaterialCategory(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("parentId")
    val parentId: Int?,
    @SerializedName("sort")
    val sort: Int?
)

data class MaterialItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String?,
    @SerializedName("isTop")
    val isTop: Int?,
    @SerializedName("accessType")
    val accessType: Int?,
    @SerializedName("subjectId")
    val subjectId: Int?,
    @SerializedName("categoryId")
    val categoryId: Int?,
    @SerializedName("subjectName")
    val subjectName: String?,
    @SerializedName("categoryName")
    val categoryName: String?,
    @SerializedName("categoryList")
    val categoryList: List<String>?,
    @SerializedName("baiduUrl")
    val baiduUrl: String?,
    @SerializedName("quarkUrl")
    val quarkUrl: String?,
    @SerializedName("baiduCode")
    val baiduCode: String?,
    @SerializedName("quarkCode")
    val quarkCode: String?,
    @SerializedName("linkRemark")
    val linkRemark: String?
)
