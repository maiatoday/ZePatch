package de.berlindroid.zepatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import de.berlindroid.zepatch.WizardViewModel.UiState.Done

@Composable
fun Celebration(
    modifier: Modifier = Modifier,
    state: Done
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WizardSectionTitle(
            title = "Done!",
            helpText = "You have successfully completed a patch. Congratulations and now give your self a pat on the back."
        )
        Image(
            bitmap = state.embroideryPreviewImage,
            contentDescription = "reduced bitmap",
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error.isNullOrBlank()) {
            // TODO @ DROIDCON -> MAKE FANCY HURRAY ANIMATION
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "🎉",
                fontSize = 300.sp,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Patch '${state.name}' seems to have saved successfully!"
            )
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "🥺",
                fontSize = 300.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Error in '${state.name}': ${state.error}."
            )
        }
    }
}
