package com.texthip.thip.ui.group.myroom.mock

data class GroupRoomData(
    val id: Int,
    val title: String,
    val isSecret: Boolean,
    val description: String,
    val startDate: String,
    val endDate: String,
    val members: Int,
    val maxMembers: Int,
    val daysLeft: Int,
    val genre: String,
    val bookData: GroupBookData,
    val recommendations: List<GroupCardItemRoomData>
)
