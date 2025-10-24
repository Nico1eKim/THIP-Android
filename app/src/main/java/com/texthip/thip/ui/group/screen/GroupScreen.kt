package com.texthip.thip.ui.group.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.texthip.thip.R
import com.texthip.thip.data.model.rooms.response.JoinedRoomResponse
import com.texthip.thip.data.model.rooms.response.RoomMainList
import com.texthip.thip.data.model.rooms.response.RoomMainResponse
import com.texthip.thip.ui.common.alarmpage.viewmodel.AlarmViewModel
import com.texthip.thip.ui.common.buttons.FloatingButton
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.LogoTopAppBar
import com.texthip.thip.ui.feed.component.EmptyMySubscriptionBar
import com.texthip.thip.ui.group.myroom.component.GroupMySectionHeader
import com.texthip.thip.ui.group.myroom.component.GroupPager
import com.texthip.thip.ui.group.myroom.component.GroupRoomDeadlineSection
import com.texthip.thip.ui.group.myroom.component.GroupSearchTextField
import com.texthip.thip.ui.group.viewmodel.GroupUiState
import com.texthip.thip.ui.group.viewmodel.GroupViewModel
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    onNavigateToMakeRoom: () -> Unit = {},
    onNavigateToGroupDone: () -> Unit = {}, // 완료된 화면으로 이동
    onNavigateToAlarm: () -> Unit = {}, // 알림 화면으로 이동
    onNavigateToGroupSearch: () -> Unit = {},   // 검색 화면으로 이동
    onNavigateToGroupMy: () -> Unit = {},   // 내 모임방 화면으로 이동
    onNavigateToGroupRecruit: (Int) -> Unit = {},   // 모집 중인 모임방 화면으로 이동
    onNavigateToGroupRoom: (Int) -> Unit = {},  // 기록장 화면으로 이동
    onNavigateToGroupSearchAllRooms: () -> Unit = {},
    viewModel: GroupViewModel = hiltViewModel(),
    alarmViewModel: AlarmViewModel = hiltViewModel()
) {
    // 화면 재진입 시 데이터 새로고침
    LaunchedEffect(Unit) {
        viewModel.resetToInitialState()
        alarmViewModel.checkUnreadNotifications()
    }
    val uiState by viewModel.uiState.collectAsState()
    val alarmUiState by alarmViewModel.uiState.collectAsState()

    GroupContent(
        uiState = uiState,
        hasUnreadNotifications = alarmUiState.hasUnreadNotifications,
        onNavigateToMakeRoom = onNavigateToMakeRoom,
        onNavigateToGroupDone = onNavigateToGroupDone,
        onNavigateToAlarm = onNavigateToAlarm,
        onNavigateToGroupSearch = onNavigateToGroupSearch,
        onNavigateToGroupMy = onNavigateToGroupMy,
        onNavigateToGroupRecruit = onNavigateToGroupRecruit,
        onNavigateToGroupRoom = onNavigateToGroupRoom,
        onNavigateToGroupSearchAllRooms = onNavigateToGroupSearchAllRooms,
        onRefreshGroupData = {
            viewModel.refreshGroupData()
            alarmViewModel.checkUnreadNotifications()
        },
        onCardVisible = { cardIndex -> viewModel.loadMoreGroups() },
        onSelectGenre = { genreIndex -> viewModel.selectGenre(genreIndex) },
        onHideToast = { viewModel.hideToast() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupContent(
    uiState: GroupUiState,
    hasUnreadNotifications: Boolean = false,
    onNavigateToMakeRoom: () -> Unit = {},
    onNavigateToGroupDone: () -> Unit = {},
    onNavigateToAlarm: () -> Unit = {},
    onNavigateToGroupSearch: () -> Unit = {},
    onNavigateToGroupMy: () -> Unit = {},
    onNavigateToGroupRecruit: (Int) -> Unit = {},
    onNavigateToGroupRoom: (Int) -> Unit = {},
    onNavigateToGroupSearchAllRooms: () -> Unit = {},
    onRefreshGroupData: () -> Unit = {},
    onCardVisible: (Int) -> Unit = {},
    onSelectGenre: (Int) -> Unit = {},
    onHideToast: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // 탭 전환 시 스크롤을 맨 위로 초기화
    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefreshGroupData,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                // 검색창
                GroupSearchTextField(
                    modifier = Modifier.padding(top = 75.dp, bottom = 32.dp),
                    onClick = onNavigateToGroupSearch
                )

                // 내 모임방 헤더 + 카드
                GroupMySectionHeader(
                    onClick = onNavigateToGroupMy
                )
                Spacer(Modifier.height(20.dp))

                GroupPager(
                    groupCards = uiState.myJoinedRooms,
                    userName = uiState.userName,
                    onCardClick = { joinedRoom ->
                        if (joinedRoom.deadlineDate == null) {
                            // 시작 후
                            onNavigateToGroupRoom(joinedRoom.roomId)
                        } else {
                            // 시작 전
                            onNavigateToGroupRecruit(joinedRoom.roomId)
                        }
                    },
                    onCardVisible = onCardVisible
                )
                Spacer(Modifier.height(32.dp))

                Spacer(
                    Modifier
                        .padding(bottom = 32.dp)
                        .height(10.dp)
                        .fillMaxWidth()
                        .background(color = colors.DarkGrey02)
                )

                EmptyMySubscriptionBar(
                    modifier = Modifier.padding(horizontal = 30.dp),
                    text = stringResource(R.string.look_around_all_rooms),
                    onClick = onNavigateToGroupSearchAllRooms
                )

                Spacer(Modifier.height(32.dp))

                // 마감 임박한 독서 모임방
                GroupRoomDeadlineSection(
                    roomMainList = uiState.roomMainList,
                    selectedGenreIndex = uiState.selectedGenreIndex,
                    errorMessage = uiState.roomSectionsError,
                    onGenreSelect = onSelectGenre,
                    onRoomClick = { room ->
                        onNavigateToGroupRecruit(room.roomId)
                    }
                )
                Spacer(Modifier.height(102.dp))
            }
        }

        // 상단바
        LogoTopAppBar(
            leftIcon = painterResource(R.drawable.ic_done),
            hasNotification = hasUnreadNotifications,
            onLeftClick = onNavigateToGroupDone,
            onRightClick = onNavigateToAlarm
        )

        // 오른쪽 하단 FAB
        FloatingButton(
            icon = painterResource(id = R.drawable.ic_makegroup),
            onClick = onNavigateToMakeRoom
        )

        // 토스트 팝업
        AnimatedVisibility(
            visible = uiState.showToast,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 2000)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 2000)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .zIndex(2f)
        ) {
            ToastWithDate(
                message = uiState.toastMessage
            )
        }
    }

    // 토스트 3초 후 자동 숨김 - showToast가 true가 된 시점부터 카운트
    LaunchedEffect(uiState.showToast) {
        if (uiState.showToast) {
            delay(3000L)
            onHideToast()
        }
    }
}


