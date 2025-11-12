package com.texthip.thip.data.model.notification.response

import kotlinx.serialization.Serializable

@Serializable
data class NotificationEnabledResponse(
    val isEnabled: Boolean
)