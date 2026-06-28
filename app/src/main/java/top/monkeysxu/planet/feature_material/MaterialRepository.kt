package top.monkeysxu.planet.feature_material

import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_material.api.MaterialApiService
import top.monkeysxu.planet.feature_material.model.MaterialCategory
import top.monkeysxu.planet.feature_material.model.MaterialItem
import top.monkeysxu.planet.feature_material.model.MaterialSubject

class MaterialRepository(
    private val apiService: MaterialApiService
) {

    suspend fun getSubjects(): Resource<List<MaterialSubject>> {
        return try {
            val response = apiService.getSubjects()
            if (response.isSuccess) {
                Resource.Success(response.data ?: emptyList())
            } else {
                Resource.Error(response.msg ?: "获取科目失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun getCategories(subjectId: Int? = null): Resource<List<MaterialCategory>> {
        return try {
            val response = apiService.getCategories(subjectId)
            if (response.isSuccess) {
                Resource.Success(response.data ?: emptyList())
            } else {
                Resource.Error(response.msg ?: "获取分类失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun getMaterialList(
        subjectId: Int? = null,
        categoryId: Int? = null,
        pageNum: Int = 1,
        keyword: String? = null
    ): Resource<List<MaterialItem>> {
        return try {
            val response = apiService.getMaterialList(subjectId, categoryId, pageNum, keyword = keyword)
            if (response.isSuccess) {
                Resource.Success(response.listData ?: emptyList())
            } else {
                Resource.Error(response.msg ?: "获取资料失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }
}
