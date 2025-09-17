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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.embroidermodder.punching.Histogram
import com.embroidermodder.punching.colorHistogram
import de.berlindroid.zepatch.WizardViewModel.UiState.SetupEmbroidery
import de.berlindroid.zepatch.isBusy

@Composable
fun BitmapToStitches(
    modifier: Modifier = Modifier,
    state: SetupEmbroidery,
    onCreateEmbroidery: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WizardSectionTitle(
            title = "Generate Stitches",
            helpText = "Turn the reduced bitmap into an embroidery stitch file and preview the result."
        )
        Image(
            bitmap = state.reducedBitmap,
            contentDescription = "reduced bitmap",
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onCreateEmbroidery, enabled = !state.isCompleted() && !state.isBusy()) { Text("Generate") }

        state.embroideryPreviewImage?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
        } ?: Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (state.currentlyEmbroidering) CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun BitmapToStitchesPreview() {
    val imageBitmap = ImageBitmap(width = 100, height = 100)
    val histogram = imageBitmap.asAndroidBitmap().colorHistogram()
    BitmapToStitches(
        state = SetupEmbroidery(
            image = imageBitmap,
            colorCount = 44,
            reducedBitmap = imageBitmap,
            reducedHistogram = histogram,
            name = "MyPatch",
            currentlyEmbroidering = false,
        ),
        onCreateEmbroidery = {},
    )
}

