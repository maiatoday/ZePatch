package de.berlindroid.zepatch.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import de.berlindroid.zepatch.utils.CaptureToBitmap
import de.berlindroid.zepatch.utils.reduceColors

/**
 * Renders the provided [patchable] into a Bitmap-like [ImageBitmap] and displays it via [Image].
 * Uses a composable utility that captures the composed content from a graphics layer.
 */
@Composable
fun PatchableToBitmap(
    modifier: Modifier = Modifier,
    patchable: @Composable () -> Unit,
) {
    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Compose the content through the composable utility; it will invoke the callback when ready.
        CaptureToBitmap(
            modifier = Modifier.fillMaxWidth(),
            autoCapture = true,
            onBitmap = { img -> image = img },
            content = patchable
        )

        val imageBitmap = image
        if (imageBitmap != null) {
            val reducedBitmap: ImageBitmap? = remember(imageBitmap) {
                try {
                    imageBitmap
                        .asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
                        .reduceColors(3)
                        .asImageBitmap()
                } catch (t: Throwable) {
                    Log.e("PatchableToBitmap", "Error ${t.message}")
                    null
                }
            }

            reducedBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = "patch bitmap reduced to 3 colors",
                    modifier = Modifier.weight(1f)
                )
            }

        } else {
            CircularProgressIndicator()
        }
    }
}

