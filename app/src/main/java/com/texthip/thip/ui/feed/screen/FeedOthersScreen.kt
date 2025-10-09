package com.texthip.thip.ui.feed.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.texthip.thip.R
import com.texthip.thip.data.model.feed.response.FeedList
import com.texthip.thip.data.model.feed.response.FeedUsersInfoResponse
import com.texthip.thip.ui.common.header.AuthorHeader
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.feed.component.FeedSubscribeBarlist
import com.texthip.thip.ui.feed.component.OthersFeedCard
import com.texthip.thip.ui.feed.viewmodel.FeedOthersUiState
import com.texthip.thip.ui.feed.viewmodel.FeedOthersViewModel
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import com.texthip.thip.utils.color.hexToColor
import kotlinx.coroutines.delay

@Composable
fun FeedOthersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubscriptionList: (userId: Long) -> Unit = {},
    onNavigateToFeedComment: (feedId: Long) -> Unit = {},
    viewModel: FeedOthersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    FeedOthersContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onLikeClick = { feedId -> viewModel.changeFeedLike(feedId) },
        onBookmarkClick = { feedId -> viewModel.changeFeedSave(feedId) },
        onToggleFollow = {
            val followedMessage =
                context.getString(R.string.toast_thip, uiState.userInfo?.nickname ?: "")
            val unfollowedMessage =
                context.getString(R.string.toast_thip_cancel, uiState.userInfo?.nickname ?: "")
            viewModel.toggleFollow(followedMessage, unfollowedMessage)
        },
        onHideToast = viewModel::hideToast,
        onNavigateToSubscriptionList = onNavigateToSubscriptionList,
        onNavigateToFeedComment = onNavigateToFeedComment
    )
}

@Composable
fun FeedOthersContent(
    uiState: FeedOthersUiState,
    onNavigateBack: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onBookmarkClick: (Long) -> Unit,
    onToggleFollow: () -> Unit,
    onHideToast: () -> Unit,
    onNavigateToSubscriptionList: (userId: Long) -> Unit,
    onNavigateToFeedComment: (feedId: Long) -> Unit = {},
) {
    val userInfo = uiState.userInfo
    LaunchedEffect(uiState.showToast) {
        if (uiState.showToast) {
            delay(2000)
            onHideToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DefaultTopAppBar(
                isRightIconVisible = false,
                isTitleVisible = false,
                onLeftClick = onNavigateBack,
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (userInfo != null) {
                // 스크롤 영역
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        AuthorHeader(
                            profileImage = userInfo.profileImageUrl,
                            nickname = userInfo.nickname,
                            badgeText = userInfo.aliasName,
                            badgeTextColor = hexToColor(userInfo.aliasColor),
                            buttonText = if (userInfo.isFollowing) stringResource(R.string.thip_cancel) else stringResource(
                                R.string.thip
                            ),
                            // TODO: 띱하기/취소하기 로직 연결
                            onButtonClick = onToggleFollow,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FeedSubscribeBarlist(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            followerNum = userInfo.followerCount,
                            followerProfileImageUrls = userInfo.latestFollowerProfileImageUrls,
                            onClick = { onNavigateToSubscriptionList(userInfo.creatorId) }
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            text = stringResource(R.string.whole_num, userInfo.totalFeedCount),
                            style = typography.menu_m500_s14_h24,
                            color = colors.Grey,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp, start = 20.dp)
                        )
                        HorizontalDivider(
                            color = colors.DarkGrey03,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    if (userInfo.totalFeedCount == 0) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 244.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_feed),
                                    style = typography.smalltitle_sb600_s18_h24,
                                    color = colors.White
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.feeds,
                            key = { _, item -> item.feedId }
                        ) { index, feed ->
                            Spacer(modifier = Modifier.height(if (index == 0) 20.dp else 40.dp))
                            OthersFeedCard(
                                feedItem = feed,
                                onLikeClick = { onLikeClick(feed.feedId) },
                                onBookmarkClick = { onBookmarkClick(feed.feedId) },
                                onContentClick = { onNavigateToFeedComment(feed.feedId) }
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            if (index < uiState.feeds.lastIndex) {
                                HorizontalDivider(
                                    color = colors.DarkGrey03,
                                    thickness = 10.dp
                                )
                            }
                        }
                    }
                }
            }
        }
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
                message = uiState.toastMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun FeedOthersScreenPrev() {

    val mockUserInfo = FeedUsersInfoResponse(
        creatorId = 1,
        profileImageUrl = "",
        nickname = "김독서",
        aliasName = "문학가",
        aliasColor = "#A0F8E8",
        followerCount = 120,
        totalFeedCount = 5,
        isFollowing = true,
        latestFollowerProfileImageUrls = emptyList()
    )
    val mockFeeds = List(5) {
        FeedList(
            feedId = it.toLong(), postDate = "1시간 전", isbn = "1234",
            bookTitle = "미리보기 책 제목 ${it + 1}", bookAuthor = "작가",
            contentBody = "미리보기 피드 내용입니다. 내용은 여기에 표시됩니다.",
            contentUrls = emptyList(), likeCount = 10, commentCount = 2,
            isPublic = true, isSaved = false, isLiked = true, isWriter = false
        )
    }

    ThipTheme {
        FeedOthersContent(
            uiState = FeedOthersUiState(
                isLoading = false,
                userInfo = mockUserInfo,
                feeds = mockFeeds
            ),
            onNavigateBack = {},
            onLikeClick = {},
            onToggleFollow = {},
            onHideToast = {},
            onBookmarkClick = {},
            onNavigateToSubscriptionList = {}
        )
    }
}