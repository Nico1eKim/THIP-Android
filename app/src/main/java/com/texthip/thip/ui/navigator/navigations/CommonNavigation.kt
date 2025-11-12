package com.texthip.thip.ui.navigator.navigations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.texthip.thip.ui.common.alarmpage.screen.AlarmScreen
import com.texthip.thip.ui.common.screen.RegisterBookScreen
import com.texthip.thip.ui.navigator.extensions.navigateFromNotification
import com.texthip.thip.ui.navigator.routes.CommonRoutes

// Common 관련 네비게이션
fun NavGraphBuilder.commonNavigation(
    navController: NavHostController,
    navigateBack: () -> Unit
) {
    // Alarm 화면
    composable<CommonRoutes.Alarm> {
        AlarmScreen(
            onNavigateBack = navigateBack,
            onNotificationNavigation = { response ->
                navController.navigateFromNotification(response)
            }
        )
    }

    // 책 요청 화면
    composable<CommonRoutes.RegisterBook> {
        RegisterBookScreen(
            onLeftClick = navigateBack
        )
    }
}