/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import org.lunaris.dolby.domain.models.BandGain
import org.lunaris.dolby.domain.models.BandMode
import org.lunaris.dolby.domain.models.EqualizerPreset
import org.lunaris.dolby.domain.models.EqualizerUiState
import org.lunaris.dolby.ui.components.CompactFrequencyResponseCurve
import org.lunaris.dolby.ui.components.DolbyPageHeading
import org.lunaris.dolby.ui.components.DolbyTopAppBar
import org.lunaris.dolby.ui.components.ExpressiveEmptyState
import org.lunaris.dolby.ui.components.ExpressiveErrorState
import org.lunaris.dolby.ui.components.ExpressiveLoadingState
import org.lunaris.dolby.ui.components.InteractiveFrequencyResponseCurve
import org.lunaris.dolby.ui.components.ModernConfirmDialog
import org.lunaris.dolby.ui.components.ModernSettingsCard
import org.lunaris.dolby.ui.components.expressivePress
import org.lunaris.dolby.ui.components.groupedItemShape
import org.lunaris.dolby.ui.viewmodel.EqualizerViewModel
import org.lunaris.dolby.utils.HapticFeedbackHelper
import org.lunaris.dolby.utils.rememberHapticFeedback
import kotlin.math.abs

enum class EqualizerViewMode {
    CURVE,
    SLIDERS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModernEqualizerScreen(
    viewModel: EqualizerViewModel,
    navController: NavController,
    bottomPadding: Dp = 0.dp,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val showTitle by remember { derivedStateOf { scrollState.value > 72 } }
    val isScrolled by remember { derivedStateOf { scrollState.value > 8 } }
    var showActions by remember { mutableStateOf(false) }
    var showAutoEqDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(EqualizerViewMode.CURVE) }

