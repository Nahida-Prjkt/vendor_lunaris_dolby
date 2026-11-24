/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.lunaris.dolby.DolbyConstants
import org.lunaris.dolby.R
import org.lunaris.dolby.service.AppProfileMonitorService

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppProfileSettingsCard(
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("dolby_prefs", Context.MODE_PRIVATE) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var isEnabled by remember {
        mutableStateOf(prefs.getBoolean("app_profile_monitoring_enabled", false))
    }
    var isDeviceStateMemoryEnabled by remember {
        mutableStateOf(prefs.getBoolean(DolbyConstants.PREF_DEVICE_STATE_MEMORY, false))
    }
    var showToasts by remember {
        mutableStateOf(prefs.getBoolean("app_profile_show_toasts", true))
    }
    var headphoneOnlyMode by remember {
        mutableStateOf(prefs.getBoolean("app_profile_headphone_only", false))
    }
    var showPermissionDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, prefs) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = prefs.getBoolean("app_profile_monitoring_enabled", false)
                isDeviceStateMemoryEnabled = prefs.getBoolean(DolbyConstants.PREF_DEVICE_STATE_MEMORY, false)
                showToasts = prefs.getBoolean("app_profile_show_toasts", true)
                headphoneOnlyMode = prefs.getBoolean("app_profile_headphone_only", false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ModernSettingsCard(
        title = stringResource(R.string.app_profiles_title),
        subtitle = stringResource(R.string.app_profiles_desc),
        icon = Icons.Rounded.Apps,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ModernSettingSwitch(
                title = stringResource(R.string.app_profiles_auto_switch),
                subtitle = if (isDeviceStateMemoryEnabled) {
                    stringResource(R.string.device_state_memory_conflict_summary)
                } else {
                    ""
                },
                checked = isEnabled,
                enabled = !isDeviceStateMemoryEnabled,
                icon = Icons.Rounded.AutoMode,
                shape = groupedCardShape(first = true, last = !isEnabled),
                onCheckedChange = { enabled ->
                    if (enabled && !hasUsageStatsPermission(context)) {
                        showPermissionDialog = true
                    } else {
                        if (enabled && isDeviceStateMemoryEnabled) {
                            return@ModernSettingSwitch
                        }
                        isEnabled = enabled
                        prefs.edit().putBoolean("app_profile_monitoring_enabled", enabled).apply()

                        if (enabled) {
                            AppProfileMonitorService.startMonitoring(context)
                        } else {
                            AppProfileMonitorService.stopMonitoring(context)
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = isDeviceStateMemoryEnabled,
                enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
                    expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                    shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(R.string.device_state_memory_conflict_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isEnabled,
                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                    expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                    shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ModernSettingSwitch(
                        title = stringResource(R.string.app_profiles_headphone_only),
                        subtitle = stringResource(R.string.app_profiles_headphone_only_description),
                        checked = headphoneOnlyMode,
                        icon = Icons.Rounded.Headphones,
                        shape = groupedCardShape(first = false, last = false),
                        onCheckedChange = { enabled ->
                            headphoneOnlyMode = enabled
                            prefs.edit().putBoolean("app_profile_headphone_only", enabled).apply()
                        }
                    )

                    ModernSettingSwitch(
                        title = stringResource(R.string.app_profiles_show_toasts),
                        subtitle = "",
                        checked = showToasts,
                        icon = Icons.Rounded.Notifications,
                        shape = groupedCardShape(first = false, last = true),
                        onCheckedChange = { show ->
                            showToasts = show
                            prefs.edit().putBoolean("app_profile_show_toasts", show).apply()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ModernSettingSwitch(
                title = stringResource(R.string.device_state_memory_title),
                subtitle = if (isEnabled) {
                    stringResource(R.string.app_profile_conflict_summary)
                } else {
                    stringResource(R.string.device_state_memory_summary)
                },
                checked = isDeviceStateMemoryEnabled,
                enabled = !isEnabled,
                icon = Icons.Rounded.Devices,
                shape = MaterialTheme.shapes.large,
                onCheckedChange = { enabled ->
                    if (enabled && isEnabled) {
                        isEnabled = false
                        prefs.edit()
                            .putBoolean("app_profile_monitoring_enabled", false)
                            .apply()
                        AppProfileMonitorService.stopMonitoring(context)
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.app_profile_conflict_toast),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    isDeviceStateMemoryEnabled = enabled
                    prefs.edit()
                        .putBoolean(DolbyConstants.PREF_DEVICE_STATE_MEMORY, enabled)
                        .apply()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onManageClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_profiles_manage),
                    style = MaterialTheme.typography.labelLargeEmphasized
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.app_profiles_permission_required),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.app_profiles_permission_required_details),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                        showPermissionDialog = false
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.app_profiles_grant_permission),
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false },
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
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOpsManager.checkOpNoThrow(
        "android:get_usage_stats",
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}
