/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import org.lunaris.dolby.data.PresetExportManager
import org.lunaris.dolby.domain.models.EqualizerPreset
import org.lunaris.dolby.domain.models.EqualizerUiState
import org.lunaris.dolby.ui.components.CompactFrequencyResponseCurve
import org.lunaris.dolby.ui.components.DolbyPageHeading
import org.lunaris.dolby.ui.components.DolbyTopAppBar
import org.lunaris.dolby.ui.components.ExpressiveEmptyState
import org.lunaris.dolby.ui.components.ExpressiveLoadingState
import org.lunaris.dolby.ui.components.ModernConfirmDialog
import org.lunaris.dolby.ui.components.ModernSettingsCard
import org.lunaris.dolby.ui.components.groupedItemShape
import org.lunaris.dolby.ui.viewmodel.EqualizerViewModel
import org.lunaris.dolby.utils.ToastHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PresetImportExportScreen(
    viewModel: EqualizerViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportManager = remember { PresetExportManager(context) }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val showTitle by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 8 }
    }

    var selectedPreset by remember { mutableStateOf<EqualizerPreset?>(null) }
    var showBatchExport by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<EqualizerPreset?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val userPresets = remember(uiState) {
        (uiState as? EqualizerUiState.Success)?.presets?.filter { it.isUserDefined }.orEmpty()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            selectedPreset?.let { preset ->
                scope.launch {
                    isLoading = true
                    exportManager.exportPresetToFile(preset, uri).fold(
                        onSuccess = {
                            ToastHelper.showToast(context, context.getString(R.string.preset_export_success))
                        },
                        onFailure = { e ->
                            ToastHelper.showToast(context, "${context.getString(R.string.preset_export_failed)}: ${e.message}")
                        }
                    )
                    isLoading = false
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                exportManager.importPresetFromFile(uri).fold(
                    onSuccess = { preset ->
                        val error = viewModel.saveImportedPreset(preset)
                        if (error != null) {
                            ToastHelper.showToast(context, error)
                        } else {
                            ToastHelper.showToast(
                                context,
                                "Preset '${preset.name}' imported! (${preset.bandMode.displayName})"
                            )
                            viewModel.loadEqualizer()
                        }
                    },
                    onFailure = { e ->
                        ToastHelper.showToast(context, "${context.getString(R.string.preset_import_failed)}: ${e.message}")
                    }
                )
                isLoading = false
            }
        }
    }

    val batchExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                val state = uiState as? EqualizerUiState.Success
                val presets = state?.presets?.filter { it.isUserDefined } ?: emptyList()

                exportManager.exportMultiplePresets(presets, uri).fold(
                    onSuccess = {
                        ToastHelper.showToast(context, "${presets.size} presets exported!")
                    },
                    onFailure = { e ->
                        ToastHelper.showToast(context, "Batch export failed: ${e.message}")
                    }
                )
                isLoading = false
                showBatchExport = false
            }
        }
    }

    val batchImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                exportManager.importMultiplePresets(uri).fold(
                    onSuccess = { presets ->
                        var successCount = 0
                        presets.forEach { preset ->
                            if (viewModel.saveImportedPreset(preset) == null) {
                                successCount++
                            }
                        }
                        ToastHelper.showToast(
                            context,
                            "Imported $successCount of ${presets.size} presets"
                        )
                        viewModel.loadEqualizer()
                    },
                    onFailure = { e ->
                        ToastHelper.showToast(context, "Batch import failed: ${e.message}")
                    }
                )
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            DolbyTopAppBar(
                title = stringResource(R.string.import_export_presets),
                showTitle = showTitle || uiState !is EqualizerUiState.Success,
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
                    if (userPresets.isNotEmpty()) {
                        FilledTonalIconButton(
                            onClick = { showBatchExport = true },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FileDownload,
                                contentDescription = stringResource(R.string.batch_export)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is EqualizerUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(key = "heading") {
                            DolbyPageHeading(
                                title = stringResource(R.string.import_export_presets),
                                subtitle = stringResource(R.string.import_export_subtitle),
                            )
                        }

                        item(key = "import_card") {
                            ModernSettingsCard(
                                title = stringResource(R.string.import_presets),
                                subtitle = stringResource(R.string.import_presets_description),
                                icon = Icons.Rounded.FileDownload
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { importLauncher.launch("*/*") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.FolderOpen,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.import_single_file),
                                                style = MaterialTheme.typography.labelLargeEmphasized,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Button(
                                            onClick = { batchImportLauncher.launch("*/*") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.FolderCopy,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.import_batch),
                                                style = MaterialTheme.typography.labelLargeEmphasized,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            scope.launch {
                                                isLoading = true
                                                exportManager.importPresetFromClipboard().fold(
                                                    onSuccess = { preset ->
                                                        val error = viewModel.saveImportedPreset(preset)
                                                        if (error != null) {
                                                            ToastHelper.showToast(context, error)
                                                        } else {
                                                            ToastHelper.showToast(
                                                                context,
                                                                "Preset imported from clipboard! (${preset.bandMode.displayName})"
                                                            )
                                                            viewModel.loadEqualizer()
                                                        }
                                                    },
                                                    onFailure = { e ->
                                                        ToastHelper.showToast(
                                                            context,
                                                            "Clipboard import failed: ${e.message}"
                                                        )
                                                    }
                                                )
                                                isLoading = false
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = CircleShape
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentPaste,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.import_from_clipboard),
                                            style = MaterialTheme.typography.labelLargeEmphasized
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "section_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.your_custom_presets),
                                        style = MaterialTheme.typography.titleLargeEmphasized,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = userPresets.size.toString(),
                                            style = MaterialTheme.typography.labelMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (userPresets.isNotEmpty()) {
                                    TextButton(
                                        onClick = { showBatchExport = true },
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = stringResource(R.string.export_all),
                                            style = MaterialTheme.typography.labelLargeEmphasized
                                        )
                                    }
                                }
                            }
                        }

                        if (userPresets.isEmpty()) {
                            item(key = "empty_presets") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    ExpressiveEmptyState(
                                        icon = Icons.Rounded.GraphicEq,
                                        title = stringResource(R.string.import_export_empty_title),
                                        subtitle = stringResource(R.string.import_export_empty_desc)
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(
                                userPresets,
                                key = { _, preset -> "${preset.name}_${preset.bandMode.value}" }
                            ) { index, preset ->
                                PresetExportCard(
                                    preset = preset,
                                    shape = groupedItemShape(index, userPresets.size),
                                    onExportFile = {
                                        selectedPreset = preset
                                        exportLauncher.launch("${preset.name.replace(" ", "_")}_${preset.bandMode.value}band.ldp")
                                    },
                                    onCopyClipboard = {
                                        scope.launch {
                                            isLoading = true
                                            exportManager.copyPresetToClipboard(preset).fold(
                                                onSuccess = {
                                                    ToastHelper.showToast(
                                                        context,
                                                        "Preset copied to clipboard!"
                                                    )
                                                },
                                                onFailure = { e ->
                                                    ToastHelper.showToast(
                                                        context,
                                                        "Copy failed: ${e.message}"
                                                    )
                                                }
                                            )
                                            isLoading = false
                                        }
                                    },
                                    onShare = {
                                        scope.launch {
                                            isLoading = true
                                            exportManager.createShareIntent(preset).fold(
                                                onSuccess = { intent ->
                                                    context.startActivity(intent)
                                                },
                                                onFailure = { e ->
                                                    ToastHelper.showToast(
                                                        context,
                                                        "Share failed: ${e.message}"
                                                    )
                                                }
                                            )
                                            isLoading = false
                                        }
                                    },
                                    onDelete = {
                                        presetToDelete = preset
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveLoadingState(message = stringResource(R.string.loading))
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            LoadingIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.processing),
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBatchExport) {
        val presetCount = userPresets.size

        AlertDialog(
            onDismissRequest = { showBatchExport = false },
            icon = {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.batch_export),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.batch_export_description, presetCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBatchExport = false
                        batchExportLauncher.launch("dolby_presets_backup.ldp")
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.export_all),
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBatchExport = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDeleteDialog && presetToDelete != null) {
        ModernConfirmDialog(
            title = stringResource(R.string.dolby_geq_delete_preset),
            message = "Are you sure you want to delete '${presetToDelete!!.name}'? This will remove it from your preset list and cannot be undone.",
            icon = Icons.Rounded.Delete,
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    val name = presetToDelete!!.name
                    viewModel.deletePreset(presetToDelete!!)
                    ToastHelper.showToast(context, "Preset '$name' deleted")
                    viewModel.loadEqualizer()
                    showDeleteDialog = false
                    presetToDelete = null
                }
            },
            onDismiss = {
                showDeleteDialog = false
                presetToDelete = null
            }
        )
    }
}

@Composable
private fun PresetExportCard(
    preset: EqualizerPreset,
    shape: Shape,
    onExportFile: () -> Unit,
    onCopyClipboard: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${preset.bandMode.displayName} · ${stringResource(R.string.dolby_eq_bands_count, preset.bandGains.size)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = onExportFile,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = stringResource(R.string.preset_export_single),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onCopyClipboard,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.preset_copy_clipboard),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onShare,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.preset_share),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.dolby_geq_delete_preset),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (preset.bandGains.size > 1) {
                CompactFrequencyResponseCurve(
                    bandGains = preset.bandGains,
                    showGrid = true,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }
    }
}
