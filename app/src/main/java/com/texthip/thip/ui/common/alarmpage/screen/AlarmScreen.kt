package com.texthip.thip.ui.common.alarmpage.screen

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
import com.texthip.thip.ui.common.alarmpage.component.AlarmFilterRow
import com.texthip.thip.ui.common.alarmpage.mock.AlarmItem
import com.texthip.thip.ui.common.cards.CardAlarm
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun AlarmScreen(
    alarmItems: List<AlarmItem>, 
    onCardClick: (AlarmItem) -> Unit = {},  // 나중에 서버랑 연동할 때 사용
    onNavigateBack: () -> Unit = {}
) {
    var selectedStates by remember { mutableStateOf(booleanArrayOf(false, false)) }
    var alarms by remember { mutableStateOf(alarmItems) }

    val filteredList = when {
        selectedStates[0] && !selectedStates[1] -> alarms.filter { it.badgeText == stringResource(R.string.alarm_feed) }
        !selectedStates[0] && selectedStates[1] -> alarms.filter { it.badgeText == stringResource(R.string.alarm_group) }
        else -> alarms
    }

    Column(
        Modifier
            .fillMaxSize()
    ) {
        DefaultTopAppBar(
            title = stringResource(R.string.alarm_string),
            onLeftClick = onNavigateBack,
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            AlarmFilterRow(
                selectedStates = selectedStates, onToggle = { idx ->
                    selectedStates = selectedStates.copyOf().also { it[idx] = !it[idx] }
                })
            Spacer(modifier = Modifier.height(20.dp))

            if (filteredList.isEmpty()) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.alarm_notification_comment),
                        style = typography.smalltitle_sb600_s18_h24,
                        color = colors.White
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { alarm ->
                        CardAlarm(
                            badgeText = alarm.badgeText,
                            title = alarm.title,
                            message = alarm.message,
                            timeAgo = alarm.timeAgo,
                            isRead = alarm.isRead,
                            onClick = {
                                alarms = alarms.map {
                                    if (it.id == alarm.id) it.copy(isRead = true) else it
                                }
                            })
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    ThipTheme {
        AlarmScreen(
            alarmItems = listOf(
                AlarmItem(1, "피드", "내 글을 좋아합니다.", "user123님이 내 글에 좋아요를 눌렀어요.", "2", false),
                AlarmItem(2, "모임", "같이 읽기를 시작했어요!", "모임방에서 20분 동안 같이 읽기가 시작되었어요!", "7", false),
                AlarmItem(3, "피드", "내 글에 댓글이 달렸어요.", "user1: 진짜 공감합니다!", "2025.01.12", true),
                AlarmItem(4, "모임", "투표가 시작되었어요!", "투표지를 먼저 열람합니다.", "17", false)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenEmptyPreview() {
    ThipTheme {
        AlarmScreen(
            alarmItems = emptyList()
        )
    }
}