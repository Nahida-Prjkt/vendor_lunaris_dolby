/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import org.lunaris.dolby.utils.HapticFeedbackHelper
import org.lunaris.dolby.utils.rememberHapticFeedback

@Composable
fun ModernIeqSelector(
    currentPreset: Int,
    onPresetChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = stringArrayResource(R.array.dolby_ieq_entries)
    val values = stringArrayResource(R.array.dolby_ieq_values)
    val options = entries.mapIndexedNotNull { index, title ->
        values.getOrNull(index)?.toIntOrNull()?.let { it to title }
    }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val select: (Int) -> Unit = { value ->
        if (value != currentPreset) {
            scope.launch { haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK) }
            onPresetChange(value)
        }
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.dolby_ieq),
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val readableWidth = maxWidth / LocalDensity.current.fontScale
            val columns = when {
                readableWidth >= 560.dp -> 4
                readableWidth >= 240.dp -> 2
                else -> 1
            }
            Column(
                Modifier.fillMaxWidth().selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.chunked(columns).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        row.forEachIndexed { index, (value, title) ->
                            key(value) {
                                val interactionSource = remember { MutableInteractionSource() }
                                val selected = currentPreset == value
                                val shape = when {
                                    selected -> RoundedCornerShape(32.dp)
                                    columns == 1 -> RoundedCornerShape(
                                        topStart = if (value == options.first().first) 32.dp else 12.dp,
                                        topEnd = if (value == options.first().first) 32.dp else 12.dp,
                                        bottomStart = if (value == options.last().first) 32.dp else 12.dp,
                                        bottomEnd = if (value == options.last().first) 32.dp else 12.dp,
                                    )
                                    else -> RoundedCornerShape(
                                        topStart = if (index == 0) 32.dp else 12.dp,
                                        bottomStart = if (index == 0) 32.dp else 12.dp,
                                        topEnd = if (index == row.lastIndex) 32.dp else 12.dp,
                                        bottomEnd = if (index == row.lastIndex) 32.dp else 12.dp,
                                    )
                                }
                                IeqOption(
                                    title = title,
                                    icon = when (value) {
                                        0 -> Icons.Rounded.PowerOff
                                        1 -> Icons.Rounded.GraphicEq
                                        2 -> Icons.Rounded.Balance
                                        3 -> Icons.Rounded.Whatshot
                                        else -> Icons.Rounded.GraphicEq
                                    },
                                    selected = selected,
                                    shape = shape,
                                    interactionSource = interactionSource,
                                    onClick = { select(value) },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                )
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        Text(
            text = stringResource(when (currentPreset) {
                1 -> R.string.dolby_ieq_detailed_summary
                2 -> R.string.dolby_ieq_balanced_summary
                3 -> R.string.dolby_ieq_warm_summary
                else -> R.string.dolby_ieq_off_summary
            }),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IeqOption(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    shape: RoundedCornerShape,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ieq_option_press",
    )
    val container by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "ieq_option_container",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "ieq_option_content",
    )
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { role = Role.RadioButton },
        shape = shape,
        color = container,
        contentColor = content,
        interactionSource = interactionSource,
    ) {
        Column(
            Modifier.heightIn(min = 120.dp).padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (selected) Icons.Rounded.Check else icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = title,
                style = if (selected) MaterialTheme.typography.titleMediumEmphasized
                    else MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
