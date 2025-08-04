package com.texthip.thip.ui.common.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.theme.ThipTheme
import com.texthip.thip.ui.theme.ThipTheme.colors
import com.texthip.thip.ui.theme.ThipTheme.typography

@Composable
fun SearchBookTextField(
    modifier: Modifier = Modifier,
    text: String,
    hint: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit = {}
) {
    val myStyle = typography.menu_r400_s14_h24.copy(
        color = colors.White
    )
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shape)
            .background(colors.DarkGrey),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            BasicTextField(
                value = text,
                onValueChange = { onValueChange(it) },
                singleLine = true,
                textStyle = myStyle,
                cursorBrush = SolidColor(colors.NeonGreen),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch(text) }
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                text = hint,
                                color = colors.Grey02,
                                style = myStyle
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (text.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_x_circle_grey02),
                    contentDescription = "Clear text",
                    modifier = Modifier
                        .clickable { onValueChange("") },
                    tint = Color.Unspecified
                )
            }
            Spacer(Modifier.width(20.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                modifier = Modifier
                    .clickable {
                        onSearch(text)
                    },
                tint = colors.White
            )
            Spacer(Modifier.width(12.dp))
        }
    }
}


@Preview()
@Composable
private fun SearchBookTextFieldPreview() {
    ThipTheme {
        var text by rememberSaveable { mutableStateOf("") }
        SearchBookTextField(
            text = text,
            hint = "책 제목, 저자검색",
            onValueChange = { text = it },
            onSearch = { /* 검색 실행 */ }
        )
    }
}
