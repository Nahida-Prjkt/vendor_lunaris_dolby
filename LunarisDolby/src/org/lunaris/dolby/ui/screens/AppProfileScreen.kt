/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import org.lunaris.dolby.data.AppInfo
import org.lunaris.dolby.domain.models.AppProfileUiState
import org.lunaris.dolby.ui.components.DolbyPageHeading
import org.lunaris.dolby.ui.components.DolbyTopAppBar
import org.lunaris.dolby.ui.components.ExpressiveEmptyState
import org.lunaris.dolby.ui.components.ExpressiveErrorState
import org.lunaris.dolby.ui.components.ExpressiveLoadingState
import org.lunaris.dolby.ui.components.ModernConfirmDialog
import org.lunaris.dolby.ui.components.expressivePress
import org.lunaris.dolby.ui.components.groupedItemShape
import org.lunaris.dolby.ui.components.profileIconFor
import org.lunaris.dolby.ui.viewmodel.AppProfileViewModel
import org.lunaris.dolby.utils.HapticFeedbackHelper
import org.lunaris.dolby.utils.rememberHapticFeedback

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppProfileScreen(
    viewModel: AppProfileViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val showTitle by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 8 }
    }
    var searchQuery by remember { mutableStateOf("") }
    var assignedOnly by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DolbyTopAppBar(
                title = stringResource(R.string.app_profiles_title),
                showTitle = showTitle || uiState !is AppProfileUiState.Success,
                scrolled = isScrolled,
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.padding(start = 8.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.dolby_back)
                        )
                    }
                },
                actions = {
                    if (uiState is AppProfileUiState.Success) {
                        val state = uiState as AppProfileUiState.Success
                        if (state.appsWithProfiles.isNotEmpty()) {
                            FilledTonalIconButton(
                                onClick = { showClearAllDialog = true },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ClearAll,
                                    contentDescription = stringResource(R.string.app_profiles_clear_all)
                                )
                            }
                        }
                    }
                    FilledTonalIconButton(
                        onClick = { viewModel.loadApps() },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.dolby_refresh)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        when (val state = uiState) {
            is AppProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingState(message = stringResource(R.string.app_profiles_loading))
                }
            }
            is AppProfileUiState.Success -> {
                val assignedCount = remember(state.apps) { state.apps.count { it.assignedProfile >= 0 } }
                val filteredApps = remember(state.apps, searchQuery, assignedOnly) {
                    state.apps.filter { app ->
                        val matchesFilter = !assignedOnly || app.assignedProfile >= 0
                        val matchesQuery = searchQuery.isBlank() ||
                            app.appName.contains(searchQuery, ignoreCase = true) ||
                            app.packageName.contains(searchQuery, ignoreCase = true)
                        matchesFilter && matchesQuery
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    item(key = "header") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DolbyPageHeading(
                                title = stringResource(R.string.app_profiles_title),
                                subtitle = stringResource(R.string.app_profiles_subtitle),
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = {
                                            Text(
                                                text = stringResource(R.string.app_profiles_search_placeholder),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            cursorColor = MaterialTheme.colorScheme.primary
                                        ),
                                        singleLine = true
                                    )
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Clear,
                                                contentDescription = stringResource(R.string.dolby_autoeq_clear),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !assignedOnly,
                                    onClick = { assignedOnly = false },
                                    label = {
                                        Text(
                                            text = stringResource(R.string.app_profiles_filter_all, state.apps.size),
                                            style = MaterialTheme.typography.labelLargeEmphasized
                                        )
                                    },
                                    leadingIcon = if (!assignedOnly) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else null,
                                    shape = CircleShape
                                )
                                FilterChip(
                                    selected = assignedOnly,
                                    onClick = { assignedOnly = true },
                                    label = {
                                        Text(
                                            text = stringResource(R.string.app_profiles_filter_assigned, assignedCount),
                                            style = MaterialTheme.typography.labelLargeEmphasized
                                        )
                                    },
                                    leadingIcon = if (assignedOnly) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else null,
                                    shape = CircleShape
                                )
                            }
                        }
                    }

                    if (filteredApps.isEmpty()) {
                        item(key = "empty") {
                            ExpressiveEmptyState(
                                icon = Icons.Rounded.SearchOff,
                                title = stringResource(R.string.app_profiles_no_apps_found),
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    } else {
                        itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { index, app ->
                            AppProfileItem(
                                app = app,
                                shape = groupedItemShape(index, filteredApps.size),
                                onProfileSelected = { profile ->
                                    viewModel.setAppProfile(app.packageName, profile)
                                }
                            )
                        }
                    }
                }
            }
            is AppProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadApps() }
                    )
                }
            }
        }
    }

    if (showClearAllDialog) {
        ModernConfirmDialog(
            title = stringResource(R.string.app_profiles_clear_all),
            message = stringResource(R.string.app_profiles_clear_all_message),
            icon = Icons.Rounded.ClearAll,
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllAppProfiles()
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false }
        )
    }
}

@Composable
private fun AppProfileItem(
    app: AppInfo,
    shape: Shape,
    onProfileSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val profiles = stringArrayResource(R.array.dolby_profile_entries)
    val profileValues = stringArrayResource(R.array.dolby_profile_values)
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val iconPx = with(density) { 44.dp.roundToPx() }
    val appBitmap: ImageBitmap? = remember(app.packageName, iconPx) {
        runCatching { app.icon?.toBitmap(iconPx, iconPx)?.asImageBitmap() }.getOrNull()
    }

    val isAssigned = app.assignedProfile >= 0
    val containerColor by animateColorAsState(
        targetValue = if (isAssigned) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "app_row_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isAssigned) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "app_row_fg"
    )

    val currentProfileName = if (isAssigned) {
        val index = profileValues.indexOfFirst { it.toIntOrNull() == app.assignedProfile }
        if (index >= 0) profiles[index] else stringResource(R.string.app_profiles_default)
    } else {
        stringResource(R.string.app_profiles_default)
    }

    val pillInteraction = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appBitmap != null) {
                Image(
                    bitmap = appBitmap,
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAssigned) {
                        contentColor.copy(alpha = 0.78f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box {
                Surface(
                    onClick = {
                        scope.launch { haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TICK) }
                        expanded = true
                    },
                    interactionSource = pillInteraction,
                    modifier = Modifier.expressivePress(pillInteraction, scaleDown = 0.94f),
                    shape = CircleShape,
                    color = if (isAssigned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor = if (isAssigned) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isAssigned) {
                            Icon(
                                imageVector = profileIconFor(app.assignedProfile),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = currentProfileName,
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = MaterialTheme.shapes.large
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.app_profiles_default),
                                style = if (app.assignedProfile == -1) {
                                    MaterialTheme.typography.bodyLargeEmphasized
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                },
                                color = if (app.assignedProfile == -1) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        trailingIcon = if (app.assignedProfile == -1) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null,
                        onClick = {
                            scope.launch { haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK) }
                            onProfileSelected(-1)
                            expanded = false
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    profiles.forEachIndexed { index, profileName ->
                        val profileValue = profileValues.getOrNull(index)?.toIntOrNull() ?: return@forEachIndexed
                        val selected = app.assignedProfile == profileValue
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = profileIconFor(profileValue),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            },
                            text = {
                                Text(
                                    text = profileName,
                                    style = if (selected) {
                                        MaterialTheme.typography.bodyLargeEmphasized
                                    } else {
                                        MaterialTheme.typography.bodyLarge
                                    },
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            },
                            trailingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null,
                            onClick = {
                                scope.launch { haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK) }
                                onProfileSelected(profileValue)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
