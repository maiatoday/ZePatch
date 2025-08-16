package de.berlindroid.zepatch.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Reduces the number of colors in this [Bitmap] using a simple k-means quantization on RGB.
 *
 * Rules:
 * - Only fully transparent pixels (alpha == 0) are ignored during palette computation.
 * - Fully transparent pixels are kept as-is in the result.
 * - Non-transparent pixels keep their original alpha while their RGB is mapped to the nearest palette color.
 *
 * Notes:
 * - This returns a new immutable Bitmap; the original is not modified.
 * - For tiny images or when the number of distinct colors is already <= [numColors],
 *   it will effectively keep original colors (except it copies into a new Bitmap).
 */
fun Bitmap.reduceColors(numColors: Int): Bitmap {
    val width = this.width
    val height = this.height
    if (width == 0 || height == 0) return this

    val requested = max(1, numColors)

    val pixels = IntArray(width * height)
    this.getPixels(pixels, 0, width, 0, 0, width, height)

    // Collect non-transparent colors (alpha > 0) for clustering
    val opaqueColors = ArrayList<Int>(pixels.size)
    for (c in pixels) {
        if (Color.alpha(c) != 0) opaqueColors.add(c)
    }

    // If there are no opaque colors, just return a copy
    if (opaqueColors.isEmpty()) {
        return this.copy(this.config ?: Bitmap.Config.ARGB_8888, /*isMutable*/ false)
    }

    // Build a set of distinct opaque colors to potentially short-circuit
    val distinct = LinkedHashSet<Int>(opaqueColors.size)
    distinct.addAll(opaqueColors)

    val palette: List<Int> = if (distinct.size <= requested) {
        // Already within target; use the distinct colors themselves as palette
        distinct.toList()
    } else {
        // Run a lightweight k-means on RGB
        kMeansPalette(distinct.toList(), requested)
    }

    // Map every non-transparent pixel to nearest palette color (preserving original alpha)
    val out = IntArray(pixels.size)
    for (i in pixels.indices) {
        val c = pixels[i]
        val a = Color.alpha(c)
        if (a == 0) {
            out[i] = c // keep fully transparent as-is
        } else {
            val rgb = c and 0x00FFFFFF
            val nearest = findNearest(rgb, palette)
            val r = Color.red(nearest)
            val g = Color.green(nearest)
            val b = Color.blue(nearest)
            out[i] = Color.argb(a, r, g, b)
        }
    }

    val result = Bitmap.createBitmap(width, height, this.config ?: Bitmap.Config.ARGB_8888)
    result.setPixels(out, 0, width, 0, 0, width, height)
    return result
}

private fun kMeansPalette(colors: List<Int>, k: Int): List<Int> {
    // Initialize centroids by picking k evenly spaced colors from the input list
    val n = colors.size
    val centroids = MutableList(k) { idx ->
        val pos = ((idx.toLong() * n) / k).toInt().coerceIn(0, n - 1)
        colors[pos]
    }

    val assignments = IntArray(n) { -1 }

    repeat(10) { // small, fixed iteration count for speed/determinism
        var changed = false

        // Assign step
        for (i in 0 until n) {
            val color = colors[i] and 0x00FFFFFF
            var best = 0
            var bestDist = Double.POSITIVE_INFINITY
            for (cIdx in 0 until k) {
                val dist = rgbDistance(color, centroids[cIdx] and 0x00FFFFFF)
                if (dist < bestDist) {
                    bestDist = dist
                    best = cIdx
                }
            }
            if (assignments[i] != best) {
                assignments[i] = best
                changed = true
            }
        }

        // If nothing changed, we're done
        if (!changed) return@repeat

        // Update step
        val sumR = IntArray(k)
        val sumG = IntArray(k)
        val sumB = IntArray(k)
        val counts = IntArray(k)

        for (i in 0 until n) {
            val idx = assignments[i]
            val c = colors[i]
            sumR[idx] += Color.red(c)
            sumG[idx] += Color.green(c)
            sumB[idx] += Color.blue(c)
            counts[idx] += 1
        }

        for (cIdx in 0 until k) {
            if (counts[cIdx] == 0) {
                // Reinitialize empty cluster to a random-ish color from input to avoid dead cluster
                val fallback = colors[(cIdx * 997) % n]
                centroids[cIdx] = fallback
            } else {
                val r = (sumR[cIdx].toDouble() / counts[cIdx]).roundToInt().coerceIn(0, 255)
                val g = (sumG[cIdx].toDouble() / counts[cIdx]).roundToInt().coerceIn(0, 255)
                val b = (sumB[cIdx].toDouble() / counts[cIdx]).roundToInt().coerceIn(0, 255)
                centroids[cIdx] = Color.rgb(r, g, b)
            }
        }
    }

    return centroids
}

private fun findNearest(rgb: Int, palette: List<Int>): Int {
    var best = palette[0]
    var bestDist = Double.POSITIVE_INFINITY
    for (p in palette) {
        val d = rgbDistance(rgb, p and 0x00FFFFFF)
        if (d < bestDist) {
            bestDist = d
            best = p
        }
    }
    return best
}

private fun rgbDistance(c1: Int, c2: Int): Double {
    val r1 = (c1 shr 16) and 0xFF
    val g1 = (c1 shr 8) and 0xFF
    val b1 = c1 and 0xFF
    val r2 = (c2 shr 16) and 0xFF
    val g2 = (c2 shr 8) and 0xFF
    val b2 = c2 and 0xFF
    val dr = (r1 - r2).toDouble()
    val dg = (g1 - g2).toDouble()
    val db = (b1 - b2).toDouble()
    return dr * dr + dg * dg + db * db
}