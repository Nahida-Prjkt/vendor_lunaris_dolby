/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import org.lunaris.dolby.utils.HapticFeedbackHelper
import org.lunaris.dolby.utils.rememberHapticFeedback

fun groupedCardShape(
    first: Boolean,
    last: Boolean,
    outerCorner: Dp = 24.dp,
    innerCorner: Dp = 6.dp
): RoundedCornerShape = RoundedCornerShape(
    topStart = if (first) outerCorner else innerCorner,
    topEnd = if (first) outerCorner else innerCorner,
    bottomStart = if (last) outerCorner else innerCorner,
    bottomEnd = if (last) outerCorner else innerCorner
)

fun groupedItemShape(index: Int, count: Int): RoundedCornerShape =
    groupedCardShape(first = index == 0, last = index == count - 1)

fun Modifier.expressivePress(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    scaleDown: Float = 0.96f
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) scaleDown else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "expressive_press_scale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.squishable(
    enabled: Boolean = true,
    scaleDown: Float = 0.94f
): Modifier {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "squish_scale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (enabled) {
                try {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        try {
                            waitForUpOrCancellation()
                        } finally {
                            isPressed = false
                        }
                    }
                } finally {
                    isPressed = false
                }
            }
        }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun tileSelectionBorder(selected: Boolean): BorderStroke {
    val width by animateDpAsState(
        targetValue = if (selected) 1.5.dp else 1.dp,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "tile_border_width"
    )
    val color by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "tile_border_color"
    )
    return BorderStroke(width, color)
}

@Composable
fun ExpressiveLoadingState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Surface(
            shape = MaterialShapes.Cookie9Sided.toShape(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        ) {
            LoadingIndicator(
                modifier = Modifier
                    .padding(14.dp)
                    .size(56.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExpressiveErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = MaterialShapes.Cookie9Sided.toShape(),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Button(
                onClick = onRetry,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.app_profiles_retry))
            }
        }
    }
}

@Composable
fun ExpressiveEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = MaterialShapes.Cookie9Sided.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAction,
                shape = CircleShape
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLargeEmphasized
                )
            }
        }
    }
}

