package com.texthip.thip.ui.group.myroom.screen

import androidx.compose.foundation.background
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
import com.texthip.thip.data.model.rooms.response.MyRoomResponse
import com.texthip.thip.ui.common.cards.CardItemRoom
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.group.myroom.component.GroupMyRoomFilterRow
import com.texthip.thip.ui.group.myroom.mock.RoomType
import com.texthip.thip.ui.group.myroom.viewmodel.GroupMyUiState
import com.texthip.thip.ui.group.myroom.viewmodel.GroupMyViewModel
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import com.texthip.thip.utils.rooms.RoomUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMyScreen(
    onCardClick: (MyRoomResponse) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: GroupMyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.refreshData()
    }

    GroupMyContent(
        uiState = uiState,
        onCardClick = onCardClick,
        onNavigateBack = onNavigateBack,
        onRefresh = { viewModel.refreshData() },
        onLoadMore = { viewModel.loadMoreMyRooms() },
        onChangeRoomType = { viewModel.changeRoomType(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMyContent(
    uiState: GroupMyUiState,
    onCardClick: (MyRoomResponse) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onChangeRoomType: (RoomType) -> Unit = {}
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

    // Filter 상태를 
    val selectedStates = remember(uiState.currentRoomType) {
        when (uiState.currentRoomType) {
            RoomType.PLAYING -> booleanArrayOf(true, false, false) // 진행중
            RoomType.RECRUITING -> booleanArrayOf(false, true, false) // 모집중
            RoomType.EXPIRED -> booleanArrayOf(false, false, true) // 완료
            else -> booleanArrayOf(false, false, false) // 전체(아무것도 선택 안함)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
    ) {
        DefaultTopAppBar(
            title = stringResource(R.string.my_group_room),
            onLeftClick = onNavigateBack,
        )

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier
                    .background(colors.Black)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                GroupMyRoomFilterRow(
                    selectedStates = selectedStates,
                    onToggle = { idx ->
                        val newRoomType = when(idx) {
                            // 진행중 버튼을 눌렀을 때
                            0 -> {
                                if (selectedStates[0]) {
                                    // 이미 선택된 상태면 전체로 변경
                                    RoomType.ALL
                                } else {
                                    // 선택되지 않은 상태면 진행중만
                                    RoomType.PLAYING
                                }
                            }
                            // 모집중 버튼을 눌렀을 때  
                            1 -> {
                                if (selectedStates[1]) {
                                    RoomType.ALL
                                } else {
                                    RoomType.RECRUITING
                                }
                            }
                            // 완료 버튼을 눌렀을 때
                            2 -> {
                                if (selectedStates[2]) {
                                    RoomType.ALL
                                } else {
                                    RoomType.EXPIRED
                                }
                            }
                            else -> RoomType.ALL
                        }
                        onChangeRoomType(newRoomType)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.myRooms.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.myRooms) { room ->
                            CardItemRoom(
                                title = room.roomName,
                                participants = room.memberCount,
                                maxParticipants = room.recruitCount,
                                isRecruiting = RoomUtils.isRecruitingByType(room.type),
                                endDate = room.endDate,
                                imageUrl = room.bookImageUrl,
                                isSecret = !room.isPublic,
                                onClick = { onCardClick(room) }
                            )
                        }
                    }
                } else if (!uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.group_myroom_error_comment1),
                            color = colors.White,
                            style = typography.smalltitle_sb600_s18_h24
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.group_myroom_error_comment2),
                            color = colors.Grey,
                            style = typography.copy_r400_s14
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun GroupMyScreenPreview() {
    ThipTheme {
        GroupMyContent(
            uiState = GroupMyUiState(
                myRooms = listOf(
                    MyRoomResponse(
                        roomId = 1,
                        roomName = "🌙 미드나이트 라이브러리 함께읽기",
                        bookImageUrl = "https://picsum.photos/300/400?1",
                        memberCount = 18,
                        recruitCount = 20,
                        type = "RECRUITING",
                        endDate = "2025-02-15",
                        isPublic = true
                    ),
                    MyRoomResponse(
                        roomId = 2,
                        roomName = "📚 현대문학 깊이 탐구하기",
                        bookImageUrl = "https://picsum.photos/300/400?2",
                        memberCount = 12,
                        recruitCount = 15,
                        type = "PLAYING",
                        endDate = "2025-01-28",
                        isPublic = false
                    ),
                    MyRoomResponse(
                        roomId = 3,
                        roomName = "🔬 과학책으로 세상 이해하기",
                        bookImageUrl = "https://picsum.photos/300/400?3",
                        memberCount = 25,
                        recruitCount = 30,
                        type = "RECRUITING",
                        endDate = "2025-03-01",
                        isPublic = true
                    ),
                    MyRoomResponse(
                        roomId = 4,
                        roomName = "✨ 철학 고전 함께 읽기",
                        bookImageUrl = "https://picsum.photos/300/400?4",
                        memberCount = 8,
                        recruitCount = 12,
                        type = "PLAYING",
                        endDate = "2025-02-10",
                        isPublic = false
                    ),
                    MyRoomResponse(
                        roomId = 5,
                        roomName = "🎨 예술과 문학의 아름다운 만남",
                        bookImageUrl = "https://picsum.photos/300/400?5",
                        memberCount = 6,
                        recruitCount = 10,
                        type = "RECRUITING",
                        endDate = "2025-02-20",
                        isPublic = true
                    ),
                    MyRoomResponse(
                        roomId = 6,
                        roomName = "💭 심리학 도서 탐험대",
                        bookImageUrl = "https://picsum.photos/300/400?6",
                        memberCount = 14,
                        recruitCount = 18,
                        type = "PLAYING",
                        endDate = "2025-01-30",
                        isPublic = false
                    )
                ),
                currentRoomType = RoomType.ALL,
                isLoading = false,
                hasMore = true
            )
        )
    }
}
