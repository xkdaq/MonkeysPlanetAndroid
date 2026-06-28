package top.monkeysxu.planet.feature_home

import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_home.api.HomeApiService
import top.monkeysxu.planet.feature_home.model.HomeData

class HomeRepository(
    private val apiService: HomeApiService
) {

    suspend fun getHomeData(): Resource<HomeData> {
        return try {
            val response = apiService.getHomeData()
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.msg ?: "获取首页数据失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }
}
