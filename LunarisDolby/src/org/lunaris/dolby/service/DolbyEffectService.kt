/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import org.lunaris.dolby.DolbyConstants
import org.lunaris.dolby.data.DeviceStateManager
import org.lunaris.dolby.data.DolbyRepository

class DolbyEffectService : Service() {

    private val audioManager by lazy { getSystemService(AudioManager::class.java) }
    private val dolbyPrefs: SharedPreferences by lazy {
        getSharedPreferences("dolby_prefs", Context.MODE_PRIVATE)
    }
    private val isDeviceStateMemoryEnabled: Boolean
        get() = dolbyPrefs.getBoolean(DolbyConstants.PREF_DEVICE_STATE_MEMORY, false)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var repository: DolbyRepository
    private lateinit var deviceStateManager: DeviceStateManager
    private var previousActiveDevice: AudioDeviceInfo? = null
    private var communicationBypassActive = false
    private var deviceChangePendingDuringCommunication = false

    private val restoreAfterCommunication = object : Runnable {
        override fun run() {
            if (!communicationBypassActive) return

            if (audioManager.mode != AudioManager.MODE_NORMAL) {
                Log.d(TAG, "Audio mode is ${audioManager.mode}, delaying Dolby restore")
                handler.postDelayed(this, COMMUNICATION_RESTORE_RETRY_MS)
                return
            }

            if (deviceChangePendingDuringCommunication) {
                Log.d(TAG, "Applying deferred output-device state after communication")
            }

            Log.d(TAG, "Communication route settle window ended, restoring Dolby state")
            repository.setCommunicationBypassActive(false)
            communicationBypassActive = false
            restoreCurrentDeviceState()
            deviceChangePendingDuringCommunication = false
        }
    }

    private val modeChangedListener = AudioManager.OnModeChangedListener { mode ->
        Log.d(TAG, "Audio mode changed: $mode")
        handler.removeCallbacks(restoreAfterCommunication)

        if (isCommunicationMode(mode)) {
            enterCommunicationBypass()
        } else if (mode == AudioManager.MODE_NORMAL && communicationBypassActive) {
            Log.d(
                TAG,
                "Communication ended, keeping Dolby bypassed for ${COMMUNICATION_RESTORE_DELAY_MS}ms"
            )
            handler.postDelayed(restoreAfterCommunication, COMMUNICATION_RESTORE_DELAY_MS)
        }
    }
    private var hadActivePlayback = false

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            Log.d(TAG, "Devices added: ${addedDevices.map { it.debugString() }}")
            if (communicationBypassActive) {
                deviceChangePendingDuringCommunication = true
                Log.d(TAG, "Deferring device restore while communication bypass is active")
                return
            }
            handleDeviceChange()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            Log.d(TAG, "Devices removed: ${removedDevices.map { it.debugString() }}")
            if (communicationBypassActive) {
                deviceChangePendingDuringCommunication = true
                Log.d(TAG, "Skipping device snapshot while communication bypass is active")
                return
            }

