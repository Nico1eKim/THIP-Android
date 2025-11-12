package com.texthip.thip.ui.common.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.texthip.thip.R
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun CardItemRoom(
    modifier: Modifier = Modifier,
    title: String,
    participants: Int,
    maxParticipants: Int,
    isRecruiting: Boolean,
    endDate: String? = null,
    imageUrl: String? = null,
    hasBorder: Boolean = false,
    isSecret: Boolean? = null,
    isExpired: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hasBorder)
                    Modifier
                        .border(
                            width = 1.dp,
                            color = colors.Grey02,
                            shape = RoundedCornerShape(12.dp)
                        )
                else Modifier
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = colors.DarkGrey
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 이미지
                Box(
                    modifier = Modifier.size(width = 80.dp, height = 107.dp)
                ) {
                    AsyncImage(
                        model = imageUrl ?: R.drawable.img_book_cover_sample,
                        contentDescription = "책 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isSecret == true) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_secret_cover),
                            contentDescription = "비밀방",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(107.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        color = colors.White,
                        style = typography.smalltitle_sb600_s18_h24,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_group),
                            contentDescription = "그룹 아이콘",
                            tint = colors.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        if (isRecruiting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.card_item_participant_count,
                                        participants,
                                    ),
                                    style = typography.menu_sb600_s12,
                                    color = colors.White
                                )
                                Spacer(modifier = Modifier.width(2.dp))

                                Text(
                                    text = stringResource(
                                        R.string.card_item_participant_count_max,
                                        maxParticipants
                                    ),
                                    style = typography.info_m500_s12,
                                    color = colors.Grey
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.card_item_participating_count,
                                        participants
                                    ),
                                    style = typography.menu_sb600_s12,
                                    color = colors.White
                                )
                                Text(
                                    text = stringResource(R.string.card_item_participant_string),
                                    style = typography.info_m500_s12,
                                    color = colors.Grey
                                )
                            }
                        }
                    }
                    if (!isExpired) {
                        endDate?.let {
                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = endDate
                                        + if (isRecruiting) stringResource(
                                    R.string.card_item_end
                                ) else stringResource(R.string.card_item_finish),

                                color = if (isRecruiting) colors.Red else colors.Grey01,
                                style = typography.menu_sb600_s12_h20,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview()
@Composable
fun CardItemRoomPreview() {
    ThipTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CardItemRoom(
                title = "모임방 이름입니다. 모임방 이름입니다.",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = "3일 뒤",
                imageUrl = null
            )
            CardItemRoom(
                title = "모임방 이름입니다. 모임방 이름입니다.",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = "3",
                imageUrl = null
            )
            CardItemRoom(
                title = "모임방 이름입니다. 모임방 이름입니다.",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = true,
                endDate = "3",
                imageUrl = null,
                hasBorder = true
            )
            CardItemRoom(
                title = "모임방 이름입니다. 모임방 이름입니다.",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                endDate = "3",
                imageUrl = null,
                hasBorder = true
            )
            CardItemRoom(
                title = "모임방 이름입니다. 모임방 이름입니다.",
                participants = 22,
                maxParticipants = 30,
                isRecruiting = false,
                imageUrl = null,
                hasBorder = true
            )
        }
    }
}