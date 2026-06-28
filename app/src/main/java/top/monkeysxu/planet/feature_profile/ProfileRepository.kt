package top.monkeysxu.planet.feature_profile

import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.model.UserInfo
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.feature_profile.api.PasswordRequest
import top.monkeysxu.planet.feature_profile.api.UserApiService

class ProfileRepository(
    private val apiService: UserApiService,
    private val tokenManager: TokenManager
) {

    suspend fun getUserInfo(): Resource<UserInfo> {
        return try {
            val response = apiService.getUserInfo()
            if (response.isSuccess && response.data != null) {
                tokenManager.saveUserInfo(response.data)
                Resource.Success(response.data)
            } else {
                Resource.Error(response.msg ?: "获取用户信息失败")
            }
        } catch (e: Exception) {
            // 网络异常时尝试读取本地缓存
            val local = tokenManager.getUserInfo()
            if (local != null) {
                Resource.Success(local)
            } else {
                Resource.Error(e.message ?: "网络异常")
            }
        }
    }

    suspend fun logout(): Resource<Unit> {
        return try {
            tokenManager.clearLoginState()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "退出失败")
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> {
        return try {
            val userInfo = tokenManager.getUserInfo()
            val phone = userInfo?.phone
            if (phone.isNullOrEmpty()) {
                return Resource.Error("未获取到手机号")
            }
            val response = apiService.changePassword(
                PasswordRequest(phone = phone, oldPassword = oldPassword, newPassword = newPassword)
            )
            if (response.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.msg ?: "修改密码失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }
}
