package de.berlindroid.zepatch.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.launch

/**
 * Composable utility that renders [content] into a compositing graphics layer and can
 * capture it into an [ImageBitmap].
 *
 * How it works:
 * - We draw the composable's content into a remembered graphics layer using drawWithContent.
 * - We then draw that content normally so it appears on screen.
 * - We can capture the current pixels of that layer via graphicsLayer.toImageBitmap().
 *
 * Behavior:
 * - If [autoCapture] is true, it will capture once on first composition and invoke [onBitmap].
 * - It also supports manual capture by tapping the content (clickable), which will invoke [onBitmap].
 */
@Composable
fun CaptureToBitmap(
    modifier: Modifier = Modifier,
    autoCapture: Boolean = true,
    onBitmap: (ImageBitmap) -> Unit,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    var captured by remember { mutableStateOf(false) }

    // Optionally capture once after first composition
    LaunchedEffect(autoCapture) {
        if (autoCapture && !captured) {
            captured = true
            val capturedImage = graphicsLayer.toImageBitmap()
            onBitmap(capturedImage)
        }
    }

    Box(
        modifier = modifier
            .drawWithContent {
                // Record content drawing into the graphics layer
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                // Draw the content normally so it appears on screen
                this.drawContent()
            }
            .clickable {
                coroutineScope.launch {
                    val bitmap = graphicsLayer.toImageBitmap()
                    onBitmap(bitmap)
                }
            }
    ) {
        content()
    }
}
