package de.berlindroid.zepatch.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap

val RANDOMIZER_COLORS = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Black,
    Color.White
)

fun randomBitmap(w: Int = 40, h: Int = 50) = createBitmap(w, h).randomize(RANDOMIZER_COLORS)

fun Bitmap.randomize(colors: List<Color>): Bitmap {
    setPixels(
        List(width * height) {
            colors.random().toArgb()
        }.toIntArray(),
        0, width, 0, 0, width, height
    )
    return this
}
