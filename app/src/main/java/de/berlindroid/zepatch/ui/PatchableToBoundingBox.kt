package de.berlindroid.zepatch.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun PatchableBoundingBox(
    modifier: Modifier = Modifier,
    patchable: @Composable (Boolean, (ImageBitmap) -> Unit) -> Unit
) {
    Box(
        modifier = modifier.border(
            width = 1.dp,
            color = Color.Black,
        ),
    ) {
        CompositionLocalProvider(LocalPatchInList provides true) {
            patchable(false, {})
        }
    }
}

/**
 * CompositionLocal to indicate a patch is being rendered inside the list (preview context),
 * so interactive controls inside patches can be disabled.
 */
val LocalPatchInList = compositionLocalOf { false }
