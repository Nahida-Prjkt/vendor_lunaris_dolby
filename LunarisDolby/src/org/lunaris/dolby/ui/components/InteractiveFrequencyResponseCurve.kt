/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.lunaris.dolby.domain.models.BandGain
import org.lunaris.dolby.utils.HapticFeedbackHelper
import org.lunaris.dolby.utils.rememberHapticFeedback
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun InteractiveFrequencyResponseCurve(
    bandGains: List<BandGain>,
    onBandGainChange: (index: Int, newGain: Int) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isEditable: Boolean = true
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest

    val plotInkColor by animateColorAsState(
        targetValue = if (isActive) onPrimaryContainerColor else onSurfaceVariantColor,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "eq_ink_color"
    )
    val curveColor by animateColorAsState(
        targetValue = if (isActive) primaryColor else secondaryColor,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "eq_curve_color"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) {
            primaryContainerColor.copy(alpha = 0.62f)
        } else {
            surfaceContainerHighest
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "eq_bg_color"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> primaryColor.copy(alpha = 0.65f)
            !isEditable -> errorColor.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "eq_border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isActive) 1.75.dp else 1.dp,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "eq_border_width"
    )

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var lastHapticDb by remember { mutableIntStateOf(0) }
    var dragPoints by remember(bandGains.size) {
        mutableStateOf(bandGains.map { it.gain.toFloat() })
    }
    var fromPoints by remember(bandGains.size) {
        mutableStateOf(bandGains.map { it.gain.toFloat() })
    }
    var targetPoints by remember(bandGains.size) {
        mutableStateOf(bandGains.map { it.gain.toFloat() })
    }
    val transitionProgress = remember(bandGains.size) { Animatable(1f) }
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    LaunchedEffect(bandGains) {
        if (draggedIndex == null) {
            val newTargets = bandGains.map { it.gain.toFloat() }
            if (fromPoints.size == newTargets.size && targetPoints.size == newTargets.size) {
                val t = transitionProgress.value
                val current = List(newTargets.size) { idx ->
                    fromPoints[idx] + (targetPoints[idx] - fromPoints[idx]) * t
                }
                fromPoints = current
                targetPoints = newTargets
                transitionProgress.snapTo(0f)
                transitionProgress.animateTo(1f, animationSpec = spatialSpec)
                fromPoints = newTargets
                dragPoints = newTargets
            } else {
                fromPoints = newTargets
                targetPoints = newTargets
                dragPoints = newTargets
                transitionProgress.snapTo(1f)
            }
        }
    }

    val renderedPoints = if (draggedIndex != null) {
        dragPoints
    } else if (fromPoints.size == targetPoints.size) {
        val t = transitionProgress.value
        List(targetPoints.size) { idx ->
            fromPoints[idx] + (targetPoints[idx] - fromPoints[idx]) * t
        }
    } else {
        bandGains.map { it.gain.toFloat() }
    }

    val shape = MaterialTheme.shapes.large

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(backgroundColor)
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = shape
                )
                .pointerInput(isEditable, bandGains.size) {
                    if (isEditable && bandGains.size > 1) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val hPad = 24.dp.toPx()
                                val vPadTop = 28.dp.toPx()
                                val vPadBottom = 30.dp.toPx()
                                val plotW = (size.width - hPad * 2f).coerceAtLeast(1f)
                                val plotH = (size.height - vPadTop - vPadBottom).coerceAtLeast(1f)
                                val centerY = vPadTop + plotH / 2f
                                val halfPlotH = plotH / 2f
                                val stepX = plotW / (bandGains.size - 1).toFloat()

                                val startSnapshot = if (fromPoints.size == targetPoints.size && targetPoints.size == bandGains.size) {
                                    val t = transitionProgress.value
                                    List(bandGains.size) { idx ->
                                        fromPoints[idx] + (targetPoints[idx] - fromPoints[idx]) * t
                                    }
                                } else {
                                    bandGains.map { it.gain.toFloat() }
                                }

                                var closestIndex = -1
                                var closestDistance = Float.MAX_VALUE

                                bandGains.forEachIndexed { index, _ ->
                                    val x = hPad + index * stepX
                                    val gain = startSnapshot.getOrElse(index) { 0f }
                                    val normalizedGain = (gain / 150f).coerceIn(-1f, 1f)
                                    val y = centerY - (normalizedGain * halfPlotH)

                                    val dx = offset.x - x
                                    val dy = offset.y - y
                                    val distance = sqrt(dx * dx + dy * dy)

                                    if (distance < closestDistance && distance < 128f) {
                                        closestDistance = distance
                                        closestIndex = index
                                    }
                                }

                                if (closestIndex != -1) {
                                    dragPoints = startSnapshot
                                    draggedIndex = closestIndex
                                    lastHapticDb = (startSnapshot.getOrElse(closestIndex) { 0f } / 10f).roundToInt()
                                    scope.launch {
                                        haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TICK)
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                draggedIndex?.let { index ->
                                    val vPadTop = 28.dp.toPx()
                                    val vPadBottom = 30.dp.toPx()
                                    val plotH = (size.height - vPadTop - vPadBottom).coerceAtLeast(1f)
                                    val centerY = vPadTop + plotH / 2f
                                    val halfPlotH = plotH / 2f
                                    val y = change.position.y
                                    val normalizedGain = ((centerY - y) / halfPlotH).coerceIn(-1f, 1f)
                                    val rawGain = (normalizedGain * 150f).roundToInt().coerceIn(-150, 150)
                                    val snappedGain = if (abs(rawGain) <= 3) 0 else rawGain

                                    val currentDb = (snappedGain / 10f).roundToInt()
                                    if (currentDb != lastHapticDb) {
                                        val intensity = if (snappedGain == 0) {
                                            HapticFeedbackHelper.HapticIntensity.CLICK
                                        } else {
                                            HapticFeedbackHelper.HapticIntensity.TEXTURE_TICK
                                        }
                                        scope.launch { haptic.performHaptic(intensity) }
                                        lastHapticDb = currentDb
                                    }

                                    if (index < dragPoints.size && dragPoints[index].roundToInt() != snappedGain) {
                                        dragPoints = dragPoints.toMutableList().apply {
                                            this[index] = snappedGain.toFloat()
                                        }
                                    }
                                    change.consume()
                                }
                            },
                            onDragEnd = {
                                draggedIndex?.let { index ->
                                    if (index < dragPoints.size) {
                                        fromPoints = dragPoints
                                        targetPoints = dragPoints
                                        onBandGainChange(index, dragPoints[index].roundToInt())
                                    }
                                }
                                draggedIndex = null
                            },
                            onDragCancel = {
                                draggedIndex = null
                            }
                        )
                    }
                }
        ) {
            val hPad = 24.dp.toPx()
            val vPadTop = 28.dp.toPx()
            val vPadBottom = 30.dp.toPx()
            val plotW = (size.width - hPad * 2f).coerceAtLeast(1f)
            val plotH = (size.height - vPadTop - vPadBottom).coerceAtLeast(1f)
            val centerY = vPadTop + plotH / 2f
            val halfPlotH = plotH / 2f

            val gridColor = plotInkColor.copy(alpha = 0.14f)
            val gridVerticalColor = plotInkColor.copy(alpha = 0.10f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

            for (step in listOf(-1f, -0.5f, 0.5f, 1f)) {
                val y = centerY - step * halfPlotH
                drawLine(
                    color = gridColor,
                    start = Offset(hPad, y),
                    end = Offset(size.width - hPad, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawLine(
                color = plotInkColor.copy(alpha = 0.42f),
                start = Offset(hPad, centerY),
                end = Offset(size.width - hPad, centerY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )

            if (bandGains.size > 1 && renderedPoints.size == bandGains.size) {
                val stepX = plotW / (bandGains.size - 1).toFloat()

                bandGains.forEachIndexed { index, _ ->
                    val x = hPad + index * stepX
                    drawLine(
                        color = gridVerticalColor,
                        start = Offset(x, vPadTop),
                        end = Offset(x, vPadTop + plotH),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val path = Path()
                renderedPoints.forEachIndexed { index, gain ->
                    val x = hPad + index * stepX
                    val normalizedGain = (gain / 150f).coerceIn(-1f, 1f)
                    val y = centerY - (normalizedGain * halfPlotH)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = hPad + (index - 1) * stepX
                        val prevGain = renderedPoints[index - 1]
                        val prevNormalizedGain = (prevGain / 150f).coerceIn(-1f, 1f)
                        val prevY = centerY - (prevNormalizedGain * halfPlotH)

                        val cpX1 = prevX + stepX * 0.42f
                        val cpX2 = x - stepX * 0.42f
                        path.cubicTo(cpX1, prevY, cpX2, y, x, y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(hPad + plotW, vPadTop + plotH)
                    lineTo(hPad, vPadTop + plotH)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            curveColor.copy(alpha = if (isActive) 0.36f else 0.24f),
                            curveColor.copy(alpha = 0.02f)
                        ),
                        startY = vPadTop,
                        endY = vPadTop + plotH
                    )
                )

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(curveColor, tertiaryColor.takeIf { isActive } ?: curveColor, curveColor)
                    ),
                    style = Stroke(
                        width = if (isActive) 3.5.dp.toPx() else 2.75.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                renderedPoints.forEachIndexed { index, gain ->
                    val x = hPad + index * stepX
                    val normalizedGain = (gain / 150f).coerceIn(-1f, 1f)
                    val y = centerY - (normalizedGain * halfPlotH)

                    val isBeingDragged = draggedIndex == index
                    val baseRadius = if (bandGains.size >= 15) 4.5.dp.toPx() else 5.5.dp.toPx()
                    val pointRadius = if (isBeingDragged) 8.dp.toPx() else baseRadius

                    if (isBeingDragged) {
                        drawLine(
                            color = curveColor.copy(alpha = 0.55f),
                            start = Offset(x, centerY),
                            end = Offset(x, y),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawCircle(
                            color = curveColor.copy(alpha = 0.24f),
                            radius = 18.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawCircle(
                        color = backgroundColor,
                        radius = pointRadius + 2.dp.toPx(),
                        center = Offset(x, y)
                    )

                    drawCircle(
                        color = if (isBeingDragged) primaryColor else curveColor,
                        radius = pointRadius,
                        center = Offset(x, y)
                    )

                    if (isBeingDragged) {
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        val labelStride = when {
            bandGains.size >= 18 -> 3
            bandGains.size >= 13 -> 2
            else -> 1
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp, start = 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bandGains.forEachIndexed { index, bandGain ->
                val showLabel = index == 0 || index == bandGains.lastIndex || index % labelStride == 0
                if (showLabel) {
                    Text(
                        text = if (bandGain.frequency >= 1000) {
                            "${bandGain.frequency / 1000}k"
                        } else {
                            "${bandGain.frequency}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = plotInkColor.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp, top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "+15",
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = plotInkColor.copy(alpha = 0.72f),
                fontSize = 9.sp,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Text(
                text = "0",
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = plotInkColor.copy(alpha = 0.85f),
                fontSize = 9.sp,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Text(
                text = "-15",
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = plotInkColor.copy(alpha = 0.72f),
                fontSize = 9.sp,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        AnimatedVisibility(
            visible = draggedIndex != null,
            enter = scaleIn(MaterialTheme.motionScheme.fastSpatialSpec()) +
                fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
            exit = scaleOut(MaterialTheme.motionScheme.fastSpatialSpec()) +
                fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            val activeIdx = draggedIndex ?: 0
            if (activeIdx in bandGains.indices && activeIdx < renderedPoints.size) {
                val freq = bandGains[activeIdx].frequency
                val freqLabel = if (freq >= 1000) "%.1f kHz".format(freq / 1000f) else "$freq Hz"
                val gainDb = renderedPoints[activeIdx] / 10f
                val sign = if (gainDb > 0.05f) "+" else ""

                Surface(
                    shape = CircleShape,
                    color = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "$freqLabel · $sign%.1f dB".format(gainDb + 0f),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactFrequencyResponseCurve(
    bandGains: List<BandGain>,
    modifier: Modifier = Modifier,
    curveColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    showGrid: Boolean = true
) {
    val plotInkColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
    ) {
        val hPad = 8.dp.toPx()
        val vPad = 8.dp.toPx()
        val plotW = (size.width - hPad * 2f).coerceAtLeast(1f)
        val plotH = (size.height - vPad * 2f).coerceAtLeast(1f)
        val centerY = vPad + plotH / 2f
        val halfPlotH = plotH / 2f

        if (showGrid) {
            drawLine(
                color = plotInkColor.copy(alpha = 0.35f),
                start = Offset(hPad, centerY),
                end = Offset(size.width - hPad, centerY),
                strokeWidth = 1.dp.toPx()
            )
            for (step in listOf(-0.5f, 0.5f)) {
                val y = centerY - step * halfPlotH
                drawLine(
                    color = plotInkColor.copy(alpha = 0.14f),
                    start = Offset(hPad, y),
                    end = Offset(size.width - hPad, y),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
        }

        if (bandGains.size > 1) {
            val path = Path()
            val stepX = plotW / (bandGains.size - 1).toFloat()

            bandGains.forEachIndexed { index, bandGain ->
                val x = hPad + index * stepX
                val normalizedGain = (bandGain.gain / 150f).coerceIn(-1f, 1f)
                val y = centerY - (normalizedGain * halfPlotH * 0.9f)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = hPad + (index - 1) * stepX
                    val prevNormalized = (bandGains[index - 1].gain / 150f).coerceIn(-1f, 1f)
                    val prevY = centerY - (prevNormalized * halfPlotH * 0.9f)
                    val cpX1 = prevX + stepX * 0.42f
                    val cpX2 = x - stepX * 0.42f
                    path.cubicTo(cpX1, prevY, cpX2, y, x, y)
                }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(hPad + plotW, size.height)
                lineTo(hPad, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        curveColor.copy(alpha = 0.28f),
                        curveColor.copy(alpha = 0.02f)
                    )
                )
            )

            drawPath(
                path = path,
                color = curveColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
