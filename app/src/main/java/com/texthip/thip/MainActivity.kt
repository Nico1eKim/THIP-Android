package com.texthip.thip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.texthip.thip.data.manager.AuthStateManager
import com.texthip.thip.data.manager.TokenManager
import com.texthip.thip.ui.navigator.navigations.authNavigation
import com.texthip.thip.ui.navigator.routes.CommonRoutes
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.utils.permission.NotificationPermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var authStateManager: AuthStateManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    private var notificationData by mutableStateOf<NotificationData?>(null)

    data class NotificationData(
        val notificationId: String?,
        val fromNotification: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 앱 시작 시 알림 권한 요청
        requestNotificationPermissionIfNeeded()

        // 푸시 알림에서 온 데이터 처리
        handleNotificationIntent(intent)

        setContent {
            ThipTheme {
                RootNavHost(
                    authStateManager = authStateManager,
                    notificationData = notificationData
                )
            }
        }
//        getKakaoKeyHash(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // 새로운 Intent가 들어올 때 (백그라운드에서 알림 클릭 시)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        Log.d("MainActivity", "Handling notification intent with extras: ${intent.extras?.keySet()}")
        
        val customNotificationId = intent.getStringExtra("notification_id")
        val customFromNotification = intent.getBooleanExtra("from_notification", false)

        // FCM 백그라운드 알림에서 온 데이터 확인 (시스템이 자동 생성한 알림의 경우)
        val fcmNotificationId = intent.getStringExtra("gcm.notification.data.notificationId")
            ?: intent.getStringExtra("notificationId")

        var newNotificationData: NotificationData? = null

        // 커스텀 알림에서 온 경우 (포그라운드에서 생성된 알림)
        if (customFromNotification && customNotificationId != null) {
            Log.d("MainActivity", "Processing custom notification: $customNotificationId")
            newNotificationData = NotificationData(customNotificationId, customFromNotification)
            
            // Intent extras 완전 제거
            cleanupNotificationExtras(intent, listOf("notification_id", "from_notification"))
        }
        // FCM 백그라운드 시스템 알림에서 온 경우
        else if (fcmNotificationId != null) {
            Log.d("MainActivity", "Processing FCM notification: $fcmNotificationId")
            newNotificationData = NotificationData(fcmNotificationId, true)
            
            // Intent extras 완전 제거
            cleanupNotificationExtras(intent, listOf(
                "gcm.notification.data.notificationId", 
                "notificationId"
            ))
        }

        // 새로운 알림 데이터가 있고, 기존 데이터와 다른 경우에만 업데이트
        if (newNotificationData != null && newNotificationData != notificationData) {
            Log.d("MainActivity", "Setting new notification data: ${newNotificationData.notificationId}")
            notificationData = newNotificationData
        } else if (newNotificationData != null) {
            Log.d("MainActivity", "Notification data unchanged, skipping update")
        }
    }

    private fun cleanupNotificationExtras(intent: Intent, keys: List<String>) {
        keys.forEach { key ->
            try {
                intent.removeExtra(key)
                Log.v("MainActivity", "Removed extra: $key")
            } catch (e: Exception) {
                Log.w("MainActivity", "Failed to remove extra: $key", e)
            }
        }
        
        // Intent 플래그도 정리
        intent.replaceExtras(intent.extras)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (NotificationPermissionUtils.shouldRequestNotificationPermission(this)) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun RootNavHost(
    authStateManager: AuthStateManager,
    notificationData: MainActivity.NotificationData? = null
) {
    val navController = rememberNavController()
    val firebaseAnalytics = Firebase.analytics

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            // 목적지의 route가 null이 아닐 경우에만 이벤트를 로깅
            destination.route?.let { route ->
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                    param(FirebaseAnalytics.Param.SCREEN_NAME, route)
                    param(FirebaseAnalytics.Param.SCREEN_CLASS, route)
                }
                Log.d("GA_Tracker", "Screen viewed: $route")
            }
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        authStateManager.tokenExpiredEvent.collectLatest {
            navController.navigate(CommonRoutes.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }


    NavHost(
        navController = navController,
        startDestination = CommonRoutes.Splash
    ) {
        // --- 인증 관련 화면들 ---
        authNavigation(
            onNavigateToLogin = {
                navController.navigate(CommonRoutes.Login) {
                    popUpTo(CommonRoutes.Splash) { inclusive = true }
                }
            },
            onNavigateToHome = {
                navController.navigate(CommonRoutes.Main) {
                    popUpTo(CommonRoutes.Splash) { inclusive = true }
                }
            },
            onNavigateToSignup = {
                navController.navigate(CommonRoutes.SignupFlow)
            },
            onNavigateToMainAfterSignup = {
                navController.navigate(CommonRoutes.Main) { // 혹은 MainGraph
                    popUpTo(CommonRoutes.Splash) { inclusive = true }
                }
            },
            navController = navController
        )


        // --- 메인 관련 화면들 ---
        composable<CommonRoutes.Main> { // MainScreen으로 가는 경로 추가
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(CommonRoutes.Login) {
                        // 메인 화면으로 돌아올 수 없도록 모든 화면 기록 삭제
                        popUpTo(CommonRoutes.Main) {
                            inclusive = true
                        }
                    }
                },
                notificationData = notificationData
            )
        }
    }
}