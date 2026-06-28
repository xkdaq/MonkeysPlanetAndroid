package top.monkeysxu.planet.feature_profile.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_profile.api.AuthResult

class LoginViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Resource<AuthResult>>()
    val loginResult: LiveData<Resource<AuthResult>> = _loginResult

    fun login(phone: String, password: String) {
        if (phone.isBlank()) {
            _loginResult.value = Resource.Error("请输入手机号")
            return
        }
        if (password.isBlank()) {
            _loginResult.value = Resource.Error("请输入密码")
            return
        }
        if (!phone.matches(Regex("^1[3-9]\\d{9}$"))) {
            _loginResult.value = Resource.Error("手机号格式不正确")
            return
        }

        _loginResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.login(phone, password)
            _loginResult.value = result
        }
    }
}
