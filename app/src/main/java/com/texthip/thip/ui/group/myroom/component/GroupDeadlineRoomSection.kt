package com.texthip.thip.ui.group.myroom.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.common.buttons.GenreChipRow
import com.texthip.thip.ui.common.cards.CardItemRoom
import com.texthip.thip.ui.group.myroom.mock.GroupCardItemRoomData
import com.texthip.thip.ui.group.myroom.mock.GroupRoomSectionData
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GroupRoomDeadlineSection(
    roomSections: List<GroupRoomSectionData>,
    onRoomClick: (GroupCardItemRoomData) -> Unit
) {
    val sideMargin = 30.dp

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { roomSections.size }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val horizontalPadding = sideMargin
            val cardWidth = maxWidth - (horizontalPadding * 2)
            val scale = 0.94f
            val desiredGap = 12.dp // TODO: 이 부분을 10dp로 하면 양 옆의 카드에 살짝 다음 내용이 보여서 12정도가 어떤지

            val pageSpacing = (-(cardWidth - (cardWidth * scale)) / 2) + desiredGap

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 30.dp),
                pageSpacing = pageSpacing,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val section = roomSections[page]
                var selectedGenre by remember { mutableIntStateOf(0) }

                val isCurrent = pagerState.currentPage == page
                val scale = if (isCurrent) 1f else 0.94f

                Box(
                    modifier = Modifier
                        .width(cardWidth)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .fillMaxHeight()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colors.White.copy(0.25f),
                                    colors.Black.copy(0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(vertical = 20.dp, horizontal = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = section.title,
                            style = typography.title_b700_s20_h24,
                            color = colors.White
                        )
                        Spacer(Modifier.height(40.dp))

                        GenreChipRow(
                            genres = section.genres,
                            selectedIndex = selectedGenre,
                            onSelect = { idx -> selectedGenre = idx }
                        )
                        Spacer(Modifier.height(20.dp))

                        val cards = section.rooms.filter { it.genreIndex == selectedGenre }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(584.dp)
                        ) {
                            if (cards.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Spacer(Modifier.height(40.dp))
                                    Text(
                                        text = stringResource(R.string.group_no_room_exist),
                                        style = typography.smalltitle_sb600_s16_h20,
                                        color = colors.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.group_no_room_error_comment),
                                        style = typography.copy_r400_s14,
                                        color = colors.Grey,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    cards.forEach { room ->
                                        CardItemRoom(
                                            title = room.title,
                                            participants = room.participants,
                                            maxParticipants = room.maxParticipants,
                                            isRecruiting = room.isRecruiting,
                                            endDate = room.endDate,
                                            imageRes = room.imageRes,
                                            onClick = { onRoomClick(room) },
                                            hasBorder = true,
                                        )
                                    }
                                }
                            }

                            if (cards.size < 4) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f, fill = true)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview()
@Composable
fun PreviewGroupRoomPagerSection() {
    ThipTheme {
        val genres = listOf("문학", "과학·IT", "사회과학", "인문학", "예술")

        // 마감 임박한 독서 모임방
        val deadlineRooms = listOf(
            GroupCardItemRoomData(
                id = 1,
                title = "시집만 읽는 사람들 3월",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 2,
                title = "일본 소설 좋아하는 사람들",
                participants = 15,
                maxParticipants = 20,
                isRecruiting = true,
                endDate = 2,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 3,
                title = "명작 같이 읽기방",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 4,
                title = "명작 같이 읽기방",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 5,
                title = "물리책 읽는 방",
                participants = 13,
                maxParticipants = 20,
                isRecruiting = true,
                endDate = 1,
                genreIndex = 1
            )
        )

        // 인기 있는 독서 모임방
        val popularRooms = listOf(
            GroupCardItemRoomData(
                id = 6,
                title = "베스트셀러 토론방",
                participants = 28,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 7,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 7,
                title = "인기 소설 완독방",
                participants = 25,
                maxParticipants = 25,
                isRecruiting = false,
                endDate = 5,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 8,
                title = "트렌드 과학서 읽기",
                participants = 20,
                maxParticipants = 25,
                isRecruiting = true,
                endDate = 10,
                genreIndex = 1
            )
        )

        // 인플루언서, 작가 독서 모임방
        val influencerRooms = listOf(
            GroupCardItemRoomData(
                id = 9,
                title = "작가와 함께하는 독서방",
                participants = 30,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = 14,
                genreIndex = 0
            ),
            GroupCardItemRoomData(
                id = 10,
                title = "유명 북튜버와 읽기",
                participants = 18,
                maxParticipants = 20,
                isRecruiting = true,
                endDate = 8,
                genreIndex = 2
            ),
            GroupCardItemRoomData(
                id = 11,
                title = "작가 초청 인문학방",
                participants = 15,
                maxParticipants = 20,
                isRecruiting = true,
                endDate = 12,
                genreIndex = 3
            )
        )

        val roomSections = listOf(
            GroupRoomSectionData(
                title = stringResource(R.string.deadline_string),
                rooms = deadlineRooms,
                genres = genres
            ),
            GroupRoomSectionData(
                title = "인기 있는 독서 모임방",
                rooms = popularRooms,
                genres = genres
            ),
            GroupRoomSectionData(
                title = "인플루언서·작가 독서 모임방",
                rooms = influencerRooms,
                genres = genres
            )
        )

        GroupRoomDeadlineSection(
            roomSections = roomSections,
            onRoomClick = {}
        )
    }
}

@Preview(name = "Empty Genre Data")
@Composable
fun PreviewGroupRoomPagerSectionEmptyGenre() {
    ThipTheme {
        val genres = listOf("문학", "과학·IT", "사회과학", "인문학", "예술")

        // 특정 장르에만 데이터가 있는 경우 (문학 장르만 데이터 존재)
        val deadlineRooms = listOf(
            GroupCardItemRoomData(
                id = 12,
                title = "시집만 읽는 사람들 3월",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = 3,
                genreIndex = 0 // 문학 장르만
            )
        )

        val roomSections = listOf(
            GroupRoomSectionData(
                title = "마감 임박한 독서 모임방",
                rooms = deadlineRooms,
                genres = genres
            )
        )

        GroupRoomDeadlineSection(
            roomSections = roomSections,
            onRoomClick = {}
        )
    }
}