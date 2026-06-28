package top.monkeysxu.planet.feature_profile.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_profile.api.AuthResult

class RegisterViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    private val _sendCodeResult = MutableLiveData<Resource<Unit>>()
    val sendCodeResult: LiveData<Resource<Unit>> = _sendCodeResult

    private val _registerResult = MutableLiveData<Resource<AuthResult>>()
    val registerResult: LiveData<Resource<AuthResult>> = _registerResult

    fun sendCode(phone: String) {
        if (!isValidPhone(phone)) {
            _sendCodeResult.value = Resource.Error("请输入正确的手机号")
            return
        }
        _sendCodeResult.value = Resource.Loading()
        viewModelScope.launch {
            _sendCodeResult.value = repository.sendVerifyCode(phone)
        }
    }

    fun register(phone: String, code: String, password: String, confirmPassword: String) {
        val error = when {
            !isValidPhone(phone) -> "请输入正确的手机号"
            code.length != 6 -> "请输入6位验证码"
            password.length !in 6..20 -> "密码长度应为6-20位"
            password != confirmPassword -> "两次输入的密码不一致"
            else -> null
        }
        if (error != null) {
            _registerResult.value = Resource.Error(error)
            return
        }
        _registerResult.value = Resource.Loading()
        viewModelScope.launch {
            _registerResult.value = repository.register(phone, code, password)
        }
    }

    private fun isValidPhone(phone: String): Boolean =
        phone.matches(Regex("^1[3-9]\\d{9}$"))
}
