package top.monkeysxu.planet.feature_home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_home.model.HomeData

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val _homeData = MutableLiveData<Resource<HomeData>>()
    val homeData: LiveData<Resource<HomeData>> = _homeData

    fun loadHomeData() {
        viewModelScope.launch {
            _homeData.value = Resource.Loading()
            val result = repository.getHomeData()
            _homeData.value = result
        }
    }

    fun refresh() {
        loadHomeData()
    }
}
