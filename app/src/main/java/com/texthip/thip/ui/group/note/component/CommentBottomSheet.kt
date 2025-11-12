package com.texthip.thip.ui.group.note.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.texthip.thip.R
import com.texthip.thip.data.model.comments.response.CommentList
import com.texthip.thip.data.model.comments.response.ReplyList
import com.texthip.thip.ui.common.bottomsheet.CustomBottomSheet
import com.texthip.thip.ui.common.bottomsheet.MenuBottomSheet
import com.texthip.thip.ui.common.forms.CommentTextField
import com.texthip.thip.ui.group.note.viewmodel.CommentsEvent
import com.texthip.thip.ui.group.note.viewmodel.CommentsUiState
import com.texthip.thip.ui.group.note.viewmodel.CommentsViewModel
import com.texthip.thip.ui.group.room.mock.MenuBottomSheetItem
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import com.texthip.thip.utils.rooms.advancedImePadding

@Composable
fun CommentBottomSheet(
    uiState: CommentsUiState,
    isExpired: Boolean = false,
    onDismiss: () -> Unit,
    onProfileClick: (userId: Long) -> Unit = {},
    onShowToast: (String) -> Unit = {},
    viewModel: CommentsViewModel = hiltViewModel(),
) {
    var inputText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<Int?>(null) }
    var replyingToNickname by remember { mutableStateOf<String?>(null) }

    var selectedCommentForMenu by remember { mutableStateOf<CommentList?>(null) }
    var selectedReplyForMenu by remember { mutableStateOf<ReplyList?>(null) }

    val isOverlayVisible = selectedCommentForMenu != null || selectedReplyForMenu != null

    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    val focusManager = LocalFocusManager.current

    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            replyingToCommentId = null
            replyingToNickname = null
            focusManager.clearFocus()
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
        CustomBottomSheet(onDismiss = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .advancedImePadding()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.8f)
                ) {
                    Text(
                        text = stringResource(R.string.comments),
                        style = typography.title_b700_s20_h24,
                        color = colors.White,
                        modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.isLoading) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (uiState.comments.isEmpty()) {
                            EmptyCommentView()
                        } else {
                            CommentLazyList(
                                listState = listState,
                                isExpired = isExpired,
                                commentList = uiState.comments,
                                isLoadingMore = uiState.isLoadingMore,
                                isLastPage = uiState.isLast,
                                onLoadMore = { viewModel.onEvent(CommentsEvent.LoadMoreComments) },
                                onReplyClick = { commentId, nickname ->
                                    replyingToCommentId = commentId
                                    replyingToNickname = nickname

                                    focusRequester.requestFocus()
                                },
                                onEvent = viewModel::onEvent,
                                onCommentLongPress = { comment ->
                                    selectedCommentForMenu = comment
                                },
                                onReplyLongPress = { reply -> selectedReplyForMenu = reply },
                                onProfileClick = onProfileClick,
                                onShowToast = onShowToast
                            )
                        }
                    }
                }

                if (!isExpired) {
                    CommentTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        hint = stringResource(R.string.reply_to),
                        input = inputText,
                        onInputChange = { inputText = it },
                        onSendClick = {
                            viewModel.onEvent(
                                CommentsEvent.CreateComment(
                                    content = inputText,
                                    parentId = replyingToCommentId
                                )
                            )
                            inputText = ""
                            replyingToCommentId = null
                            replyingToNickname = null
                            focusManager.clearFocus()
                        },
                        replyTo = replyingToNickname,
                        onCancelReply = {
                            replyingToCommentId = null
                            replyingToNickname = null
                        }
                    )
                }
            }
        }
    }

    val itemForMenu = selectedCommentForMenu ?: selectedReplyForMenu
    if (itemForMenu != null) {
        val isWriter = when (itemForMenu) {
            is CommentList -> itemForMenu.isWriter
            is ReplyList -> itemForMenu.isWriter
            else -> false
        }

        MenuBottomSheet(
            items = if (isWriter) {
                listOf(
                    MenuBottomSheetItem(
                        text = stringResource(R.string.delete),
                        color = colors.Red,
                        onClick = {
                            val commentId = when (val item = itemForMenu) {
                                is CommentList -> item.commentId
                                is ReplyList -> item.commentId
                                else -> null
                            }
                            commentId?.let { viewModel.onEvent(CommentsEvent.DeleteComment(it)) }
                            onShowToast("댓글 삭제를 완료했습니다.")

                            selectedCommentForMenu = null
                            selectedReplyForMenu = null
                        }
                    )
                )
            } else {
                listOf(
                    MenuBottomSheetItem(
                        text = stringResource(R.string.report),
                        color = colors.Red,
                        onClick = {
                            onShowToast("댓글 신고를 완료했습니다.")
                            selectedCommentForMenu = null
                            selectedReplyForMenu = null
                        }
                    )
                )
            },
            onDismiss = {
                selectedCommentForMenu = null
                selectedReplyForMenu = null
            }
        )
    }
}

