/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
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
    val fontScale = LocalDensity.current.fontScale
    val width = 360.dp * fontScale.coerceIn(1f, 1.3f)

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        shape = CircleShape,
        contentPadding = PaddingValues(6.dp),
        expandedShadowElevation = 6.dp,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            toolbarContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        BoxWithConstraints(Modifier.widthIn(max = width).width(width)) {
            val itemWidth = maxWidth / routes.size
            val compact = maxWidth / fontScale < 320.dp
            val offset by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                label = "navigation_selection",
            )

            Box {
                Box(Modifier.matchParentSize()) {
                    Box(
                        Modifier.offset(x = offset)
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    )
                }
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).selectableGroup()) {
                    routes.forEachIndexed { index, route ->
                        NavToolbarItem(
                            icon = when (route) {
                                "equalizer" -> Icons.Rounded.GraphicEq
                                "advanced" -> Icons.Rounded.Tune
                                else -> Icons.Rounded.Home
                            },
                            label = stringResource(when (route) {
                                "equalizer" -> R.string.equalizer
                                "advanced" -> R.string.advanced
                                else -> R.string.home
                            }),
                            selected = selectedIndex == index,
                            compact = compact,
                            onClick = {
                                if (route != currentRoute) {
                                    scope.launch {
                                        haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                                    }
                                    onNavigate(route)
                                }
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavToolbarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSecondaryContainer,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "navigation_content",
    )
    val itemModifier = modifier
        .heightIn(min = 48.dp)
        .clip(CircleShape)
        .selectable(selected = selected, role = Role.Tab, onClick = onClick)
        .squishable(scaleDown = 0.95f)
        .padding(horizontal = 8.dp, vertical = 10.dp)
    val content: @Composable () -> Unit = {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = color)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
    if (compact) {
        Column(
            modifier = itemModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) { content() }
    } else {
        Row(
            modifier = itemModifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
}
