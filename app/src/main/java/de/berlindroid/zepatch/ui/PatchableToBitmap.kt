package de.berlindroid.zepatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import de.berlindroid.zepatch.utils.CaptureToBitmap

/**
 * Renders the provided [patchable] into a Bitmap-like [ImageBitmap] and displays it via [Image].
 * Uses a composable utility that captures the composed content from a graphics layer.
 */
@Composable
fun PatchableToBitmap(
    modifier: Modifier = Modifier,
    onBitmap: (ImageBitmap) -> Unit = {},
    patchable: @Composable () -> Unit,
) {
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    var capture by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(onClick = {
            image = null
            capture = true
        }) { Text("Do it") }
        // Compose the content through the composable utility; it will invoke the callback when ready.
        CaptureToBitmap(
            modifier = Modifier.fillMaxWidth(),
            capture = capture,
            onBitmap = { img ->
                image = img
                capture = false
            },
            content = patchable
        )

        image?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
            onBitmap(it)
        } ?: CircularProgressIndicator()

    }
}


