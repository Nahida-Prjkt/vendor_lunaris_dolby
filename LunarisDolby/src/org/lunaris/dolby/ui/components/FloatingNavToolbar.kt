/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
    val navigate: (String) -> Unit = { route ->
        if (route != currentRoute) {
            scope.launch { haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK) }
            onNavigate(route)
        }
    }

    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .widthIn(max = 440.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(6.dp).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavToolbarItem(
                icon = Icons.Rounded.Home,
                label = stringResource(R.string.home),
                selected = currentRoute == "settings",
                onClick = { navigate("settings") },
                modifier = Modifier.weight(1f),
            )
            NavToolbarItem(
                icon = Icons.Rounded.GraphicEq,
                label = stringResource(R.string.equalizer),
                selected = currentRoute == "equalizer",
                onClick = { navigate("equalizer") },
                modifier = Modifier.weight(1f),
            )
            NavToolbarItem(
                icon = Icons.Rounded.Tune,
                label = stringResource(R.string.advanced),
                selected = currentRoute == "advanced",
                onClick = { navigate("advanced") },
                modifier = Modifier.weight(1f),
            )
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
    val containerColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "navigation_container",
    )
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp).semantics { role = Role.Tab },
        shape = RoundedCornerShape(26.dp),
        color = containerColor,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Text(
                label,
                style = if (selected) MaterialTheme.typography.labelMediumEmphasized
                    else MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
