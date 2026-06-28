package top.monkeysxu.planet.core.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("data")
    val data: T?,
    @SerializedName("encrypted")
    val encrypted: Boolean? = null,
    @SerializedName("rows")
    val rows: T? = null,
    @SerializedName("total")
    val total: Long? = null
) {
    /** 后端成功码统一为 0 */
    val isSuccess: Boolean get() = code == 0

    /** 兼容 R<T>.data 和 TableDataInfo.rows 两种字段 */
    val listData: T? get() = data ?: rows
}
