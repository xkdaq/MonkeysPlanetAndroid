package top.monkeysxu.planet.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import top.monkeysxu.planet.core.model.AnswerRecord

data class PracticeProgress(
    val bankId: Int,
    val categoryId: Int?,
    val practiceType: Int,
    val currentIndex: Int,
    val totalCount: Int,
    val answerRecords: List<AnswerRecord>,
    val correctCount: Int,
    val wrongCount: Int,
    val practiceMode: String,
    val duration: Int,
    val questionIds: List<Long>,
    val saveTime: Long
)

class PracticeProgressStore(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore
    private val gson = Gson()

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "practice_prefs")
    }

    fun progressKey(bankId: Int, categoryId: Int?, practiceType: Int): String {
        return "practice_progress_${bankId}_${categoryId ?: 0}_${practiceType}"
    }

    suspend fun save(progress: PracticeProgress) {
        val key = stringPreferencesKey(progressKey(progress.bankId, progress.categoryId, progress.practiceType))
        dataStore.edit { prefs ->
            prefs[key] = gson.toJson(progress)
        }
    }

    suspend fun get(bankId: Int, categoryId: Int?, practiceType: Int): PracticeProgress? {
        val key = stringPreferencesKey(progressKey(bankId, categoryId, practiceType))
        val json = dataStore.data.map { it[key] }.first()
        return if (!json.isNullOrEmpty()) {
            gson.fromJson(json, object : TypeToken<PracticeProgress>() {}.type)
        } else null
    }

    suspend fun remove(bankId: Int, categoryId: Int?, practiceType: Int) {
        val key = stringPreferencesKey(progressKey(bankId, categoryId, practiceType))
        dataStore.edit { prefs ->
            prefs.remove(key)
        }
    }
}
