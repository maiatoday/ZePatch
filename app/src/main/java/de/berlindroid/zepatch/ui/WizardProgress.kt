package de.berlindroid.zepatch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.WizardViewModel.UiState
import de.berlindroid.zepatch.WizardViewModel.UiState.Done
import de.berlindroid.zepatch.WizardViewModel.UiState.EmbroiderBitmap
import de.berlindroid.zepatch.WizardViewModel.UiState.ReduceBitmap
import de.berlindroid.zepatch.WizardViewModel.UiState.SetupComposable
import de.berlindroid.zepatch.isBusy

@Composable
fun WizardProgress(
    state: UiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = onPrev, enabled = state !is SetupComposable && !state.isBusy()) {
            Text(
                "Previous"
            )
        }
        WizardPrgressIndicator(
            modifier = Modifier
                .height(48.dp)
                .weight(1f)
                .padding(horizontal = 8.dp),
            totalSteps = 4,
            currentStep = when (state) {
                is SetupComposable -> 0
                is ReduceBitmap -> 1
                is EmbroiderBitmap -> 2
                is Done -> 3
                else -> 0
            }
        )
        Button(
            onClick = {
                if (state is Done) {
                    onDone()
                } else {
                    onNext()
                }
            },
            enabled = state.isCompleted() && !state.isBusy()
        ) {
            when (state) {
                is EmbroiderBitmap -> Text("Save")
                is Done -> Text("Finish")
                else -> Text("Next")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WizardProgresPreview() {
    WizardProgress(
        state = SetupComposable("hello"),
        onPrev = {},
        onNext = {},
        onDone = {},
    )
}
