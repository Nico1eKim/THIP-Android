package com.texthip.thip.data.model.notification.request

import kotlinx.serialization.Serializable

@Serializable
data class NotificationEnabledRequest(
    val enable: Boolean,
    val deviceId: String
)