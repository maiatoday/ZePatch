package de.berlindroid.zepatch

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.embroidermodder.punching.Histogram
import com.embroidermodder.punching.reduceColors
import de.berlindroid.zepatch.WizardViewModel.UiState
import de.berlindroid.zepatch.WizardViewModel.UiState.*
import de.berlindroid.zepatch.stiches.StitchToPES
import de.berlindroid.zepatch.utils.multiLet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel to manage the Wizard screen state.
 */
class WizardViewModel(application: Application) : AndroidViewModel(application) {

    sealed class UiState(
        open val error: String? = null
    ) {
        abstract fun previous(): UiState
        abstract fun next(): UiState
        abstract fun isCompleted(): Boolean
        abstract fun copyWithError(error: String): UiState

        data class SelectPatchable(
            // input

            // configureable
            val name: String? = null,

            // outputs
            override val error: String? = null,
        ) : UiState(error) {
            override fun previous(): UiState = this

            override fun next(): UiState = SetupComposable(name!!)
            override fun isCompleted(): Boolean = name?.isNotEmpty() == true
            override fun copyWithError(error: String) = SelectPatchable(error = error)
        }

        data class SetupComposable(
            // input
            val name: String,

            // configurable
            // bitmap size???

            // output
            val image: ImageBitmap? = null,

            override val error: String? = null
        ) : UiState(error) {
            override fun previous(): UiState = SelectPatchable()

            override fun next(): UiState = SetupBitmap(name, image!!)

            override fun isCompleted(): Boolean = image != null

            override fun copyWithError(error: String): UiState = copy(error = error)
        }

        data class SetupBitmap(
            // input
            val name: String,
            val image: ImageBitmap,

            // configurable
            val colorCount: Int = 3,
            val currentlyReducingColors: Boolean = false,
            // TODO
//        val tolerance: Int? = null,

            // output
            val reducedBitmap: ImageBitmap? = null,
            val reducedHistogram: Histogram? = null,

            override val error: String? = null
        ) : UiState(error) {
            override fun previous(): UiState = SetupComposable(name)

            override fun next(): UiState = SetupEmbroidery(
                name,
                image,
                colorCount,
                reducedBitmap!!,
                reducedHistogram!!
            )

            override fun isCompleted(): Boolean = reducedBitmap != null
                    && reducedHistogram != null

            override fun copyWithError(error: String): UiState = copy(error = error)
        }

        data class SetupEmbroidery(
            // input
            val name: String,
            val image: ImageBitmap,
            val colorCount: Int,
            val reducedBitmap: ImageBitmap,
            val reducedHistogram: Histogram,

            // configurable
            // TODO
            // density
            // satin border yes no
            // sizes
            // colors?
            val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null,
            val currentlyEmbroidering: Boolean = false,

            // output
            val embroideryData: ByteArray? = null,
            val embroideryPreviewImage: ImageBitmap? = null,

            override val error: String? = null
        ) : UiState(error) {
            override fun previous(): UiState = SetupBitmap(name, image, colorCount)

            override fun next(): UiState {
                embroideryData?.multiLet(launcher) { data, launcher ->
                    val magic = String(data.toList().subList(0, 8).toByteArray())
                    val byteCount = data.size
                    val kbCount = byteCount / 1024
                    val mbCount = kbCount / 1024

                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        type = "application/octet"
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_TITLE, "$name.pes")
                    }

                    launcher.launch(intent)
                    Log.i("EMBROIDER", "Found $byteCount bytes. ($kbCount KB, $mbCount MB)\nFile magic '$magic'.")
                }

                return Done(
                    name,
                    image,
                    colorCount,
                    reducedBitmap,
                    reducedHistogram,
                    embroideryData!!,
                    embroideryPreviewImage!!
                )
            }

            override fun isCompleted(): Boolean =
                launcher != null && embroideryData != null && embroideryPreviewImage != null