            if (isDeviceStateMemoryEnabled) {
                removedDevices.forEach { device ->
                    val key = deviceStateManager.deviceKey(device)
                    Log.d(TAG, "Snapshotting state for removed device: $key")
                    deviceStateManager.saveSnapshot(key, repository)
                }
            }
            handleDeviceChange()
        }
    }

    private val playbackCallback = object : AudioManager.AudioPlaybackCallback() {
        override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
            if (communicationBypassActive) {
                Log.d(TAG, "Skipping playback-triggered Dolby restore during communication bypass")
                return
            }

            val isActive = configs?.any { it.isActive } == true
            val playbackStarted = isActive && !hadActivePlayback
            hadActivePlayback = isActive
            if (getCurrentOutputDevice()?.id != previousActiveDevice?.id) {
                handleDeviceChange()
            } else if (playbackStarted) {
                repository.applySavedState()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = DolbyRepository(this)
        deviceStateManager = DeviceStateManager(this)

        communicationBypassActive = isCommunicationMode(audioManager.mode)
        repository.setCommunicationBypassActive(communicationBypassActive)

        val currentDevice = getCurrentOutputDevice()
        previousActiveDevice = currentDevice

        if (communicationBypassActive) {
            Log.d(TAG, "Service started in communication mode, bypassing Dolby")
            repository.applySavedState()
        } else {
            restoreCurrentDeviceState()
        }

        audioManager.registerAudioDeviceCallback(audioDeviceCallback, handler)
        audioManager.registerAudioPlaybackCallback(playbackCallback, handler)
        audioManager.addOnModeChangedListener(mainExecutor, modeChangedListener)
        Log.d(TAG, "Dolby effect service created")
    }

    private fun enterCommunicationBypass() {
        if (!communicationBypassActive) {
            Log.d(TAG, "Entering communication mode, bypassing Dolby without changing user state")
        }

        communicationBypassActive = true
        repository.setCommunicationBypassActive(true)
        repository.applySavedState()
    }

    private fun isCommunicationMode(mode: Int): Boolean {
        return mode == AudioManager.MODE_IN_COMMUNICATION ||
            mode == AudioManager.MODE_IN_CALL
    }

    private fun restoreCurrentDeviceState() {
        val currentDevice = getCurrentOutputDevice()

        if (currentDevice != null) {
            previousActiveDevice = currentDevice
            repository.updateSpeakerState()

            if (isDeviceStateMemoryEnabled) {
                val key = deviceStateManager.deviceKey(currentDevice)
                Log.d(TAG, "Restoring current device after communication: $key")
                val restored = deviceStateManager.restoreSnapshot(key, repository)
                if (!restored) {
                    repository.applySavedState()
                }
            } else {
                repository.applySavedState()
            }
        } else {
            repository.updateSpeakerState()
            repository.applySavedState()
            previousActiveDevice = null
        }
    }

    private fun handleDeviceChange() {
        if (communicationBypassActive) {
            deviceChangePendingDuringCommunication = true
            Log.d(TAG, "Deferring device change while communication bypass is active")
            return
        }

        val newDevice = getCurrentOutputDevice()
        val oldDevice = previousActiveDevice

        if (oldDevice != null && isDeviceStateMemoryEnabled) {
            val oldKey = deviceStateManager.deviceKey(oldDevice)
            Log.d(TAG, "Saving snapshot for previous device: $oldKey")
            deviceStateManager.saveSnapshot(oldKey, repository)
        }

        if (newDevice != null) {
            val newKey = deviceStateManager.deviceKey(newDevice)
            if (isDeviceStateMemoryEnabled) {
                Log.d(TAG, "Restoring snapshot for new device: $newKey")
                val restored = deviceStateManager.restoreSnapshot(newKey, repository)
                if (!restored) {
                    Log.d(TAG, "First time device, applying saved state as base")
                    repository.applySavedState()
                }
            } else {
                Log.d(TAG, "Device state memory disabled, applying saved state")
                repository.applySavedState()
            }
            previousActiveDevice = newDevice
        } else {
            repository.updateSpeakerState()
            repository.applySavedState()
            previousActiveDevice = null
        }
    }

    private fun getCurrentOutputDevice(): AudioDeviceInfo? {
        val routedDevice = try {
            audioManager
                .getDevicesForAttributes(ATTRIBUTES_MEDIA)
                .firstOrNull()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get active media route", e)
            null
        } ?: return null

        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val routedAddress = routedDevice.address.orEmpty()

        return outputs.firstOrNull { device ->
            device.isSink &&
                device.type == routedDevice.type &&
                (routedAddress.isEmpty() || device.address == routedAddress)
        } ?: outputs.firstOrNull { device ->
            device.isSink && device.type == routedDevice.type
        }.also { device ->
            if (device == null) {
                Log.w(
                    TAG,
                    "Unable to map active media route: " +
                        "type=${routedDevice.type}, address=${routedDevice.address}"
                )
            }
        }
    }

    private fun AudioDeviceInfo.debugString(): String =
        "name=$productName,type=$type,id=$id,address=$address,isSink=$isSink"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isCommunicationMode(audioManager.mode)) {
            enterCommunicationBypass()
        } else if (!communicationBypassActive) {
            repository.applySavedState()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(restoreAfterCommunication)

        if (!communicationBypassActive && isDeviceStateMemoryEnabled) {
            previousActiveDevice?.let { device ->
                val key = deviceStateManager.deviceKey(device)
                deviceStateManager.saveSnapshot(key, repository)
            }
        }

        audioManager.removeOnModeChangedListener(modeChangedListener)
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        audioManager.unregisterAudioPlaybackCallback(playbackCallback)
        repository.setCommunicationBypassActive(false)
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Dolby effect service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "DolbyEffectService"
        private const val COMMUNICATION_RESTORE_DELAY_MS = 8_000L
        private const val COMMUNICATION_RESTORE_RETRY_MS = 1_000L

        private val ATTRIBUTES_MEDIA = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        fun start(context: Context) {
            val intent = Intent(context, DolbyEffectService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, DolbyEffectService::class.java)
            context.stopService(intent)
        }
    }
}
