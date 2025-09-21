package de.berlindroid.zepatch.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.berlindroid.zepatch.SharedImageStore
import de.berlindroid.zepatch.WizardViewModel
import de.berlindroid.zepatch.WizardViewModel.UiState.EmbroiderBitmap

@ExperimentalMaterial3Api
@Composable
fun PatchableDetail(
    modifier: Modifier = Modifier,
    name: String,
    onBackClick: () -> Unit,
    patchable: @Composable (Boolean, (ImageBitmap) -> Unit) -> Unit,
) {
    val viewModel: WizardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Once we SAFed a file, we should fill it
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.savePesAfterSelection(result)
    }

    // Reset wizard state when a different patchable is opened
    LaunchedEffect(name) {
        viewModel.setPatchableNameAndStart(name)
    }

    // If a shared/picked image is waiting, consume it and push the wizard forward
    val context = LocalContext.current
    LaunchedEffect(name) {
        val uri = SharedImageStore.take()
        if (uri != null) {
            try {
                val bmp = context.contentResolver.openInputStream(uri)?.use { ins ->
                    BitmapFactory.decodeStream(ins)
                }
                val safe = bmp?.copy(Bitmap.Config.ARGB_8888, false)?.asImageBitmap()
                if (safe != null) {
                    viewModel.updateBitmap(safe)
                    if (viewModel.isStateCompleted()) {
                        viewModel.progressToNextState()
                    }
                }
            } catch (_: Exception) {
                // Ignored; Wizard will show error if needed later
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is EmbroiderBitmap) {
            viewModel.setLauncher(launcher)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.reset()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                },
                title = {
                    Text(name)
                },
            )
        },
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            WizardContent(
                uiState,
                patchable = patchable,
                onBitmapUpdated = viewModel::updateBitmap,
                onColorCountUpdated = viewModel::updateColorCount,
                onComputeReducedBitmap = viewModel::computeReducedBitmap,
                onUpdateEmbroidery = viewModel::updateEmbroideryConfig,
                onCreateEmbroidery = viewModel::createEmbroidery
            )

            WizardProgress(
                state = uiState,
                onPrev = viewModel::onBackPressed,
                onNext = {
                    if (viewModel.isStateCompleted()) {
                        viewModel.progressToNextState()
                    }
                },
                onDone = {
                    viewModel.reset()
                    onBackClick()
                }
            )
        }
    }
}