@Preview
@Composable
fun PreviewGroupScreen() {
    ThipTheme {
        GroupContent(
            uiState = GroupUiState(
                userName = "김독서",
                myJoinedRooms = listOf(
                    JoinedRoomResponse(
                        roomId = 1,
                        bookImageUrl = "https://picsum.photos/300/400?joined1",
                        roomTitle = "미드나이트 라이브러리",
                        memberCount = 18,
                        userPercentage = 75
                    ),
                    JoinedRoomResponse(
                        roomId = 2,
                        bookImageUrl = "https://picsum.photos/300/400?joined2",
                        roomTitle = "코스모스",
                        memberCount = 25,
                        userPercentage = 42
                    ),
                    JoinedRoomResponse(
                        roomId = 3,
                        bookImageUrl = "https://picsum.photos/300/400?joined3",
                        roomTitle = "사피엔스",
                        memberCount = 15,
                        userPercentage = 88
                    )
                ),
                roomMainList = RoomMainList(
                    deadlineRoomList = listOf(
                        RoomMainResponse(
                            roomId = 4,
                            bookImageUrl = "https://picsum.photos/300/400?deadline1",
                            roomName = "🌙 미드나이트 라이브러리 함께읽기",
                            recruitCount = 20,
                            memberCount = 18,
                            deadlineDate = "D-2"
                        ),
                        RoomMainResponse(
                            roomId = 5,
                            bookImageUrl = "https://picsum.photos/300/400?deadline2",
                            roomName = "📚 현대문학 깊이 탐구하기",
                            recruitCount = 15,
                            memberCount = 12,
                            deadlineDate = "D-3"
                        ),
                        RoomMainResponse(
                            roomId = 6,
                            bookImageUrl = "https://picsum.photos/300/400?deadline3",
                            roomName = "🔬 과학책으로 세상 이해하기",
                            recruitCount = 30,
                            memberCount = 25,
                            deadlineDate = "D-5"
                        )
                    ),
                    popularRoomList = listOf(
                        RoomMainResponse(
                            roomId = 7,
                            bookImageUrl = "https://picsum.photos/300/400?popular1",
                            roomName = "✨ 철학 고전 함께 읽기",
                            recruitCount = 12,
                            memberCount = 10,
                            deadlineDate = "D-7"
                        ),
                        RoomMainResponse(
                            roomId = 8,
                            bookImageUrl = "https://picsum.photos/300/400?popular2",
                            roomName = "🎨 예술과 문학의 만남",
                            recruitCount = 20,
                            memberCount = 16,
                            deadlineDate = "D-10"
                        ),
                        RoomMainResponse(
                            roomId = 9,
                            bookImageUrl = "https://picsum.photos/300/400?popular3",
                            roomName = "💭 심리학 도서 탐험대",
                            recruitCount = 18,
                            memberCount = 14,
                            deadlineDate = "D-12"
                        )
                    )
                ),
                selectedGenreIndex = 2,
                isRefreshing = false,
                hasMoreMyGroups = true,
                isLoadingMoreMyGroups = false,
                roomSectionsError = null,
                showToast = false,
                toastMessage = ""
            )
        )
    }
}