package de.berlindroid.zepatch.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import de.berlindroid.zepatch.WizardViewModel.UiState
import de.berlindroid.zepatch.WizardViewModel.UiState.Done
import de.berlindroid.zepatch.WizardViewModel.UiState.SelectPatchable
import de.berlindroid.zepatch.WizardViewModel.UiState.ReduceBitmap
import de.berlindroid.zepatch.WizardViewModel.UiState.SetupComposable
import de.berlindroid.zepatch.WizardViewModel.UiState.EmbroiderBitmap
import de.berlindroid.zepatch.patchables

@Composable
fun WizardContent(
    state: UiState,
    patchable: @Composable (Boolean, (ImageBitmap) -> Unit) -> Unit,
    onBitmapUpdated: (ImageBitmap) -> Unit,
    onColorCountUpdated: (Int) -> Unit,
    onComputeReducedBitmap: () -> Unit,
    onCreateEmbroidery: () -> Unit,
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

            when (state) {
                is SelectPatchable -> Unit

                is SetupComposable -> PatchableToBitmap(
                    patchable = patchable,
                    onBitmap = onBitmapUpdated
                )

                is ReduceBitmap -> PatchableToReducedBitmap(
                    state = state,
                    onColorCountChanged = onColorCountUpdated,
                    computeReducedBitmap = onComputeReducedBitmap,
                )

                is EmbroiderBitmap -> BitmapToStitches(
                    state = state,
                    onCreateEmbroidery = onCreateEmbroidery,
                )

                is Done -> Celebration(
                    state = state
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun WizardContentPreview() {
    WizardContent(
        state = ReduceBitmap("name", createBitmap(100, 100).asImageBitmap()),
        patchable = patchables.values.first(),
        onBitmapUpdated = {},
        onColorCountUpdated = {},
        onComputeReducedBitmap = {},
        onCreateEmbroidery = {},
    )
}
