package top.monkeysxu.planet.feature_profile.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.model.UserInfo
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.feature_profile.api.UserApiService

class ProfileEditViewModel(
    private val api: UserApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _userInfo = MutableLiveData<Resource<UserInfo>>()
    val userInfo: LiveData<Resource<UserInfo>> = _userInfo

    private val _avatarResult = MutableLiveData<Resource<String>>()
    val avatarResult: LiveData<Resource<String>> = _avatarResult

    private val _saveResult = MutableLiveData<Resource<Unit>>()
    val saveResult: LiveData<Resource<Unit>> = _saveResult

    fun load() = viewModelScope.launch {
        val local = tokenManager.getUserInfo()
        if (local != null) _userInfo.value = Resource.Success(local)
        try {
            val response = api.getUserInfo()
            if (response.isSuccess && response.data != null) {
                tokenManager.saveUserInfo(response.data)
                _userInfo.value = Resource.Success(response.data)
            }
        } catch (_: Exception) {
            if (local == null) _userInfo.value = Resource.Error("获取用户信息失败")
        }
    }

    fun uploadAvatar(bytes: ByteArray, mimeType: String, fileName: String) {
        _avatarResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", fileName, body)
                val upload = api.uploadFile(part)
                val url = upload.data?.url
                if (upload.isSuccess && !url.isNullOrBlank()) {
                    _avatarResult.value = Resource.Success(url)
                } else {
                    _avatarResult.value = Resource.Error(upload.msg ?: "头像上传失败")
                }
            } catch (e: Exception) {
                _avatarResult.value = Resource.Error(e.message ?: "头像上传失败")
            }
        }
    }

    fun save(original: UserInfo, nickname: String, gender: Int, avatarUrl: String?) {
        if (nickname.isBlank()) {
            _saveResult.value = Resource.Error("请输入昵称")
            return
        }
        _saveResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                if (nickname != original.nickname) {
                    val result = api.updateNickname(mapOf("nickname" to nickname))
                    if (!result.isSuccess) throw IllegalStateException(result.msg ?: "昵称更新失败")
                }
                if (gender != (original.gender ?: 0)) {
                    val result = api.updateGender(mapOf("gender" to gender))
                    if (!result.isSuccess) throw IllegalStateException(result.msg ?: "性别更新失败")
                }
                if (!avatarUrl.isNullOrBlank() && avatarUrl != original.avatarUrl) {
                    val result = api.updateAvatar(mapOf("avatarUrl" to avatarUrl))
                    if (!result.isSuccess) throw IllegalStateException(result.msg ?: "头像更新失败")
                }
                val updated = original.copy(
                    nickname = nickname,
                    gender = gender,
                    avatarUrl = avatarUrl ?: original.avatarUrl
                )
                tokenManager.saveUserInfo(updated)
                _saveResult.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _saveResult.value = Resource.Error(e.message ?: "保存失败")
            }
        }
    }
}
