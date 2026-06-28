package top.monkeysxu.planet.feature_profile.bindphone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.model.UserInfo
import top.monkeysxu.planet.core.storage.TokenManager
import top.monkeysxu.planet.feature_profile.api.BindPhoneRequest
import top.monkeysxu.planet.feature_profile.api.UserApiService

class BindPhoneViewModel(
    private val api: UserApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _userInfo = MutableLiveData<Resource<UserInfo>>()
    val userInfo: LiveData<Resource<UserInfo>> = _userInfo

    private val _sendCodeResult = MutableLiveData<Resource<Unit>>()
    val sendCodeResult: LiveData<Resource<Unit>> = _sendCodeResult

    private val _bindResult = MutableLiveData<Resource<Unit>>()
    val bindResult: LiveData<Resource<Unit>> = _bindResult

    fun loadUserInfo() = viewModelScope.launch {
        try {
            val response = api.getUserInfo()
            if (response.isSuccess && response.data != null) {
                tokenManager.saveUserInfo(response.data)
                _userInfo.value = Resource.Success(response.data)
            } else {
                _userInfo.value = Resource.Error(response.msg ?: "获取用户信息失败")
            }
        } catch (e: Exception) {
            val local = tokenManager.getUserInfo()
            _userInfo.value = if (local != null) Resource.Success(local)
            else Resource.Error(e.message ?: "获取用户信息失败")
        }
    }

    fun sendCode(phone: String) {
        if (!validPhone(phone)) {
            _sendCodeResult.value = Resource.Error("请输入正确的手机号")
            return
        }
        _sendCodeResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = api.sendVerifyCode(phone)
                _sendCodeResult.value = if (response.isSuccess) Resource.Success(Unit)
                else Resource.Error(response.msg ?: "验证码发送失败")
            } catch (e: Exception) {
                _sendCodeResult.value = Resource.Error(e.message ?: "验证码发送失败")
            }
        }
    }

    fun bind(phone: String, code: String, password: String, confirmPassword: String) {
        val error = when {
            !validPhone(phone) -> "请输入正确的手机号"
            code.length != 6 -> "请输入6位验证码"
            password.length !in 6..20 -> "密码长度应为6-20位"
            password != confirmPassword -> "两次输入的密码不一致"
            else -> null
        }
        if (error != null) {
            _bindResult.value = Resource.Error(error)
            return
        }
        _bindResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = api.bindPhone(BindPhoneRequest(phone, code, password))
                if (response.isSuccess) {
                    loadUserInfo()
                    _bindResult.value = Resource.Success(Unit)
                } else {
                    _bindResult.value = Resource.Error(response.msg ?: "绑定失败")
                }
            } catch (e: Exception) {
                _bindResult.value = Resource.Error(e.message ?: "绑定失败")
            }
        }
    }

    private fun validPhone(phone: String) = phone.matches(Regex("^1[3-9]\\d{9}$"))
}
