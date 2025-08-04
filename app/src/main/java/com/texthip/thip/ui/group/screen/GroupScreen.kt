package com.texthip.thip.ui.group.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.texthip.thip.R
import com.texthip.thip.ui.common.buttons.FloatingButton
import com.texthip.thip.ui.common.topappbar.LogoTopAppBar
import com.texthip.thip.ui.group.myroom.component.GroupMySectionHeader
import com.texthip.thip.ui.group.myroom.component.GroupPager
import com.texthip.thip.ui.group.myroom.component.GroupRoomDeadlineSection
import com.texthip.thip.ui.group.myroom.component.GroupSearchTextField
import com.texthip.thip.ui.group.viewmodel.GroupViewModel
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors

@Composable
fun GroupScreen(
    onNavigateToMakeRoom: () -> Unit = {},
    onNavigateToGroupDone: () -> Unit = {}, // 완료된 화면으로 이동
    onNavigateToAlarm: () -> Unit = {}, // 알림 화면으로 이동
    onNavigateToGroupSearch: () -> Unit = {},   // 검색 화면으로 이동
    onNavigateToGroupMy: () -> Unit = {},   // 내 모임방 화면으로 이동
    onNavigateToGroupRecruit: (Int) -> Unit = {},   // 모집 중인 모임방 화면으로 이동
    onNavigateToGroupRoom: (Int) -> Unit = {},  // 기록장 화면으로 이동
    viewModel: GroupViewModel = viewModel()
) {
    val myGroups by viewModel.myGroups.collectAsState()
    val roomSections by viewModel.roomSections.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 상단바
            LogoTopAppBar(
                leftIcon = painterResource(R.drawable.ic_done),
                hasNotification = false,
                onLeftClick = onNavigateToGroupDone,
                onRightClick = onNavigateToAlarm
            )

            // 검색창
            GroupSearchTextField(
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp),
                onClick = onNavigateToGroupSearch
            )

            // 내 모임방 헤더 + 카드
            GroupMySectionHeader(
                onClick = onNavigateToGroupMy
            )
            Spacer(Modifier.height(20.dp))

            GroupPager(
                groupCards = myGroups,
                onCardClick = { groupCard ->
                    onNavigateToGroupRoom(groupCard.id)
                }
            )
            Spacer(Modifier.height(32.dp))

            Spacer(
                Modifier
                    .padding(bottom = 32.dp)
                    .height(10.dp)
                    .fillMaxWidth()
                    .background(color = colors.DarkGrey02)
            )

            // 마감 임박한 독서 모임방
            GroupRoomDeadlineSection(
                roomSections = roomSections,
                onRoomClick = { room ->
                    if (room.isRecruiting) {
                        onNavigateToGroupRecruit(room.id)
                    } else {
                        onNavigateToGroupRoom(room.id)
                    }
                }
            )
            Spacer(Modifier.height(102.dp))
        }
        // 오른쪽 하단 FAB
        FloatingButton(
            icon = painterResource(id = R.drawable.ic_makegroup),
            onClick = onNavigateToMakeRoom
        )
    }
}


@Preview
@Composable
fun PreviewGroupScreen() {
    ThipTheme {
        val previewViewModel = remember { GroupViewModel() }
        GroupScreen(viewModel = previewViewModel)
    }
}