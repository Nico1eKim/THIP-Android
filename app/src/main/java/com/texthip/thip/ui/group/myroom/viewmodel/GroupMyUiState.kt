package com.texthip.thip.ui.group.myroom.viewmodel

import com.texthip.thip.data.model.rooms.response.MyRoomResponse
import com.texthip.thip.ui.group.myroom.mock.RoomType

data class GroupMyUiState(
    val myRooms: List<MyRoomResponse> = emptyList(),
    val currentRoomType: RoomType = RoomType.ALL,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
) {
    val hasContent: Boolean get() = myRooms.isNotEmpty()
    val canLoadMore: Boolean get() = !isLoading && !isLoadingMore && hasMore
}