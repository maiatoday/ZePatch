package de.berlindroid.zepatch.stiches

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlin.io.encoding.Base64
import kotlin.math.PI
import kotlin.math.cos
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
    fun convert(context: Context, embroidery: Embroidery): ByteArray? {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        try {
            val py = Python.getInstance()

            val converter = py.getModule("converter")
            val result = converter.callAttr("convert", embroidery)

            val pesBytes = result.toJava(ByteArray::class.java)
            Log.i("PYTHON", "Found these bytes: ${Base64.encode(pesBytes)}.")

            return pesBytes
        } catch (th: Throwable) {
            Log.e("PYTHON", "No.", th)
            Toast.makeText(context, "Python error: ${th.message}.", Toast.LENGTH_LONG).show()
            return null
        }
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

}
