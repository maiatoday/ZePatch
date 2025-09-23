package de.berlindroid.zepatch.patchable

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.annotations.Patch
import de.berlindroid.zepatch.ui.LocalPatchInList
import de.berlindroid.zepatch.ui.SafeArea

@OptIn(ExperimentalMaterial3Api::class)
@Patch("Shared Image")
@Composable
fun SharedImage(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    // Provide an in-patch picker so users can choose an image directly from this patch
    val context = LocalContext.current
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bmp = context.contentResolver.openInputStream(uri)?.use { ins ->
                    BitmapFactory.decodeStream(ins)
                }
                val safe = bmp?.copy(Bitmap.Config.ARGB_8888, false)?.asImageBitmap()
                if (safe != null) {
                    preview = safe
                    // Pass the selected bitmap directly into the wizard pipeline
                    onBitmap(safe)
                }
            } catch (_: Exception) {
                // ignore and keep UI as-is
            }
        }
    }
    val inList = LocalPatchInList.current
    Column {
        Button(
            modifier = Modifier.padding(8.dp),
            enabled = !inList,
            onClick = { launcher.launch("image/*") }) {
            Text("Choose image from gallery")
        }
        SafeArea(
            shouldCapture = shouldCapture,
            onBitmap = onBitmap,
        ) {
            preview?.let { img ->
                Image(
                    modifier = Modifier.size(200.dp),
                    bitmap = img,
                    contentDescription = null
                )
            }
        }
    }
}
