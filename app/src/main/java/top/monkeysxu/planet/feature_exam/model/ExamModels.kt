package top.monkeysxu.planet.feature_exam.model

import com.google.gson.annotations.SerializedName

data class Bank(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("questionCount")
    val questionCount: Int?
)

data class Category(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("parentId")
    val parentId: Int?,
    @SerializedName("sort")
    val sort: Int?,
    @SerializedName("children")
    val children: List<Category>?
)

data class Question(
    @SerializedName("id")
    val id: Long,
    @SerializedName("content")
    val content: String,
    @SerializedName("type")
    val type: Int,
    @SerializedName("options")
    val options: String?,
    @SerializedName("answer")
    val answer: String?,
    @SerializedName("analysis")
    val analysis: String?,
    @SerializedName("optionsMap")
    val optionsMap: Map<String, String>? = null
)
// type: 1-单选 2-多选 3-判断 4-填空 5-问答 6-材料

data class StartPracticeResult(
    val questions: List<Question>,
    val total: Int,
    val practiceType: Int?,
    val bankId: Long?
)

data class PracticeRecordPage(
    val list: List<PracticeRecord>,
    val total: Int,
    val totalQuestions: Int,
    val totalCorrect: Int,
    val totalDurationText: String
)

data class PracticeRecord(
    val id: Long,
    val practiceType: Int,
    val practiceTypeName: String,
    val totalCount: Int,
    val correctCount: Int,
    val accuracy: Int,
    val duration: Int,
    val durationText: String,
    val createTime: String,
    val bankName: String
)
