package de.berlindroid.zepatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.WizardViewModel

@Composable
fun WizardPrgressIndicator(
    modifier: Modifier = Modifier,
    totalSteps: Int,
    currentStep: Int,
) {
    Canvas(
        modifier = modifier
            .height(24.dp)
    ) {
        val radius = size.height / 4f
        val centerY = size.height / 2f
        val spacing = (size.width - (radius * 2f * totalSteps)) / (totalSteps - 1).coerceAtLeast(1)
        val strokeWidth = radius / 2f
        val lineColor = Color(0xFF9E9E9E)
        val fillColor = Color(0xFF9E9E9E)
        val outlineColor = Color(0xFF9E9E9E)

        // Compute centers for steps
        val centers = (0 until totalSteps).map { i ->
            val x = radius + i * (2f * radius + spacing)
            Offset(x, centerY)
        }

        // Draw connecting lines
        for (i in 0 until totalSteps - 1) {
            val start = centers[i]
            val end = centers[i + 1]
            drawLine(
                color = lineColor,
                start = Offset(start.x + radius, centerY),
                end = Offset(end.x - radius, centerY),
                strokeWidth = strokeWidth
            )
        }

        // Draw circles
        val displayCurrent = (totalSteps - 1 - currentStep).coerceIn(0, totalSteps - 1)
        centers.forEachIndexed { i, c ->
            val index = totalSteps - 1 - i
            if (index == displayCurrent) {
                // highlighted filled circle with thin outline
                drawCircle(color = fillColor, radius = radius, center = c)
                drawCircle(
                    color = outlineColor,
                    radius = radius,
                    center = c,
                    style = Stroke(width = strokeWidth / 2f)
                )
            } else {
                drawCircle(
                    color = outlineColor,
                    radius = radius,
                    center = c,
                    style = Stroke(width = strokeWidth / 1.5f)
                )
            }
        }
    }
}
