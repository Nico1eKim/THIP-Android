package com.texthip.thip.ui.group.room.screen

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.texthip.thip.R
import com.texthip.thip.data.model.rooms.response.CurrentVote
import com.texthip.thip.data.model.rooms.response.RoomsPlayingResponse
import com.texthip.thip.ui.common.bottomsheet.MenuBottomSheet
import com.texthip.thip.ui.common.modal.DialogPopup
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.GradationTopAppBar
import com.texthip.thip.ui.group.room.component.GroupRoomBody
import com.texthip.thip.ui.group.room.component.GroupRoomHeader
import com.texthip.thip.ui.group.room.mock.MenuBottomSheetItem
import com.texthip.thip.ui.group.room.viewmodel.GroupRoomEvent
import com.texthip.thip.ui.group.room.viewmodel.GroupRoomToastType
import com.texthip.thip.ui.group.room.viewmodel.GroupRoomUiState
import com.texthip.thip.ui.group.room.viewmodel.GroupRoomViewModel
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.utils.type.GenreBackgroundImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GroupRoomScreen(
    roomId: Int,
    isExpired: Boolean = false,
    onBackClick: () -> Unit = {},
    onNavigateToMates: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToNote: (page: Int?, isOverview: Boolean?) -> Unit = { _, _ -> },
    onNavigateToBookDetail: (isbn: String) -> Unit = {},
    viewModel: GroupRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeToast by remember { mutableStateOf<GroupRoomToastType?>(null) }

    // 화면이 처음 그려질 때 데이터 로딩 실행
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchRoomsPlaying(roomId)
    }

    LaunchedEffect(key1 = viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is GroupRoomEvent.ShowToast -> {
                    activeToast = event.type
                }
            }
        }
    }

    // UI 상태에 따라 다른 화면을 보여줌
    when (val state = uiState) {
        is GroupRoomUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is GroupRoomUiState.Success -> {
            // 성공 시, 실제 데이터를 화면에 표시
            GroupRoomContent(
                roomDetails = state.roomsPlaying,
                isExpired = isExpired,
                onBackClick = onBackClick,
                onNavigateToMates = onNavigateToMates,
                onNavigateToChat = onNavigateToChat,
                onNavigateToNote = onNavigateToNote,
                onNavigateToBookDetail = onNavigateToBookDetail,
                onLeaveRoomConfirm = { viewModel.leaveRoom(roomId) },
                activeToast = activeToast,
                onDismissToast = { activeToast = null }
            )
        }

        is GroupRoomUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = colors.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupRoomContent(
    roomDetails: RoomsPlayingResponse,
    isExpired: Boolean = false,
    onBackClick: () -> Unit = {},
    onNavigateToMates: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToNote: (page: Int?, isOverview: Boolean?) -> Unit = { _, _ -> },
    onNavigateToBookDetail: (isbn: String) -> Unit = {},
    onLeaveRoomConfirm: () -> Unit = {},
    activeToast: GroupRoomToastType?,
    onDismissToast: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var isLeaveDialogVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    val isOwner = roomDetails.isHost

    val imageModel = remember(roomDetails.roomImageUrl) {
        // 서버에서 받은 문자열이 "_image"로 끝나면 Enum에서 로컬 리소스를 찾음
        if (roomDetails.roomImageUrl.endsWith("_image")) {
            GenreBackgroundImage.fromServerValue(roomDetails.roomImageUrl).imageResId
        }
        // 그렇지 않으면 URL로 그대로 사용
        else {
            roomDetails.roomImageUrl
        }
    }

    Box(
        if (isBottomSheetVisible || isLeaveDialogVisible || isDeleteDialogVisible) {
            Modifier
                .fillMaxSize()
                .blur(5.dp)
        } else {
            Modifier.fillMaxSize()
        }
    ) {
        Box(modifier = Modifier.verticalScroll(scrollState)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(336.dp)
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0594f to Color.Transparent,
                                    0.94f to colors.Black
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp, bottom = 20.dp)
            ) {
                GroupRoomHeader(
                    roomName = roomDetails.roomName,
                    roomDescription = roomDetails.roomDescription,
                    isPublic = roomDetails.isPublic,
                    progressStartDate = roomDetails.progressStartDate,
                    progressEndDate = roomDetails.progressEndDate,
                    memberCount = roomDetails.memberCount,
                    category = roomDetails.category,
                    categoryColor = roomDetails.categoryColor,
                ) {
                    onNavigateToMates()
                }

                Spacer(Modifier.height(30.dp))

                GroupRoomBody(
                    bookTitle = roomDetails.bookTitle,
                    authorName = roomDetails.authorName,
                    isbn = roomDetails.isbn,
                    currentPage = roomDetails.currentPage,
                    userPercentage = roomDetails.userPercentage,
                    currentVotes = roomDetails.currentVotes,
                    onNavigateToBookDetail = onNavigateToBookDetail,
                    // 일반 노트 카드 클릭 시 필터 없이 이동
                    onNavigateToNote = { onNavigateToNote(null, null) },
                    onNavigateToChat = onNavigateToChat,
                    // 투표 카드 클릭 시 필터 값과 함께 이동
                    onVoteClick = { vote: CurrentVote ->
                        if (vote.isOverview) {
                            onNavigateToNote(null, true)
                        } else {
                            onNavigateToNote(vote.page, false)
                        }
                    }
                )
            }
        }

        GradationTopAppBar(
            onLeftClick = onBackClick,
//            isRightIconVisible = true,
            isRightIconVisible = !isExpired || !isOwner, // TODO: 방 삭제 기능 추가 시 변경
            onRightClick = { isBottomSheetVisible = true },
        )

        AnimatedVisibility(
            visible = activeToast != null,
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
                .zIndex(3f)
        ) {
            LaunchedEffect(activeToast) {
                if (activeToast != null) {
                    delay(3000L)
                    if (activeToast == GroupRoomToastType.LEAVE_ROOM_SUCCESS) {
                        onBackClick()
                    }
                    onDismissToast()
                }
            }

            when (activeToast) {
                GroupRoomToastType.LEAVE_ROOM_SUCCESS -> {
                    ToastWithDate(message = stringResource(R.string.leave_room_toast))
                }

                GroupRoomToastType.ACTION_FAILURE -> {
                    ToastWithDate(
                        message = stringResource(R.string.leave_room_fail_toast),
                        color = colors.Red
                    )
                }

                null -> {}
            }
        }
    }

    if (isBottomSheetVisible) {
        val menuItems = if (isOwner) {
            // 방 주인일 때
            listOf(
                MenuBottomSheetItem(
                    text = stringResource(R.string.delete_room),
                    color = colors.Red,
                    onClick = { isDeleteDialogVisible = true }
                )
            )
        } else {
            // 방 참여자일 때
            listOf(
                MenuBottomSheetItem(
                    text = stringResource(R.string.leave_room),
                    color = colors.White,
                    onClick = { isLeaveDialogVisible = true }
                ),
                MenuBottomSheetItem(
                    text = stringResource(R.string.report_room),
                    color = colors.Red,
                    onClick = { /* 신고 로직 */ }
                )
            )
        }

        MenuBottomSheet(
            items = menuItems,
            onDismiss = { isBottomSheetVisible = false }
        )
    }

    if (isLeaveDialogVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DialogPopup(
                title = stringResource(R.string.leave_room_modal_title),
                description = stringResource(R.string.leave_room_modal_content),
                onConfirm = {
                    onLeaveRoomConfirm()
                    isLeaveDialogVisible = false
                },
                onCancel = {
                    isLeaveDialogVisible = false
                }
            )
        }
    }

    if (isDeleteDialogVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DialogPopup(
                title = stringResource(R.string.delete_room_modal_title),
                description = stringResource(R.string.delete_room_modal_content),
                onConfirm = {
                    // 방 삭제하기 로직
                    isDeleteDialogVisible = false
                },
                onCancel = {
                    isDeleteDialogVisible = false
                }
            )
        }
    }
}

@Preview
@Composable
private fun GroupRoomScreenPreview() {
    ThipTheme {
        GroupRoomContent(
            roomDetails = RoomsPlayingResponse(
                isHost = true,
                roomId = 1,
                roomName = "호르몬 체인지 완독하는 방",
                roomImageUrl = "문학_image",
                isPublic = false,
                progressStartDate = "2023.10.01",
                progressEndDate = "2023.10.31",
                category = "문학",
                categoryColor = "#A0F8E8",
                roomDescription = "‘시집만 읽는 사람들’ 3월 모임입니다.",
                memberCount = 22,
                recruitCount = 30,
                isbn = "12345",
                bookTitle = "호르몬 체인지",
                authorName = "최정화",
                currentPage = 100,
                userPercentage = 5,
                currentVotes = emptyList()
            ),
            onBackClick = {},
            activeToast = null
        )
    }
}