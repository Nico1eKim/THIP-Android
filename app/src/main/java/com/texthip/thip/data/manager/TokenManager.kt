package com.texthip.thip.data.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val APP_TOKEN_KEY = stringPreferencesKey("app_token") // 정식 액세스토큰
        private val TEMP_TOKEN_KEY = stringPreferencesKey("temp_token") // 임시 토큰
        //private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    // ====== 정식 토큰 ======
    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[APP_TOKEN_KEY] = token
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { prefs -> prefs[APP_TOKEN_KEY] }
    }

    suspend fun getTokenOnce(): String? {
        return dataStore.data.map { prefs -> prefs[APP_TOKEN_KEY] }.first()
    }

    suspend fun deleteToken() {
        dataStore.edit { prefs -> prefs.remove(APP_TOKEN_KEY) }
    }

    // ====== 임시 토큰 ======
    suspend fun saveTempToken(token: String) {
        dataStore.edit { prefs -> prefs[TEMP_TOKEN_KEY] = token }
    }

    suspend fun getTempTokenOnce(): String? {
        return dataStore.data.map { prefs -> prefs[TEMP_TOKEN_KEY] }.first()
    }

    suspend fun deleteTempToken() {
        dataStore.edit { prefs -> prefs.remove(TEMP_TOKEN_KEY) }
    }

    // ====== Refresh 토큰 (추후 확장용) ======
    /*suspend fun saveRefreshToken(token: String) {
        dataStore.edit { prefs -> prefs[REFRESH_TOKEN_KEY] = token }
    }

    suspend fun getRefreshTokenOnce(): String? {
        return dataStore.data.map { prefs -> prefs[REFRESH_TOKEN_KEY] }.first()
    }*/

    suspend fun clearTokens() {
        dataStore.edit { prefs -> prefs.clear() }
    }
}

