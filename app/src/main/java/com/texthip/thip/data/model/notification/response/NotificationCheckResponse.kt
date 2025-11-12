package com.texthip.thip.data.model.notification.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class NotificationCheckResponse(
    @SerialName("route") val route: NotificationRoute,
    @SerialName("params") val params: Map<String, JsonElement>
)

@Serializable
enum class NotificationRoute {
    @SerialName("FEED_USER")
    FEED_USER,

    @SerialName("FEED_DETAIL")
    FEED_DETAIL,

    @SerialName("ROOM_MAIN")
    ROOM_MAIN,

    @SerialName("ROOM_DETAIL")
    ROOM_DETAIL,

    @SerialName("ROOM_POST_DETAIL")
    ROOM_POST_DETAIL
}