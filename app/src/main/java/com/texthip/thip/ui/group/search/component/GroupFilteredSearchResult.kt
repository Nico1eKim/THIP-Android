package com.texthip.thip.ui.group.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.data.model.rooms.response.SearchRoomItem
import com.texthip.thip.ui.common.buttons.GenreChipRow
import com.texthip.thip.ui.common.cards.CardItemRoomSmall
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun GroupFilteredSearchResult(
    genres: List<String>,
    selectedGenreIndex: Int,
    onGenreSelect: (Int) -> Unit,
    resultCount: Int,
    roomList: List<SearchRoomItem>,
    onRoomClick: (SearchRoomItem) -> Unit = {},
    canLoadMore: Boolean = false,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
) {
    val allChipText = stringResource(id = R.string.all)
    val chipList = remember(genres) { listOf(allChipText) + genres }

    val finalSelectedIndex = if (selectedGenreIndex != -1) {
        // 특정 장르가 선택되었다면, '전체' 칩 때문에 +1 된 인덱스를 사용
        selectedGenreIndex + 1
    } else {
        // 특정 장르가 선택되지 않았다면, '전체' 칩(인덱스 0)을 선택
        0
    }


    Column {
        GenreChipRow(
            modifier = Modifier.width(12.dp),
            genres = chipList,
            selectedIndex = finalSelectedIndex,
            onSelect = { newIndex ->
                when (newIndex) {
                    // 칩 선택이 해제된 경우 (동일 칩 재클릭)
                    -1 -> onGenreSelect(-1)
                    // '전체' 칩이 선택된 경우 -> 장르 필터 해제
                    0 -> onGenreSelect(-1)
                    // 특정 장르가 선택된 경우 -> 원래 인덱스로 변환하여 전달
                    else -> onGenreSelect(newIndex - 1)
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.group_searched_room_size, resultCount),
                color = colors.Grey,
                style = typography.menu_m500_s14_h24
            )
        }
        Spacer(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.DarkGrey02)
        )

        if (roomList.isEmpty()) {
            GroupEmptyResult(
                mainText = stringResource(R.string.group_no_search_result1),
                subText = stringResource(R.string.group_no_search_result2)
            )
        } else {
            val listState = rememberLazyListState()

            // 무한 스크롤 트리거 감지
            val shouldLoadMore by remember {
                derivedStateOf {
                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    lastVisibleItem != null && lastVisibleItem.index >= roomList.size - 3 && canLoadMore
                }
            }

            LaunchedEffect(shouldLoadMore) {
                if (shouldLoadMore) {
                    onLoadMore()
                }
            }

            LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 20.dp)) {
                itemsIndexed(roomList) { index, room ->
                    CardItemRoomSmall(
                        title = room.roomName,
                        participants = room.memberCount,
                        maxParticipants = room.recruitCount,
                        endDate = room.deadlineDate,
                        imageUrl = room.bookImageUrl,
                        isWide = true,
                        isSecret = !room.isPublic,
                        onClick = { onRoomClick(room) }
                    )
                    if (index < roomList.size - 1) {
                        Spacer(
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 12.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.DarkGrey02)
                        )
                    }
                }

                // 로딩 인디케이터
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.White)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupFilteredSearchResultPreview() {
    ThipTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            var selectedGenre by remember { mutableIntStateOf(0) }

            GroupFilteredSearchResult(
                genres = listOf("문학", "과학•IT", "사회과학", "인문학", "예술"),
                selectedGenreIndex = selectedGenre,
                onGenreSelect = { selectedGenre = it },
                resultCount = 3,
                roomList = listOf(
                    SearchRoomItem(
                        roomId = 1,
                        roomName = "해리포터 독서모임",
                        memberCount = 5,
                        recruitCount = 10,
                        deadlineDate = "7일 뒤",
                        bookImageUrl = null,
                        isPublic = true
                    ), SearchRoomItem(
                        roomId = 2,
                        roomName = "소설 읽기 모임",
                        memberCount = 8,
                        recruitCount = 12,
                        deadlineDate = "3일 뒤",
                        bookImageUrl = null,
                        isPublic = false
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupFilteredSearchResultEmptyPreview() {
    ThipTheme {
        Box(
            modifier = Modifier
                .background(colors.Black)
                .padding(16.dp)
        ) {
            var selectedGenre by remember { mutableIntStateOf(0) }

            GroupFilteredSearchResult(
                genres = listOf("전체", "소설", "에세이", "자기계발", "경제/경영", "과학"),
                selectedGenreIndex = selectedGenre,
                onGenreSelect = { selectedGenre = it },
                resultCount = 0,
                roomList = emptyList()
            )
        }
    }
}

