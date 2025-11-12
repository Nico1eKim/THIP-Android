package com.texthip.thip.ui.feed.screen

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.data.model.feed.response.FeedList
import com.texthip.thip.data.model.feed.response.FeedMineInfoResponse
import com.texthip.thip.data.model.feed.response.MyFeedItem
import com.texthip.thip.ui.common.header.AuthorHeader
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.feed.component.FeedSubscribeBarlist
import com.texthip.thip.ui.feed.component.OthersFeedCard
import com.texthip.thip.ui.feed.viewmodel.FeedUiState
import com.texthip.thip.ui.feed.viewmodel.FeedViewModel
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import com.texthip.thip.utils.color.hexToColor

@Composable
fun FeedMyScreen(
    viewModel: FeedViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSubscriptionList: (userId: Long) -> Unit = {},
    onNavigateToFeedComment: (feedId: Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onTabSelected(1)
    }

    FeedMyContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onLikeClick = { feedId -> viewModel.changeFeedLike(feedId) },
        onBookmarkClick = { feedId -> viewModel.changeFeedSave(feedId) },
        onNavigateToSubscriptionList = onNavigateToSubscriptionList,
        onNavigateToFeedComment = onNavigateToFeedComment
    )
}

@Composable
fun FeedMyContent(
    uiState: FeedUiState,
    onNavigateBack: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onBookmarkClick: (Long) -> Unit,
    onNavigateToSubscriptionList: (userId: Long) -> Unit,
    onNavigateToFeedComment: (feedId: Long) -> Unit = {},
) {
    val userInfo = uiState.myFeedInfo

    fun MyFeedItem.toFeedList(): FeedList {
        return FeedList(
            feedId = this.feedId.toLong(),
            postDate = this.postDate,
            isbn = this.isbn,
            bookTitle = this.bookTitle,
            bookAuthor = this.bookAuthor,
            contentBody = this.contentBody,
            contentUrls = this.contentUrls,
            likeCount = this.likeCount,
            commentCount = this.commentCount,
            isPublic = this.isPublic,
            isSaved = this.isSaved,
            isLiked = this.isLiked,
            isWriter = this.isWriter
        )
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

            if (uiState.isRefreshing || (uiState.isLoading && uiState.myFeeds.isEmpty())) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (userInfo != null) {
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
                            showButton = false,
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
                            items = uiState.myFeeds,
                            key = { _, item -> item.feedId }
                        ) { index, feed ->
                            Spacer(modifier = Modifier.height(if (index == 0) 20.dp else 40.dp))
                            OthersFeedCard(
                                feedItem = feed.toFeedList(),
                                onLikeClick = { onLikeClick(feed.feedId.toLong()) },
                                onBookmarkClick = { onBookmarkClick(feed.feedId.toLong()) },
                                onContentClick = { onNavigateToFeedComment(feed.feedId.toLong()) }
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            if (index < uiState.myFeeds.lastIndex) {
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
    }
}

@Preview
@Composable
private fun FeedMyScreenPreview() {
    // Preview용 가짜 데이터
    val mockUserInfo = FeedMineInfoResponse(
        creatorId = 1,
        profileImageUrl = "",
        nickname = "김작가",
        aliasName = "소설가",
        aliasColor = "#A0F8E8",
        totalFeedCount = 5,
        followerCount = 150,
        latestFollowerProfileImageUrls = emptyList(),
        isFollowing = true,
    )
    val mockFeeds = List(5) {
        MyFeedItem(
            feedId = it, postDate = "2시간 전", isbn = "1234",
            bookTitle = "나의 책 제목 ${it + 1}", bookAuthor = "나",
            contentBody = "내가 작성한 피드 내용입니다. 내용은 여기에 표시됩니다.",
            contentUrls = emptyList(), likeCount = 15, commentCount = 3,
            isPublic = true, isSaved = false, isLiked = it % 2 == 0,
            isWriter = true
        )
    }

    ThipTheme {
        FeedMyContent(
            uiState = FeedUiState(
                isLoading = false,
                myFeedInfo = mockUserInfo,
                myFeeds = mockFeeds
            ),
            onLikeClick = {},
            onBookmarkClick = {},
            onNavigateToSubscriptionList = {},
            onNavigateBack = {},
        )
    }
}
