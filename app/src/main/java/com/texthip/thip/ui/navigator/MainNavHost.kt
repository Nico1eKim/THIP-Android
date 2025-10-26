package com.texthip.thip.ui.navigator

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.texthip.thip.ui.navigator.navigations.commonNavigation
import com.texthip.thip.ui.navigator.navigations.feedNavigation
import com.texthip.thip.ui.navigator.navigations.groupNavigation
import com.texthip.thip.ui.navigator.navigations.myPageNavigation
import com.texthip.thip.ui.navigator.navigations.searchNavigation
import com.texthip.thip.ui.navigator.routes.MainTabRoutes

// 메인 네비게이션
@Composable
fun MainNavHost(
    navController: NavHostController,
    onNavigateToLogin: () -> Unit,
    onFeedTabReselected: Int = 0
) {
    NavHost(
        navController = navController,
        startDestination = MainTabRoutes.Feed,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        feedNavigation(
            navController = navController,
            navigateBack = navController::popBackStack,
            onFeedTabReselected = onFeedTabReselected
        )
        groupNavigation(
            navController = navController,
            navigateBack = navController::popBackStack
        )
        searchNavigation(navController)
        myPageNavigation(
            navController = navController,
            onNavigateToLogin = onNavigateToLogin
        )
        commonNavigation(
            navController = navController,
            navigateBack = navController::popBackStack
        )
    }
}