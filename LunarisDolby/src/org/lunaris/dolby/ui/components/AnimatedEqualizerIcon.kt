/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun AnimatedEqualizerIconDynamic(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 24.dp,
    barCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer_dynamic")

    val barHeights = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.22f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 760 + (index * 65),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_height_$index"
        )
    }

    Canvas(modifier = modifier.size(size)) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height

        val barWidths = List(barCount) { index ->
            val normalizedPosition = if (barCount > 1) index.toFloat() / (barCount - 1) else 0.5f
            val centerOffset = (normalizedPosition - 0.5f) * 2f
            val widthFactor = 1.0f - (centerOffset * centerOffset).pow(0.6f)
            0.5f + (widthFactor * 0.5f)
        }

        val totalWidthFactor = barWidths.sum() + (barCount - 1) * 0.3f
        val baseBarWidth = canvasWidth / totalWidthFactor

        var currentX = 0f

        barHeights.forEachIndexed { index, heightAnimation ->
            val barWidth = baseBarWidth * barWidths[index]
            val barHeight = canvasHeight * heightAnimation.value
            val y = canvasHeight - barHeight

            drawRoundRect(
                color = color,
                topLeft = Offset(currentX, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            currentX += barWidth + (baseBarWidth * 0.3f)
        }
    }
}

private fun gaussian(x: Float, center: Float, sigma: Float): Float =
    exp(-((x - center) * (x - center)) / (2f * sigma * sigma))

@Composable
fun AnimatedWaveformBanner(
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    barCount: Int = 30,
    animated: Boolean = true
) {
    val activityScale by animateFloatAsState(
        targetValue = if (animated) 1f else 0.32f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "waveform_activity"
    )

    val phase: Float
    val swell: Float
    if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "waveform_banner")
        val animatedPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "waveform_phase"
        )
        val animatedSwell by infiniteTransition.animateFloat(
            initialValue = 0.84f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "waveform_swell"
        )
        phase = animatedPhase
        swell = animatedSwell
    } else {
        phase = 0f
        swell = 0.9f
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val centerY = canvasHeight / 2f
        val slotWidth = canvasWidth / barCount
        val barWidth = slotWidth * 0.44f
        val radius = barWidth / 2f
        val maxHalfHeight = centerY * 0.88f

        for (index in 0 until barCount) {
            val position = (index + 0.5f) / barCount
            val centerX = position * canvasWidth

            val envelope = (
                gaussian(position, 0.30f, 0.13f) * 0.95f +
                    gaussian(position, 0.62f, 0.12f) * 0.82f +
                    gaussian(position, 0.46f, 0.06f) * 0.48f +
                    gaussian(position, 0.83f, 0.09f) * 0.34f +
                    gaussian(position, 0.12f, 0.09f) * 0.28f
                ).coerceIn(0f, 1f)

            val ripple = if (animated) {
                0.70f + 0.30f * sin(phase + position * 13f)
            } else {
                0.85f
            }
            val level = (envelope * ripple * swell * activityScale).coerceIn(0f, 1f)
            val halfHeight = maxHalfHeight * level

            val alpha = (0.30f + envelope * 0.70f).coerceIn(0f, 1f)
            val color = if (envelope > 0.52f) {
                lerpColor(barColor, accentColor, (envelope - 0.52f) / 0.48f)
            } else {
                barColor
            }

            if (halfHeight <= radius * 1.15f) {
                drawCircle(
                    color = color.copy(alpha = alpha * 0.65f),
                    radius = radius * 0.8f,
                    center = Offset(centerX, centerY)
                )
            } else {
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(centerX - radius, centerY - halfHeight),
                    size = Size(barWidth, halfHeight * 2f),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
        }
    }
}

private fun lerpColor(from: Color, to: Color, fraction: Float): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * t,
        green = from.green + (to.green - from.green) * t,
        blue = from.blue + (to.blue - from.blue) * t,
        alpha = from.alpha + (to.alpha - from.alpha) * t
    )
}
