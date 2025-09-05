package de.berlindroid.zepatch.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.graphics.scale
import androidx.core.text.isDigitsOnly
import androidx.compose.ui.tooling.preview.Preview
import com.embroidermodder.punching.reduceColors
import kotlinx.coroutines.launch

@Composable
fun PatchableToReducedBitmap(
    modifier: Modifier = Modifier,
    image: ImageBitmap? = null,
    colorCount: Int = 3,
    onReducedBitmap: (ImageBitmap) -> Unit = {},
    onColorCountChanged: (Int) -> Unit = {}
) {

    var reducedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        image?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(onClick = {
            // TODO: PARRALELEIZE & VMIZE
            coroutineScope.launch {
                reducedImage = null
                image?.let {
                    val aspect = it.width / it.height.toFloat()
                    reducedImage = it.asAndroidBitmap()
                        .copy(Bitmap.Config.ARGB_8888, false)
                        .scale((512 * aspect).toInt(), 512, false)
                        .reduceColors(colorCount)
                        .asImageBitmap()
                }
            }
        }) { Text("Do it") }

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

        reducedImage?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
            onReducedBitmap(it)
        } ?: CircularProgressIndicator()
    }
}

@Preview
@Composable
fun PatchableToReducedBitmapPreview() {
    PatchableToReducedBitmap(
        image = ImageBitmap(width = 100, height = 100), // Example ImageBitmap
        colorCount = 5,
        onReducedBitmap = {},
        onColorCountChanged = {}
    )
}


