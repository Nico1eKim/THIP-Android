package com.texthip.thip.data.model.notification.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationExistsUncheckedResponse(
    @SerialName("exists") val exists: Boolean
)