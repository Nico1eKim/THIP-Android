package com.texthip.thip.ui.group.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.texthip.thip.ui.common.buttons.GenreChipRow
import com.texthip.thip.ui.common.cards.CardItemRoomSmall
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun GroupFilteredSearchResult(
    genres: List<String>,
    selectedGenreIndex: Int,
    onGenreSelect: (Int) -> Unit,
    resultCount: Int,
    roomList: List<GroupCardItemRoomData>,
    onRoomClick: (GroupCardItemRoomData) -> Unit = {}
) {
    Column {
        GenreChipRow(
            modifier = Modifier.width(20.dp),
            genres = genres,
            selectedIndex = selectedGenreIndex,
            onSelect = onGenreSelect
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
            LazyColumn {
                itemsIndexed(roomList) { index, room ->
                    CardItemRoomSmall(
                        title = room.title,
                        participants = room.participants,
                        maxParticipants = room.maxParticipants,
                        endDate = room.endDate,
                        imageRes = room.imageRes,
                        isWide = true,
                        isSecret = room.isSecret,
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
                    GroupCardItemRoomData(
                        id = 1,
                        title = "해리포터 독서모임",
                        participants = 5,
                        maxParticipants = 10,
                        isRecruiting = true,
                        endDate = 7,
                        imageRes = R.drawable.bookcover_sample,
                        genreIndex = 1,
                        isSecret = false
                    ), GroupCardItemRoomData(
                        id = 2,
                        title = "소설 읽기 모임",
                        participants = 8,
                        maxParticipants = 12,
                        isRecruiting = false,
                        endDate = 3,
                        imageRes = R.drawable.bookcover_sample,
                        genreIndex = 1,
                        isSecret = true
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

