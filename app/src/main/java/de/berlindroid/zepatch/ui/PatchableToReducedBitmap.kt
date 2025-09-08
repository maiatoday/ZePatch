package de.berlindroid.zepatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly

@Composable
fun PatchableToReducedBitmap(
    modifier: Modifier = Modifier,
    image: ImageBitmap? = null,
    colorCount: Int = 3,
    reducedImage: ImageBitmap? = null,
    computeReducedBitmap: () -> Unit = {},
    onColorCountChanged: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WizardSectionTitle(
            title = "Reduce Colors",
            helpText = "Choose the number of colors and generate a simplified bitmap suitable for stitching."
        )
        image?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
        }
        TextField(
            value = "$colorCount",
            onValueChange = {
                if (it.isNotBlank() && it.isDigitsOnly()) {
                    onColorCountChanged(it.toInt())
                } else {
                    colorCount
                }
            },
            label = { Text("How many colors do you need?") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Go
            )
        )

        Button(onClick = computeReducedBitmap) { Text("Reduce") }

        reducedImage?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
        } ?: CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun PatchableToReducedBitmapPreview() {
    PatchableToReducedBitmap(
        image = ImageBitmap(width = 100, height = 100), // Example ImageBitmap
        colorCount = 5,
        onColorCountChanged = {}
    )
}


