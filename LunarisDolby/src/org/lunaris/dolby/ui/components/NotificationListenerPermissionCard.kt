/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import org.lunaris.dolby.R
import org.lunaris.dolby.service.DolbyNotificationListener

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationListenerPermissionCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        isEnabled = isNotificationListenerEnabled(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AnimatedVisibility(
        visible = !isEnabled,
        enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
            expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()),
        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
            shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = MaterialShapes.Cookie9Sided.toShape(),
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.notification_access_required),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = stringResource(R.string.notification_access_required_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { showPermissionDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.enable_notification_access),
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
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
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.enable_notification_access),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notification_access_permission_details),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        openNotificationListenerSettings(context)
                        showPermissionDialog = false
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.open_settings),
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

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val cn = ComponentName(context, DolbyNotificationListener::class.java)
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(cn.flattenToString()) == true
}

private fun openNotificationListenerSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    context.startActivity(intent)
}
