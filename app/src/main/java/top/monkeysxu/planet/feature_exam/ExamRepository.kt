package top.monkeysxu.planet.feature_exam

import top.monkeysxu.planet.core.model.Resource
import top.monkeysxu.planet.feature_exam.api.ExamApiService
import top.monkeysxu.planet.feature_exam.api.RecordRequest
import top.monkeysxu.planet.feature_exam.model.Bank
import top.monkeysxu.planet.feature_exam.model.Category
import top.monkeysxu.planet.feature_exam.model.Question
import top.monkeysxu.planet.feature_exam.model.StartPracticeResult

class ExamRepository(
    private val apiService: ExamApiService
) {

    suspend fun getBanks(): Resource<List<Bank>> {
        return try {
            val response = apiService.getBanks()
            if (response.isSuccess) {
                Resource.Success(response.data ?: emptyList())
            } else {
                Resource.Error(response.msg ?: "获取题库失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun getCategoryTree(bankId: Int): Resource<List<Category>> {
        return try {
            val response = apiService.getCategoryTree(bankId)
            if (response.isSuccess) {
                Resource.Success(response.data ?: emptyList())
            } else {
                Resource.Error(response.msg ?: "获取分类失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun startPractice(bankId: Int, categoryId: Int?, type: Int): Resource<List<Question>> {
        return try {
            // 顺序练习(1)和随机练习(2)限制20题，专项练习(3)加载全部
            val limit = if (type == 1 || type == 2) 20 else 999
            val response = apiService.startPractice(bankId, categoryId, type, limit)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.questions)
            } else {
                Resource.Error(response.msg ?: "开始练习失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun getQuestion(questionId: Long): Resource<Question> {
        return try {
            val response = apiService.getQuestion(questionId)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.msg ?: "获取题目失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }

    suspend fun saveRecord(request: RecordRequest): Resource<Unit> {
        return try {
            val response = apiService.saveRecord(request)
            if (response.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.msg ?: "保存记录失败")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "网络异常")
        }
    }
}
