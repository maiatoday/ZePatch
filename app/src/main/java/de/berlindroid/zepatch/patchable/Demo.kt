package de.berlindroid.zepatch.patchable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Demo(
    modifier: Modifier = Modifier,
) {
    SafeArea {
        Text("Demo")
    }
}

// TODO: share this?
@Composable
fun SafeArea(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.padding(32.dp),
    ) {
        content()
    }
}

@Preview
@Composable
fun PreviewDemo() {

}