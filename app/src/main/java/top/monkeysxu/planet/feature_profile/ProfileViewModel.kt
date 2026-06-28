package top.monkeysxu.planet.feature_profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.core.model.UserInfo

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _userInfo = MutableLiveData<Resource<UserInfo>>()
    val userInfo: LiveData<Resource<UserInfo>> = _userInfo

    private val _logoutResult = MutableSharedFlow<Resource<Unit>>()
    val logoutResult = _logoutResult.asSharedFlow()

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _changePasswordResult = MutableLiveData<Resource<Unit>>()
    val changePasswordResult: LiveData<Resource<Unit>> = _changePasswordResult

    fun checkLoginState() {
        viewModelScope.launch {
            _isLoggedIn.value = repository.isLoggedIn()
        }
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            _userInfo.value = Resource.Loading()
            val result = repository.getUserInfo()
            _userInfo.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = repository.logout()
            _logoutResult.emit(result)
            if (result is Resource.Success) {
                _isLoggedIn.value = false
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordResult.value = Resource.Loading()
            val result = repository.changePassword(oldPassword, newPassword)
            _changePasswordResult.value = result
        }
    }
}
