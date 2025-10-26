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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.texthip.thip.R
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import kotlinx.coroutines.delay

private const val DUMMY_AI_REVIEW =
    "레이 커즈와일의 마침내 특이점이 시작된다는 읽는 내내 머릿속이 폭발하는 느낌이었다. 인공지능, 나노기술, 생명공학이 동시에 발전해서 결국 인간의 지능과 기계를 융합하는 시대가 온다는 주장인데, 솔직히 처음엔 SF소설 같은 이야기로 느껴졌다. \n" +
            "\n" +
            "하지만 커즈와일이 데이터와 과학적 근거를 차근차근 쌓아가며 기술 발전이 기하급수적이라는 걸 보여줄 때는 설득력이 꽤 컸다. 특히 인간 수명 연장과 의식 업로드에 대한 부분은 조금 무섭기도 하고 설레기도 했다. \n" +
            "\n" +
            "이 책이 단순히 기술 낙관주의가 아니라, 우리가 맞이할 변화에 대해 어떤 윤리적 기준과 사회적 합의가 필요한지 고민하게 만든 점이 좋았다. 읽고 나니 ‘미래는 멀리 있지 않다’는 말이 실감났다. 당장 내가 AI를 어떻게 활용하고, 기술과 함께 어떻게 성장할지 스스로 계획을 세우고 싶어졌다. 띱에서 다른 사람들은 이 책을 읽고 어떤 생각을 했을지 궁금하다. \n" +
            "\n" +
            "레이 커즈와일의 마침내 특이점이 시작된다는 읽는 내내 머릿속이 폭발하는 느낌이었다. 인공지능, 나노기술, 생명공학이 동시에 발전해서 결국 인간의 지능과 기계를 융합하는 시대가 온다는 주장인데, 솔직히 처음엔 SF소설 같은 이야기로 느껴졌다. \n" +
            "\n" +
            "하지만 커즈와일이 데이터와 과학적 근거를 차근차근 쌓아가며 기술 발전이 기하급수적이라는 걸 보여줄 때는 설득력이 꽤 컸다. 특히 인간 수명 연장과 의식 업로드에 대한 부분은 조금 무섭기도 하고 설레기도 했다. 이 책이 단순히 기술 낙관주의가 아니라, 우리가 맞이할 변화에 대해 어떤 윤리적 기준과 사회적 합의가 필요한지 고민하게 만든 점이 좋았다. 읽고 나니 ‘미래는 멀리 있지 않다’는 말이 실감났다. 당장 내가 AI를 어떻게 활용하고, 기술과 함께 어떻게 성장할지 스스로 계획을 세우고 싶어졌다. 띱에서 다른 사람들은 이 책 읽고 어떤 생각을 했을지 궁금하다. 레이 커즈와일의 마침내 특이점이 시작된다는 읽는 내내 머릿속이 폭발하는 느낌이었다. 인공지능, 나노기술, 생명공학이 동시에 발전해서 결국 인간의 지능과 기계를 융합하는 시대가 온다는 주장인데, 솔직히 처음엔 SF소설 같은 이야기로 느껴졌다. 하지만 커즈와일이 데이터와 과학적 근거를 차근차근 쌓아가며 기술 발전이 기하급수적이라는 걸 보여줄 때는 설득력이 꽤 컸다. 특히 인간 수명 연장과 의식 업로드에 대한 부분은 조금 무섭기도 하고 설레기도 했다. 이 책이 단순히 기술 낙관주의가 아니라, 우리가 맞이할 변화에 대해 어떤 윤리적 기준과 사회적 합의가 필요한지 고민하게 만든 점이 좋았다. 읽고 나니 ‘미래는 멀리 있지 않다’는 말이 실감났다. 당장 내가 AI를 어떻게 활용하고, 기술과 함께 어떻게 성장할지 스스로 계획을 세우고 싶어졌다. 띱에서 다른 사람들은 이 책 읽고 어떤 생각을 했을지 궁금하다."

@Composable
fun GroupNoteAiScreen(
    roomId: Int, // TODO: 이 roomId로 ViewModel에서 AI 독후감 요청
    onBackClick: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var aiReviewText by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current

    var showToast by remember { mutableStateOf(false) }

    // TODO: HiltViewModel을 사용해 실제 데이터 로직 구현
    LaunchedEffect(key1 = roomId) {
        delay(3000) // 3초 딜레이 (네트워크 요청 시뮬레이션)
        aiReviewText = DUMMY_AI_REVIEW
        isLoading = false
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(3000L)
            showToast = false // onHideToast() 역할
        }
    }

    GroupNoteAiContent(
        isLoading = isLoading,
        aiReviewText = aiReviewText,
        showToast = showToast,
        onBackClick = onBackClick,
        onCopyClick = { text ->
            clipboardManager.setText(AnnotatedString(text))
            showToast = true
        }
    )
}

@Composable
fun GroupNoteAiContent(
    isLoading: Boolean,
    aiReviewText: String?,
    showToast: Boolean = false,
    onBackClick: () -> Unit,
    onCopyClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DefaultTopAppBar(
                title = stringResource(R.string.ai_book_review_title),
                onLeftClick = onBackClick
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
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
                } else if (aiReviewText != null) {
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
                            text = aiReviewText,
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
                            .clickable { onCopyClick(aiReviewText) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.copy_to_clipboard),
                            style = typography.smalltitle_sb600_s18_h24,
                            color = colors.White
                        )
                    }
                }
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
            isLoading = true,
            aiReviewText = null,
            onBackClick = {},
            onCopyClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupNoteAiScreenDonePreview() {
    ThipTheme {
        GroupNoteAiContent(
            isLoading = false,
            aiReviewText = DUMMY_AI_REVIEW,
            onBackClick = {},
            onCopyClick = {}
        )
    }
}