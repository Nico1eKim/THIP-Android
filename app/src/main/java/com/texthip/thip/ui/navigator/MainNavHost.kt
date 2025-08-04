package com.texthip.thip.ui.navigator

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.texthip.thip.ui.navigator.routes.MainTabRoutes
import com.texthip.thip.ui.navigator.navigations.feedNavigation
import com.texthip.thip.ui.navigator.navigations.groupNavigation
import com.texthip.thip.ui.navigator.navigations.myPageNavigation
import com.texthip.thip.ui.navigator.navigations.searchNavigation
import com.texthip.thip.ui.navigator.navigations.commonNavigation

// 메인 네비게이션
@Composable
fun MainNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = MainTabRoutes.Feed) {
        feedNavigation(navController)
        groupNavigation(
            navController = navController,
            navigateBack = navController::popBackStack
        )
        searchNavigation(navController)
        myPageNavigation(navController)
        commonNavigation(
            navController = navController,
            navigateBack = navController::popBackStack
        )
    }
}