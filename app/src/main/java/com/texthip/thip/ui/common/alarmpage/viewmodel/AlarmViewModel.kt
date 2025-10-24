package com.texthip.thip.ui.common.alarmpage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.texthip.thip.data.repository.NotificationRepository
import com.texthip.thip.data.model.notification.response.NotificationCheckResponse
import com.texthip.thip.ui.common.alarmpage.mock.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private var nextCursor: String? = null
    private var isLastPage = false
    private var isLoadingData = false
    private var loadJob: Job? = null

    private fun updateState(update: (AlarmUiState) -> AlarmUiState) {
        _uiState.value = update(_uiState.value)
    }

    init {
        checkUnreadNotifications()

        // Repository의 알림 업데이트 이벤트 구독
        viewModelScope.launch {
            repository.notificationUpdateFlow.collect { notificationId ->
                updateNotificationAsRead(notificationId)
                checkUnreadNotifications()
            }
        }

        // 푸시 알림 도착 시 아이콘 상태만 갱신
        viewModelScope.launch {
            repository.notificationRefreshFlow.collect {
                checkUnreadNotifications()
            }
        }
    }

    fun loadNotifications(reset: Boolean = false) {
        // reset 시 기존 작업 취소
        if (reset) {
            loadJob?.cancel()
            loadJob = null
        }

        // 중복 로드 방지 (reset이 아닌 경우에만)
        if (isLoadingData && !reset) return
        if (isLastPage && !reset) return

        // launch 전에 isLoadingData 선반영 (플리커 방지)
        isLoadingData = true

        // UI 상태 즉시 반영
        if (reset) {
            updateState {
                it.copy(
                    isLoading = true,
                    notifications = emptyList(),
                    hasMore = true
                )
            }
            nextCursor = null
            isLastPage = false
        } else {
            updateState { it.copy(isLoadingMore = true) }
        }

        // 하나의 loadJob에 작업 바인딩
        val currentJob = viewModelScope.launch {
            try {
                val type =
                    if (uiState.value.currentNotificationType == NotificationType.FEED_AND_ROOM) {
                        null
                    } else {
                        uiState.value.currentNotificationType.value
                    }

                repository.getNotifications(nextCursor, type)
                    .onSuccess { notificationListResponse ->
                        notificationListResponse?.let { response ->
                            val currentList =
                                if (reset) emptyList() else uiState.value.notifications
                            updateState {
                                it.copy(
                                    notifications = currentList + response.notifications,
                                    error = null,
                                    hasMore = !response.isLast
                                )
                            }
                            nextCursor = response.nextCursor
                            isLastPage = response.isLast
                        } ?: run {
                            updateState { it.copy(hasMore = false) }
                            isLastPage = true
                        }
                    }
                    .onFailure { exception ->
                        updateState { it.copy(error = exception.message) }
                    }
            } finally {
                if (loadJob == coroutineContext[Job]) {
                    isLoadingData = false
                    updateState { it.copy(isLoading = false, isLoadingMore = false) }
                    loadJob = null
                }
            }
        }
        loadJob = currentJob
    }

    fun loadMoreNotifications() {
        loadNotifications(reset = false)
    }

    fun refreshData() {
        loadNotifications(reset = true)
        checkUnreadNotifications()
    }

    fun changeNotificationType(notificationType: NotificationType) {
        if (notificationType != uiState.value.currentNotificationType) {
            updateState { it.copy(currentNotificationType = notificationType) }
            loadNotifications(reset = true)
        }
    }

    fun checkNotification(notificationId: Int, onNavigate: (NotificationCheckResponse) -> Unit) {
        viewModelScope.launch {
            repository.checkNotification(notificationId)
                .onSuccess { response ->
                    response?.let {
                        // 로컬 상태에서 해당 알림을 읽음으로 표시
                        updateNotificationAsRead(notificationId)
                        onNavigate(it)
                    }
                }
                .onFailure { exception ->
                    updateState { it.copy(error = exception.message) }
                }
        }
    }

    private fun updateNotificationAsRead(notificationId: Int) {
        val updatedNotifications = uiState.value.notifications.map { notification ->
            if (notification.notificationId == notificationId) {
                notification.copy(isChecked = true)
            } else {
                notification
            }
        }
        updateState { it.copy(notifications = updatedNotifications) }
    }

    fun checkUnreadNotifications() {
        viewModelScope.launch {
            repository.existsUncheckedNotifications()
                .onSuccess { response ->
                    response?.let {
                        updateState { state ->
                            state.copy(hasUnreadNotifications = it.exists)
                        }
                    }
                }
                .onFailure { exception ->
                    // 에러 발생 시 기존 상태 유지 (로그만 남김)
                    updateState { it.copy(error = exception.message) }
                }
        }
    }
}