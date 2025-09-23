package de.berlindroid.zepatch.patchable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zepatch.R
import de.berlindroid.zepatch.annotations.Patch
import de.berlindroid.zepatch.ui.SafeArea

@OptIn(ExperimentalMaterial3Api::class)
@Patch("AppLogo")
@Composable
fun AppLogo(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.ai_logo),
            contentDescription = null
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Patch("BerlindroidLogo")
@Composable
fun BerlindroidLogo(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.voltron_nosign),
            contentDescription = null
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Patch("GoogleLogo")
@Composable
fun GoogleLogo(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.g),
            contentDescription = null
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Patch("YubicoLogo")
@Composable
fun YubicoLogo(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.yubico),
            contentDescription = null
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Patch("Android")
@Composable
fun AndroidLogo(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.andriod),
            contentDescription = null
        )
    }
}


@Patch("AndyA")
@Composable
fun AndyA(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    var name by remember { mutableStateOf("Andy") }
    Column {
        SafeArea(
            shouldCapture = shouldCapture,
            onBitmap = onBitmap,
        ) {
            Column(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Blue,
                                Color.Cyan
                            )
                        ),
                        shape = RoundedCornerShape(
                            topStartPercent = 50,
                            topEndPercent = 50,
                            bottomStartPercent = 50,
                            bottomEndPercent = 0
                        )
                    ),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = FontFamily.Cursive,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 70.sp,
                    text = name,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        TextField(value = name, onValueChange = { name = it })
    }
}

@Patch("AndyB")
@Composable
fun AndyB(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Column(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(
                        topStartPercent = 0,
                        topEndPercent = 50,
                        bottomStartPercent = 50,
                        bottomEndPercent = 0
                    )
                ),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.fillMaxWidth(),
                fontFamily = FontFamily.Cursive,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 64.sp,
                style = TextStyle.Default.copy(shadow = Shadow(blurRadius = 12f)),
                color = Color.Black,
                text = "AndyB",
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Patch("flex")
@Composable
fun Emoji(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Text("💪", fontSize = 180.sp)
    }
}

@Patch("ROBOT")
@Composable
fun Emoji2(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Text("🦾🤖\nHereWeGo", fontSize = 34.sp)
    }
}

@Preview
@Composable
fun PreviewAndyA() {
    AndyA()
}

@Preview
@Composable
fun PreviewAndyB() {
    AndyB()
}

@Preview
@Composable
fun PreviewFlex() {
    Emoji()
}

@Preview
@Composable
fun PreviewBerlindroid() {
    BerlindroidLogo()
}


@OptIn(ExperimentalMaterial3Api::class)
@Patch("Shared Image")
@Composable
fun SharedImage(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    SafeArea(
        shouldCapture = shouldCapture,
        onBitmap = onBitmap,
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.ai_logo),
            contentDescription = null
        )
    }
}
