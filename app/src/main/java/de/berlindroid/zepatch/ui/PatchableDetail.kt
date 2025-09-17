package de.berlindroid.zepatch.ui

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.berlindroid.zepatch.WizardViewModel
import de.berlindroid.zepatch.WizardViewModel.UiState.SetupEmbroidery

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

    LaunchedEffect(uiState) {
        if (uiState is SetupEmbroidery) {
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
                computeReducedBitmap = viewModel::computeReducedBitmap
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
