package com.texthip.thip.ui.group.room.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.texthip.thip.R
import com.texthip.thip.ui.common.cards.CardItemRoomSmall
import com.texthip.thip.ui.common.cards.CardRoomBook
import com.texthip.thip.ui.common.modal.DialogPopup
import com.texthip.thip.ui.common.modal.ToastWithDate
import com.texthip.thip.ui.common.topappbar.DefaultTopAppBar
import com.texthip.thip.ui.group.myroom.mock.GroupBookData
import com.texthip.thip.ui.group.myroom.mock.GroupBottomButtonType
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.group.myroom.mock.GroupRoomData
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import kotlinx.coroutines.delay

@Composable
fun GroupRoomRecruitScreen(
    detail: GroupRoomData,
    buttonType: GroupBottomButtonType,
    onRecommendationClick: (GroupCardItemRoomData) -> Unit = {},
    onParticipation: () -> Unit = {},   // 참여
    onCancelParticipation: () -> Unit = {}, // 참여 취소
    onCloseRecruitment: () -> Unit = {}, // 모집 마감
    onBackClick: () -> Unit = {} // 뒤로가기 추가
) {
    val context = LocalContext.current
    var currentButtonType by remember { mutableStateOf(buttonType) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.group_room_recruiting),
            contentDescription = "배경 이미지",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        // 그라데이션 페이드 오버레이 (상단과 하단이 더 어두움)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.Black.copy(alpha = 1f),
                            colors.Black.copy(alpha = 0.3f),
                            colors.Black.copy(alpha = 1f),
                            colors.Black.copy(alpha = 1f),
                            colors.Black.copy(alpha = 1f),
                            colors.Black.copy(alpha = 1f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column {
                DefaultTopAppBar(
                    isRightIconVisible = false,
                    isTitleVisible = false,
                    onLeftClick = onBackClick, // 뒤로가기 콜백 연결
                )

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = detail.title,
                            style = typography.bigtitle_b700_s22_h24,
                            color = colors.White
                        )
                        if (detail.isSecret) {
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "비밀방",
                                tint = colors.White
                            )
                        } else {
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_unlock),
                                contentDescription = "오픈방",
                                tint = colors.White
                            )
                        }
                    }

                    Text(
                        modifier = Modifier.padding(top = 40.dp),
                        text = stringResource(R.string.group_room_desc),
                        style = typography.menu_sb600_s14_h24,
                        color = colors.White,
                    )

                    Text(
                        text = detail.description,
                        style = typography.copy_r400_s12_h20,
                        color = colors.Grey,
                        modifier = Modifier
                            .padding(top = 5.dp, bottom = 20.dp)
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        //모집 기간
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar),
                                    contentDescription = "모임 활동기간",
                                    tint = colors.White
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.group_period),
                                    style = typography.view_m500_s12_h20,
                                    color = colors.White
                                )
                            }

                            Text(
                                modifier = Modifier.padding(top = 12.dp),
                                text = stringResource(
                                    R.string.group_room_period,
                                    detail.startDate,
                                    detail.endDate
                                ),
                                style = typography.timedate_r400_s11,
                                color = colors.Grey
                            )
                        }

                        //참여 인원
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(end = 18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_group),
                                    contentDescription = "참여 중인 독서 메이트",
                                    tint = colors.White
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.group_mate),
                                    style = typography.view_m500_s12_h20,
                                    color = colors.White
                                )
                            }
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.group_room_screen_participant_count,
                                        detail.members
                                    ),
                                    style = typography.menu_sb600_s12,
                                    color = colors.White
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = stringResource(
                                        R.string.group_room_screen_participant_count_max,
                                        detail.maxMembers
                                    ),
                                    style = typography.info_m500_s12,
                                    color = colors.Grey
                                )
                            }
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp, bottom = 30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .background(colors.Grey03, shape = RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row {
                                Text(
                                    text = stringResource(R.string.group_recruiting),
                                    style = typography.info_m500_s12,
                                    color = colors.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = stringResource(
                                        R.string.group_room_screen_end_date,
                                        detail.daysLeft
                                    ),
                                    style = typography.info_m500_s12,
                                    color = colors.NeonGreen
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(
                            Modifier
                                .background(colors.Grey03, shape = RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row {
                                Text(
                                    text = stringResource(R.string.group_genre),
                                    style = typography.info_m500_s12,
                                    color = colors.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = detail.genre,
                                    style = typography.info_m500_s12,
                                    color = colors.genreColor
                                )
                            }
                        }
                    }

                    //읽을 책 정보
                    CardRoomBook(
                        title = detail.bookData.title,
                        author = detail.bookData.author,
                        publisher = detail.bookData.publisher,
                        description = detail.bookData.description,
                        imageRes = detail.bookData.imageRes
                    )

                    // 추천 모임방이 있을 때만 표시
                    if (detail.recommendations.isNotEmpty()) {
                        Text(
                            modifier = Modifier.padding(top = 40.dp),
                            text = stringResource(R.string.group_recommend),
                            style = typography.smalltitle_sb600_s18_h24,
                            color = colors.White
                        )

                        //추천 모임방
                        LazyRow(
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(detail.recommendations) { rec ->
                                CardItemRoomSmall(
                                    title = rec.title,
                                    participants = rec.participants,
                                    maxParticipants = rec.maxParticipants,
                                    endDate = rec.endDate,
                                    imageRes = rec.imageRes,
                                    onClick = { onRecommendationClick(rec) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 하단 버튼
        val buttonText = when (currentButtonType) {
            GroupBottomButtonType.JOIN -> stringResource(R.string.group_room_screen_participant)
            GroupBottomButtonType.CANCEL -> stringResource(R.string.group_room_screen_cancel)
            GroupBottomButtonType.CLOSE -> stringResource(R.string.group_room_screen_end)
        }

        Button(
            onClick = {
                when (currentButtonType) {
                    GroupBottomButtonType.JOIN -> {
                        onParticipation() // 외부 콜백 호출
                        showToast = true
                        toastMessage = context.getString(R.string.group_participant_complete_alarm)
                        currentButtonType = GroupBottomButtonType.CANCEL
                    }

                    GroupBottomButtonType.CANCEL -> {
                        dialogTitle = context.getString(R.string.group_participant_cancel_popup)
                        dialogDescription =
                            context.getString(R.string.group_participant_cancel_comment)
                        pendingAction = {
                            onCancelParticipation()
                            showToast = true
                            toastMessage =
                                context.getString(R.string.group_participant_cancel_alarm)
                            currentButtonType = GroupBottomButtonType.JOIN
                        }
                        showDialog = true
                    }

                    GroupBottomButtonType.CLOSE -> {
                        dialogTitle = context.getString(R.string.group_participant_close_popup)
                        dialogDescription =
                            context.getString(R.string.group_participant_close_comment)
                        pendingAction = {
                            onCloseRecruitment()
                            showToast = true
                            toastMessage = context.getString(R.string.group_participant_close_alarm)
                        }
                        showDialog = true
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.Purple
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(
                text = buttonText,
                style = typography.smalltitle_sb600_s18_h24,
                color = colors.White
            )
        }

        // 토스트 팝업
        if (showToast) {
            ToastWithDate(
                message = toastMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .zIndex(2f)
            )
        }

        if (showDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.Black.copy(alpha = 0.5f))
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                DialogPopup(
                    title = dialogTitle,
                    description = dialogDescription,
                    onConfirm = {
                        showDialog = false
                        pendingAction?.invoke()
                    },
                    onCancel = {
                        showDialog = false
                        pendingAction = null
                    }
                )
            }
        }
    }

    // 토스트 3초
    LaunchedEffect(showToast) {
        if (showToast) {
            delay(3000)
            showToast = false
        }
    }
}

@Preview(name = "참여 버튼 상태")
@Composable
fun GroupRoomRecruitScreenPreviewJoin() {
    ThipTheme {
        val recommendations = listOf(
            GroupCardItemRoomData(
                id = 1,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 19,
                maxParticipants = 25,
                isRecruiting = true,
                endDate = 2,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 2,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 12,
                maxParticipants = 16,
                isRecruiting = true,
                endDate = 6,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 3,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 30,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 0,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 4,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 10,
                maxParticipants = 12,
                isRecruiting = true,
                endDate = 8,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 5,
                title = "에세이 나눔방",
                participants = 14,
                maxParticipants = 20,
                isRecruiting = true,
                endDate = 4,
                genreIndex = 0
            )
        )

        val bookData = GroupBookData(
            title = "심장보다 단단한 토마토 한 알",
            author = "고선지",
            publisher = "푸른출판사",
            description = "'시집만 읽는 사람들' 3월 모임에서 읽는 시집. 상처받고 단단해진 마음을 담은 감동적인 시와 해설이 어우러진 책으로, 읽는 이로 하여금 자신의 이야기를 투영하게 하는 힘이 있다.",
            imageRes = R.drawable.bookcover_sample
        )

        val detailJoin = GroupRoomData(
            id = 1,
            title = "시집만 읽는 사람들 3월",
            isSecret = true,
            description = "'시집만 읽는 사람들' 3월 모임입니다. 이번 달 모임에서는 심장보다 단단한 토마토 한 알을 함께 읽어요.",
            startDate = "2025.01.12",
            endDate = "2025.02.12",
            members = 22,
            maxMembers = 30,
            daysLeft = 4,
            genre = "문학",
            bookData = bookData,
            recommendations = recommendations
        )

        GroupRoomRecruitScreen(
            detail = detailJoin,
            buttonType = GroupBottomButtonType.JOIN,
            onRecommendationClick = {},
            onParticipation = {},
            onCancelParticipation = {},
            onCloseRecruitment = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "참여 취소 버튼 상태")
@Composable
fun GroupRoomRecruitScreenPreviewCancel() {
    ThipTheme {
        val recommendations = listOf(
            GroupCardItemRoomData(
                id = 6,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 19,
                maxParticipants = 25,
                isRecruiting = true,
                endDate = 2,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 7,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 12,
                maxParticipants = 16,
                isRecruiting = true,
                endDate = 6,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 8,
                title = "에세이 나눔방",
                participants = 14,
                maxParticipants = 20,
                isRecruiting = true,
                endDate = 4,
                genreIndex = 0
            )
        )

        val bookData = GroupBookData(
            title = "심장보다 단단한 토마토 한 알",
            author = "고선지",
            publisher = "푸른출판사",
            description = "'시집만 읽는 사람들' 3월 모임에서 읽는 시집. 상처받고 단단해진 마음을 담은 감동적인 시와 해설이 어우러진 책으로, 읽는 이로 하여금 자신의 이야기를 투영하게 하는 힘이 있다.",
            imageRes = R.drawable.bookcover_sample
        )

        val detailCancel = GroupRoomData(
            id = 2,
            title = "시집만 읽는 사람들 3월",
            isSecret = true,
            description = "'시집만 읽는 사람들' 3월 모임입니다. 이번 달 모임에서는 심장보다 단단한 토마토 한 알을 함께 읽어요.",
            startDate = "2025.01.12",
            endDate = "2025.02.12",
            members = 23, // 참여 후 인원 증가
            maxMembers = 30,
            daysLeft = 4,
            genre = "고전 문학",
            bookData = bookData,
            recommendations = recommendations
        )

        GroupRoomRecruitScreen(
            detail = detailCancel,
            buttonType = GroupBottomButtonType.CANCEL,
            onRecommendationClick = {},
            onParticipation = {},
            onCancelParticipation = {},
            onCloseRecruitment = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "모집 마감 버튼 상태")
@Composable
fun GroupRoomRecruitScreenClose() {
    ThipTheme {
        val recommendations = listOf(
            GroupCardItemRoomData(
                id = 9,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 19,
                maxParticipants = 25,
                isRecruiting = true,
                endDate = 2,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 10,
                title = "일본 소설 좋아하는 사람들 일본 소설 좋아하는 사람들",
                participants = 12,
                maxParticipants = 16,
                isRecruiting = true,
                endDate = 6,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 11,
                title = "미스터리 소설 탐구",
                participants = 8,
                maxParticipants = 15,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0
            )
        )

        val bookData = GroupBookData(
            title = "심장보다 단단한 토마토 한 알",
            author = "고선지",
            publisher = "푸른출판사",
            description = "'시집만 읽는 사람들' 3월 모임에서 읽는 시집. 상처받고 단단해진 마음을 담은 감동적인 시와 해설이 어우러진 책으로, 읽는 이로 하여금 자신의 이야기를 투영하게 하는 힘이 있다.",
            imageRes = R.drawable.bookcover_sample
        )

        val detailClose = GroupRoomData(
            id = 3,
            title = "시집만 읽는 사람들 3월",
            isSecret = false, // 오픈방으로 변경
            description = "'시집만 읽는 사람들' 3월 모임입니다. 이번 달 모임에서는 심장보다 단단한 토마토 한 알을 함께 읽어요. 모임장이 모집을 마감할 수 있는 상태입니다.",
            startDate = "2025.01.12",
            endDate = "2025.02.12",
            members = 15, // 적절한 인원
            maxMembers = 30,
            daysLeft = 7, // 마감일이 조금 더 남음
            genre = "문학",
            bookData = bookData,
            recommendations = recommendations
        )

        GroupRoomRecruitScreen(
            detail = detailClose,
            buttonType = GroupBottomButtonType.CLOSE,
            onRecommendationClick = {},
            onParticipation = {},
            onCancelParticipation = {},
            onCloseRecruitment = {},
            onBackClick = {}
        )
    }
}