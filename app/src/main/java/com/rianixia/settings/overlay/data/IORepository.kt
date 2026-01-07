package com.rianixia.settings.overlay.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class IoDevice(
    val name: String,
    val path: String,
    val currentScheduler: String,
    val availableSchedulers: List<String>
)

object IORepository {
    private const val TAG = "IORepo"
    
    // Property Keys
    private const val PROP_DEVICE_LIST = "persist.sys.rianixia.io.devices"
    private const val PROP_DEV_PREFIX = "persist.sys.rianixia.io.dev."
    private const val PROP_TARGET_SCHED = "persist.sys.rianixia.io.scheduler"

    /**
     * Reads the device list and their states from system properties.
     * Acts as the single source of truth for the UI.
     */
    suspend fun getIoDevices(): List<IoDevice> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<IoDevice>()
        
        // 1. Read the list of devices (e.g., "mmcblk0,sda,sdb")
        val deviceListStr = getProp(PROP_DEVICE_LIST, "")
        if (deviceListStr.isBlank()) {
            return@withContext emptyList()
        }

        val deviceNames = deviceListStr.split(",").filter { it.isNotBlank() }

        // 2. Fetch details for each device from properties
        deviceNames.forEach { name ->
            // Read prop: persist.sys.rianixia.io.dev.<name>
            // Format: "[mq-deadline] none kyber"
            val schedString = getProp("$PROP_DEV_PREFIX$name", "")
            
            if (schedString.isNotBlank()) {
                val (current, available) = parseSchedulerString(schedString)
                // Generate path for UI display (technical decoration only)
                val fakePath = "/sys/block/$name"
                devices.add(IoDevice(name, fakePath, current, available))
            }
        }

        devices.sortedBy { it.name }
    }

    /**
     * Parses the property value to identify active and available schedulers.
     * Logic: The token enclosed in [] is active.
     */
    private fun parseSchedulerString(content: String): Pair<String, List<String>> {
        val available = mutableListOf<String>()
        var current = "none" // Default fallback
        
        val tokens = content.split(Regex("\\s+"))
        tokens.forEach { token ->
            if (token.startsWith("[") && token.endsWith("]")) {
                val clean = token.substring(1, token.length - 1)
                current = clean
                available.add(clean)
            } else {
                available.add(token)
            }
        }
        return Pair(current, available)
    }

    suspend fun getTargetScheduler(): String = withContext(Dispatchers.IO) {
        getProp(PROP_TARGET_SCHED, "")
    }

    /**
     * Sets the global target scheduler property.
     * Does NOT update internal state or return success/fail.
     * The UI must wait for the Native Controller to reflect this change in getIoDevices().
     */
    suspend fun setTargetScheduler(scheduler: String) = withContext(Dispatchers.IO) {
        Log.i(TAG, "Setting global target scheduler property: $scheduler")
        setProp(PROP_TARGET_SCHED, scheduler)
    }

    private fun getProp(key: String, default: String): String {
        return try {
            val p = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            reader.readLine()?.trim()?.takeIf { it.isNotEmpty() } ?: default
        } catch (e: Exception) {
            default
        }
    }

    private fun setProp(key: String, value: String) {
        try {
            // Standard shell execution for system/root property setting
            Runtime.getRuntime().exec("setprop $key $value")
        } catch (e: Exception) {
            try {
                // Fallback to su if standard shell lacks permission context
                Runtime.getRuntime().exec(arrayOf("su", "-c", "setprop $key $value"))
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to set prop $key", e2)
            }
        }
    }
}