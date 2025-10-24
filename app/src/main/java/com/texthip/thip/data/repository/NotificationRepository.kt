package com.texthip.thip.data.repository

import android.content.Context
import com.texthip.thip.data.model.base.handleBaseResponse
import com.texthip.thip.data.model.notification.request.FcmTokenRequest
import com.texthip.thip.data.model.notification.request.FcmTokenDeleteRequest
import com.texthip.thip.data.model.notification.request.NotificationEnabledRequest
import com.texthip.thip.data.model.notification.request.NotificationCheckRequest
import com.texthip.thip.data.model.notification.response.NotificationEnabledResponse
import com.texthip.thip.data.model.notification.response.NotificationListResponse
import com.texthip.thip.data.model.notification.response.NotificationCheckResponse
import com.texthip.thip.data.model.notification.response.NotificationExistsUncheckedResponse
import com.texthip.thip.data.service.NotificationService
import com.texthip.thip.utils.auth.getAppScopeDeviceId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationService: NotificationService,
    @param:ApplicationContext private val context: Context
) {
    private val _notificationUpdateFlow = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val notificationUpdateFlow: SharedFlow<Int> = _notificationUpdateFlow.asSharedFlow()

    private val _notificationRefreshFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val notificationRefreshFlow: SharedFlow<Unit> = _notificationRefreshFlow.asSharedFlow()
    suspend fun registerFcmToken(
        deviceId: String,
        fcmToken: String
    ): Result<Unit?> {
        return runCatching {
            val request = FcmTokenRequest(
                deviceId = deviceId,
                fcmToken = fcmToken,
                platformType = "ANDROID"
            )
            val response = notificationService.registerFcmToken(request)
            response.handleBaseResponse().getOrNull()
        }
    }

    suspend fun getNotificationEnableState(): Result<NotificationEnabledResponse?> {
        return runCatching {
            val deviceId = context.getAppScopeDeviceId()
            val response = notificationService.getNotificationEnableState(deviceId)
            response.handleBaseResponse().getOrNull()
        }
    }

    suspend fun updateNotificationEnabled(enabled: Boolean): Result<NotificationEnabledResponse?> {
        return runCatching {
            val deviceId = context.getAppScopeDeviceId()
            val request = NotificationEnabledRequest(
                enable = enabled,
                deviceId = deviceId
            )
            val response = notificationService.updateNotificationEnabled(request)
            response.handleBaseResponse().getOrNull()
        }
    }

    suspend fun deleteFcmToken(): Result<Unit?> {
        return runCatching {
            val deviceId = context.getAppScopeDeviceId()
            val request = FcmTokenDeleteRequest(deviceId = deviceId)
            val response = notificationService.deleteFcmToken(request)
            response.handleBaseResponse().getOrNull()
        }
    }

    suspend fun getNotifications(
        cursor: String? = null,
        type: String? = null
    ): Result<NotificationListResponse?> {
        return runCatching {
            val response = notificationService.getNotifications(cursor, type)
            response.handleBaseResponse().getOrNull()
        }
    }

    suspend fun checkNotification(notificationId: Int): Result<NotificationCheckResponse?> {
        return runCatching {
            val request = NotificationCheckRequest(notificationId = notificationId)
            val response = notificationService.checkNotification(request)
            val result = response.handleBaseResponse().getOrNull()

            // 알림 읽기 성공 시 다른 ViewModel들에게 알림 (비차단 emit)
            if (result != null) {
                _notificationUpdateFlow.tryEmit(notificationId)
            }
            result
        }
    }

    fun onNotificationReceived() {
        _notificationRefreshFlow.tryEmit(Unit)
    }

    suspend fun existsUncheckedNotifications(): Result<NotificationExistsUncheckedResponse?> {
        return runCatching {
            val response = notificationService.existsUncheckedNotifications()
            response.handleBaseResponse().getOrNull()
        }
    }
}