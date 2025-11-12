package com.texthip.thip.ui.group.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.common.buttons.GenreChipButton
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun GroupRecentSearch(
    recentSearches: List<String>,
    onSearchClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    onViewAllRoomsClick: () -> Unit = {}
) {
    Column {
        Text(
            text = stringResource(R.string.group_recent_search),
            color = colors.White,
            style = typography.smalltitle_sb600_s18_h24
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (recentSearches.isEmpty()) {
            Text(
                text = stringResource(R.string.group_no_recent_search),
                color = colors.Grey01,
                style = typography.menu_r400_s14_h24
            )
        } else {
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                maxLines = 2,
            ) {
                recentSearches.take(9).forEach { keyword ->
                    GenreChipButton(
                        text = keyword,
                        onClick = { onSearchClick(keyword) },
                        onCloseClick = { onRemove(keyword) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onViewAllRoomsClick()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.look_around_all_rooms_title),
                color = colors.White,
                style = typography.smalltitle_sb600_s18_h24
            )

            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupRecentSearchPreview() {
    ThipTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            var searches by remember {
                mutableStateOf(listOf("해리포터", "소설", "추리소설", "로맨스", "SF", "판타지"))
            }

            GroupRecentSearch(
                recentSearches = searches,
                onSearchClick = { keyword ->
                    // 검색 동작 시뮬레이션
                },
                onRemove = { keyword ->
                    searches = searches.filter { it != keyword }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupRecentSearchEmptyPreview() {
    ThipTheme {
        Box(
            modifier = Modifier
                .background(colors.Black)
                .padding(16.dp)
        ) {
            GroupRecentSearch(
                recentSearches = emptyList(),
                onSearchClick = { },
                onRemove = { }
            )
        }
    }
}
