package com.texthip.thip.data.model.rooms.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RoomMainResponse(
    @SerialName("roomId") val roomId: Int,
    @SerialName("bookImageUrl") val bookImageUrl: String?,
    @SerialName("roomName") val roomName: String,
    @SerialName("recruitCount") val recruitCount: Int,
    @SerialName("memberCount") val memberCount: Int,
    @SerialName("deadlineDate") val deadlineDate: String
)

@Serializable
data class RoomMainList(
    @SerialName("deadlineRoomList") val deadlineRoomList: List<RoomMainResponse> = emptyList(),
    @SerialName("popularRoomList") val popularRoomList: List<RoomMainResponse> = emptyList(),
    @SerialName("recentRoomList") val recentRoomList: List<RoomMainResponse> = emptyList()
)