    Scaffold(
        topBar = {
            DolbyTopAppBar(
                title = stringResource(R.string.dolby_preset),
                showTitle = showTitle || uiState !is EqualizerUiState.Success,
                scrolled = isScrolled,
                actions = {
                    FilledTonalIconButton(
                        onClick = { showSaveDialog = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = stringResource(R.string.dolby_save_preset)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { showAutoEqDialog = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = stringResource(R.string.dolby_autoeq_title)
                        )
                    }
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        FilledTonalIconButton(
                            onClick = { showActions = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.dolby_more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false },
                            shape = MaterialTheme.shapes.large
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.preset_import_export)) },
                                leadingIcon = { Icon(Icons.Rounded.ImportExport, null) },
                                onClick = {
                                    showActions = false
                                    navController.navigate(Screen.ImportExport.route)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dolby_geq_reset_gains)) },
                                leadingIcon = { Icon(Icons.Rounded.RestartAlt, null) },
                                onClick = {
                                    showActions = false
                                    showResetDialog = true
                                },
                            )
                            if ((uiState as? EqualizerUiState.Success)?.currentPreset?.isUserDefined == true) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.dolby_geq_delete_preset),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showActions = false
                                        showDeleteDialog = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is EqualizerUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveLoadingState(message = stringResource(R.string.loading))
                    }
                }
                is EqualizerUiState.Success -> {
                    ModernEqualizerContent(
                        state = state,
                        scrollState = scrollState,
                        viewModel = viewModel,
                        viewMode = viewMode,
                        onViewModeChange = { viewMode = it },
                        bottomPadding = bottomPadding,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                is EqualizerUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadEqualizer() }
                        )
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SavePresetDialog(
            onSave = { name ->
                val error = viewModel.savePreset(name)
                if (error == null) {
                    showSaveDialog = false
                }
                error
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    if (showDeleteDialog && uiState is EqualizerUiState.Success) {
        val state = uiState as EqualizerUiState.Success
        ModernConfirmDialog(
            title = stringResource(R.string.dolby_geq_delete_preset),
            message = stringResource(R.string.dolby_geq_delete_preset_prompt),
            icon = Icons.Rounded.Delete,
            isDestructive = true,
            onConfirm = {
                viewModel.deletePreset(state.currentPreset)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showResetDialog) {
        ModernConfirmDialog(
            title = stringResource(R.string.dolby_geq_reset_gains),
            message = stringResource(R.string.dolby_geq_reset_gains_prompt),
            icon = Icons.Rounded.RestartAlt,
            isDestructive = true,
            onConfirm = {
                viewModel.resetGains()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showAutoEqDialog) {
        AutoEqSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showAutoEqDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModernEqualizerContent(
    state: EqualizerUiState.Success,
    scrollState: ScrollState,
    viewModel: EqualizerViewModel,
    viewMode: EqualizerViewMode,
    onViewModeChange: (EqualizerViewMode) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val isFlatPreset = state.currentPreset.name == stringResource(R.string.dolby_preset_default)
    val isBandModeCompatible = state.currentPreset.bandMode == state.bandMode
    val canEdit = isBandModeCompatible || isFlatPreset
    val isActive = canEdit && !isFlatPreset

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 16.dp + bottomPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        DolbyPageHeading(
            title = stringResource(R.string.dolby_preset),
            subtitle = stringResource(R.string.dolby_equalizer_summary),
            badgeText = "${state.currentPreset.name} · ${state.bandMode.displayName}",
            badgeIcon = Icons.Rounded.GraphicEq,
        )

        ModernSettingsCard(
            title = stringResource(R.string.dolby_geq_preset),
            icon = Icons.Rounded.LibraryMusic,
        ) {
            ModernPresetSelector(
                presets = state.presets,
                currentPreset = state.currentPreset,
                onPresetSelected = { viewModel.setPreset(it) }
            )
        }

        AnimatedVisibility(
            visible = !isBandModeCompatible && !isFlatPreset,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.band_mode_mismatch),
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "This ${state.currentPreset.bandMode.displayName} preset cannot be edited in ${state.bandMode.displayName} mode.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.setBandMode(state.currentPreset.bandMode) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                R.string.dolby_eq_switch_to_mode,
                                state.currentPreset.bandMode.displayName
                            ),
                            style = MaterialTheme.typography.labelLargeEmphasized
                        )
                    }
                }
            }
        }

        EqualizerViewModeSwitcher(
            viewMode = viewMode,
            onViewModeChange = onViewModeChange
        )

        val viewTransitionSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                fadeIn(animationSpec = viewTransitionSpec) togetherWith
                    fadeOut(animationSpec = viewTransitionSpec)
            },
            label = "equalizer_view_transition"
        ) { mode ->
            when (mode) {
                EqualizerViewMode.CURVE -> {
                    CurveViewContent(
                        state = state,
                        viewModel = viewModel,
                        canEdit = canEdit,
                        isActive = isActive
                    )
                }
                EqualizerViewMode.SLIDERS -> {
                    SlidersViewContent(
                        state = state,
                        viewModel = viewModel,
                        canEdit = canEdit
                    )
                }
            }
        }

        BandModeSelector(
            currentMode = state.bandMode,
            onModeChange = { viewModel.setBandMode(it) }
        )

        BandTunerCard(
            bandGains = state.bandGains,
            bandMode = state.bandMode,
            onGainChange = { index, gain ->
                if (canEdit) {
                    viewModel.setBandGain(index, gain)
                }
            },
            enabled = canEdit
        )
    }
}

