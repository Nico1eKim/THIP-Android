package com.texthip.thip.ui.mypage.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.texthip.thip.R
import com.texthip.thip.ui.common.buttons.ToggleSwitchButton
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.mypage.viewmodel.MypageNotificationEditUiState
import com.texthip.thip.ui.mypage.viewmodel.MypageNotificationEditViewModel
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import com.texthip.thip.utils.permission.NotificationPermissionUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyPageNotificationEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: MypageNotificationEditViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var toastMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var toastDateTime by rememberSaveable { mutableStateOf("") }

    // 알림 권한 요청 런처
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // 권한이 허용되면 알림 활성화
                viewModel.onNotificationToggle(true)
                toastMessage = "push_on"
                val dateFormat = SimpleDateFormat("yyyy년 M월 d일 H시 m분", Locale.KOREAN)
                toastDateTime = dateFormat.format(Date())
            } else {
                // 권한이 거부되면 토스트 메시지 표시
                toastMessage = "permission_denied"
                val dateFormat = SimpleDateFormat("yyyy년 M월 d일 H시 m분", Locale.KOREAN)
                toastDateTime = dateFormat.format(Date())
            }
        }
    )

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            toastMessage = null
        }
    }

    MyPageNotificationEditContent(
        uiState = uiState,
        toastMessage = toastMessage,
        toastDateTime = toastDateTime,
        onNavigateBack = onNavigateBack,
        onNotificationToggle = { enabled ->
            if (enabled) {
                // 알림을 켜려고 할 때 권한 확인
                if (NotificationPermissionUtils.shouldRequestNotificationPermission(context)) {
                    // 권한이 필요하면 권한 요청
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // 권한이 이미 있거나 필요없으면 바로 설정 변경
                    viewModel.onNotificationToggle(enabled)
                    toastMessage = "push_on"
                    val dateFormat = SimpleDateFormat("yyyy년 M월 d일 H시 m분", Locale.KOREAN)
                    toastDateTime = dateFormat.format(Date())
                }
            } else {
                // 알림을 끄는 경우는 권한 체크 없이 바로 설정 변경
                viewModel.onNotificationToggle(enabled)
                toastMessage = "push_off"
                val dateFormat = SimpleDateFormat("yyyy년 M월 d일 H시 m분", Locale.KOREAN)
                toastDateTime = dateFormat.format(Date())
            }
        }
    )
}

@Composable
fun MyPageNotificationEditContent(
    uiState: MypageNotificationEditUiState,
    toastMessage: String?,
    toastDateTime: String,
    onNavigateBack: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 2000)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 2000)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 15.dp, vertical = 15.dp)
                .zIndex(1f)
        ) {
            toastMessage?.let { message ->
                ToastWithDate(
                    message = when (message) {
                        "push_on" -> stringResource(R.string.push_on)
                        "push_off" -> stringResource(R.string.push_off)
                        "permission_denied" -> stringResource(R.string.notification_permission_required)
                        else -> stringResource(R.string.push_off)
                    },
                    date = toastDateTime,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Column(
            Modifier
                .background(colors.Black)
                .fillMaxSize()
        ) {
            DefaultTopAppBar(
                title = stringResource(R.string.notification_settings),
                onLeftClick = onNavigateBack,
            )
            Spacer(modifier = Modifier.height(40.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.push_notification),
                    style = typography.smalltitle_sb600_s18_h24,
                    color = colors.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.notification_description),
                        style = typography.menu_r400_s14_h24,
                        color = colors.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .weight(1f)
                    )
                    ToggleSwitchButton(
                        isChecked = uiState.isNotificationEnabled,
                        onToggleChange = onNotificationToggle
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MypageNotificationEditContentPrev() {
    MyPageNotificationEditContent(
        uiState = MypageNotificationEditUiState(
            isNotificationEnabled = true,
            isLoading = false,
            isUpdating = false,
            errorMessage = null
        ),
        toastMessage = null,
        toastDateTime = "",
        onNavigateBack = {},
        onNotificationToggle = {}
    )
}