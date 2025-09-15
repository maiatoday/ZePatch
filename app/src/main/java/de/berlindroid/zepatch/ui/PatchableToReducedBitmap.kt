package de.berlindroid.zepatch.ui

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import de.berlindroid.zepatch.WizardViewModel
import de.berlindroid.zepatch.isBusy
import de.berlindroid.zepatch.utils.multiLet

@Composable
fun PatchableToReducedBitmap(
    modifier: Modifier = Modifier,
    state: WizardViewModel.UiState.SetupBitmap,
    computeReducedBitmap: () -> Unit = {},
    onColorCountChanged: (Int) -> Unit = {}
) {
    // Local text state so users can clear or type partial numbers without snapping back to default
    var colorText by rememberSaveable { mutableStateOf(state.colorCount.toString()) }

    // Keep local text in sync if colorCount changes from outside (e.g., recomputations)
    LaunchedEffect(state.colorCount) {
        colorText = state.colorCount.toString()
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
            bitmap = state.image,
            contentDescription = "patch bitmap",
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = colorText,
            onValueChange = { new ->
                // Allow empty and numeric-only input
                if (new.isEmpty()) {
                    colorText = ""
                } else if (new.isDigitsOnly()) {
                    colorText = new
                    new.toIntOrNull()?.let { onColorCountChanged(it) }
                }
                // ignore non-digit edits
            },
            label = { Text("How many colors do you need?") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isValidColorCount(colorText)) {
                        focusManager.clearFocus()
                        computeReducedBitmap()
                    }
                }
            ),
            isError = !isValidColorCount(colorText), // Set the error state
            supportingText = {
                if (!isValidColorCount(colorText)) {
                    Text(
                        text = "Please enter a valid color count (between 1 and 7)",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Button(
            enabled = isValidColorCount(colorText) && !state.isBusy(),
            onClick = computeReducedBitmap
        ) { Text("Reduce") }

        state.reducedBitmap?.multiLet(state.reducedHistogram) { bitmap, histogram ->
            Column {
                Image(
                    bitmap = bitmap,
                    contentDescription = "reduced bitmap",
                    modifier = Modifier.fillMaxWidth()
                )

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
        }

        if (state.currentlyReducingColors) {
            CircularProgressIndicator()
        }
    }
}

fun isValidColorCount(colorCountText: String): Boolean =
    (colorCountText.toIntOrNull() ?: -1) in 2..7
