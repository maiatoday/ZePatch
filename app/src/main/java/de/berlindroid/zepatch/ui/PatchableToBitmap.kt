package de.berlindroid.zepatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders the provided [patchable] into a Bitmap-like [ImageBitmap] and displays it via [Image].
 * Uses a composable utility that captures the composed content from a graphics layer.
 */
@Composable
fun PatchableToBitmap(
    modifier: Modifier = Modifier,
    onBitmap: (ImageBitmap) -> Unit = {},
    patchable: @Composable (Boolean, (ImageBitmap) -> Unit) -> Unit,
) {
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    var shouldCapture by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WizardSectionTitle(
            title = "Generate Bitmap",
            helpText = "Render your composable into a bitmap. Tap the button to capture the preview."
        )

        // Render the patchable; it will capture via SafeArea when shouldCapture is true
        patchable(shouldCapture) { img ->
            image = img
            shouldCapture = false
        }
        Button(onClick = {
            image = null
            shouldCapture = true
        }) { Text("Generate Bitmap") }
        image?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
            onBitmap(it)
        } ?: Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (shouldCapture) CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PatchableToBitmapPreview() {
    PatchableToBitmap(
        patchable = { _, _ ->
            Text("This is a sample patchable content.")
        }
    )
}


