package de.berlindroid.zepatch.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import com.embroidermodder.punching.reduceColors
import de.berlindroid.zepatch.stiches.StitchToPES
import de.berlindroid.zepatch.stiches.StitchToPES.createEmbroideryFromBitmap
import de.berlindroid.zepatch.utils.CaptureToBitmap


@Composable
fun BitmapToStitches(
    modifier: Modifier = Modifier,
    name: String,
    patchable: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var bytes by remember { mutableStateOf<ByteArray?>(null) }
    var capture by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        savePesAfterSelection(context, result, bytes)
    }

    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(onClick = {
            image = null
            capture = true
        }) { Text("Do it") }  // Compose the content through the composable utility; it will invoke the callback when ready.
        CaptureToBitmap(
            modifier = Modifier.fillMaxWidth(),
            capture = capture,
            onBitmap = { img ->
                // TODO: PARRALELEIZE & VMIZE
                val aspect = img.width / img.height.toFloat()

                val reduced =
                    img.asAndroidBitmap()
                        .copy(Bitmap.Config.ARGB_8888, false)
                        .scale((512 * aspect).toInt(), 512, false)
                        .reduceColors(3) // TODO UI For color count selection

                val embroidery = createEmbroideryFromBitmap(
                    name,
                    reduced,
                    mmWidth = 30f * aspect,
                    mmHeight = 30f,
                    mmDensity = 0.5f
                )

                val pes = StitchToPES.convert(context, embroidery)
                if (pes == null || pes.isEmpty()) {
                    Log.e("EMBNO", "No pes found.")
                } else {
                    bytes = pes
                }

                val png = StitchToPES.convert(context, embroidery, "png")
                if (png == null || png.isEmpty()) {
                    Log.e("PNGNO", "No png returned.")
                } else {
                    val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)

                    image = decoded
                        .scale(decoded.width * 2, decoded.height * 2)
                        .asImageBitmap()
                }
                capture = false
            },
            content = patchable
        )

        image?.let {
            Image(
                bitmap = it,
                contentDescription = "patch bitmap",
                modifier = Modifier.fillMaxWidth()
            )
        } ?: CircularProgressIndicator()

        bytes?.let { pesBytes ->
            IconButton(onClick = {
                val magic = String(pesBytes.toList().subList(0, 8).toByteArray())
                val byteCount = pesBytes.size
                val kbCount = byteCount / 1024
                val mbCount = kbCount / 1024

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "application/octet"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, "$name.pes")
                }

                launcher.launch(intent)

                Toast.makeText(
                    context,
                    "Found $byteCount bytes. ($kbCount KB, $mbCount MB)\nFile magic '$magic'.",
                    Toast.LENGTH_LONG
                ).show()
            }) {
                Icon(painterResource(android.R.drawable.ic_menu_save), null)
            }
        }
    }
}


private fun savePesAfterSelection(context: Context, result: ActivityResult, bytes: ByteArray?) {
    // TODO MOVE ME INTO VM

    if (bytes == null || bytes.isEmpty()) {
        Log.e("NO", "No PES created. Why are we here?")
        return
    }
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

@Preview(showBackground = true)
@Composable
private fun BitmapToStitchesPreview() {
    BitmapToStitches(Modifier, "Demo") {
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

