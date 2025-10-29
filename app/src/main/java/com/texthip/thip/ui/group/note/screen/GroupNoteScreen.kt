package com.texthip.thip.ui.group.note.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.texthip.thip.R
import com.texthip.thip.data.model.rooms.response.PostList
import com.texthip.thip.data.model.rooms.response.RoomsRecordsPinResponse
import com.texthip.thip.ui.common.bottomsheet.MenuBottomSheet
import com.texthip.thip.ui.common.buttons.ExpandableFloatingButton
import com.texthip.thip.ui.common.buttons.FabMenuItem
import com.texthip.thip.ui.common.buttons.FilterButton
import com.texthip.thip.ui.common.header.HeaderMenuBarTab
import com.texthip.thip.ui.common.modal.DialogPopup
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.feed.viewmodel.FeedViewModel
import com.texthip.thip.ui.group.note.component.CommentBottomSheet
import com.texthip.thip.ui.group.note.component.FilterHeaderSection
import com.texthip.thip.ui.group.note.component.TextCommentCard
import com.texthip.thip.ui.group.note.component.VoteCommentCard
import com.texthip.thip.ui.group.note.viewmodel.CommentsViewModel
import com.texthip.thip.ui.group.note.viewmodel.GroupNoteEvent
import com.texthip.thip.ui.group.note.viewmodel.GroupNoteSideEffect
import com.texthip.thip.ui.group.note.viewmodel.GroupNoteUiState
import com.texthip.thip.ui.group.note.viewmodel.GroupNoteViewModel
import com.texthip.thip.ui.group.room.mock.MenuBottomSheetItem
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import com.texthip.thip.utils.type.SortType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GroupNoteScreen(
    roomId: Int,
    isExpired: Boolean = false,
    onBackClick: () -> Unit = {},
    onCreateNoteClick: (recentPage: Int, totalPage: Int, isOverviewPossible: Boolean) -> Unit,
    onCreateVoteClick: (recentPage: Int, totalPage: Int, isOverviewPossible: Boolean) -> Unit,
    onNavigateToFeedWrite: (pinInfo: RoomsRecordsPinResponse, recordContent: String) -> Unit,
    onEditNoteClick: (post: PostList) -> Unit = {},
    onEditVoteClick: (post: PostList) -> Unit = {},
    onNavigateToUserProfile: (userId: Long) -> Unit = {},
    onNavigateToMyProfile: () -> Unit = {},
    onNavigateToAiReview: () -> Unit = {},
    resultTabIndex: Int? = null,
    onResultConsumed: () -> Unit = {},
    initialPage: Int? = null,
    initialIsOverview: Boolean? = null,
    initialPostId: Int? = null,
    openComments: Boolean = false,
    viewModel: GroupNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // FeedViewModel을 통해 현재 사용자 정보 가져오기
    val feedViewModel: FeedViewModel = hiltViewModel()
    val feedUiState by feedViewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId = feedUiState.myFeedInfo?.creatorId

    // 내 피드 정보가 없으면 로드
    LaunchedEffect(Unit) {
        if (feedUiState.myFeedInfo == null) {
            feedViewModel.onTabSelected(1)
        }
    }

    var showProgressBar by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    var progressJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(resultTabIndex) {
        if (resultTabIndex != null) {
            viewModel.onEvent(GroupNoteEvent.OnTabSelected(resultTabIndex))
            onResultConsumed()

            showProgressBar = true
            progress.snapTo(0f)
            progressJob = scope.launch {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                )
                delay(500)
                if (showProgressBar) {
                    showProgressBar = false
                }
            }
        }
    }

    LaunchedEffect(uiState.isLoading) {
        // 로딩이 끝났고, 프로그레스 바가 보이는 중이라면
        if (!uiState.isLoading && showProgressBar) {
            progressJob?.cancel() // 진행 중인 3초 애니메이션 취소
            progress.snapTo(1f) // 즉시 100%로 변경
            delay(500) // 100% 상태를 잠시 보여줌
            showProgressBar = false // 프로그레스 바 숨기기
        }
    }

    LaunchedEffect(key1 = roomId) {
        // 기록 생성 후 돌아온 경우가 아닐 때 (처음 진입 시) 초기화
        if (resultTabIndex == null) {
            viewModel.initialize(roomId, initialPage, initialIsOverview, initialPostId)
        }
    }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is GroupNoteSideEffect.NavigateToFeedWrite -> {
                    onNavigateToFeedWrite(effect.pinInfo, effect.recordContent)
                }
            }
        }
    }

    GroupNoteContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        isExpired = isExpired,
        onBackClick = onBackClick,
        onCreateNoteClick = {
            uiState.let { s ->
                onCreateNoteClick(s.recentBookPage, s.totalBookPage, s.isOverviewPossible)
            }
        },
        onCreateVoteClick = {
            uiState.let { s ->
                onCreateVoteClick(s.recentBookPage, s.totalBookPage, s.isOverviewPossible)
            }
        },
        onEditNoteClick = onEditNoteClick,
        onEditVoteClick = onEditVoteClick,
        onNavigateToUserProfile = { userId ->
            // 현재 사용자 ID와 비교하여 적절한 네비게이션 수행
            if (currentUserId != null && currentUserId == userId) {
                // 내 프로필로 이동
                onNavigateToMyProfile()
            } else {
                // 다른 사용자 프로필로 이동
                onNavigateToUserProfile(userId)
            }
        },
        onNavigateToAiReview = onNavigateToAiReview,
        showProgressBar = showProgressBar,
        progress = progress.value,
        openComments = openComments
    )
}

