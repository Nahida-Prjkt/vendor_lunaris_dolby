/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

    ModernSettingsCard(
        title = stringResource(R.string.dolby_ieq),
        icon = Icons.Rounded.AutoGraph,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val readableWidth = maxWidth / LocalDensity.current.fontScale
                val columns = when {
                    readableWidth >= 560.dp -> 4
                    readableWidth >= 240.dp -> 2
                    else -> 1
                }
                val rows = options.chunked(columns)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    rows.forEachIndexed { rowIndex, row ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            row.forEachIndexed { colIndex, (value, title) ->
                                key(value) {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val selected = currentPreset == value
                                    val isTopRow = rowIndex == 0
                                    val isBottomRow = rowIndex == rows.lastIndex
                                    val isStartCol = colIndex == 0
                                    val isEndCol = colIndex == row.lastIndex

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
                                        topStartTarget = if (selected || (isTopRow && isStartCol)) 26.dp else 10.dp,
                                        topEndTarget = if (selected || (isTopRow && isEndCol)) 26.dp else 10.dp,
                                        bottomStartTarget = if (selected || (isBottomRow && isStartCol)) 26.dp else 10.dp,
                                        bottomEndTarget = if (selected || (isBottomRow && isEndCol)) 26.dp else 10.dp,
                                        interactionSource = interactionSource,
                                        onClick = { select(value) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                    )
                                }
                            }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                val motionScheme = MaterialTheme.motionScheme
                AnimatedContent(
                    targetState = currentPreset,
                    transitionSpec = {
                        fadeIn(motionScheme.fastEffectsSpec()) togetherWith
                            fadeOut(motionScheme.fastEffectsSpec())
                    },
                    label = "ieq_summary_transition"
                ) { preset ->
                    Text(
                        text = stringResource(
                            when (preset) {
                                1 -> R.string.dolby_ieq_detailed_summary
                                2 -> R.string.dolby_ieq_balanced_summary
                                3 -> R.string.dolby_ieq_warm_summary
                                else -> R.string.dolby_ieq_off_summary
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IeqOption(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    topStartTarget: Dp,
    topEndTarget: Dp,
    bottomStartTarget: Dp,
    bottomEndTarget: Dp,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ieq_option_press",
    )
    val topStart by animateDpAsState(
        targetValue = if (pressed) 14.dp else topStartTarget,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ieq_ts"
    )
    val topEnd by animateDpAsState(
        targetValue = if (pressed) 14.dp else topEndTarget,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ieq_te"
    )
    val bottomStart by animateDpAsState(
        targetValue = if (pressed) 14.dp else bottomStartTarget,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ieq_bs"
    )
    val bottomEnd by animateDpAsState(
        targetValue = if (pressed) 14.dp else bottomEndTarget,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ieq_be"
    )
    val container by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else colors.surfaceContainerHigh,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "ieq_option_container",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimaryContainer else colors.onSurface,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "ieq_option_content",
    )
    val badgeBg by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surfaceContainerHighest,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "ieq_badge_bg"
    )
    val badgeFg by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "ieq_badge_fg"
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { role = Role.RadioButton },
        shape = RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        ),
        color = container,
        contentColor = content,
        interactionSource = interactionSource,
    ) {
        Column(
            Modifier
                .heightIn(min = 108.dp)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = badgeBg,
                contentColor = badgeFg,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val motionScheme = MaterialTheme.motionScheme
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = {
                            (scaleIn(motionScheme.fastSpatialSpec()) +
                                fadeIn(motionScheme.fastEffectsSpec()))
                                .togetherWith(
                                    scaleOut(motionScheme.fastSpatialSpec()) +
                                        fadeOut(motionScheme.fastEffectsSpec())
                                )
                        },
                        label = "ieq_icon_swap"
                    ) { isSelected ->
                        Icon(
                            imageVector = if (isSelected) Icons.Rounded.Check else icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            Text(
                text = title,
                style = if (selected) MaterialTheme.typography.titleSmallEmphasized
                else MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
