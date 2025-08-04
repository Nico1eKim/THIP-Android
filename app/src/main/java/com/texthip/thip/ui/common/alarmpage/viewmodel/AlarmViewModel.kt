package com.texthip.thip.ui.common.alarmpage.viewmodel

import androidx.lifecycle.ViewModel
import com.texthip.thip.ui.common.alarmpage.mock.AlarmItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmViewModel : ViewModel() {
    private val _alarmItems = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarmItems: StateFlow<List<AlarmItem>> = _alarmItems.asStateFlow()

    // 알림 더미 데이터
    init {
        _alarmItems.value = listOf(
            AlarmItem(1, "피드", "내 글을 좋아합니다.", "user123님이 내 글에 좋아요를 눌렀어요.", "2시간 전", false),
            AlarmItem(2, "모임", "같이 읽기를 시작했어요!", "모임방에서 20분 동안 같이 읽기가 시작되었어요!", "7시간 전", false),
            AlarmItem(3, "피드", "내 글에 댓글이 달렸어요.", "user1: 진짜 공감합니다!", "2025.01.12", true),
            AlarmItem(4, "모임", "투표가 시작되었어요!", "투표지를 먼저 열람합니다.", "17시간 전", false),
            AlarmItem(5, "피드", "팔로워가 새 글을 올렸어요.", "user456님이 새 리뷰를 작성했습니다.", "1일 전", true),
            AlarmItem(6, "모임", "새로운 모임방 초대", "호르몬 체인지 완독하는 방에 초대되었습니다.", "2일 전", false)
        )
    }

    fun onCardClick(item: AlarmItem) {
        // TODO: 알림 카드 클릭 처리
    }
}