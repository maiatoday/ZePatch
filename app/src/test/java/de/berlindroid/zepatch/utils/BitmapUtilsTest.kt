package de.berlindroid.zepatch.utils

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BitmapUtilsTest {

    @Test
    fun reduceColors_keepsTransparentPixelsUnchanged() {
        val bmp = Bitmap.createBitmap(4, 1, Bitmap.Config.ARGB_8888)
        val pixels = intArrayOf(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.TRANSPARENT
        )
        bmp.setPixels(pixels, 0, 4, 0, 0, 4, 1)

        val reduced = bmp.reduceColors(2)

        val out = IntArray(4)
        reduced.getPixels(out, 0, 4, 0, 0, 4, 1)

        // last pixel should remain fully transparent
        assertEquals(0, Color.alpha(out[3]))
    }

    @Test
    fun reduceColors_reducesDistinctOpaqueColors() {
        val bmp = Bitmap.createBitmap(6, 1, Bitmap.Config.ARGB_8888)
        val pixels = intArrayOf(
            Color.RED,           // R
            Color.GREEN,         // G
            Color.BLUE,          // B
            Color.YELLOW,        // Y
            Color.CYAN,          // C
            Color.MAGENTA        // M
        )
        bmp.setPixels(pixels, 0, 6, 0, 0, 6, 1)

        val reduced = bmp.reduceColors(2)

        val out = IntArray(6)
        reduced.getPixels(out, 0, 6, 0, 0, 6, 1)

        // Count distinct non-transparent RGB colors in output
        val distinct = out.filter { Color.alpha(it) != 0 }
            .map { it and 0x00FFFFFF }
            .toSet()

        assertTrue("Expected at most 2 distinct non-transparent colors, got ${distinct.size}", distinct.size <= 2)
    }

    @Test
    fun reduceColors_preservesAlphaForNonTransparentPixels() {
        val bmp = Bitmap.createBitmap(2, 1, Bitmap.Config.ARGB_8888)
        val semiAlphaRed = Color.argb(128, 255, 0, 0)
        val pixels = intArrayOf(
            semiAlphaRed,
            Color.BLUE
        )
        bmp.setPixels(pixels, 0, 2, 0, 0, 2, 1)

        val reduced = bmp.reduceColors(1)

        val out = IntArray(2)
        reduced.getPixels(out, 0, 2, 0, 0, 2, 1)

        // Alpha of first pixel should remain 128 (semi-transparent)
        assertEquals(128, Color.alpha(out[0]))
    }
}