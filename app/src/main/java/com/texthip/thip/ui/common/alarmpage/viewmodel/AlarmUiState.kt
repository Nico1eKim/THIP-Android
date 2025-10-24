package com.texthip.thip.ui.common.alarmpage.viewmodel

import com.texthip.thip.data.model.notification.response.NotificationResponse
import com.texthip.thip.ui.common.alarmpage.mock.NotificationType

data class AlarmUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val currentNotificationType: NotificationType = NotificationType.FEED_AND_ROOM,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val hasUnreadNotifications: Boolean = false  // API에서 가져온 읽지 않은 알림 존재 여부
) {
    val canLoadMore: Boolean get() = !isLoading && !isLoadingMore && hasMore
}