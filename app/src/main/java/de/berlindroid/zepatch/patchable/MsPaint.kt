package de.berlindroid.zepatch.patchable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.annotations.Patch
import de.berlindroid.zepatch.ui.LocalPatchInList
import de.berlindroid.zepatch.ui.SafeArea

private enum class OutlineShape { Circle, Diamond, Square, Heart }
private data class StrokePath(val color: Color, val points: MutableList<Offset>)

@OptIn(ExperimentalMaterial3Api::class)
@Patch("MsPaint")
@Composable
fun MsPaint(
    shouldCapture: Boolean = false,
    onBitmap: (ImageBitmap) -> Unit = {},
) {
    val inList = LocalPatchInList.current

    // Palette colors as requested
    val palette = listOf(
        Color.White,
        Color.Black,
        Color.Red,
        Color.Yellow,
        Color.Magenta,
        Color(0xFFFFA500), // orange
        Color(0xFF800080), // purple
        Color(0xFF0000FF), // blue
        Color(0xFFFFC0CB), // pink
        Color(0xFFA52A2A), // brown
    )

    val selectedColor = remember { mutableStateOf(Color.Red) }
    val outlineShape = remember { mutableStateOf(OutlineShape.Diamond) }

    // list of strokes (each stroke is a polyline of points)
    val strokes = remember { mutableStateListOf<StrokePath>() }

    // Clearing drawing when outline shape changes
    LaunchedEffect(outlineShape.value) {
        strokes.clear()
    }

    Column(modifier = Modifier.padding(8.dp)) {
        // Top controls: palette (outside the SafeArea)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            palette.forEach { color ->
                val selected = selectedColor.value == color
                Spacer(modifier = Modifier.width(2.dp))
                Row {
                    Box(
                        modifier = Modifier
                            .size(if (selected) 28.dp else 24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.DarkGray, CircleShape)
                            .padding(2.dp)
                            .clickable { selectedColor.value = color }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SafeArea contains the drawing canvas only
        SafeArea(
            shouldCapture = shouldCapture,
            onBitmap = onBitmap,
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.Transparent)
                    .pointerInput(selectedColor.value, outlineShape.value) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                strokes.add(StrokePath(selectedColor.value, mutableListOf(offset)))
                            },
                            onDrag = { _, dragAmount ->
                                val last = strokes.lastOrNull() ?: return@detectDragGestures
                                val lastPoint = last.points.last()
                                last.points.add(lastPoint + dragAmount)
                            }
                        )
                    }
                    .height(if (inList) 180.dp else 240.dp)
            ) {
                // Compute outline path within canvas
                val padding = 16f
                val rect = Rect(padding, padding, size.width - padding, size.height - padding)
                val outline = Path().apply {
                    when (outlineShape.value) {
                        OutlineShape.Circle -> addOval(rect)
                        OutlineShape.Square -> addRect(rect)
                        OutlineShape.Diamond -> {
                            moveTo(rect.center.x, rect.top)
                            lineTo(rect.right, rect.center.y)
                            lineTo(rect.center.x, rect.bottom)
                            lineTo(rect.left, rect.center.y)
                            close()
                        }
                        OutlineShape.Heart -> {
                            val w = rect.width
                            val h = rect.height
                            val x = rect.left
                            val y = rect.top
                            // A symmetric heart built with two cubic Bezier curves inside the rect
                            moveTo(x + 0.5f * w, y + 0.28f * h)
                            cubicTo(
                                x + 0.15f * w, y + 0.00f * h,
                                x + 0.00f * w, y + 0.38f * h,
                                x + 0.50f * w, y + 0.88f * h,
                            )
                            cubicTo(
                                x + 1.00f * w, y + 0.38f * h,
                                x + 0.85f * w, y + 0.00f * h,
                                x + 0.50f * w, y + 0.28f * h,
                            )
                            close()
                        }
                    }
                }

                // Fill inside white
                drawPath(outline, color = Color.White, style = Fill)
                // Draw outline stroke black
                drawPath(outline, color = Color.Black, style = Stroke(width = 6f))

                // Clip to inside for drawing
                clipPath(outline) {
                    val strokeWidth = 12f
                    strokes.forEach { stroke ->
                        // draw as connected segments
                        for (i in 1 until stroke.points.size) {
                            drawLine(
                                color = stroke.color,
                                start = stroke.points[i - 1],
                                end = stroke.points[i],
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom controls: outline shape selection (outside SafeArea)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { outlineShape.value = OutlineShape.Circle },
                    enabled = !inList
                ) {
                    Text("Circle")
                }
                Button(
                    onClick = { outlineShape.value = OutlineShape.Diamond },
                    enabled = !inList
                ) {
                    Text("Diamond")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { outlineShape.value = OutlineShape.Square }, enabled = !inList) {
                    Text("Square")
                }
                Button(onClick = { outlineShape.value = OutlineShape.Heart }, enabled = !inList) {
                    Text("Heart")
                }
            }
        }
    }
}