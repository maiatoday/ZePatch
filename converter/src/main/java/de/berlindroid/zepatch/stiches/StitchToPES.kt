package de.berlindroid.zepatch.stiches

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.embroidermodder.punching.reduceColors
import de.berlindroid.zepatch.converter.R
import java.nio.Buffer
import java.nio.IntBuffer
import kotlin.io.encoding.Base64
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class XY(
    val x: Float,
    val y: Float,
)

data class Thread(
    val color: Int,
    val stitches: Array<XY>,
    val absolute: Boolean = false,
)

data class Embroidery(
    val name: String,
    val threads: Array<Thread>
)

object StitchToPES {
    fun convert(context: Context, embroidery: Embroidery, fileFormat:String = "pes"): ByteArray? {
        Log.i("PYTHON", "Creating PES for '${embroidery.name}'.")

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        try {
            val py = Python.getInstance()

            val converter = py.getModule("converter")
            val result = converter.callAttr("convert", embroidery, fileFormat)

            val pesBytes = result.toJava(ByteArray::class.java)
            Log.i("PYTHON", "Found these bytes: ${Base64.encode(pesBytes)}.")

            return pesBytes
        } catch (th: Throwable) {
            Log.e("PYTHON", "No.", th)
            Toast.makeText(context, "Python error: ${th.message}.", Toast.LENGTH_LONG).show()
            return null
        }
    }

    /**
     * Crappy first iteration, do not look to closely.
     */
    fun createEmbroideryFromBitmap(
        name: String,
        bitmap: Bitmap,
        mmWidth: Float,
        mmHeight: Float,
        mmDensity: Float,
    ): Embroidery {
        val threads = mutableMapOf<Int, Thread>()

        val stitchHeight = (mmHeight / mmDensity).roundToInt()
        val stitchWidth = (mmWidth / mmDensity).roundToInt()

        for (y in 0..<stitchHeight) {
            val mmY = y * mmDensity
            val pixelY = ((y / stitchHeight.toFloat()) * bitmap.height).toInt()

            val range = if (y % 2 == 0) {
                0..<stitchWidth
            } else {
                stitchWidth - 1 downTo 0
            }

            for (x in range) {
                val pixelX = ((x / stitchWidth.toFloat()) * bitmap.width).toInt()
                val pixel = bitmap[pixelX, pixelY]

                if (pixel.alpha != 255) {
                    // ignore non opaque pixel
                    continue
                }

                if (pixel !in threads) {
                    threads[pixel] = Thread(color = pixel, stitches = arrayOf(), absolute = true)
                }

                val mmX = x * mmDensity

                val oldThread = threads[pixel]
                threads[pixel] =
                    oldThread!!.copy(stitches = oldThread.stitches + arrayOf(XY(mmX, mmY)))
            }
        }
        return Embroidery(
            name,
            threads.values.toTypedArray()
        )
    }

    fun createDummyEmbroidery(distance: Float = 25f, step: Float = 1.5f) = Embroidery(
        name = "RedCircleInGreenFilledRectangle",
        threads = arrayOf(
            Thread(
                color = 0xff0000,
                absolute = false,
                stitches = (0..distance.toInt()).map { y ->
                    val dir = if (y % 2 == 0) 1 else -1
                    (0..distance.toInt()).map { x ->
                        XY(dir * step, 0f)
                    } + XY(0f, step)
                }.flatten().toTypedArray()
            ),
            Thread(
                color = 0x00ff00,
                absolute = true,
                stitches = (0..360).map { angle ->
                    val alpha = angle.radians()
                    XY(
                        x = cos(alpha) * distance * step / 2 + distance / 2 * step,
                        y = sin(alpha) * distance * step / 2 + distance / 2 * step,
                    )
                }.toTypedArray()
            )
        )
    )

    private fun Int.radians(): Float = ((this / 180.0) * PI).toFloat()

    private fun getSampleBitmap(context: Context) =
        ResourcesCompat.getDrawable(context.resources, R.drawable.test, null)
            ?.toBitmap(config = Bitmap.Config.ARGB_4444)
            ?.scale(32, 32, false)
            ?.reduceColors(3)

}
