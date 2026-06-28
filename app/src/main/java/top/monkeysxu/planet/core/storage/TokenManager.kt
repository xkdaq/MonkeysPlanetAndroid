package top.monkeysxu.planet.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import top.monkeysxu.planet.core.model.UserInfo

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "planet_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_USER_INFO = stringPreferencesKey("user_info")
    }

    private val gson = Gson()

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[KEY_TOKEN] }.first()
    }

    suspend fun saveUserInfo(userInfo: UserInfo) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_INFO] = gson.toJson(userInfo)
        }
    }

    suspend fun getUserInfo(): UserInfo? {
        val json = context.dataStore.data.map { it[KEY_USER_INFO] }.first()
        return if (!json.isNullOrEmpty()) {
            gson.fromJson(json, UserInfo::class.java)
        } else null
    }

    suspend fun clearLoginState() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_INFO)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }
}
