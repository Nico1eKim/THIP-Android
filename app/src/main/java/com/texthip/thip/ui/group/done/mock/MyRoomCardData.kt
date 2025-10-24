package com.texthip.thip.ui.group.done.mock

import com.texthip.thip.ui.group.myroom.mock.RoomType

data class MyRoomCardData(
    val roomId: Int,
    val bookImageUrl: String?,
    val roomName: String,
    val recruitCount: Int,
    val memberCount: Int,
    val endDate: String,
    val type: String
)

data class MyRoomsPaginationResult(
    val data: List<MyRoomCardData>,
    val nextCursor: String?,
    val isLast: Boolean
)

// 타입 기반 모집 상태 확인 함수
fun MyRoomCardData.isRecruitingByType(): Boolean {
    return when (type) {
        RoomType.RECRUITING.value -> true
        RoomType.ALL.value -> false
        RoomType.PLAYING.value -> false
        RoomType.EXPIRED.value -> false
        else -> false
    }
}

fun MyRoomCardData.getEndDateInDays(): Int {
    return when {
        endDate.contains("일 뒤") -> {
            endDate.replace("일 뒤", "").trim().toIntOrNull() ?: 0
        }
        else -> 0
    }
}