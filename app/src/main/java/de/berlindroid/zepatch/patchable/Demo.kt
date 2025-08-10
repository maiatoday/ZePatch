package de.berlindroid.zepatch.patchable

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.berlindroid.zepatch.ui.SafeArea

@Composable
fun Demo(
    modifier: Modifier = Modifier,
) {
    SafeArea {
        Text("Demo")
    }
}

@Preview
@Composable
fun PreviewDemo() {
    Demo()
}