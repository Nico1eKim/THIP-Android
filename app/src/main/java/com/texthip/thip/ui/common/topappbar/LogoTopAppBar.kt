package com.texthip.thip.ui.common.topappbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.theme.ThipTheme.colors

@Composable
fun LogoTopAppBar(
    modifier: Modifier = Modifier,
    leftIcon: Painter? = null,
    hasNotification: Boolean,
    onLeftClick: () -> Unit = {},
    onRightClick: () -> Unit = {}
) {
    val rightIcon = if (hasNotification) {
        painterResource(R.drawable.ic_notice_yes)
    } else {
        painterResource(R.drawable.ic_notice_no)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.Black)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .height(24.dp)
                .align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leftIcon?.let {
                Icon(
                    painter = it,
                    contentDescription = "Left Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.clickable { onLeftClick() }
                )
            }
            Icon(
                painter = rightIcon,
                contentDescription = "Right Icon",
                tint = Color.Unspecified,
                modifier = Modifier.clickable { onRightClick() }
            )
        }
    }
}

@Preview
@Composable
private fun LogoTopAppBarPreview() {
    LogoTopAppBar(
        leftIcon = painterResource(R.drawable.ic_search),
        hasNotification = true,
        onLeftClick = {},
        onRightClick = {}
    )
}