package com.texthip.thip.ui.group.myroom.mock

import com.texthip.thip.R

data class GroupCardData(
    val id: Int = 0, // 모임방 ID 추가
    val title: String,
    val members: Int,
    val imageRes: Int = R.drawable.bookcover_sample,
    val progress: Int, // 진행률 (0~100)
    val nickname: String
)