@Composable
private fun EqualizerViewModeSwitcher(
    viewMode: EqualizerViewMode,
    onViewModeChange: (EqualizerViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ViewModePill(
                title = stringResource(R.string.dolby_eq_view_curve),
                icon = Icons.Rounded.ShowChart,
                isSelected = viewMode == EqualizerViewMode.CURVE,
                onClick = {
                    if (viewMode != EqualizerViewMode.CURVE) {
                        scope.launch { haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK) }
                        onViewModeChange(EqualizerViewMode.CURVE)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            ViewModePill(
                title = stringResource(R.string.dolby_eq_view_sliders),
                icon = Icons.Rounded.Tune,
                isSelected = viewMode == EqualizerViewMode.SLIDERS,
                onClick = {
                    if (viewMode != EqualizerViewMode.SLIDERS) {
                        scope.launch { haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK) }
                        onViewModeChange(EqualizerViewMode.SLIDERS)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ViewModePill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "view_pill_bg"
    )
    val fgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "view_pill_fg"
    )

    Surface(
        selected = isSelected,
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(48.dp)
            .expressivePress(interactionSource, scaleDown = 0.96f)
            .semantics { role = Role.Tab },
        shape = CircleShape,
        color = bgColor,
        contentColor = fgColor
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = if (isSelected) MaterialTheme.typography.titleSmallEmphasized
                else MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun CurveViewContent(
    state: EqualizerUiState.Success,
    viewModel: EqualizerViewModel,
    canEdit: Boolean,
    isActive: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (canEdit) {
                        stringResource(R.string.frequency_response)
                    } else {
                        stringResource(R.string.dolby_eq_readonly_title)
                    },
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = if (canEdit) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Surface(
                    shape = CircleShape,
                    color = if (canEdit) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = getFrequencyRange(state.bandGains),
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = if (canEdit) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = if (canEdit) {
                    stringResource(R.string.dolby_eq_drag_hint)
                } else {
                    stringResource(R.string.dolby_eq_readonly_hint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (canEdit) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            InteractiveFrequencyResponseCurve(
                bandGains = state.bandGains,
                onBandGainChange = { index, newGain ->
                    if (canEdit) {
                        viewModel.setBandGain(index, newGain)
                    }
                },
                isActive = isActive,
                isEditable = canEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun SlidersViewContent(
    state: EqualizerUiState.Success,
    viewModel: EqualizerViewModel,
    canEdit: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.frequency_response),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = getFrequencyRange(state.bandGains),
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                CompactFrequencyResponseCurve(
                    bandGains = state.bandGains,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(376.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dolby_geq_slider_label_gain),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = if (canEdit) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    if (!canEdit) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = stringResource(R.string.locked),
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.bandGains) { index, bandGain ->
                        ModernEqualizerBand(
                            frequency = bandGain.frequency,
                            gain = bandGain.gain,
                            onGainChange = { newGain ->
                                if (canEdit) {
                                    viewModel.setBandGain(index, newGain)
                                }
                            },
                            enabled = canEdit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BandTunerCard(
    bandGains: List<BandGain>,
    bandMode: BandMode,
    onGainChange: (Int, Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (bandGains.isEmpty()) return

    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    var selectedIndex by remember(bandMode, bandGains.size) { mutableIntStateOf(0) }
    val index = selectedIndex.coerceIn(0, bandGains.lastIndex)
    val band = bandGains[index]

    var sliderValue by remember(index, band.gain) { mutableFloatStateOf(band.gain / 10f) }
    var lastHapticStep by remember(index) { mutableIntStateOf(band.gain) }
    val isNonZero = abs(sliderValue) > 0.05f

    ModernSettingsCard(
        title = stringResource(R.string.band_tuner),
        subtitle = stringResource(R.string.band_tuner_desc),
        icon = Icons.Rounded.GraphicEq,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BandStepButton(
                        icon = Icons.Rounded.ChevronLeft,
                        contentDescription = stringResource(R.string.band_tuner_previous),
                        enabled = enabled && index > 0,
                        onClick = { selectedIndex = index - 1 }
                    )

                    val tunerMotionScheme = MaterialTheme.motionScheme
                    AnimatedContent(
                        targetState = index,
                        transitionSpec = {
                            val dir = if (targetState > initialState) 1 else -1
                            (slideInHorizontally(tunerMotionScheme.fastSpatialSpec()) { dir * it / 3 } +
                                fadeIn(tunerMotionScheme.fastEffectsSpec()))
                                .togetherWith(
                                    slideOutHorizontally(tunerMotionScheme.fastSpatialSpec()) { -dir * it / 3 } +
                                        fadeOut(tunerMotionScheme.fastEffectsSpec())
                                )
                        },
                        label = "band_tuner_step",
                        modifier = Modifier.weight(1f)
                    ) { targetIdx ->
                        val targetBand = bandGains.getOrElse(targetIdx) { band }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(
                                    R.string.band_tuner_label,
                                    targetIdx + 1,
                                    formatFrequency(targetBand.frequency)
                                ),
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = if (enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(
                                    R.string.band_tuner_position,
                                    targetIdx + 1,
                                    bandGains.size
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    BandStepButton(
                        icon = Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.band_tuner_next),
                        enabled = enabled && index < bandGains.lastIndex,
                        onClick = { selectedIndex = index + 1 }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dolby_geq_slider_label_gain),
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Surface(
                        shape = CircleShape,
                        color = when {
                            !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest
                            isNonZero -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ) {
                        Text(
                            text = formatGain(sliderValue),
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            color = when {
                                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                                isNonZero -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        val step = (newValue * 10).toInt()
                        if (step != lastHapticStep) {
                            scope.launch {
                                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TEXTURE_TICK)
                            }
                            lastHapticStep = step
                        }
                        sliderValue = newValue
                    },
                    onValueChangeFinished = {
                        onGainChange(index, (sliderValue * 10).toInt())
                    },
                    enabled = enabled,
                    valueRange = -15f..15f,
                    steps = 299,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        activeTickColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledThumbColor = MaterialTheme.colorScheme.outline,
                        disabledActiveTrackColor = MaterialTheme.colorScheme.outline,
                        disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BandAdjustButton(
                    label = stringResource(R.string.band_tuner_decrement),
                    enabled = enabled && band.gain > -150,
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        bottomStart = 24.dp,
                        topEnd = 10.dp,
                        bottomEnd = 10.dp
                    ),
                    onClick = {
                        val newGain = (band.gain - 1).coerceAtLeast(-150)
                        sliderValue = newGain / 10f
                        onGainChange(index, newGain)
                    },
                    modifier = Modifier.weight(1f)
                )
                BandAdjustButton(
                    label = stringResource(R.string.band_tuner_reset),
                    enabled = enabled && band.gain != 0,
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        sliderValue = 0f
                        onGainChange(index, 0)
                    },
                    modifier = Modifier.weight(1f)
                )
                BandAdjustButton(
                    label = stringResource(R.string.band_tuner_increment),
                    enabled = enabled && band.gain < 150,
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        bottomStart = 10.dp,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    ),
                    onClick = {
                        val newGain = (band.gain + 1).coerceAtMost(150)
                        sliderValue = newGain / 10f
                        onGainChange(index, newGain)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BandStepButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = {
            scope.launch {
                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
            }
            onClick()
        },
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(width = 48.dp, height = 42.dp)
            .expressivePress(interactionSource, enabled = enabled, scaleDown = 0.9f),
        shape = CircleShape,
        color = if (enabled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
private fun BandAdjustButton(
    label: String,
    enabled: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = {
            scope.launch {
                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TICK)
            }
            onClick()
        },
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .height(48.dp)
            .expressivePress(interactionSource, enabled = enabled, scaleDown = 0.94f),
        shape = shape,
        color = if (enabled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

private fun formatFrequency(frequency: Int): String =
    if (frequency >= 1000) "%.1f kHz".format(frequency / 1000f) else "$frequency Hz"

private fun formatGain(gain: Float): String {
    val rounded = Math.round(gain * 10f) / 10f
    return if (rounded > 0f) "+%.1f dB".format(rounded) else "%.1f dB".format(rounded + 0f)
}

private fun getFrequencyRange(bandGains: List<BandGain>): String {
    if (bandGains.isEmpty()) return ""
    return "${formatFrequency(bandGains.minOf { it.frequency })} – " +
        formatFrequency(bandGains.maxOf { it.frequency })
}

@Composable
private fun BandModeSelector(
    currentMode: BandMode,
    onModeChange: (BandMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val modes = BandMode.values()

    ModernSettingsCard(
        title = stringResource(R.string.band_configuration),
        subtitle = stringResource(R.string.choose_equalizer_precision),
        icon = Icons.Rounded.Tune,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            modes.forEachIndexed { index, mode ->
                BandModeTile(
                    mode = mode,
                    isSelected = currentMode == mode,
                    isStart = index == 0,
                    isEnd = index == modes.lastIndex,
                    onClick = {
                        if (currentMode != mode) {
                            scope.launch {
                                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                            }
                            onModeChange(mode)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BandModeTile(
    mode: BandMode,
    isSelected: Boolean,
    isStart: Boolean,
    isEnd: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val innerCorner by animateDpAsState(
        targetValue = when {
            pressed -> 10.dp
            isSelected -> 24.dp
            else -> 10.dp
        },
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "band_mode_corner"
    )
    val startCorner = if (isStart || isSelected) 24.dp else innerCorner
    val endCorner = if (isEnd || isSelected) 24.dp else innerCorner
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "band_mode_bg"
    )
    val fgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "band_mode_fg"
    )

    Surface(
        selected = isSelected,
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(78.dp)
            .expressivePress(interactionSource, scaleDown = 0.95f)
            .semantics { role = Role.RadioButton },
        color = bgColor,
        contentColor = fgColor,
        shape = RoundedCornerShape(
            topStart = startCorner,
            bottomStart = startCorner,
            topEnd = endCorner,
            bottomEnd = endCorner
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = mode.value,
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mode.displayName,
                style = if (isSelected) {
                    MaterialTheme.typography.labelSmallEmphasized
                } else {
                    MaterialTheme.typography.labelSmall
                },
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernPresetSelector(
    presets: List<EqualizerPreset>,
    currentPreset: EqualizerPreset,
    onPresetSelected: (EqualizerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                scope.launch {
                    haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TICK)
                }
                expanded = it
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentPreset.isUserDefined) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = currentPreset.name,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = MaterialTheme.shapes.large
            ) {
                presets.forEach { preset ->
                    val isCurrent = preset.name == currentPreset.name
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = if (isCurrent) {
                                            MaterialTheme.typography.bodyLargeEmphasized
                                        } else {
                                            MaterialTheme.typography.bodyLarge
                                        },
                                        color = if (isCurrent) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (preset.isUserDefined) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            scope.launch {
                                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                            }
                            onPresetSelected(preset)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (presets.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets, key = { "${it.name}_${it.isUserDefined}_${it.bandMode.value}" }) { preset ->
                    val selected = preset.name == currentPreset.name
                    val chipInteraction = remember { MutableInteractionSource() }
                    val chipBg by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        label = "preset_chip_bg"
                    )
                    val chipFg by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        label = "preset_chip_fg"
                    )

                    Surface(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                scope.launch {
                                    haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                                }
                                onPresetSelected(preset)
                            }
                        },
                        interactionSource = chipInteraction,
                        modifier = Modifier.expressivePress(chipInteraction, scaleDown = 0.94f),
                        shape = CircleShape,
                        color = chipBg,
                        contentColor = chipFg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (preset.isUserDefined) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = preset.name,
                                style = if (selected) {
                                    MaterialTheme.typography.labelLargeEmphasized
                                } else {
                                    MaterialTheme.typography.labelLarge
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernEqualizerBand(
    frequency: Int,
    gain: Int,
    onGainChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var sliderValue by remember(gain) { mutableFloatStateOf(gain / 10f) }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    var lastHapticValue by remember { mutableIntStateOf((gain / 10f).toInt()) }
    val isNonZero = abs(sliderValue) > 0.05f

    Column(
        modifier = modifier
            .width(62.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = when {
                !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest
                isNonZero -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ) {
            val sign = if (sliderValue > 0.05f) "+" else ""
            Text(
                text = "$sign%.1f".format(sliderValue + 0f),
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    isNonZero -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                if (enabled) {
                    val intValue = (newValue * 10).toInt() / 10
                    if (intValue != lastHapticValue) {
                        scope.launch {
                            haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TEXTURE_TICK)
                        }
                        lastHapticValue = intValue
                    }
                    sliderValue = newValue
                }
            },
            onValueChangeFinished = {
                if (enabled) {
                    onGainChange((sliderValue * 10).toInt())
                }
            },
            enabled = enabled,
            valueRange = -15f..15f,
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = 270f
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = constraints.minHeight,
                            maxWidth = constraints.maxHeight,
                            minHeight = constraints.minWidth,
                            maxHeight = constraints.maxHeight,
                        )
                    )
                    layout(placeable.height, placeable.width) {
                        placeable.place(-placeable.width, 0)
                    }
                }
                .weight(1f)
                .width(48.dp),
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                activeTrackColor = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                inactiveTrackColor = if (enabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledThumbColor = MaterialTheme.colorScheme.outline,
                disabledActiveTrackColor = MaterialTheme.colorScheme.outline,
                disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
        Text(
            text = if (frequency >= 1000) "${frequency / 1000}k" else "$frequency",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SavePresetDialog(
    onSave: (String) -> String?,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.dolby_geq_new_preset),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = {
                        presetName = it
                        errorMessage = null
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.dolby_geq_preset_name),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = errorMessage != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val error = onSave(presetName)
                    if (error != null) {
                        errorMessage = error
                    }
                },
                enabled = presetName.isNotBlank(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(android.R.string.ok),
                    style = MaterialTheme.typography.labelLargeEmphasized
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = CircleShape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoEqSelectionDialog(
    viewModel: EqualizerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initAutoEq(context)
        viewModel.updateSearchQuery("")
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredList by viewModel.filteredAutoEqList.collectAsState()
    val isLoading by viewModel.isSearchLoading.collectAsState()
    val activeAutoEqId by viewModel.currentAppliedAutoEqId.collectAsState()
    val listState = rememberLazyListState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.93f)
                .heightIn(min = 380.dp, max = 640.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = MaterialShapes.Cookie9Sided.toShape(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dolby_autoeq_title),
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isLoading && filteredList.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.dolby_autoeq_results_count, filteredList.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FilledTonalIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.dolby_close)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.dolby_autoeq_search_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = stringResource(R.string.dolby_autoeq_search),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = stringResource(R.string.dolby_autoeq_clear),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isEmpty() && activeAutoEqId.isNotEmpty()) {
                    val activeEntry = filteredList.find { it.id == activeAutoEqId }
                    if (activeEntry != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.dolby_autoeq_currently_applied),
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeEntry.name,
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${activeEntry.source} · ${activeEntry.measurementRig}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = stringResource(R.string.dolby_autoeq_active),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }

                when {
                    isLoading && filteredList.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ExpressiveLoadingState(message = stringResource(R.string.loading))
                        }
                    }
                    filteredList.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ExpressiveEmptyState(
                                icon = Icons.Rounded.SearchOff,
                                title = stringResource(R.string.dolby_autoeq_no_results)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            contentPadding = PaddingValues(bottom = 4.dp)
                        ) {
                            itemsIndexed(filteredList, key = { _, it -> it.id }) { idx, entry ->
                                val isSelected = entry.id == activeAutoEqId
                                val itemInteraction = remember { MutableInteractionSource() }

                                Surface(
                                    onClick = {
                                        viewModel.applyAutoEqProfileNetwork(context, entry)
                                        onDismiss()
                                    },
                                    interactionSource = itemInteraction,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .expressivePress(itemInteraction, scaleDown = 0.98f),
                                    shape = groupedItemShape(idx, filteredList.size),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    contentColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = entry.name,
                                                style = MaterialTheme.typography.bodyLargeEmphasized,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onSecondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${entry.source} · ${entry.measurementRig}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = stringResource(R.string.dolby_autoeq_selected),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = onDismiss,
                        shape = CircleShape
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            style = MaterialTheme.typography.labelLargeEmphasized
                        )
                    }
                }
            }
        }
    }
}
