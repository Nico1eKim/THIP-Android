package com.texthip.thip.data.model.notification.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationCheckRequest(
    @SerialName("notificationId") val notificationId: Int
)