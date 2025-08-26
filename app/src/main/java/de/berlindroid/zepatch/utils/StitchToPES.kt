package de.berlindroid.zepatch.utils

import android.util.Log
import com.chaquo.python.Python

object StitchToPES {
    fun doit(): ByteArray? {
        try {
            val python = Python.getInstance()

            val converter = python.getModule("converter")
            val result = converter.callAttr("convert", null)
            val pesBytes = result.toJava(ByteArray::class.java)
            Log.i("YOLO", pesBytes.toHexString())
            return pesBytes
        } catch (th: Throwable) {
            Log.e("PYTHON", "No.", th)
            return null
        }
    }
}
