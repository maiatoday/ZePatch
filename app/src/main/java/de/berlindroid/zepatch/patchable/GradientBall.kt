package de.berlindroid.zepatch.patchable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.ui.SafeArea

/**
 * Draws a circular ball with a retro-style vertical gradient consisting of
 * Cyan (top), Pink (middle), and Purple (bottom) bands with transparent gaps.
 */
@Composable
fun GradientBall(
    modifier: Modifier = Modifier,
    topColor: Color = Color.Cyan,
    middleColor: Color = Color(0xFFFF69B4), // Pink
    bottomColor: Color = Color(0xFF800080), // Purple
) {
    SafeArea {
        Canvas(modifier = modifier.size(56.dp)) {
            val height = size.height
            val brushRetro = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to topColor,
                    0.43f to topColor,
                    0.43f to Color.Transparent,
                    0.45f to Color.Transparent,
                    0.45f to topColor,
                    0.57f to middleColor,
                    0.57f to Color.Transparent,
                    0.6f to Color.Transparent,
                    0.6f to middleColor,
                    0.7f to middleColor,
                    0.7f to Color.Transparent,
                    0.74f to Color.Transparent,
                    0.74f to middleColor,
                    0.84f to bottomColor,
                    0.84f to Color.Transparent,
                    0.9f to Color.Transparent,
                    0.9f to bottomColor,
                    1.0f to bottomColor
                ),
                startY = 0f,
                endY = height
            )

            val radius = size.minDimension / 2f
            drawCircle(
                brush = brushRetro,
                radius = radius,
                center = Offset(size.width / 2f, size.height / 2f)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewGradientBall() {
    GradientBall(modifier = Modifier.size(200.dp))
}
