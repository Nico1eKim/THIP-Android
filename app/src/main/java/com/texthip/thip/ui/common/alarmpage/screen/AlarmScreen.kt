package com.texthip.thip.ui.common.alarmpage.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.texthip.thip.R
import com.texthip.thip.data.model.notification.response.NotificationCheckResponse
import com.texthip.thip.data.model.notification.response.NotificationResponse
import com.texthip.thip.ui.common.alarmpage.component.AlarmFilterRow
import com.texthip.thip.ui.common.alarmpage.component.CardAlarm
import com.texthip.thip.ui.common.alarmpage.mock.NotificationType
import com.texthip.thip.ui.common.alarmpage.viewmodel.AlarmUiState
import com.texthip.thip.ui.common.alarmpage.viewmodel.AlarmViewModel
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    onNavigateBack: () -> Unit = {},
    onNotificationNavigation: (NotificationCheckResponse) -> Unit = {},
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.refreshData()
    }

    AlarmContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRefresh = { viewModel.refreshData() },
        onLoadMore = { viewModel.loadMoreNotifications() },
        onChangeNotificationType = { viewModel.changeNotificationType(it) },
        onNotificationClick = { notificationId ->
            viewModel.checkNotification(notificationId) { response ->
                onNotificationNavigation(response)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmContent(
    uiState: AlarmUiState,
    onNavigateBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onChangeNotificationType: (NotificationType) -> Unit = {},
    onNotificationClick: (Int) -> Unit = {}
) {
    val listState = rememberLazyListState()

    // 무한 스크롤 로직
    val shouldLoadMore by remember(uiState.canLoadMore, uiState.isLoadingMore) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            uiState.canLoadMore && !uiState.isLoadingMore && totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    // 필터 상태 매핑
    val selectedStates = remember(uiState.currentNotificationType) {
        when (uiState.currentNotificationType) {
            NotificationType.FEED -> booleanArrayOf(true, false)
            NotificationType.ROOM -> booleanArrayOf(false, true)
            else -> booleanArrayOf(false, false) // FEED_AND_ROOM
        }
    }

    Column(
        Modifier
            .fillMaxSize()
    ) {
        DefaultTopAppBar(
            title = stringResource(R.string.alarm_string),
            onLeftClick = onNavigateBack,
        )

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                AlarmFilterRow(
                    selectedStates = selectedStates,
                    onToggle = { idx ->
                        val newNotificationType = when {
                            // 피드 버튼을 눌렀을 때
                            idx == 0 -> {
                                if (selectedStates[0]) {
                                    // 이미 선택된 상태면 전체로 변경
                                    NotificationType.FEED_AND_ROOM
                                } else {
                                    // 선택되지 않은 상태면 피드만
                                    NotificationType.FEED
                                }
                            }
                            // 모임 버튼을 눌렀을 때  
                            idx == 1 -> {
                                if (selectedStates[1]) {
                                    NotificationType.FEED_AND_ROOM
                                } else {
                                    NotificationType.ROOM
                                }
                            }

                            else -> NotificationType.FEED_AND_ROOM
                        }
                        onChangeNotificationType(newNotificationType)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.notifications.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.notifications, key = { it.notificationId }) { notification ->
                            CardAlarm(
                                badgeText = notification.notificationType,
                                title = removeBracketPrefix(notification.title),
                                message = notification.content,
                                timeAgo = notification.postDate,
                                isRead = notification.isChecked,
                                onClick = {
                                    onNotificationClick(notification.notificationId)
                                }
                            )
                        }
                    }
                } else if (!uiState.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.alarm_notification_comment),
                            style = typography.smalltitle_sb600_s18_h24,
                            color = colors.White
                        )
                    }
                }
            }
        }
    }
}

private fun removeBracketPrefix(title: String): String {
    return title.replace(Regex("^\\[.*?\\]\\s*"), "").trim()
}


@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    ThipTheme {
        AlarmContent(
            uiState = AlarmUiState(
                notifications = listOf(
                    NotificationResponse(
                        notificationId = 1,
                        title = "[피드] 내 글을 좋아합니다.",
                        content = "user123님이 내 글에 좋아요를 눌렀어요.",
                        isChecked = false,
                        notificationType = "피드",
                        postDate = "2시간 전"
                    ),
                    NotificationResponse(
                        notificationId = 2,
                        title = "[모임] 같이 읽기를 시작했어요!",
                        content = "모임방에서 20분 동안 같이 읽기가 시작되었어요!",
                        isChecked = false,
                        notificationType = "모임",
                        postDate = "7시간 전"
                    ),
                    NotificationResponse(
                        notificationId = 3,
                        title = "[모임] 투표가 시작되었어요!",
                        content = "투표지를 먼저 열람합니다.",
                        isChecked = true,
                        notificationType = "모임",
                        postDate = "17시간 전"
                    )
                ),
                currentNotificationType = NotificationType.FEED_AND_ROOM,
                isLoading = false,
                hasMore = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenEmptyPreview() {
    ThipTheme {
        AlarmContent(
            uiState = AlarmUiState(
                notifications = emptyList(),
                isLoading = false
            )
        )
    }
}