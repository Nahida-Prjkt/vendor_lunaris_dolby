/*
 * Copyright (C) 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package org.lunaris.dolby.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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

    ModernSettingsCard(
        title = stringResource(R.string.dolby_profile_title),
        icon = Icons.Rounded.Headphones,
        modifier = modifier,
    ) {
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            profiles.forEachIndexed { index, profile ->
                val value = values.getOrNull(index)?.toIntOrNull() ?: return@forEachIndexed
                val selected = currentProfile == value
                val icon = when (value) {
                    0 -> Icons.Rounded.AutoAwesome
                    1 -> Icons.Rounded.Movie
                    2 -> Icons.Rounded.MusicNote
                    3 -> Icons.Rounded.SportsEsports
                    4 -> Icons.Rounded.Work
                    5 -> Icons.Rounded.Coffee
                    else -> Icons.Rounded.Favorite
                }
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            scope.launch {
                                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                            }
                            onProfileChange(value)
                        }
                    },
                    label = {
                        Text(
                            profile,
                            style = if (selected) MaterialTheme.typography.titleSmallEmphasized
                                else MaterialTheme.typography.titleSmall,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (selected) Icons.Rounded.Check else icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    modifier = Modifier.heightIn(min = 48.dp).semantics { role = Role.RadioButton },
                    shape = RoundedCornerShape(if (selected) 24.dp else 16.dp),
                    border = null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}
