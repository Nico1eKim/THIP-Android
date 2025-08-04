package com.texthip.thip.ui.navigator.navigations

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.texthip.thip.ui.common.alarmpage.screen.AlarmScreen
import com.texthip.thip.ui.common.alarmpage.viewmodel.AlarmViewModel
import com.texthip.thip.ui.navigator.routes.CommonRoutes

// Common 관련 네비게이션
fun NavGraphBuilder.commonNavigation(
    navController: NavHostController,
    navigateBack: () -> Unit
) {
    // Alarm 화면
    composable<CommonRoutes.Alarm> {
        val alarmViewModel: AlarmViewModel = viewModel()
        val alarmItems by alarmViewModel.alarmItems.collectAsState()
        
        AlarmScreen(
            alarmItems = alarmItems,
            onCardClick = { alarmViewModel.onCardClick(it) },
            onNavigateBack = navigateBack
        )
    }
}