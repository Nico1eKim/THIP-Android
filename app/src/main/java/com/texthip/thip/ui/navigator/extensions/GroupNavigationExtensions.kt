package com.texthip.thip.ui.navigator.extensions

import androidx.navigation.NavHostController
import com.texthip.thip.ui.navigator.routes.CommonRoutes
import com.texthip.thip.ui.navigator.routes.GroupRoutes
import com.texthip.thip.ui.navigator.routes.MainTabRoutes


// Group 관련 네비게이션 확장 함수들
fun NavHostController.navigateToGroup() {
    navigate(MainTabRoutes.Group)
}

// 모임방 만들기 화면으로 이동
fun NavHostController.navigateToGroupMakeRoom() {
    navigate(GroupRoutes.MakeRoom)
}

// 책 정보가 미리 선택된 모임방 만들기 화면으로 이동
fun NavHostController.navigateToGroupMakeRoomWithBook(
    isbn: String,
    title: String,
    imageUrl: String,
    author: String
) {
    navigate(
        GroupRoutes.MakeRoomWithBook(
            isbn = isbn,
            title = title,
            imageUrl = imageUrl,
            author = author
        )
    )
}

// 완료된 모임방 목록으로 이동
fun NavHostController.navigateToGroupDone() {
    navigate(GroupRoutes.Done)
}

// 모임방 검색 화면으로 이동
fun NavHostController.navigateToGroupSearch(viewAll: Boolean = false) {
    navigate(GroupRoutes.Search(viewAll = viewAll))
}

// 내 모임방 화면으로 이동
fun NavHostController.navigateToGroupMy() {
    navigate(GroupRoutes.My)
}

// 알람 화면으로 이동
fun NavHostController.navigateToAlarm() {
    navigate(CommonRoutes.Alarm)
}

// 모집중인 모임방 상세 화면으로 이동
fun NavHostController.navigateToGroupRecruit(roomId: Int) {
    navigate(GroupRoutes.Recruit(roomId))
}

// 비밀번호 입력 화면으로 이동
fun NavHostController.navigateToGroupRoomUnlock(roomId: Int) {
    navigate(GroupRoutes.RoomUnlock(roomId))
}

// 추천 모임방으로 이동 (현재 화면을 대체)
fun NavHostController.navigateToRecommendedGroupRecruit(roomId: Int) {
    navigate(GroupRoutes.Recruit(roomId)) {
        popUpTo(currentDestination?.route ?: return@navigate) {
            inclusive = true
        }
    }
}

// 진행중인 모임방 화면으로 이동
fun NavHostController.navigateToGroupRoom(roomId: Int, isExpired: Boolean = false) {
    navigate(GroupRoutes.Room(roomId, isExpired))
}

// 독서메이트 화면으로 이동
fun NavHostController.navigateToGroupRoomMates(roomId: Int) {
    navigate(GroupRoutes.RoomMates(roomId))
}

// 오늘의 한마디 회면으로 이동
fun NavHostController.navigateToGroupRoomChat(roomId: Int, isExpired: Boolean = false) {
    navigate(GroupRoutes.RoomChat(roomId, isExpired))
}

// 기록장 화면으로 이동
fun NavHostController.navigateToGroupNote(
    roomId: Int,
    page: Int? = null,
    isOverview: Boolean? = null,
    isExpired: Boolean = false,
    postId: Int? = null
) {
    navigate(
        GroupRoutes.Note(
            roomId = roomId,
            page = page,
            openComments = false,
            isExpired = isExpired,
            postId = postId,
            isOverview = isOverview
        )
    )
}

// 기록 생성 화면으로 이동
fun NavHostController.navigateToGroupNoteCreate(
    roomId: Int,
    recentBookPage: Int,
    totalBookPage: Int,
    isOverviewPossible: Boolean,
    postId: Int? = null,
    page: Int? = null,
    content: String? = null,
    isOverview: Boolean? = null
) {
    navigate(
        GroupRoutes.NoteCreate(
            roomId = roomId,
            recentBookPage = recentBookPage,
            totalBookPage = totalBookPage,
            isOverviewPossible = isOverviewPossible,
            postId = postId,
            page = page,
            content = content,
            isOverview = isOverview
        )
    )
}

// 투표 생성 화면으로 이동
fun NavHostController.navigateToGroupVoteCreate(
    roomId: Int,
    recentPage: Int,
    totalPage: Int,
    isOverviewPossible: Boolean,
    postId: Int? = null,
    page: Int? = null,
    isOverview: Boolean? = null,
    title: String? = null,
    options: List<String>? = null
) {
    navigate(
        GroupRoutes.VoteCreate(
            roomId = roomId,
            recentPage = recentPage,
            totalPage = totalPage,
            isOverviewPossible = isOverviewPossible,
            postId = postId,
            page = page,
            isOverview = isOverview,
            title = title,
            options = options
        )
    )
}

// AI 독후감 생성 화면으로 이동
fun NavHostController.navigateToGroupNoteAi(roomId: Int) {
    navigate(GroupRoutes.NoteAi(roomId = roomId))
}