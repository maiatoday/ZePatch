package de.berlindroid.zepatch.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Common SafeArea wrapper to provide consistent padding around patchable content.
 */
@Composable
fun SafeArea(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.padding(32.dp),
    ) {
        content()
    }
}
