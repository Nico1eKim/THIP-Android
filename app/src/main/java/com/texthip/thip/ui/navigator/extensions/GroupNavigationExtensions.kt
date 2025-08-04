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

// 완료된 모임방 목록으로 이동
fun NavHostController.navigateToGroupDone() {
    navigate(GroupRoutes.Done)
}

// 모임방 검색 화면으로 이동
fun NavHostController.navigateToGroupSearch() {
    navigate(GroupRoutes.Search)
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

// 추천 모임방으로 이동 (현재 화면을 대체)
fun NavHostController.navigateToRecommendedGroupRecruit(roomId: Int) {
    navigate(GroupRoutes.Recruit(roomId)) {
        popUpTo(currentDestination?.route ?: return@navigate) {
            inclusive = true
        }
    }
}

// 진행중인 모임방 화면으로 이동
fun NavHostController.navigateToGroupRoom(roomId: Int) {
    navigate(GroupRoutes.Room(roomId))
}


