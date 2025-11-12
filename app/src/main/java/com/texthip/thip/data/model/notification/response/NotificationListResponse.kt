package com.texthip.thip.data.model.notification.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationListResponse(
    @SerialName("notifications") val notifications: List<NotificationResponse>,
    @SerialName("nextCursor") val nextCursor: String?,
    @SerialName("isLast") val isLast: Boolean
)

@Serializable
data class NotificationResponse(
    @SerialName("notificationId") val notificationId: Int,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("isChecked") val isChecked: Boolean,
    @SerialName("notificationType") val notificationType: String,
    @SerialName("postDate") val postDate: String
)