            override fun copyWithError(error: String): UiState = copy(error = error)
        }

        // Dr. Peter Venkman: Human sacrifice, dogs and cats living together - MASS HYSTERIA!
        data class Done(
            val name: String,
            val image: ImageBitmap,
            val colorCount: Int,
            val reducedBitmap: ImageBitmap,
            val reducedHistogram: Histogram,
            val embroideryData: ByteArray,
            val embroideryPreviewImage: ImageBitmap,
            override val error: String? = null
        ) : UiState(error) {
            override fun previous(): UiState = SetupEmbroidery(name, image, colorCount, reducedBitmap, reducedHistogram)

            override fun next(): UiState = SelectPatchable()

            override fun isCompleted(): Boolean = true

            override fun copyWithError(error: String): UiState = copy(error = error)
        }
    }

    private val _uiState = MutableStateFlow<UiState>(SelectPatchable())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onBackPressed() {
        _uiState.update { it.previous() }
    }

    fun isStateCompleted(): Boolean = _uiState.value.isCompleted()

    fun reset() {
        _uiState.update { SelectPatchable() }
    }

    fun setPatchableNameAndStart(name: String) {
        _uiState.update {
            if (it is SelectPatchable) {
                it.copy(name = name).next()
            } else {
                it.copyWithError("Not in selection state, so how did you get it selected?")
            }
        }
    }

    fun progressToNextState() {
        _uiState.update {
            if (_uiState.value.isCompleted()) {
                it.next()
            } else {
                it.copyWithError("State is incomplete! Please complete it. Or else it stays incomplete. Only completed states can be completely completed.")
            }
        }
    }

    fun updateBitmap(bitmap: ImageBitmap) {
        _uiState.update {
            when (it) {
                is SetupComposable -> it.copy(image = bitmap)
                else -> it.copyWithError("Bitmap cannot be updated in state ${it.javaClass.simpleName}.")
            }
        }
    }

    fun updateColorCount(count: Int) {
        _uiState.update {
            when (it) {
                is SetupBitmap -> it.copy(colorCount = count)
                else -> it.copyWithError("Colors could not be updated in state ${it.javaClass.simpleName}.")
            }
        }
    }

    fun computeReducedBitmap() {
        if (_uiState.value !is SetupBitmap) {
            _uiState.update { it.copyWithError("Cannot create reduced bitmap in state ${it.javaClass.simpleName}.") }
            return
        }

        _uiState.update {
            (it as? SetupBitmap)?.copy(
                reducedBitmap = null,
                reducedHistogram = null,
                currentlyReducingColors = true
            ) ?: it.copyWithError("Inbetween change, we keep on trucking")
        }

        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value as SetupBitmap
            val aspect = state.image.width / state.image.height.toFloat()
            val (reducedBmp, histogram) = state.image.asAndroidBitmap()
                .copy(Bitmap.Config.ARGB_8888, false)
                .scale((512 * aspect).toInt(), 512, false)
                .reduceColors(
                    state.colorCount,
                    minTolerance = 100
                )

            _uiState.update {
                when (it) {
                    is SetupBitmap -> it.copy(
                        reducedBitmap = reducedBmp.asImageBitmap(),
                        reducedHistogram = histogram,
                        currentlyReducingColors = false,
                    )

                    else -> it.copyWithError("Mode changed while processing. Call ask Jeeves for help!")
                }
            }
        }
    }

    fun createEmbroidery() {
        if (_uiState.value !is SetupEmbroidery) {
            _uiState.update {
                it.copyWithError("No, sadly you have to be in embroidery mode!")
            }
            return
        }

        _uiState.update {
            (it as? SetupEmbroidery)?.copy(
                embroideryData = null,
                embroideryPreviewImage = null,
                currentlyEmbroidering = true
            ) ?: it
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val state = _uiState.value as SetupEmbroidery

                val aspect = state.reducedBitmap.width / state.reducedBitmap.height.toFloat()
                val embroidery = StitchToPES.createEmbroideryFromBitmap(
                    name = state.name,
                    bitmap = state.reducedBitmap.asAndroidBitmap(),
                    histogram = state.reducedHistogram,
                    mmWidth = 50f * aspect,
                    mmHeight = 50f,
                    mmDensityX = 0.5f,
                    mmDensityY = 0.2f,
                    satinBorderThickness = 10f,
                )

                val context = getApplication<Application>().applicationContext
                val pes = StitchToPES.convert(context, embroidery)
                val png = StitchToPES.convert(context, embroidery, "png")

                val previewImage = if (png == null || png.isEmpty()) {
                    null
                } else {
                    val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)
                    decoded.scale(decoded.width * 2, decoded.height * 2).asImageBitmap()
                }

                _uiState.update {
                    (it as? SetupEmbroidery)?.copy(
                        embroideryData = pes,
                        embroideryPreviewImage = previewImage,
                        currentlyEmbroidering = false,
                    ) ?: it
                }
            } catch (th: Throwable) {
                _uiState.update {
                    val message = "Throwable thrown: ${th}."
                    (it as? SetupEmbroidery)?.copy(
                        embroideryData = null,
                        embroideryPreviewImage = null,
                        currentlyEmbroidering = false,
                    )?.copyWithError(message)
                        ?: it.copyWithError(message)
                }
            }
        }
    }

    fun savePesAfterSelection(result: ActivityResult) {
        val state = _uiState.value
        if (!state.isCompleted()) {
            _uiState.update { it.copyWithError("Incomplete embroidery data, please fill it.") }
            return
        }

        val bytes = when (state) {
            is SetupEmbroidery -> state.embroideryData
            is Done -> state.embroideryData
            else -> null
        }

        if (bytes == null || bytes.isEmpty()) {
            Log.e("NO", "No PES created. Why are we here?")
            return
        }
        Log.i("YOLO?", "File for saving selected. $result")

        val context = getApplication<Application>().applicationContext
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
            }
        }
    }

    fun setLauncher(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if (_uiState.value is SetupEmbroidery) {
            _uiState.update { (it as SetupEmbroidery).copy(launcher = launcher) }
        }
    }
}

fun UiState.isBusy(): Boolean = when (this) {
    is Done -> false
    is SelectPatchable -> false
    is SetupBitmap -> currentlyReducingColors
    is SetupComposable -> false
    is SetupEmbroidery -> currentlyEmbroidering
}
