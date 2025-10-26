package com.texthip.thip.ui.group.myroom.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.texthip.thip.R
import com.texthip.thip.ui.common.buttons.OptionChipButton
import com.texthip.thip.ui.theme.ThipTheme

@Composable
fun GroupMyRoomFilterRow(
    selectedStates: BooleanArray,
    onToggle: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OptionChipButton(
            text = stringResource(R.string.on_going),
            isFilled = true,
            isSelected = selectedStates[0],
            onClick = { onToggle(0) }
        )
        OptionChipButton(
            text = stringResource(R.string.recruiting),
            isFilled = true,
            isSelected = selectedStates[1],
            onClick = { onToggle(1) }
        )
        OptionChipButton(
            text = stringResource(R.string.finish),
            isFilled = true,
            isSelected = selectedStates[2],
            onClick = { onToggle(2) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GroupMyRoomFilterRowPreview() {
    ThipTheme {
        var selectedStates by remember { mutableStateOf(booleanArrayOf(true, false)) }
        
        GroupMyRoomFilterRow(
            selectedStates = selectedStates,
            onToggle = { index ->
                selectedStates = selectedStates.copyOf().apply {
                    this[index] = !this[index]
                }
            }
        )
    }
}

