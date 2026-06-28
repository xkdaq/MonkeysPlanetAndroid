package top.monkeysxu.planet.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.viewLimitDataStore: DataStore<Preferences> by preferencesDataStore(name = "view_limit_prefs")

class ViewLimitStore(private val context: Context) {

    companion object {
        private const val MAX_VIEWS_PER_DAY = 10
        private val KEY_DATE = stringPreferencesKey("view_limit_date")
        private val KEY_COUNT = intPreferencesKey("view_count")
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun canViewToday(): Boolean {
        val today = getTodayString()
        val savedDate = context.viewLimitDataStore.data.map { it[KEY_DATE] }.first()

        if (savedDate != today) {
            // 新的一天，重置计数
            context.viewLimitDataStore.edit { prefs ->
                prefs[KEY_DATE] = today
                prefs[KEY_COUNT] = 0
            }
            return true
        }

        val count = context.viewLimitDataStore.data.map { it[KEY_COUNT] ?: 0 }.first()
        return count < MAX_VIEWS_PER_DAY
    }

    suspend fun recordView() {
        val today = getTodayString()
        val savedDate = context.viewLimitDataStore.data.map { it[KEY_DATE] }.first()

        if (savedDate != today) {
            context.viewLimitDataStore.edit { prefs ->
                prefs[KEY_DATE] = today
                prefs[KEY_COUNT] = 1
            }
        } else {
            val count = context.viewLimitDataStore.data.map { it[KEY_COUNT] ?: 0 }.first()
            context.viewLimitDataStore.edit { prefs ->
                prefs[KEY_COUNT] = count + 1
            }
        }
    }

    suspend fun getRemainingViews(): Int {
        val today = getTodayString()
        val savedDate = context.viewLimitDataStore.data.map { it[KEY_DATE] }.first()

        if (savedDate != today) {
            return MAX_VIEWS_PER_DAY
        }

        val count = context.viewLimitDataStore.data.map { it[KEY_COUNT] ?: 0 }.first()
        return (MAX_VIEWS_PER_DAY - count).coerceAtLeast(0)
    }
}
