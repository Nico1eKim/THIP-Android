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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.data.manager.Genre
import com.texthip.thip.data.model.rooms.response.RoomMainList
import com.texthip.thip.data.model.rooms.response.RoomMainResponse
import com.texthip.thip.ui.common.buttons.GenreChipRow
import com.texthip.thip.ui.common.cards.CardItemRoom
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography
import com.texthip.thip.utils.rooms.toDisplayStrings

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GroupRoomDeadlineSection(
    roomMainList: RoomMainList?,
    selectedGenreIndex: Int,
    errorMessage: String? = null,
    onGenreSelect: (Int) -> Unit,
    onRoomClick: (RoomMainResponse) -> Unit
) {
    val sideMargin = 30.dp

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
            val desiredGap = 12.dp

            val pageSpacing = (-(cardWidth - (cardWidth * scale)) / 2) + desiredGap

            // Genre enum을 현지화된 문자열로 변환
            val genreStrings = Genre.entries.toDisplayStrings()

            // 마감 임박 방 목록, 인기 방 목록, 최신 생성 모임방을 섹션으로 구성
            val roomSections = listOf(
                Pair(
                    stringResource(R.string.room_section_deadline),
                    roomMainList?.deadlineRoomList ?: emptyList()
                ),
                Pair(
                    stringResource(R.string.room_section_popular),
                    roomMainList?.popularRoomList ?: emptyList()
                ),
                Pair(
                    stringResource(R.string.room_section_recent),
                    roomMainList?.recentRoomList ?: emptyList()
                ),
            )

            val actualPageCount = roomSections.size

            val effectivePagerState = rememberPagerState(
                initialPage = 2,
                pageCount = { Int.MAX_VALUE }
            )

            HorizontalPager(
                state = effectivePagerState,
                contentPadding = PaddingValues(horizontal = 30.dp),
                pageSpacing = pageSpacing,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val actualPage = page % actualPageCount
                val (sectionTitle, rooms) = roomSections[actualPage]

                val isCurrent = effectivePagerState.currentPage == page
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
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = sectionTitle,
                            style = typography.title_b700_s20_h24,
                            color = colors.White,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(40.dp))

                        GenreChipRow(
                            genres = genreStrings,
                            selectedIndex = selectedGenreIndex,
                            onSelect = onGenreSelect
                        )
                        Spacer(Modifier.height(20.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(584.dp)
                                .padding(horizontal = 20.dp)
                        ) {
                            when {
                                // 에러 상태
                                errorMessage != null -> {
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 30.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.error_data_load_failed),
                                            style = typography.smalltitle_sb600_s16_h20,
                                            color = colors.White,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = errorMessage,
                                            style = typography.copy_r400_s14,
                                            color = colors.Grey,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                // 데이터 없음 상태
                                rooms.isEmpty() -> {
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 30.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
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
                                }
                                // 정상 데이터 표시
                                else -> {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(20.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rooms.forEach { room ->
                                            // RoomMainResponse를 CardItemRoom에 맞게 변환
                                            CardItemRoom(
                                                title = room.roomName,
                                                participants = room.memberCount,
                                                maxParticipants = room.recruitCount,
                                                isRecruiting = true, // RoomMainResponse에는 모집중인 방만 있음
                                                endDate = room.deadlineDate,
                                                imageUrl = room.bookImageUrl,
                                                onClick = { onRoomClick(room) },
                                                hasBorder = true,
                                            )
                                        }
                                    }

                                    if (rooms.size < 4) {
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
    }
}


@Preview
@Composable
fun PreviewGroupRoomPagerSection() {
    ThipTheme {
        // RoomMainResponse 형태의 더미 데이터
        val deadlineRooms = listOf(
            RoomMainResponse(
                roomId = 1,
                roomName = "시집만 읽는 사람들 3월",
                memberCount = 22,
                recruitCount = 30,
                deadlineDate = "3일 뒤",
                bookImageUrl = "https://picsum.photos/300/200?1"
            ),
            RoomMainResponse(
                roomId = 2,
                roomName = "일본 소설 좋아하는 사람들",
                memberCount = 15,
                recruitCount = 20,
                deadlineDate = "2일 뒤",
                bookImageUrl = "https://picsum.photos/300/200?2"
            )
        )

        val popularRooms = listOf(
            RoomMainResponse(
                roomId = 6,
                roomName = "베스트셀러 토론방",
                memberCount = 28,
                recruitCount = 30,
                deadlineDate = "7일 뒤",
                bookImageUrl = "https://picsum.photos/300/200?6"
            )
        )

        val roomMainList = RoomMainList(
            deadlineRoomList = deadlineRooms,
            popularRoomList = emptyList()
        )

        GroupRoomDeadlineSection(
            roomMainList = roomMainList,
            selectedGenreIndex = 0,
            errorMessage = null,
            onGenreSelect = {},
            onRoomClick = {}
        )
    }
}

@Preview(name = "Empty Genre Data")
@Composable
fun PreviewGroupRoomPagerSectionEmptyGenre() {
    ThipTheme {
        val deadlineRooms = listOf(
            RoomMainResponse(
                roomId = 12,
                roomName = "시집만 읽는 사람들 3월",
                memberCount = 22,
                recruitCount = 30,
                deadlineDate = "3일 뒤",
                bookImageUrl = "https://picsum.photos/300/200?12"
            )
        )

        val roomMainList = RoomMainList(
            deadlineRoomList = deadlineRooms,
            popularRoomList = emptyList()
        )

        GroupRoomDeadlineSection(
            roomMainList = roomMainList,
            selectedGenreIndex = 0,
            errorMessage = null,
            onGenreSelect = {},
            onRoomClick = {}
        )
    }
}