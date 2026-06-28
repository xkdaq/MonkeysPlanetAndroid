package top.monkeysxu.planet.core.model

import com.google.gson.annotations.SerializedName

data class AnswerRecord(
    @SerializedName("questionId")
    val questionId: Long,
    @SerializedName("userAnswer")
    val userAnswer: String,
    @SerializedName("isCorrect")
    val isCorrect: Boolean
)
