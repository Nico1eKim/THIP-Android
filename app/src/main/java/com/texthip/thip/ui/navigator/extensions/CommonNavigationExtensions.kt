package com.texthip.thip.ui.navigator.extensions

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.texthip.thip.ui.navigator.routes.MainTabRoutes


// 공통 네비게이션 확장 함수들


// Bottom Navigation용 Tab 이동 (메인 탭에만 사용)
fun NavHostController.navigateToTab(route: MainTabRoutes) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

// 메인 루트 헬퍼 함수들
fun NavDestination.isMainTabRoute(): Boolean {
    return when (route) {
        MainTabRoutes.Feed::class.qualifiedName,
        MainTabRoutes.Group::class.qualifiedName,
        MainTabRoutes.Search::class.qualifiedName,
        MainTabRoutes.MyPage::class.qualifiedName -> true
        else -> false
    }
}

// 매인 루트인지 확인
fun NavDestination.isRoute(targetRoute: MainTabRoutes): Boolean {
    return route == targetRoute::class.qualifiedName
}


