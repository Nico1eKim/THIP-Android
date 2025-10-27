package com.texthip.thip.ui.group.note.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.texthip.thip.R
import com.texthip.thip.ui.common.modal.DialogPopup
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.group.note.viewmodel.GroupNoteAiUiState
import com.texthip.thip.ui.group.note.viewmodel.GroupNoteAiViewModel
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import kotlinx.coroutines.delay

@Composable
fun GroupNoteAiScreen(
    roomId: Int,
    onBackClick: () -> Unit,
    viewModel: GroupNoteAiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    var showToast by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = roomId) {
        viewModel.generateAiReview(roomId)
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(3000L)
            showToast = false
        }
    }

    GroupNoteAiContent(
        uiState = uiState,
        showToast = showToast,
        showExitDialog = showExitDialog,
        onBackClick = { showExitDialog = true },
        onCopyClick = { text ->
            clipboardManager.setText(AnnotatedString(text))
            showToast = true
        },
        onConfirmExit = onBackClick,
        onDismissExitDialog = { showExitDialog = false }
    )
}

@Composable
fun GroupNoteAiContent(
    uiState: GroupNoteAiUiState,
    showToast: Boolean = false,
    showExitDialog: Boolean = false,
    onBackClick: () -> Unit,
    onCopyClick: (String) -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit
) {
    val isOverlayVisible = showExitDialog

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isOverlayVisible) Modifier.blur(5.dp) else Modifier)
        ) {
            DefaultTopAppBar(
                title = stringResource(R.string.ai_book_review_title),
                onLeftClick = onBackClick
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    // 로딩 중
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.ai_review_loading),
                            style = typography.smalltitle_sb600_s18_h24,
                            color = colors.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.ai_review_loading_subtext),
                            style = typography.copy_r400_s14,
                            color = colors.Grey,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (uiState.aiReviewText != null) {
                    // 로딩 완료
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 26.dp, end = 26.dp, top = 10.dp, bottom = 50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_information),
                                contentDescription = "Done Icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )

                            Text(
                                text = stringResource(R.string.ai_review_done_info),
                                style = typography.info_r400_s12,
                                color = colors.Grey01
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.aiReviewText,
                            style = typography.feedcopy_r400_s14_h20,
                            color = colors.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(colors.Purple)
                            .clickable { onCopyClick(uiState.aiReviewText) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.copy_to_clipboard),
                            style = typography.smalltitle_sb600_s18_h24,
                            color = colors.White
                        )
                    }
                } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error,
                            style = typography.copy_r400_s14,
                            color = colors.Grey,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (showExitDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                DialogPopup(
                    title = stringResource(R.string.ai_review_dialog_title),
                    description = stringResource(R.string.ai_review_exit_dialog_description),
                    onConfirm = onConfirmExit,
                    onCancel = onDismissExitDialog
                )
            }
        }

        AnimatedVisibility(
            visible = showToast,
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .zIndex(2f)
        ) {
            ToastWithDate(
                message = stringResource(R.string.copy_to_clipboard_done)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupNoteAiScreenLoadingPreview() {
    ThipTheme {
        GroupNoteAiContent(
            uiState = GroupNoteAiUiState(isLoading = true),
            onBackClick = {},
            onCopyClick = {},
            onConfirmExit = {},
            onDismissExitDialog = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupNoteAiScreenDonePreview() {
    ThipTheme {
        GroupNoteAiContent(
            uiState = GroupNoteAiUiState(isLoading = false, aiReviewText = "레이 커즈와일의 마침내 특이점이 시작된다는 읽는 내내 머릿속이 폭발하는 느낌이었다. 인공지능, 나노기술, 생명공학이 동시에 발전해서 결국 인간의 지능과 기계를 융합하는 시대가 온다는 주장인데, 솔직히 처음엔 SF소설 같은 이야기로 느껴졌다."),
            onBackClick = {},
            onCopyClick = {},
            onConfirmExit = {},
            onDismissExitDialog = {}
        )
    }
}