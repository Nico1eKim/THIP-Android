package com.texthip.thip.data.model.rooms.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class JoinedRoomListResponse(
    @SerialName("roomList") val roomList: List<JoinedRoomResponse>,
    @SerialName("nickname") val nickname: String,
    @SerialName("nextCursor") val nextCursor: String? = null,
    @SerialName("isLast") val isLast: Boolean
)

@Serializable
data class JoinedRoomResponse(
    @SerialName("roomId") val roomId: Int,
    @SerialName("bookImageUrl") val bookImageUrl: String?,
    @SerialName("roomTitle") val roomTitle: String,
    @SerialName("memberCount") val memberCount: Int,
    @SerialName("userPercentage") val userPercentage: Int,
    @SerialName("deadlineDate") val deadlineDate: String? = null,
)

