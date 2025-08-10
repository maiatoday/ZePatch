package de.berlindroid.zepatch.patchable

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.berlindroid.zepatch.ui.SafeArea
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.annotations.Patch

@Patch("Demo")
@Composable
fun Demo() {
    SafeArea {
        Text("Demo")
    }
}

@Patch("Demo2")
@Composable
fun Demo2() {
    SafeArea {
        Text("Another Demo")
    }
}
@Preview
@Composable
fun PreviewDemo() {
    Demo()
}