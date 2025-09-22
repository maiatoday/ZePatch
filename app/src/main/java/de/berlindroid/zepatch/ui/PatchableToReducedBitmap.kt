package de.berlindroid.zepatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import com.embroidermodder.punching.Histogram
import de.berlindroid.zepatch.WizardViewModel.UiState.ReduceBitmap
import de.berlindroid.zepatch.isBusy
import de.berlindroid.zepatch.utils.RANDOMIZER_COLORS
import de.berlindroid.zepatch.utils.multiLet
import de.berlindroid.zepatch.utils.randomBitmap
import kotlin.random.Random

@Composable
fun PatchableToReducedBitmap(
    modifier: Modifier = Modifier,
    state: ReduceBitmap,
    computeReducedBitmap: () -> Unit = {},
    onColorCountChanged: (Int) -> Unit = {}
) {
    val containerSize = LocalWindowInfo.current.containerSize

    val imageWidth = containerSize.width * 3 / 4f
    val imageHeight = if (state.reducedBitmap != null) {
        (imageWidth * (state.reducedBitmap.height.toFloat() / state.reducedBitmap.width))
    } else {
        imageWidth
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WizardSectionTitle(
            title = "Reduce Colors",
            helpText = "Choose the number of colors and generate a simplified bitmap suitable for embroidering."
        )

        Image(
            bitmap = state.image.asAndroidBitmap().scale(imageWidth.toInt(), imageHeight.toInt(), false)
                .asImageBitmap(),
            contentDescription = "patch bitmap",
            filterQuality = FilterQuality.None
        )

        IntSelector(
            value = "${state.colorCount}",
            label = "How many colors do you need?",
            errorText = { "Please enter a valid color count (between 1 and 7)" },
            acceptableNumberEntered = { isValidColorCount(colorCountText = it) },
            onNumberChanged = { onColorCountChanged(it.toInt()) },
            onDone = {
                focusManager.clearFocus()
                computeReducedBitmap()
            }
        )

        Button(
            enabled = isValidColorCount("${state.colorCount}") && !state.isBusy(),
            onClick = computeReducedBitmap
        ) { Text("Reduce") }

        state.reducedBitmap?.multiLet(state.reducedHistogram) { bitmap, histogram ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = bitmap.asAndroidBitmap().scale(imageWidth.toInt(), imageHeight.toInt(), false)
                        .asImageBitmap(),
                    contentDescription = "reduced bitmap",
                )

                SelectedColorRow(histogram)
            }
        }

        if (state.currentlyReducingColors) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SelectedColorRow(histogram: Histogram) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val totalColors = histogram.spread.values.sum()
        items(histogram.spread.toList()) { entry ->
            val (color, amount) = entry
            Text(
                modifier = Modifier.padding(4.dp),
                text = "${((amount.toFloat() / totalColors) * 100f).toInt()}%"
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .border(1.dp, color = Color.Black)
                    .background(Color(color))
            )
            Spacer(Modifier.width(16.dp))
        }
    }
}

fun isValidColorCount(colorCountText: String): Boolean =
    (colorCountText.toIntOrNull() ?: -1) in 2..7

@Preview
@Composable
private fun ReducedPreview() {
    PatchableToReducedBitmap(
        modifier = Modifier,
        state = ReduceBitmap(
            name = "PETE!",
            image = randomBitmap().asImageBitmap(),
            colorCount = 3,
            reducedBitmap = randomBitmap().asImageBitmap(),
            reducedHistogram = Histogram(
                spread = RANDOMIZER_COLORS.associate { (it.toArgb()) to Random.nextInt() }
            )
        )
    )
}

