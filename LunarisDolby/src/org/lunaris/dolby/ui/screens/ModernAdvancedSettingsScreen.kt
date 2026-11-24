/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.lunaris.dolby.R
import org.lunaris.dolby.domain.models.DolbyUiState
import org.lunaris.dolby.ui.components.DolbyPageHeading
import org.lunaris.dolby.ui.components.DolbyTopAppBar
import org.lunaris.dolby.ui.components.ExpressiveEmptyState
import org.lunaris.dolby.ui.components.ExpressiveErrorState
import org.lunaris.dolby.ui.components.ExpressiveLoadingState
import org.lunaris.dolby.ui.components.ModernSettingSelector
import org.lunaris.dolby.ui.components.ModernSettingSlider
import org.lunaris.dolby.ui.components.ModernSettingSwitch
import org.lunaris.dolby.ui.components.ModernSettingsCard
import org.lunaris.dolby.ui.components.groupedCardShape
import org.lunaris.dolby.ui.viewmodel.DolbyViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModernAdvancedSettingsScreen(
    viewModel: DolbyViewModel,
    navController: NavController,
    bottomPadding: Dp = 0.dp,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val showTitle by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 8 }
    }

    Scaffold(
        topBar = {
            DolbyTopAppBar(
                title = stringResource(R.string.dolby_category_adv_settings),
                showTitle = showTitle || uiState !is DolbyUiState.Success,
                scrolled = isScrolled
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DolbyUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveLoadingState(message = stringResource(R.string.loading))
                    }
                }
                is DolbyUiState.Success -> {
                    ModernAdvancedSettingsContent(
                        state = state,
                        listState = listState,
                        viewModel = viewModel,
                        bottomPadding = bottomPadding,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                is DolbyUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadSettings() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernAdvancedSettingsContent(
    state: DolbyUiState.Success,
    listState: LazyListState,
    viewModel: DolbyViewModel,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val profileEntries = stringArrayResource(R.array.dolby_profile_entries)
    val profileValues = stringArrayResource(R.array.dolby_profile_values)
    val activeProfileIndex = profileValues.indexOfFirst { it.toIntOrNull() == state.settings.currentProfile }
    val activeProfileName = profileEntries.getOrElse(activeProfileIndex.coerceAtLeast(0)) { "" }
    val outputName = stringResource(
        if (state.isOnSpeaker) R.string.dolby_output_speaker else R.string.dolby_output_headphones
    )
    val outputIcon = if (state.isOnSpeaker) Icons.Rounded.Speaker else Icons.Rounded.Headphones
    val contextBadge = if (state.settings.enabled && activeProfileName.isNotEmpty()) {
        "$activeProfileName · $outputName"
    } else {
        outputName
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = 16.dp + bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            DolbyPageHeading(
                title = stringResource(R.string.dolby_category_adv_settings),
                subtitle = stringResource(R.string.dolby_advanced_summary),
                badgeText = contextBadge,
                badgeIcon = outputIcon,
            )
        }

        if (state.settings.enabled) {
            item {
                ModernSettingsCard(
                    title = stringResource(R.string.dolby_category_settings),
                    icon = Icons.Rounded.Tune
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val bassOn = state.profileSettings.bassLevel > 0
                            ModernSettingSwitch(
                                title = stringResource(R.string.dolby_bass_enhancer),
                                subtitle = stringResource(R.string.dolby_bass_enhancer_summary),
                                checked = bassOn,
                                badgeText = "${state.profileSettings.bassLevel}%",
                                shape = groupedCardShape(first = true, last = false),
                                onCheckedChange = { enabled ->
                                    if (enabled && state.profileSettings.bassLevel == 0) {
                                        viewModel.setBassLevel(50)
                                    } else if (!enabled) {
                                        viewModel.setBassLevel(0)
                                    }
                                },
                                icon = Icons.Rounded.MusicNote
                            )

                            AnimatedVisibility(
                                visible = bassOn,
                                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                                    expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
                                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                                    shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ModernSettingSelector(
                                        title = stringResource(R.string.dolby_bass_curve),
                                        currentValue = state.profileSettings.bassCurve,
                                        entries = R.array.dolby_bass_curve_entries,
                                        values = R.array.dolby_bass_curve_values,
                                        onValueChange = { viewModel.setBassCurve(it) },
                                        icon = Icons.Rounded.Equalizer
                                    )
                                    ModernSettingSlider(
                                        title = stringResource(R.string.dolby_bass_level),
                                        value = state.profileSettings.bassLevel,
                                        onValueChange = { viewModel.setBassLevel(it.toInt()) },
                                        valueRange = 0f..100f,
                                        steps = 19,
                                        valueLabel = { "$it%" }
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val midOn = state.profileSettings.midLevel > 0
                            ModernSettingSwitch(
                                title = stringResource(R.string.dolby_mid_enhancer),
                                subtitle = stringResource(R.string.dolby_mid_enhancer_summary),
                                checked = midOn,
                                badgeText = "${state.profileSettings.midLevel}%",
                                shape = groupedCardShape(first = false, last = false),
                                onCheckedChange = { enabled ->
                                    if (enabled && state.profileSettings.midLevel == 0) {
                                        viewModel.setMidLevel(40)
                                    } else if (!enabled) {
                                        viewModel.setMidLevel(0)
                                    }
                                },
                                icon = Icons.Rounded.VolumeUp
                            )

                            AnimatedVisibility(
                                visible = midOn,
                                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                                    expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
                                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                                    shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
                            ) {
                                Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
                                    ModernSettingSlider(
                                        title = stringResource(R.string.dolby_mid_level),
                                        value = state.profileSettings.midLevel,
                                        onValueChange = { viewModel.setMidLevel(it.toInt()) },
                                        valueRange = 0f..100f,
                                        steps = 19,
                                        valueLabel = { "$it%" }
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val trebleOn = state.profileSettings.trebleLevel > 0
                            ModernSettingSwitch(
                                title = stringResource(R.string.dolby_treble_enhancer),
                                subtitle = stringResource(R.string.dolby_treble_enhancer_summary),
                                checked = trebleOn,
                                badgeText = "${state.profileSettings.trebleLevel}%",
                                shape = groupedCardShape(first = false, last = !trebleOn),
                                onCheckedChange = { enabled ->
                                    if (enabled && state.profileSettings.trebleLevel == 0) {
                                        viewModel.setTrebleLevel(30)
                                    } else if (!enabled) {
                                        viewModel.setTrebleLevel(0)
                                    }
                                },
                                icon = Icons.Rounded.GraphicEq
                            )

                            AnimatedVisibility(
                                visible = trebleOn,
                                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                                    expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
                                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                                    shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
                            ) {
                                Column(modifier = Modifier.padding(top = 4.dp)) {
                                    ModernSettingSlider(
                                        title = stringResource(R.string.dolby_treble_level),
                                        value = state.profileSettings.trebleLevel,
                                        onValueChange = { viewModel.setTrebleLevel(it.toInt()) },
                                        valueRange = 0f..100f,
                                        steps = 19,
                                        valueLabel = { "$it%" }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                ModernSettingsCard(
                    title = stringResource(R.string.dolby_volume_leveler),
                    icon = Icons.Rounded.VolumeDown
                ) {
                    ModernSettingSwitch(
                        title = stringResource(R.string.dolby_volume_leveler),
                        subtitle = stringResource(R.string.dolby_volume_leveler_summary),
                        checked = state.settings.volumeLevelerEnabled,
                        onCheckedChange = { viewModel.setVolumeLeveler(it) },
                        icon = Icons.Rounded.BarChart
                    )
                }
            }

            if (state.settings.currentProfile != 0) {
                item {
                    ModernSettingsCard(
                        title = stringResource(R.string.dolby_surround_virtualizer),
                        icon = Icons.Rounded.SurroundSound
                    ) {
                        if (state.isOnSpeaker) {
                            ModernSettingSwitch(
                                title = stringResource(R.string.dolby_spk_virtualizer),
                                subtitle = stringResource(R.string.dolby_spk_virtualizer_summary),
                                checked = state.profileSettings.speakerVirtualizerEnabled,
                                onCheckedChange = { viewModel.setSpeakerVirtualizer(it) },
                                icon = Icons.Rounded.Speaker
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ModernSettingSwitch(
                                    title = stringResource(R.string.dolby_hp_virtualizer),
                                    subtitle = stringResource(R.string.dolby_hp_virtualizer_summary),
                                    checked = state.profileSettings.headphoneVirtualizerEnabled,
                                    badgeText = state.profileSettings.stereoWideningAmount.toString(),
                                    onCheckedChange = { viewModel.setHeadphoneVirtualizer(it) },
                                    icon = Icons.Rounded.Headphones
                                )

                                AnimatedVisibility(
                                    visible = state.profileSettings.headphoneVirtualizerEnabled,
                                    enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                                        expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
                                    exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                                        shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
                                ) {
                                    ModernSettingSlider(
                                        title = stringResource(R.string.dolby_hp_virtualizer_dolby_strength),
                                        value = state.profileSettings.stereoWideningAmount,
                                        onValueChange = { viewModel.setStereoWidening(it.toInt()) },
                                        valueRange = 4f..64f,
                                        steps = 59
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    ModernSettingsCard(
                        title = stringResource(R.string.dolby_dialogue_enhancer),
                        icon = Icons.Rounded.RecordVoiceOver
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModernSettingSwitch(
                                title = stringResource(R.string.dolby_dialogue_enhancer),
                                subtitle = stringResource(R.string.dolby_dialogue_enhancer_summary),
                                checked = state.profileSettings.dialogueEnhancerEnabled,
                                badgeText = state.profileSettings.dialogueEnhancerAmount.toString(),
                                onCheckedChange = { viewModel.setDialogueEnhancer(it) },
                                icon = Icons.Rounded.RecordVoiceOver
                            )

                            AnimatedVisibility(
                                visible = state.profileSettings.dialogueEnhancerEnabled,
                                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                                    expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
                                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                                    shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
                            ) {
                                ModernSettingSlider(
                                    title = stringResource(R.string.dolby_dialogue_enhancer_dolby_strength),
                                    value = state.profileSettings.dialogueEnhancerAmount,
                                    onValueChange = { viewModel.setDialogueEnhancerAmount(it.toInt()) },
                                    valueRange = 1f..12f,
                                    steps = 10
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.dolby_adv_settings_footer),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    ExpressiveEmptyState(
                        icon = Icons.Rounded.PowerSettingsNew,
                        title = stringResource(R.string.dolby_disabled_title),
                        subtitle = stringResource(R.string.dolby_disabled_advanced_summary),
                        actionLabel = stringResource(R.string.dolby_enable_action),
                        onAction = { viewModel.setDolbyEnabled(true) }
                    )
                }
            }
        }
    }
}
