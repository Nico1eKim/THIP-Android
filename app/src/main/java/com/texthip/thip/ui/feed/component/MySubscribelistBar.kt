package com.texthip.thip.ui.feed.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.texthip.thip.R
import com.texthip.thip.data.model.users.response.RecentWriterList
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography


@Composable
fun MySubscribeBarlist(
    modifier: Modifier = Modifier,
    subscriptions: List<RecentWriterList>,
    onClick: () -> Unit
) {
// 이미지 + 간격 너비
    val imageWidthWithSpacing = 36.dp + 12.dp
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable { onClick() }
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val imageWithSpacingPx = with(density) { imageWidthWithSpacing.toPx() }

        val maxVisibleCount = ((maxWidthPx - 36f) / imageWithSpacingPx).toInt()

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.ic_group),
                    contentDescription = null,
                    tint = colors.White
                )
                Text(
                    text = stringResource(R.string.my_subscription),
                    style = typography.smalltitle_sb600_s14_h20,
                    color = colors.White,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (subscriptions.isEmpty()) {
                EmptyMySubscriptionBar()
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clickable { onClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    subscriptions.take(maxVisibleCount).forEach { profile ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(36.dp)
                        ) {
                            AsyncImage(
                                model = profile.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 0.5.dp,
                                        color = colors.Grey02,
                                        shape = CircleShape
                                    )
                                    .background(Color.LightGray)
                            )
                            Text(
                                text = profile.nickname,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = typography.view_r400_s11_h20,
                                color = colors.White,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = null,
                        tint = colors.Grey
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMySubscriptionBar(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.find_thip_mate),
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.DarkGrey02)
            .clickable { onClick() }
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            text = text,
            color = colors.White,
            style = typography.view_m500_s12_h20
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_search_character),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp)
                .width(32.dp)
                .height(42.dp)
        )
    }
}

@Preview
@Composable
private fun MySubscribeBarlistPrev() {
    ThipTheme {
        val previewData = List(10) {
            RecentWriterList(
                userId = it.toLong(),
                profileImageUrl = "https://example.com/profile$it.jpg",
                nickname = "닉네임임$it"
            )
        }

        Column {
            MySubscribeBarlist(
                subscriptions = previewData,
                onClick = {}
            )
        }
    }

}

@Preview
@Composable
private fun MySubscribeBarlistWithoutDataPrev() {
    ThipTheme {
        val previewData = emptyList<RecentWriterList>()

        Column {
            MySubscribeBarlist(
                subscriptions = previewData,
                onClick = {}
            )
        }
    }

}