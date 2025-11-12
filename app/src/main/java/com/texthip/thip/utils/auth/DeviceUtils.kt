package com.texthip.thip.utils.auth

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

private val Context.appDeviceIdDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_device_id")
private val APP_DEVICE_ID_KEY = stringPreferencesKey("app_device_id")

/**
 * 앱-스코프 디바이스 식별자를 가져옵니다.
 * ⚠️ 주의: 이 ID는 앱 재설치 시마다 변경됩니다 (기기별로 영구적이지 않음)
 * 1차: Firebase Installations ID (FID) - Google 권장 앱-스코프 식별자
 * 2차: 로컬 저장된 UUID - FID 실패 시 백업
 * 3차: 새 UUID 생성 후 저장 - 최초 실행 시
 */
suspend fun Context.getAppScopeDeviceId(): String {
    return try {
        // 1차: Firebase Installations ID 시도
        getFirebaseInstallationId()
    } catch (e: Exception) {
        Log.w("DeviceUtils", "Failed to get Firebase Installation ID, using local UUID", e)
        // 2차/3차: 로컬 UUID 사용 (없으면 생성)
        getOrCreateLocalDeviceId()
    }
}

/**
 * Firebase Installations ID (FID) 가져오기
 * - 앱 재설치 시 새로 생성됨 (프라이버시 친화적)
 * - Google Play 서비스가 있는 기기에서 사용 가능
 */
private suspend fun getFirebaseInstallationId(): String {
    return try {
        FirebaseInstallations.getInstance().id.await()
    } catch (e: Exception) {
        Log.e("DeviceUtils", "Failed to get FID", e)
        throw e
    }
}

/**
 * 로컬 저장된 UUID 가져오기 또는 새로 생성
 * - 앱 데이터 삭제 시까지 유지됨
 * - FID 백업용으로 사용
 */
private suspend fun Context.getOrCreateLocalDeviceId(): String {
    // 기존 저장된 ID 확인
    val existingId = appDeviceIdDataStore.data
        .map { preferences -> preferences[APP_DEVICE_ID_KEY] }
        .first()

    return existingId ?: run {
        // 새 UUID 생성 후 저장
        val newId = UUID.randomUUID().toString()
        appDeviceIdDataStore.edit { preferences ->
            preferences[APP_DEVICE_ID_KEY] = newId
        }
        Log.i("DeviceUtils", "Generated new app-scope device ID")
        newId
    }
}

/**
 * 기존 getAndroidDeviceId() 호환성을 위한 래퍼 함수
 * @deprecated ANDROID_ID 사용 중단으로 인해 getAppScopeDeviceId() 사용 권장
 */
@Deprecated(
    message = "Use getAppScopeDeviceId() instead for privacy compliance",
    replaceWith = ReplaceWith("getAppScopeDeviceId()"),
    level = DeprecationLevel.WARNING
)
suspend fun Context.getAndroidDeviceId(): String {
    return getAppScopeDeviceId()
}

/**
 * 앱-스코프 디바이스 ID와 관련된 모든 로컬 데이터를 삭제합니다.
 * 회원 탈퇴 시 호출하여 개인정보를 완전히 정리합니다.
 */
suspend fun Context.clearAppScopeDeviceData() {
    try {
        appDeviceIdDataStore.edit { preferences ->
            preferences.clear()
        }
        Log.i("DeviceUtils", "App-scope device data cleared successfully")
    } catch (e: Exception) {
        Log.e("DeviceUtils", "Failed to clear app-scope device data", e)
    }
}