@Composable
private fun CommentLazyList(
    listState: LazyListState,
    isExpired: Boolean = false,
    commentList: List<CommentList>,
    isLoadingMore: Boolean,
    isLastPage: Boolean,
    onLoadMore: () -> Unit,
    onReplyClick: (commentId: Int, nickname: String?) -> Unit,
    onEvent: (CommentsEvent) -> Unit,
    onCommentLongPress: (CommentList) -> Unit,
    onReplyLongPress: (ReplyList) -> Unit,
    onProfileClick: (userId: Long) -> Unit,
    onShowToast: (String) -> Unit
) {
    val isScrolledToEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.totalItemsCount == 0) return@derivedStateOf false
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(isScrolledToEnd) {
        if (isScrolledToEnd && !isLoadingMore && !isLastPage) {
            onLoadMore()
        }
    }

    LazyColumn(state = listState) {
        items(
            items = commentList,
            key = { comment ->
                // commentId가 있으면 사용
                comment.commentId
                // 없다면(삭제된 댓글), replyList의 첫 번째 항목 ID를 사용
                    ?: comment.replyList.firstOrNull()?.commentId
                    // 그것마저 없다면(마지막 답글까지 삭제된 경우), 객체 자체의 hashCode를 사용
                    ?: comment.hashCode()
            }
        ) { comment ->
            CommentSection(
                commentItem = comment,
                isExpired = isExpired,
                onReplyClick = onReplyClick,
                onEvent = onEvent,
                onCommentLongPress = onCommentLongPress,
                onReplyLongPress = onReplyLongPress,
                onProfileClick = onProfileClick,
                onShowToast = onShowToast
            )
        }

        if (isLoadingMore) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}


@Composable
private fun EmptyCommentView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_comments_yet),
            style = typography.smalltitle_sb600_s18_h24,
            color = colors.White
        )
        Text(
            text = stringResource(R.string.no_comment_subtext),
            style = typography.copy_r400_s14,
            color = colors.Grey,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview
@Composable
private fun CommentBottomSheetPreview() {
    ThipTheme {
        var showSheet by remember { mutableStateOf(true) }
        if (showSheet) {
            CommentBottomSheet(
                uiState = CommentsUiState(
                    comments = listOf(
                        CommentList(
                            commentId = 1,
                            creatorId = 1,
                            creatorNickname = "User1",
                            content = "This is a comment.",
                            postDate = "2023-10-01",
                            likeCount = 5,
                            creatorProfileImageUrl = "https://example.com/image1.jpg",
                            aliasName = "칭호칭호",
                            aliasColor = "#A0F8E8",
                            isDeleted = false,
                            isLike = false,
                            isWriter = false,
                            replyList = emptyList()
                        )
                    ),
                    isLoading = false,
                    isLoadingMore = false,
                    isLast = false
                ),
                onDismiss = { showSheet = false },
            )
        }
    }
}