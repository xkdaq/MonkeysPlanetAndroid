package top.monkeysxu.planet.feature_profile.login

import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.feature_profile.api.AuthResult
import top.monkeysxu.planet.feature_profile.api.LoginRequest
import top.monkeysxu.planet.feature_profile.api.RegisterRequest
import top.monkeysxu.planet.feature_profile.api.UserApiService

class LoginRepository(
    private val apiService: UserApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(phone: String, password: String): Resource<AuthResult> {
        return try {
            val response = apiService.loginByPhone(LoginRequest(phone, password))
            if (response.isSuccess && response.data != null) {
                saveAuthResult(response.data)
                Resource.Success(response.data)
            } else {
                Resource.Error(response.msg ?: "登录失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun sendVerifyCode(phone: String): Resource<Unit> {
        return try {
            val response = apiService.sendVerifyCode(phone)
            if (response.isSuccess) Resource.Success(Unit)
            else Resource.Error(response.msg ?: "验证码发送失败")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun register(phone: String, code: String, password: String): Resource<AuthResult> {
        return try {
            val response = apiService.register(RegisterRequest(phone, code, password))
            if (response.isSuccess && response.data != null) {
                saveAuthResult(response.data)
                Resource.Success(response.data)
            } else {
                Resource.Error(response.msg ?: "注册失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    private suspend fun saveAuthResult(result: AuthResult) {
        tokenManager.saveToken(result.token)
        result.userInfo?.let { tokenManager.saveUserInfo(it) }
    }
}
