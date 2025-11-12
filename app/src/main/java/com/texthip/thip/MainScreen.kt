package com.texthip.thip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.texthip.thip.data.repository.NotificationRepository
import com.texthip.thip.ui.navigator.BottomNavigationBar
import com.texthip.thip.ui.navigator.MainNavHost
import com.texthip.thip.ui.navigator.extensions.isMainTabRoute
import com.texthip.thip.ui.navigator.extensions.navigateFromNotification
import com.texthip.thip.ui.navigator.routes.MainTabRoutes
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MainScreenEntryPoint {
    fun notificationRepository(): NotificationRepository
}

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    notificationData: MainActivity.NotificationData? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var feedReselectionTrigger by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    // 처리된 알림 ID 추적
    var processedNotificationId by remember { mutableStateOf<String?>(null) }

    // 푸시 알림에서 온 경우 알림 읽기 API 호출 및 네비게이션
    LaunchedEffect(notificationData?.notificationId, notificationData?.fromNotification) {
        val data = notificationData
        
        // 중복 처리 방지
        if (data?.notificationId == processedNotificationId) {
            return@LaunchedEffect
        }
        
        data?.let { notificationData ->
            if (notificationData.fromNotification && notificationData.notificationId != null) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        MainScreenEntryPoint::class.java
                    )
                    val notificationRepository = entryPoint.notificationRepository()

                    val notificationId = try {
                        notificationData.notificationId.toInt()
                    } catch (e: NumberFormatException) {
                        Log.e("MainScreen", "Invalid notification ID format: ${notificationData.notificationId}", e)
                        return@LaunchedEffect
                    }

                    val result = notificationRepository.checkNotification(notificationId)
                    
                    result.onSuccess { response ->
                        if (response != null) {
                            navController.navigateFromNotification(response)
                            notificationRepository.onNotificationReceived()
                            processedNotificationId = notificationData.notificationId
                        } else {
                            Log.w("MainScreen", "Notification check returned null response")
                        }
                    }.onFailure { exception ->
                        Log.e("MainScreen", "Failed to check notification: ${notificationData.notificationId}", exception)
                    }
                    
                } catch (e: Exception) {
                    Log.e("MainScreen", "Unexpected error processing notification: ${notificationData.notificationId}", e)
                }
            }
        }
    }

    val showBottomBar = currentDestination?.isMainTabRoute() ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    onTabReselected = { route ->
                        when (route) {
                            MainTabRoutes.Feed -> {
                                feedReselectionTrigger += 1
                            }

                            else -> {
                                // 다른 탭들은 향후 확장 가능
                            }
                        }
                    }
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MainNavHost(
                navController = navController,
                onNavigateToLogin = onNavigateToLogin,
                onFeedTabReselected = feedReselectionTrigger
            )
        }
    }
}