@Composable
fun GroupNoteContent(
    uiState: GroupNoteUiState,
    isExpired: Boolean = false,
    onEvent: (GroupNoteEvent) -> Unit,
    onBackClick: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onCreateVoteClick: () -> Unit,
    onEditNoteClick: (post: PostList) -> Unit,
    onEditVoteClick: (post: PostList) -> Unit,
    onNavigateToUserProfile: (userId: Long) -> Unit,
    onNavigateToAiReview: () -> Unit,
    showProgressBar: Boolean,
    progress: Float,
    openComments: Boolean = false
) {
    var isCommentBottomSheetVisible by remember { mutableStateOf(false) }
    var selectedPostForComment by remember { mutableStateOf<PostList?>(null) }
    var selectedPostForMenu by remember { mutableStateOf<PostList?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isPinDialogVisible by remember { mutableStateOf(false) }
    var postToPin by remember { mutableStateOf<PostList?>(null) }
    var showToast by remember { mutableStateOf(false) }
    var showAiReviewDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isOverlayVisible =
        isCommentBottomSheetVisible || selectedPostForMenu != null || isPinDialogVisible || showDeleteDialog || showAiReviewDialog
    var postToDelete by remember { mutableStateOf<PostList?>(null) }

    var toastMessage by remember { mutableStateOf("") }
    var isErrorToast by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val expiredRoomMessage = stringResource(R.string.expired_room_read_only_message)

    val commentsViewModel: CommentsViewModel = hiltViewModel()
    val commentsUiState by commentsViewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = isOverlayVisible) {
        if (isCommentBottomSheetVisible) {
            isCommentBottomSheetVisible = false
            selectedPostForComment = null
            onEvent(GroupNoteEvent.RefreshPosts)
        } else if (selectedPostForMenu != null) {
            selectedPostForMenu = null
        } else if (showDeleteDialog) {
            showDeleteDialog = false
            postToDelete = null
        } else if (isPinDialogVisible) {
            isPinDialogVisible = false
            postToPin = null
        }
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(3000)
            showToast = false
        }
    }

    val tabs = listOf(stringResource(R.string.group_record), stringResource(R.string.my_record))
    val sortDisplayStrings = remember { SortType.entries.map { it.displayNameRes } }
        .map { stringResource(it) }

    val listState = rememberLazyListState()

    LaunchedEffect(uiState.selectedTabIndex) {
        listState.scrollToItem(0)
    }

    val isScrolledToEnd by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            // 마지막 아이템이 보이고, 전체 아이템 수와 일치하며, 마지막 페이지가 아닐 때
            lastVisibleItem != null && lastVisibleItem.index == listState.layoutInfo.totalItemsCount - 1 && !uiState.isLastPage
        }
    }

    LaunchedEffect(isScrolledToEnd) {
        if (isScrolledToEnd) {
            onEvent(GroupNoteEvent.LoadMorePosts)
        }
    }

    // 특정 포스트로 스크롤
    LaunchedEffect(uiState.scrollToPostId, uiState.posts, uiState.isLoading) {
        val scrollToPostId = uiState.scrollToPostId

        if (scrollToPostId != null && uiState.posts.isNotEmpty() && !uiState.isLoading) {
            val targetIndex = uiState.posts.indexOfFirst { it.postId == scrollToPostId }

            if (targetIndex != -1) {
                val targetPost = uiState.posts[targetIndex]

                // 헤더 아이템들을 고려한 실제 인덱스 계산
                val actualIndex = if (uiState.selectedTabIndex == 0) {
                    targetIndex + 2 // 정보 텍스트 + 프로그레스바 아이템
                } else {
                    targetIndex + 1 // 프로그레스바 아이템만
                }

                // LazyColumn이 완전히 구성될 때까지 잠시 대기
                kotlinx.coroutines.delay(100)

                try {
                    listState.animateScrollToItem(actualIndex)

                    // openComments가 true이면 댓글 버텀시트를 자동으로 열기
                    if (openComments) {
                        kotlinx.coroutines.delay(200) // 스크롤 완료 후 잠시 대기
                        selectedPostForComment = targetPost
                        isCommentBottomSheetVisible = true
                    }
                } catch (e: Exception) {
                    // 애니메이션이 실패하면 일반 스크롤 시도
                    listState.scrollToItem(actualIndex)

                    // openComments가 true이면 댓글 버텀시트를 자동으로 열기
                    if (openComments) {
                        kotlinx.coroutines.delay(200) // 스크롤 완료 후 잠시 대기
                        selectedPostForComment = targetPost
                        isCommentBottomSheetVisible = true
                    }
                }

                onEvent(GroupNoteEvent.ClearScrollTarget)
            }
        }
    }

    Box(
        if (isOverlayVisible) {
            Modifier
                .fillMaxSize()
                .blur(5.dp)
        } else {
            Modifier.fillMaxSize()
        }
    ) {
        Box {
            Column(modifier = Modifier.fillMaxSize()) {
                DefaultTopAppBar(
                    title = stringResource(R.string.record_book),
                    onLeftClick = onBackClick
                )

                HeaderMenuBarTab(
                    titles = tabs,
                    selectedTabIndex = uiState.selectedTabIndex,
                    onTabSelected = { onEvent(GroupNoteEvent.OnTabSelected(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.posts.isEmpty()) {
                    val noRecordTextTitle = if (uiState.isOverview) {
                        stringResource(R.string.no_overviews_yet)
                    } else {
                        stringResource(R.string.no_records_yet)
                    }
                    val noRecordTextContent = when (uiState.selectedTabIndex) {
                        0 -> if (uiState.isOverview) {
                            stringResource(R.string.no_overview_subtext)
                        } else {
                            stringResource(R.string.no_group_record_subtext)
                        }

                        1 -> stringResource(R.string.no_my_record_subtext)
                        else -> ""
                    }
                    // 기록이 없을 때 중앙에 메시지
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 102.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp,
                            alignment = Alignment.CenterVertically
                        )
                    ) {
                        Text(
                            text = noRecordTextTitle,
                            style = typography.smalltitle_sb600_s18_h24,
                            color = colors.White
                        )
                        Text(
                            text = noRecordTextContent,
                            style = typography.copy_r400_s14,
                            color = colors.Grey
                        )
                    }
                } else {
                    // 피드 리스트 영역
                    LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                        if (uiState.selectedTabIndex == 0) {
                            item {
                                Row(
                                    modifier = Modifier.padding(
                                        top = 76.dp,
                                        start = 20.dp,
                                        end = 20.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_information),
                                        contentDescription = null,
                                        tint = colors.White,
                                    )
                                    Text(
                                        text = stringResource(R.string.group_note_info),
                                        modifier = Modifier.padding(start = 8.dp),
                                        color = colors.Grey01,
                                        style = typography.info_r400_s12
                                    )
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(visible = showProgressBar) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, top = 32.dp),
                                ) {
                                    Text(
                                        modifier = Modifier.padding(bottom = 12.dp),
                                        text = if (progress < 1.0f) {
                                            stringResource(R.string.posting_in_progress)
                                        } else {
                                            stringResource(R.string.posting_complete)
                                        },
                                        style = typography.view_m500_s14,
                                        color = colors.NeonGreen
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color = colors.Grey02) // 트랙(배경) 색상
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = progress)
                                                .fillMaxHeight()
                                                .background(
                                                    color = colors.NeonGreen,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        itemsIndexed(
                            uiState.posts,
                            key = { _, post -> post.postId }) { index, post ->
                            val itemModifier = if (index == uiState.posts.lastIndex) {
                                Modifier.padding(bottom = 20.dp)
                            } else {
                                Modifier
                            }

                            val showExpiredToast = {
                                toastMessage = expiredRoomMessage
                                showToast = true
                            }

                            when (post.postType) {
                                "RECORD" -> TextCommentCard(
                                    data = post,
                                    modifier = itemModifier,
                                    onCommentClick = {
                                        selectedPostForComment = post
                                        isCommentBottomSheetVisible = true
                                    },
                                    onLongPress = {
                                        if (isExpired) showExpiredToast() else {
                                            selectedPostForMenu = post
                                        }
                                    },
                                    onPinClick = {
                                        postToPin = post
                                        isPinDialogVisible = true
                                    },
                                    onLikeClick = { postId, postType ->
                                        if (isExpired) showExpiredToast() else {
                                            onEvent(GroupNoteEvent.OnLikeRecord(postId, postType))
                                        }
                                    },
                                    onProfileClick = { onNavigateToUserProfile(post.userId) }
                                )

                                "VOTE" -> VoteCommentCard(
                                    data = post,
                                    modifier = itemModifier,
                                    onCommentClick = {
                                        selectedPostForComment = post
                                        isCommentBottomSheetVisible = true
                                    },
                                    onLongPress = {
                                        if (isExpired) showExpiredToast() else {
                                            selectedPostForMenu = post
                                        }
                                    },
                                    onVote = { postId, voteItemId, type ->
                                        onEvent(GroupNoteEvent.OnVote(postId, voteItemId, type))
                                    },
                                    onLikeClick = { postId, postType ->
                                        if (isExpired) showExpiredToast() else {
                                            onEvent(GroupNoteEvent.OnLikeRecord(postId, postType))
                                        }
                                    },
                                    onProfileClick = { onNavigateToUserProfile(post.userId) }
                                )
                            }
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.selectedTabIndex == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 98.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .background(color = colors.Black)
                    )

                    FilterButton(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(top = 20.dp, end = 20.dp, bottom = 20.dp),
                        selectedOption = stringResource(uiState.selectedSort.displayNameRes),
                        options = sortDisplayStrings,
                        onOptionSelected = { selectedString ->
                            val selectedIndex = sortDisplayStrings.indexOf(selectedString)
                            if (selectedIndex != -1) {
                                val selectedSortType = SortType.entries[selectedIndex]
                                onEvent(GroupNoteEvent.OnSortSelected(selectedSortType))
                            }
                        }
                    )

                    FilterHeaderSection(
                        modifier = Modifier.padding(top = 20.dp),
                        firstPage = uiState.pageStart,
                        lastPage = uiState.pageEnd,
                        isTotalSelected = uiState.isOverview,
                        totalEnabled = uiState.totalEnabled,
                        onFirstPageChange = { onEvent(GroupNoteEvent.OnPageStartChanged(it)) },
                        onLastPageChange = { onEvent(GroupNoteEvent.OnPageEndChanged(it)) },
                        onTotalToggle = { onEvent(GroupNoteEvent.OnOverviewToggled(!uiState.isOverview)) },
                        onDisabledClick = {
                            toastMessage =
                                context.getString(R.string.condition_of_view_general_review)
                            isErrorToast = true
                            showToast = true
                        },
                        onApplyPageFilter = { onEvent(GroupNoteEvent.ApplyPageFilter) }
                    )
                }
            }

            if (!isExpired) {
                ExpandableFloatingButton(
                    menuItems = listOf(
                        FabMenuItem(
                            icon = painterResource(R.drawable.ic_write),
                            text = stringResource(R.string.write_record),
                            onClick = onCreateNoteClick
                        ),
                        FabMenuItem(
                            icon = painterResource(R.drawable.ic_vote),
                            text = stringResource(R.string.create_vote),
                            onClick = onCreateVoteClick
                        ),
                        FabMenuItem(
                            icon = painterResource(R.drawable.ic_ai_book_review),
                            text = stringResource(R.string.create_ai_book_review),
                            onClick = {
                                scope.launch {
                                    onEvent(GroupNoteEvent.CheckAiUsage)
                                    showAiReviewDialog = true
                                }
                            }
                        )
                    )
                )
            }
        }
    }

    if (isCommentBottomSheetVisible && selectedPostForComment != null) {
        LaunchedEffect(selectedPostForComment?.postId) {
            selectedPostForComment?.let { post ->
                commentsViewModel.initialize(
                    postId = post.postId.toLong(),
                    postType = post.postType
                )
            }
        }

        CommentBottomSheet(
            viewModel = commentsViewModel,
            uiState = commentsUiState,
            isExpired = isExpired,
            onDismiss = {
                isCommentBottomSheetVisible = false
                selectedPostForComment = null
                onEvent(GroupNoteEvent.RefreshPosts)
            },
            onProfileClick = onNavigateToUserProfile,
            onShowToast = { message ->
                toastMessage = message
                isErrorToast = false
                showToast = true
            }
        )
    }

    if (selectedPostForMenu != null) {
        val post = selectedPostForMenu!!
        val menuItems = if (post.isWriter) {
            listOf(
                MenuBottomSheetItem(
                    text = stringResource(R.string.modify),
                    color = colors.White,
                    onClick = {
                        when (post.postType) {
                            "RECORD" -> onEditNoteClick(post)
                            "VOTE" -> onEditVoteClick(post)
                        }
                        selectedPostForMenu = null
                    }
                ),
                MenuBottomSheetItem(
                    text = stringResource(R.string.delete),
                    color = colors.Red,
                    onClick = {
                        postToDelete = post // 삭제할 포스트 정보를 기억
                        showDeleteDialog = true
                        selectedPostForMenu = null
                    }
                )
            )
        } else {
            listOf(
                MenuBottomSheetItem(
                    text = stringResource(R.string.report),
                    color = colors.Red,
                    onClick = {
                        // TODO: 신고 처리
                        selectedPostForMenu = null
                    }
                )
            )
        }

        MenuBottomSheet(
            items = menuItems,
            onDismiss = { selectedPostForMenu = null }
        )
    }

    if (showDeleteDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DialogPopup(
                title = stringResource(R.string.delete_post_title),
                description = stringResource(R.string.delete_post_content),
                onConfirm = {
                    postToDelete?.let {
                        onEvent(GroupNoteEvent.OnDeleteRecord(it.postId, it.postType))
                    }
                    showDeleteDialog = false
                    postToDelete = null
                },
                onCancel = {
                    showDeleteDialog = false
                    postToDelete = null
                }
            )
        }
    }

    if (isPinDialogVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DialogPopup(
                title = stringResource(R.string.pin_modal_title),
                description = stringResource(R.string.pin_modal_content),
                onConfirm = {
                    postToPin?.let { post ->
                        onEvent(
                            GroupNoteEvent.OnPinRecord(
                                recordId = post.postId,
                                content = post.content
                            )
                        )
                    }
                    isPinDialogVisible = false
                    postToPin = null
                },
                onCancel = {
                    isPinDialogVisible = false
                    postToPin = null
                }
            )
        }
    }

    if (showAiReviewDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DialogPopup(
                title = stringResource(R.string.ai_review_dialog_title),
                description = stringResource(
                    R.string.ai_review_dialog_description,
                    uiState.recordCount,
                    5
                ),
                onConfirm = {
                    onNavigateToAiReview()
                    showAiReviewDialog = false
                },
                onCancel = {
                    showAiReviewDialog = false
                }
            )
        }
    }

    AnimatedVisibility(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp),
        visible = showToast,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 1000)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 1000)
        )
    ) {
        ToastWithDate(
            message = toastMessage,
            color = if (isErrorToast) colors.Red else colors.White
        )
    }
}

@Preview
@Composable
private fun GroupNoteScreenPreview() {
    ThipTheme {
        GroupNoteContent(
            uiState = GroupNoteUiState(
                posts = listOf(
                    PostList(
                        userId = 1,
                        profileImageUrl = "https://example.com/profile.jpg",
                        voteItems = emptyList(),
                        postId = 1,
                        postType = "RECORD",
                        page = 1,
                        postDate = "12시간 전",
                        nickName = "사용자1",
                        content = "첫 번째 기록입니다.",
                        isLiked = false,
                        likeCount = 10,
                        commentCount = 2,
                        isLocked = false,
                        isWriter = true,
                        isOverview = false
                    )
                ),
                selectedTabIndex = 0,
                selectedSort = SortType.LATEST,
                isLoading = false,
                isLoadingMore = false,
                pageStart = "1",
                pageEnd = "10",
                isOverview = false,
                totalEnabled = true
            ),
            onEvent = {},
            onBackClick = {},
            onCreateNoteClick = {},
            onCreateVoteClick = {},
            showProgressBar = true,
            progress = 0.5f,
            onNavigateToUserProfile = {},
            onEditNoteClick = {},
            onEditVoteClick = {},
            onNavigateToAiReview = {}
        )
    }
}