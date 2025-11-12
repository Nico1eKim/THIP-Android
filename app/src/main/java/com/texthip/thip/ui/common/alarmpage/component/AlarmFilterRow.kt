package com.texthip.thip.ui.common.alarmpage.component

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

@Composable
fun AlarmFilterRow(
    selectedStates: BooleanArray,
    onToggle: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OptionChipButton(
            text = stringResource(R.string.alarm_feed),
            isFilled = true,
            isSelected = selectedStates[0],
            onClick = { onToggle(0) }
        )
        OptionChipButton(
            text = stringResource(R.string.alarm_group),
            isFilled = true,
            isSelected = selectedStates[1],
            onClick = { onToggle(1) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmFilterRowPreview() {
    var selectedStates by remember { mutableStateOf(booleanArrayOf(false, false)) }

    AlarmFilterRow(
        selectedStates = selectedStates,
        onToggle = { idx ->
            selectedStates = selectedStates.copyOf().also { it[idx] = !it[idx] }
        }
    )
}

