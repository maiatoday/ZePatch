package de.berlindroid.zepatch.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.launch

/**
 * A composable function that captures the drawn content of its child composables into a bitmap
 * when triggered. This allows developers to programmatically generate an `ImageBitmap`
 * from the rendered UI content.
 *
 * @param modifier A [Modifier] instance to apply to this composable for layout or styling configurations.
 * @param shouldCapture A flag to trigger the capture process when set to `true`. The content will be converted
 *                to a bitmap and passed to the `onBitmap` callback.
 * @param onBitmap A callback triggered with the generated [ImageBitmap] once the content has been
 *                 successfully captured.
 * @param content A lambda expression containing the composable content to be rendered and captured
 *                into the output bitmap.
 */
@Composable
fun CaptureToBitmap(
    modifier: Modifier = Modifier,
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    Box(
        modifier = modifier
            .drawWithContent {
                // Record content drawing into the graphics layer
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
            }
            .background(Color.Transparent)
    ) {
        LaunchedEffect(shouldCapture) {
            if (shouldCapture) {
                coroutineScope.launch {
                    val bitmap = graphicsLayer.toImageBitmap()
                    onBitmap(bitmap)
                }
            }
        }
        content()
    }
}
