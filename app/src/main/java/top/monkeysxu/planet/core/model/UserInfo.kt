package top.monkeysxu.planet.core.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("hasPassword")
    val hasPassword: Boolean?,
    @SerializedName("hasPhone")
    val hasPhone: Boolean?,
    @SerializedName("username")
    val username: String? = null
)
