package de.berlindroid.zepatch.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.berlindroid.zepatch.WizardViewModel

@Composable
fun WizardContent(
    state: WizardViewModel.UiState,
    patchable: @Composable (Boolean, (ImageBitmap) -> Unit) -> Unit,
    onBitmapUpdated: (ImageBitmap) -> Unit,
    onColorCountUpdated: (Int) -> Unit,
    computeReducedBitmap: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(size = 25.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            if (!state.error.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = state.error ?: "This is Error.")
                }
            }

            val vm: WizardViewModel = viewModel()
            when (state) {
                is WizardViewModel.UiState.SelectPatchable -> Unit

                is WizardViewModel.UiState.SetupComposable -> PatchableToBitmap(
                    patchable = patchable,
                    onBitmap = onBitmapUpdated
                )

                is WizardViewModel.UiState.SetupBitmap -> PatchableToReducedBitmap(
                    state = state,
                    onColorCountChanged = onColorCountUpdated,
                    computeReducedBitmap = computeReducedBitmap,
                )

                is WizardViewModel.UiState.SetupEmbroidery -> BitmapToStitches(
                    state = state,
                    onCreateEmbroidery = vm::createEmbroidery,
                )

                is WizardViewModel.UiState.Done -> Celebration(
                    state = state
                )
            }
        }
    }
}
