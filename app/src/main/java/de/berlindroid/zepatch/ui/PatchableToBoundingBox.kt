package de.berlindroid.zepatch.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PatchableBoundingBox(
    modifier: Modifier = Modifier,
    patchable: @Composable () -> Unit
) {
    Box(
        modifier = modifier.border(
            width = 1.dp,
            color = Color.Black,
        ),
    ) {
        patchable()
    }
}