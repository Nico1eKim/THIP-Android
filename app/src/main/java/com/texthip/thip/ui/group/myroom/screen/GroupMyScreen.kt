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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.common.cards.CardItemRoom
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.group.myroom.component.GroupMyRoomFilterRow
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun GroupMyScreen(
    allDataList: List<GroupCardItemRoomData>,
    onCardClick: (GroupCardItemRoomData) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var selectedStates by remember { mutableStateOf(booleanArrayOf(false, false)) }

    val filteredList = remember(selectedStates, allDataList) {
        if (selectedStates.all { !it } || selectedStates.all { it }) {
            allDataList
        } else if (selectedStates[0]) {
            allDataList.filter { !it.isRecruiting }
        } else if (selectedStates[1]) {
            allDataList.filter { it.isRecruiting }
        } else {
            allDataList
        }
    }

    Column(
        Modifier
            .background(colors.Black)
            .fillMaxSize()
    ) {
        DefaultTopAppBar(
            title = stringResource(R.string.my_group_room),
            onLeftClick = onNavigateBack,
        )
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
                    selectedStates = selectedStates.copyOf().also { it[idx] = !it[idx] }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (filteredList.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { item ->
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
            } else {
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

@Preview
@Composable
fun MyGroupListFilterScreenPreview() {
    ThipTheme {
        val dataList = listOf(
            GroupCardItemRoomData(
                id = 1,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 2,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 30,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 3,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 1,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 4,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 5,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 6,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 30,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 7,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 1,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 8,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 9,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 10,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 30,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 11,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 1,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 12,
                title = "모임방 이름입니다. 모임방...",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 3,
                genreIndex = 0
            )
        )
        GroupMyScreen(allDataList = dataList)
    }
}

@Preview()
@Composable
fun MyGroupListEmptyScreenPreview() {
    ThipTheme {
        GroupMyScreen(allDataList = emptyList())
    }
}
