package de.berlindroid.zepatch.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import androidx.core.text.isDigitsOnly
import com.embroidermodder.punching.reduceColors
import de.berlindroid.zepatch.utils.CaptureToBitmap

@Composable
fun PatchableToReducedBitmap(
    modifier: Modifier = Modifier,
    patchable: @Composable () -> Unit,
) {

    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    var colorCount by remember { mutableStateOf<Int>(3) }
    var capture by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(onClick = {
            image = null
            capture = true
        }) { Text("Do it") }
        CaptureToBitmap(
            modifier = Modifier.fillMaxWidth(),
            capture = capture,
            onBitmap = { img ->
                // TODO: PARRALELEIZE & VMIZE
                val aspect = img.width / img.height.toFloat()
                image = img.asAndroidBitmap()
                    .copy(Bitmap.Config.ARGB_8888, false)
                    .scale((512 * aspect).toInt(), 512, false)
                    .reduceColors(colorCount)
                    .asImageBitmap()
                capture = false
            },
            content = patchable
        )

        TextField(
            value = "$colorCount",
            onValueChange = {
                if (it.isNotBlank() && it.isDigitsOnly()) {
                    colorCount = it.toInt()
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

        image?.let {
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
private fun PatchableToReducedBitmapPreview() {
    PatchableToReducedBitmap {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue)
        ) {
            Text(
                "Preview Content",
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
    }
}