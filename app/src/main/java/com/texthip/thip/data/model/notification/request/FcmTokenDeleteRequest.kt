package com.texthip.thip.data.model.notification.request

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenDeleteRequest(
    val deviceId: String
)