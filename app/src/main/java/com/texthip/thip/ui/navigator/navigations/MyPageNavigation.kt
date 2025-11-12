package com.texthip.thip.ui.navigator.navigations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.texthip.thip.ui.mypage.screen.DeleteAccountScreen
import com.texthip.thip.ui.mypage.screen.EditProfileScreen
import com.texthip.thip.ui.mypage.screen.MyPageNotificationEditScreen
import com.texthip.thip.ui.mypage.screen.MyPageScreen
import com.texthip.thip.ui.mypage.screen.MypageCustomerServiceScreen
import com.texthip.thip.ui.mypage.screen.MypageSaveScreen
import com.texthip.thip.ui.navigator.extensions.navigateToBookDetail
import com.texthip.thip.ui.navigator.extensions.navigateToCustomerService
import com.texthip.thip.ui.navigator.extensions.navigateToEditProfile
import com.texthip.thip.ui.navigator.extensions.navigateToFeedComment
import com.texthip.thip.ui.navigator.extensions.navigateToLeaveThipScreen
import com.texthip.thip.ui.navigator.extensions.navigateToNotificationSettings
import com.texthip.thip.ui.navigator.extensions.navigateToSavedFeeds
import com.texthip.thip.ui.navigator.routes.MainTabRoutes
import com.texthip.thip.ui.navigator.routes.MyPageRoutes

// MyPage
fun NavGraphBuilder.myPageNavigation(
    navController: NavHostController,
    onNavigateToLogin: () -> Unit
) {
    composable<MainTabRoutes.MyPage> {
        MyPageScreen(
            navController = navController,
            onNavigateToEditProfile = { navController.navigateToEditProfile() },
            onNavigateToSavedFeeds = { navController.navigateToSavedFeeds() },
            onNavigateToNotificationSettings = { navController.navigateToNotificationSettings() },
            onCustomerService = {navController.navigateToCustomerService()},
            onDeleteAccount = { navController.navigateToLeaveThipScreen() },
            onNavigateToLogin = onNavigateToLogin
        )
    }

    composable<MyPageRoutes.Edit> {
        EditProfileScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
    composable<MyPageRoutes.Save> {
        MypageSaveScreen(
            onNavigateBack = { navController.popBackStack() },
            onBookClick = { isbn ->
                navController.navigateToBookDetail(isbn)
            },
            onFeedClick = { feedId ->
                navController.navigateToFeedComment(feedId)
            }
        )
    }
    composable<MyPageRoutes.NotificationEdit> {
        MyPageNotificationEditScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
    composable<MyPageRoutes.LeaveThip> {
        DeleteAccountScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLogin = onNavigateToLogin
        )
    }
    composable<MyPageRoutes.CustomerService> {
        MypageCustomerServiceScreen (
            onNavigateBack = { navController.popBackStack() }
        )
    }
}