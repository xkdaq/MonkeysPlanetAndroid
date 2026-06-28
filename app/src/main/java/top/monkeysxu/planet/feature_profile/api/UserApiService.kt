package top.monkeysxu.planet.feature_profile.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.Part
import okhttp3.MultipartBody
import top.monkeysxu.planet.core.model.ApiResponse
import top.monkeysxu.planet.core.model.UserInfo

interface UserApiService {

    @POST("mp/auth/login/phone")
    suspend fun loginByPhone(@Body request: LoginRequest): ApiResponse<AuthResult>

    @POST("mp/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResult>

    @GET("mp/sms/send")
    suspend fun sendVerifyCode(@Query("phone") phone: String): ApiResponse<Any>

    @POST("mp/phone/bind")
    suspend fun bindPhone(@Body request: BindPhoneRequest): ApiResponse<Any>

    @Multipart
    @POST("mp/user/avatar/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): ApiResponse<AvatarUploadData>

    @POST("mp/auth/password")
    suspend fun changePassword(@Body request: PasswordRequest): ApiResponse<Any>

    @GET("mp/user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>

    @PUT("mp/user/info")
    suspend fun updateUserInfo(@Body userInfo: UserInfo): ApiResponse<Any>

    @PUT("mp/user/avatar")
    suspend fun updateAvatar(@Body request: Map<String, String>): ApiResponse<Any>

    @PUT("mp/user/nickname")
    suspend fun updateNickname(@Body request: Map<String, String>): ApiResponse<Any>

    @PUT("mp/user/gender")
    suspend fun updateGender(@Body request: Map<String, Int>): ApiResponse<Any>
}

data class LoginRequest(
    val phone: String,
    val password: String
)

data class RegisterRequest(
    val phone: String,
    val code: String,
    val password: String
)

data class BindPhoneRequest(
    val phone: String,
    val code: String,
    val password: String
)

data class AvatarUploadData(
    val url: String?
)

data class AuthResult(
    val token: String,
    val expire: Long?,
    val userInfo: UserInfo?
)

data class PasswordRequest(
    val phone: String,
    val oldPassword: String? = null,
    val newPassword: String
)
