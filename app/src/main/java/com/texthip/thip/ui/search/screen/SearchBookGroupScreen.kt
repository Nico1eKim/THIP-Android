package com.texthip.thip.ui.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.common.cards.CardItemRoom
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun SearchBookGroupScreen(
    recruitingList: List<GroupCardItemRoomData>,
    onCardClick: (GroupCardItemRoomData) -> Unit = {},
    onCreateRoomClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            DefaultTopAppBar(
                title = stringResource(R.string.group_recruiting_title),
                onLeftClick = {},
            )

            Column(
                Modifier
                    .background(colors.Black)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.group_searched_room_size, recruitingList.size),
                        color = colors.Grey,
                        style = typography.menu_m500_s14_h24
                    )
                }
                Spacer(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 20.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.DarkGrey02)
                )

                if (recruitingList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.book_recruiting_empty_message),
                            color = colors.White,
                            style = typography.smalltitle_sb600_s18_h24,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.book_recruiting_empty_sub_message),
                            color = colors.Grey,
                            style = typography.feedcopy_r400_s14_h20,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 70.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(recruitingList) { item ->
                            CardItemRoom(
                                title = item.title,
                                participants = item.participants,
                                maxParticipants = item.maxParticipants,
                                isRecruiting = item.isRecruiting,
                                endDate = item.endDate,
                                imageRes = item.imageRes,
                                onClick = { onCardClick(item) }
                            )
                        }
                    }
                }
            }
        }

        // 하단 버튼
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.Purple
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(0.dp),
            onClick = onCreateRoomClick
        ) {
            Text(
                text = stringResource(R.string.group_recruiting_create_button),
                style = typography.smalltitle_sb600_s18_h24,
                color = colors.White
            )
        }
    }
}

@Preview()
@Composable
fun GroupRecruitingScreenPreview() {
    ThipTheme {
        val dataList = listOf(
            GroupCardItemRoomData(
                id = 1,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 2,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 3,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 4,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 5,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 6,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 7,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 8,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                endDate = 3,
                isRecruiting = true,
                genreIndex = 0
            )
        )

        SearchBookGroupScreen(
            recruitingList = dataList
        )
    }
}

@Preview()
@Composable
fun GroupRecruitingScreenEmptyPreview() {
    ThipTheme {
        SearchBookGroupScreen(
            recruitingList = emptyList()
        )
    }
}