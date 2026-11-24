/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import org.lunaris.dolby.utils.HapticFeedbackHelper
import org.lunaris.dolby.utils.rememberHapticFeedback

@Composable
fun FloatingNavToolbar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val routes = listOf("settings", "equalizer", "advanced")
    val selectedIndex = routes.indexOf(currentRoute).coerceAtLeast(0)

    Surface(
        modifier = modifier
            .widthIn(max = 440.dp)
            .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
        shape = RoundedCornerShape(34.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 6.dp,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            routes.forEachIndexed { index, route ->
                val selected = selectedIndex == index
                NavToolbarItem(
                    icon = when (route) {
                        "equalizer" -> Icons.Rounded.GraphicEq
                        "advanced" -> Icons.Rounded.Tune
                        else -> Icons.Rounded.Home
                    },
                    label = stringResource(
                        when (route) {
                            "equalizer" -> R.string.equalizer
                            "advanced" -> R.string.advanced
                            else -> R.string.home
                        }
                    ),
                    selected = selected,
                    onClick = {
                        if (route != currentRoute) {
                            scope.launch {
                                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                            }
                            onNavigate(route)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun NavToolbarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "navigation_press",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navigation_icon_scale",
    )
    val radius by animateDpAsState(
        targetValue = when {
            pressed -> 16.dp
            selected -> 28.dp
            else -> 24.dp
        },
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "navigation_shape",
    )
    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "navigation_color",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "navigation_content_color",
    )

    Row(
        modifier = modifier
            .sizeIn(minWidth = 54.dp, minHeight = 52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(radius))
            .background(background)
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { contentDescription = label }
            .padding(horizontal = if (selected) 18.dp else 15.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
                    expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                    shrinkHorizontally(MaterialTheme.motionScheme.fastSpatialSpec()),
            ) {
                Text(
                    text = label,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clearAndSetSemantics {},
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                )
            }
        }
    }
}
