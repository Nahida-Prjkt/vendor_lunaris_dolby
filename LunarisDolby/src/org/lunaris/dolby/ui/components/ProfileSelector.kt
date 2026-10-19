/*
 * Copyright (C) 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import org.lunaris.dolby.utils.HapticFeedbackHelper
import org.lunaris.dolby.utils.rememberHapticFeedback

@Composable
fun ProfileSelector(
    currentProfile: Int,
    onProfileChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profiles = stringArrayResource(R.array.dolby_profile_entries)
    val values = stringArrayResource(R.array.dolby_profile_values)
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val options = profiles.mapIndexedNotNull { index, name ->
        values.getOrNull(index)?.toIntOrNull()?.let { it to name }
    }
    val selectProfile: (Int) -> Unit = { value ->
        if (value != currentProfile) {
            scope.launch {
                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
            }
            onProfileChange(value)
        }
    }

    ModernSettingsCard(
        title = stringResource(R.string.dolby_profile_title),
        icon = Icons.Rounded.Headphones,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            options.firstOrNull { it.first == 0 }?.let { (value, name) ->
                ProfileTile(
                    title = name,
                    icon = Icons.Rounded.AutoAwesome,
                    selected = currentProfile == value,
                    onClick = { selectProfile(value) },
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = stringResource(R.string.dolby_profile_dynamic_summary),
                )
            }

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val readableWidth = maxWidth / LocalDensity.current.fontScale
                val columns = when {
                    readableWidth >= 420.dp -> 3
                    readableWidth >= 240.dp -> 2
                    else -> 1
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.filter { it.first != 0 }.chunked(columns).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { (value, name) ->
                                ProfileTile(
                                    title = name,
                                    icon = when (value) {
                                        1 -> Icons.Rounded.Movie
                                        2 -> Icons.Rounded.MusicNote
                                        3 -> Icons.Rounded.SportsEsports
                                        4 -> Icons.Rounded.Work
                                        5 -> Icons.Rounded.Coffee
                                        else -> Icons.Rounded.Favorite
                                    },
                                    selected = currentProfile == value,
                                    onClick = { selectProfile(value) },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                )
                            }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTile(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "profile_press",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surfaceContainerHigh,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "profile_container",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurface,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "profile_content",
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
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
    ) {
        if (subtitle != null) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProfileIcon(icon, selected)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleMediumEmphasized)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) contentColor else colors.onSurfaceVariant,
                    )
                }
                ProfileSelectionMark(selected)
            }
        } else {
            Column(
                modifier = Modifier.heightIn(min = 104.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileIcon(icon, selected)
                    ProfileSelectionMark(selected)
                }
                Text(title, style = MaterialTheme.typography.titleSmallEmphasized)
            }
        }
    }
}

@Composable
private fun ProfileIcon(icon: ImageVector, selected: Boolean) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
            else MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun ProfileSelectionMark(selected: Boolean) {
    Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
        if (selected) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(3.dp),
                )
            }
        }
    }
}
