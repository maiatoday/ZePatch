package com.embroidermodder.punching

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.ColorUtils.RGBToLAB
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.createBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.min

typealias Color = Int
typealias Amount = Int

data class Histogram(
    val spread: Map<Color, Amount>
)

val Histogram.colors: Set<Color>
    get() = spread.keys

fun Bitmap.colorHistogram(): Histogram {
    // all colors
    val result = mutableMapOf<Color, Amount>()

    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)

    pixels.forEachIndexed { index: Int, pixel: Int ->
        if (pixel.alpha < 0xff) {
            result[0x00] = result.getOrDefault(0x00, 0) + 1
        } else {
            result[pixel] = result.getOrDefault(pixel, 0) + 1
        }
    }

    return Histogram(
        result
    )
}

fun Bitmap.reduceColors(
    maxColorCounts: Int,
    minTolerance: Int,
): Pair<Bitmap, Histogram> {
    if(minTolerance < 4) {
        Log.i("REDUCIN", "Color merging tolerance ($minTolerance) too low, resetting to 4.")
    }

    var tolerance = min(4, minTolerance)
    var histogram =
        colorHistogram()
            .mergeSimilarColors(maxColorCounts, tolerance)

    var maxTries = 3
    while (histogram.colors.size > maxColorCounts && maxTries > 0) {
        histogram = histogram
            .mergeSimilarColors(maxColorCounts, tolerance)
        maxTries--
    }

    // reduce more aggressively
    maxTries = 10
    while (histogram.colors.size > maxColorCounts && maxTries > 0) {
        histogram = histogram
            .mergeSimilarColors(maxColorCounts, tolerance)

        tolerance = (tolerance * 1.5f).toInt()
        maxTries--
    }

    // todo if not reject and give up

    histogram = Histogram(
        spread = histogram
            .spread
            .toList()
            .sortedByDescending { it.second }
            .take(maxColorCounts)
            .toMap()
    )

    val topColors = histogram.colors.toList()

    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    val newPixels = pixels.filteredBy(topColors)

    val result = createBitmap(width, height, config!!).apply {
        setPixels(newPixels, 0, width, 0, 0, width, height)
    }

    return result to histogram
}

fun Histogram.mergeSimilarColors(maxColorCounts: Int, tolerance: Int): Histogram {
    if (spread.size <= maxColorCounts) {
        return this
    }

    val result = mutableMapOf<Color, Amount>()

    for (e in spread) {
        var merged = false

        if (e.key.alpha == 0xFF) {
            for (r in result) {
                if (distance(e.key, r.key) < tolerance) {
                    merged = true
                    result[r.key] = r.value + e.value
                }
            }
        }

        if (!merged) {
            result[e.key] = e.value
        }
    }

    return Histogram(result)
}

fun Histogram.removeLowCounts(maxColorCounts: Int, threshold: Int): Histogram {
    if (spread.size <= maxColorCounts) {
        return this
    }

    val result = mutableMapOf<Color, Amount>()
    var removedCount = 0

    for (e in spread) {
        var keep = true
        if (e.key.alpha == 0xFF) {
            if (e.value < threshold) {
                keep = false
            }
        }

        if (keep) {
            result[e.key] = e.value
        } else {
            removedCount++
        }
    }

    Log.i("HISTOGRAM", "Removed $removedCount colors from histogram.")
    return Histogram(result)
}

private fun IntArray.filteredBy(reducedColors: List<Int>): IntArray {
    val opaqueColors = reducedColors.filter { it.alpha == 0xFF }

    val result = map { pixel ->
        if (pixel.alpha == 0xFF) {
            opaqueColors.minBy { reducedColor ->
                distance(pixel, reducedColor)
            }
        } else {
            0x00
        }
    }

    return result.toIntArray()
}

private fun distance(a: Int, b: Int): Float {
    val aR = a.red
    val aG = a.green
    val aB = a.blue

    val aLAB = DoubleArray(3)
    RGBToLAB(aR, aG, aB, aLAB)

    val bR = b.red
    val bG = b.green
    val bB = b.blue

    val bLAB = DoubleArray(3)
    RGBToLAB(bR, bG, bB, bLAB)

    return ColorUtils.distanceEuclidean(aLAB, bLAB).toFloat()
}
