package com.texthip.thip.data.manager

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.messaging.FirebaseMessaging
import com.texthip.thip.data.repository.NotificationRepository
import com.texthip.thip.utils.auth.getAppScopeDeviceId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val notificationRepository: NotificationRepository,
    @param:ApplicationContext private val context: Context
) {

    companion object {
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    }

    suspend fun handleNewToken(newToken: String) {
        val storedToken = getFcmTokenOnce()

        if (storedToken != newToken) {
            Log.d("FCM", "Token updated")

            saveFcmToken(newToken)
            sendTokenToServer(newToken)
        }
    }

    suspend fun sendCurrentTokenIfExists() {
        val storedFcmToken = getFcmTokenOnce()

        if (storedFcmToken != null) {
            sendTokenToServer(storedFcmToken)
        } else {
            // 저장된 토큰이 없으면 Firebase에서 직접 가져와서 저장하고 전송
            try {
                val token = fetchCurrentToken()
                saveFcmToken(token)
                sendTokenToServer(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to fetch and send current token", e)
            }
        }
    }

    private suspend fun fetchCurrentToken(): String = suspendCancellableCoroutine { continuation ->
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        val token = task.result
                        if (token != null) {
                            continuation.resume(token)
                        } else {
                            continuation.resumeWithException(IllegalStateException("FCM token is null"))
                        }
                    }
                    else -> {
                        val exception = task.exception ?: Exception("Unknown error fetching FCM token")
                        Log.w("FCM", "Failed to fetch token", exception)
                        continuation.resumeWithException(exception)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error fetching FCM token", e)
            continuation.resumeWithException(e)
        }
    }

    // FCM 토큰 로컬 저장 관리
    private suspend fun saveFcmToken(token: String) {
        dataStore.edit { prefs -> prefs[FCM_TOKEN_KEY] = token }
    }

    private suspend fun getFcmTokenOnce(): String? {
        return dataStore.data.map { prefs -> prefs[FCM_TOKEN_KEY] }.first()
    }

    private suspend fun sendTokenToServer(token: String) {
        val deviceId = context.getAppScopeDeviceId()
        notificationRepository.registerFcmToken(deviceId, token)
            .onSuccess {
                Log.d("FCM", "Token sent successfully")
            }
            .onFailure { exception ->
                Log.e("FCM", "Failed to send token", exception)
            }
    }
}