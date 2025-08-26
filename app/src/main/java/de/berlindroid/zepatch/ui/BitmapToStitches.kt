package de.berlindroid.zepatch.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import de.berlindroid.zepatch.stiches.Thread
import de.berlindroid.zepatch.stiches.Embroidery
import de.berlindroid.zepatch.stiches.StitchToPES
import de.berlindroid.zepatch.stiches.StitchToPES.createDummyEmbroidery
import de.berlindroid.zepatch.stiches.XY
import kotlin.math.PI
import kotlin.math.cos

/**
 * Renders the provided [patchable] into a Bitmap-like [ImageBitmap] and displays it via [Image].
 * Uses a composable utility that captures the composed content from a graphics layer.
 */
@Composable
fun BitmapToStitches(
    modifier: Modifier = Modifier,
    patchable: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var bytes by remember { mutableStateOf<ByteArray>(byteArrayOf()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (bytes.isEmpty()) {
            Log.e("NO", "No PES created. Why are we here?")
        } else {
            Log.i("YOLO?", "File for saving selected. $result")

            result.data?.let { data: Intent ->
                data.data?.let { uri: Uri ->
                    val resolver = context.contentResolver.apply {
                        takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }

                    val outputStream = resolver.openOutputStream(uri)
                    try {
                        outputStream?.write(bytes)
                    } finally {
                        outputStream?.close()
                    }

                    Toast.makeText(
                        context,
                        "Done writing file! Happy embroidering 🪡",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Button(onClick = {
            val embroidery = createDummyEmbroidery()

            val pes = StitchToPES.convert(context, embroidery)
            if (pes == null || pes.isEmpty()) {
                return@Button
            }

            bytes = pes

            val magic = String(bytes.toList().subList(0, 8).toByteArray())
            val byteCount = bytes.size
            val kbCount = byteCount / 1024
            val mbCount = kbCount / 1024

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "application/octet"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_TITLE, "title.pes")
            }

            launcher.launch(intent)

            Toast.makeText(
                context,
                "Found $byteCount bytes. ($kbCount KB, $mbCount MB)\nFile magic '$magic'.",
                Toast.LENGTH_LONG
            ).show()
        }) {
            Text("TEMP: DOIT")
        }
    }
}
