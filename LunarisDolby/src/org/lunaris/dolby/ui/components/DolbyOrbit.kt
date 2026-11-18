/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun DolbyOrbit(
    enabled: Boolean,
    illustrationHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ringColor = if (enabled) colors.onPrimaryContainer else colors.outline
    val dotColor = if (enabled) colors.primary else colors.outline
    val rotation = if (enabled) {
        rememberInfiniteTransition(label = "Dolby orbit").animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(4800, easing = LinearEasing)),
            label = "Orbit angle",
        )
    } else null

    Spacer(modifier.drawWithCache {
        val height = illustrationHeight.toPx()
        val center = Offset(size.width / 2f, height / 2f)
        val radius = minOf(size.width * 0.157f, height * 0.234f)
        val trackStroke = Stroke(width = radius * 0.14f)
        val rippleStroke = Stroke(width = 1.dp.toPx())
        val rippleColor = ringColor.copy(alpha = 0.12f)
        val dotRadius = radius * 0.17f
        val glowRadius = dotRadius * 3.3f
        val glow = Brush.radialGradient(
            colors = listOf(dotColor.copy(alpha = 0.48f), dotColor.copy(alpha = 0f)),
            center = Offset.Zero,
            radius = glowRadius,
        )

        onDrawBehind {
            for (index in 1..3) {
                drawCircle(rippleColor, radius * (1f + index * 0.73f), center, style = rippleStroke)
            }
            drawCircle(ringColor, radius, center, style = trackStroke)

            val angle = Math.toRadians(57.0 + (rotation?.value ?: 0f))
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            translate(left = x, top = y) {
                if (enabled) drawCircle(glow, glowRadius, Offset.Zero)
                drawCircle(dotColor, dotRadius, Offset.Zero)
            }
        }
    })
}
