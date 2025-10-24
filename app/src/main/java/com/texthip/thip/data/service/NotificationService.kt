package com.texthip.thip.data.service

import com.texthip.thip.data.model.notification.request.FcmTokenRequest
import com.texthip.thip.data.model.notification.request.FcmTokenDeleteRequest
import com.texthip.thip.data.model.notification.request.NotificationEnabledRequest
import com.texthip.thip.data.model.notification.request.NotificationCheckRequest
import com.texthip.thip.data.model.notification.response.NotificationEnabledResponse
import com.texthip.thip.data.model.notification.response.NotificationListResponse
import com.texthip.thip.data.model.notification.response.NotificationCheckResponse
import com.texthip.thip.data.model.notification.response.NotificationExistsUncheckedResponse
import com.texthip.thip.data.model.base.BaseResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationService {
    @POST("notifications/fcm-tokens")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest
    ): BaseResponse<Unit>

    @GET("users/notification-settings")
    suspend fun getNotificationEnableState(
        @Query("deviceId") deviceId: String
    ): BaseResponse<NotificationEnabledResponse>

    @PATCH("notifications/enable-state")
    suspend fun updateNotificationEnabled(
        @Body request: NotificationEnabledRequest
    ): BaseResponse<NotificationEnabledResponse>

    @DELETE("notifications/fcm-tokens")
    suspend fun deleteFcmToken(
        @Body request: FcmTokenDeleteRequest
    ): BaseResponse<Unit>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("cursor") cursor: String? = null,
        @Query("type") type: String? = null
    ): BaseResponse<NotificationListResponse>

    @POST("notifications/check")
    suspend fun checkNotification(
        @Body request: NotificationCheckRequest
    ): BaseResponse<NotificationCheckResponse>

    @GET("notifications/exists-unchecked")
    suspend fun existsUncheckedNotifications(): BaseResponse<NotificationExistsUncheckedResponse>
}