@Composable
fun DolbyLogo(
    modifier: Modifier = Modifier,
    leftColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    rightColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_dolby_logo_left),
            contentDescription = stringResource(R.string.dolby_title),
            tint = leftColor,
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            painter = painterResource(R.drawable.ic_dolby_logo_right),
            contentDescription = null,
            tint = rightColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DolbyMainCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    outputDeviceText: String? = null,
    outputDeviceIcon: ImageVector? = null
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dolby_power_container",
    )
    val contentColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dolby_power_content",
    )
    val logoContainerColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dolby_logo_bg",
    )
    val logoTintColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dolby_logo_tint",
    )
    val waveBarColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dolby_wave_bar",
    )
    val waveAccentColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dolby_wave_accent",
    )
    val logoScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dolby_logo_scale"
    )
    val logoRotation by animateFloatAsState(
        targetValue = if (enabled) 0f else -8f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "dolby_logo_rotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .expressivePress(interactionSource = interactionSource, scaleDown = 0.98f),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
        border = if (!enabled) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .toggleable(
                    value = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    role = Role.Switch,
                    onValueChange = {
                        scope.launch {
                            haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                        }
                        onEnabledChange(it)
                    },
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Surface(
                    shape = if (enabled) {
                        MaterialShapes.Cookie9Sided.toShape()
                    } else {
                        RoundedCornerShape(24.dp)
                    },
                    color = logoContainerColor,
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                            rotationZ = logoRotation
                        },
                ) {
                    DolbyLogo(
                        modifier = Modifier.padding(18.dp),
                        leftColor = logoTintColor,
                        rightColor = logoTintColor,
                    )
                }
                AnimatedWaveformBanner(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    barColor = waveBarColor,
                    accentColor = waveAccentColor,
                    barCount = 28,
                    animated = enabled,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dolby_enable),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                    )
                    val statusMotionScheme = MaterialTheme.motionScheme
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedContent(
                            targetState = statusText ?: stringResource(
                                if (enabled) R.string.dolby_on else R.string.dolby_off
                            ),
                            transitionSpec = {
                                fadeIn(statusMotionScheme.fastEffectsSpec()) togetherWith
                                    fadeOut(statusMotionScheme.fastEffectsSpec())
                            },
                            label = "dolby_status_text"
                        ) { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                color = if (enabled) {
                                    contentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        if (outputDeviceText != null && outputDeviceIcon != null) {
                            Surface(
                                shape = CircleShape,
                                color = if (enabled) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                contentColor = if (enabled) {
                                    contentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = outputDeviceIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = outputDeviceText,
                                        style = MaterialTheme.typography.labelSmallEmphasized,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = null,
                    thumbContent = {
                        val thumbMotionScheme = MaterialTheme.motionScheme
                        AnimatedContent(
                            targetState = enabled,
                            transitionSpec = {
                                (scaleIn(thumbMotionScheme.fastSpatialSpec()) +
                                    fadeIn(thumbMotionScheme.fastEffectsSpec()))
                                    .togetherWith(
                                        scaleOut(thumbMotionScheme.fastSpatialSpec()) +
                                            fadeOut(thumbMotionScheme.fastEffectsSpec())
                                    )
                            },
                            label = "hero_switch_icon"
                        ) { isChecked ->
                            Icon(
                                imageVector = if (isChecked) Icons.Rounded.Check else Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun ModernProfileSelector(
    currentProfile: Int,
    onProfileChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileSelector(
        currentProfile = currentProfile,
        onProfileChange = onProfileChange,
        modifier = modifier
    )
}

@Composable
fun ModernSettingsCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (subtitle != null) 14.dp else 16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (trailing != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailing()
                }
            }

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModernSettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    badgeText: String? = null
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
            checked -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "switch_row_container"
    )
    val titleColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            checked -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "switch_row_title"
    )
    val subtitleColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            checked -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "switch_row_subtitle"
    )
    val iconContainerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
            checked -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "switch_icon_bg"
    )
    val iconTintColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            checked -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "switch_icon_tint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (checked && enabled) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switch_icon_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .expressivePress(interactionSource = interactionSource, enabled = enabled, scaleDown = 0.985f)
            .clip(shape),
        shape = shape,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    role = Role.Switch,
                    onValueChange = {
                        scope.launch {
                            haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                        }
                        onCheckedChange(it)
                    },
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        shape = RoundedCornerShape(13.dp),
                        color = iconContainerColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = iconTintColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = titleColor,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (badgeText != null && checked) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                }
            }

            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = null,
                thumbContent = {
                    Crossfade(
                        targetState = checked,
                        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        label = "switch_icon"
                    ) { isChecked ->
                        Icon(
                            imageVector = if (isChecked) Icons.Rounded.Check else Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ModernSettingSlider(
    title: String,
    value: Int,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
    valueLabel: (Int) -> String = { it.toString() }
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    var lastHapticValue by remember { mutableIntStateOf(value) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = valueLabel(value),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = value.toFloat().coerceIn(valueRange.start, valueRange.endInclusive),
                onValueChange = { newValue ->
                    val intValue = newValue.toInt()
                    if (intValue != lastHapticValue) {
                        scope.launch {
                            haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TEXTURE_TICK)
                        }
                        lastHapticValue = intValue
                    }
                    onValueChange(newValue)
                },
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            )
        }
    }
}

@Composable
fun ModernSettingSelector(
    title: String,
    currentValue: Int,
    entries: Int,
    values: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val entryList = stringArrayResource(entries)
    val valueList = stringArrayResource(values)
    val options = entryList.mapIndexedNotNull { index, entry ->
        valueList.getOrNull(index)?.toIntOrNull()?.let { it to entry }
    }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (options.size in 2..4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEachIndexed { index, (value, label) ->
                        val selected = value == currentValue
                        val interactionSource = remember { MutableInteractionSource() }
                        val pressed by interactionSource.collectIsPressedAsState()
                        val corner by animateDpAsState(
                            targetValue = when {
                                pressed -> 10.dp
                                selected -> 22.dp
                                else -> 8.dp
                            },
                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                            label = "selector_corner"
                        )
                        val startCorner = if (index == 0) 22.dp else corner
                        val endCorner = if (index == options.lastIndex) 22.dp else corner
                        val itemBg by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                            label = "selector_bg"
                        )
                        val itemFg by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                            label = "selector_fg"
                        )

                        Surface(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    scope.launch {
                                        haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                                    }
                                    onValueChange(value)
                                }
                            },
                            interactionSource = interactionSource,
                            modifier = Modifier
                                .weight(1f)
                                .expressivePress(interactionSource = interactionSource, scaleDown = 0.95f)
                                .semantics { role = Role.RadioButton },
                            shape = RoundedCornerShape(
                                topStart = startCorner,
                                bottomStart = startCorner,
                                topEnd = endCorner,
                                bottomEnd = endCorner
                            ),
                            color = itemBg,
                            contentColor = itemFg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = if (selected) {
                                        MaterialTheme.typography.labelLargeEmphasized
                                    } else {
                                        MaterialTheme.typography.labelLarge
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                val currentLabel = options.firstOrNull { it.first == currentValue }?.second ?: ""
                var expanded by remember { mutableStateOf(false) }

                Box {
                    Surface(
                        onClick = {
                            scope.launch {
                                haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.TICK)
                            }
                            expanded = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentLabel,
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = MaterialTheme.shapes.large
                    ) {
                        options.forEach { (value, entry) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = entry,
                                        style = if (value == currentValue) {
                                            MaterialTheme.typography.bodyLargeEmphasized
                                        } else {
                                            MaterialTheme.typography.bodyLarge
                                        },
                                        color = if (value == currentValue) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                },
                                trailingIcon = if (value == currentValue) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null,
                                onClick = {
                                    scope.launch {
                                        haptic.performHaptic(HapticFeedbackHelper.HapticIntensity.CLICK)
                                    }
                                    expanded = false
                                    onValueChange(value)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernConfirmDialog(
    title: String,
    message: String,
    icon: ImageVector = Icons.Rounded.Info,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    val iconContainer = if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val iconTint = if (isDestructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val confirmBg = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val confirmFg = if (isDestructive) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = iconContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = iconTint
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmBg,
                    contentColor = confirmFg
                )
            ) {
                Text(
                    text = stringResource(android.R.string.yes),
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
                Text(
                    text = stringResource(android.R.string.no),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        iconContentColor = iconTint,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.extraLarge
    )
}
