package com.rianixia.settings.overlay.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class IoDevice(
    val name: String,
    val path: String, // Kept for UI compatibility, will be generated
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
     * Reads the device list populated by the external AIO Controller.
     * No sysfs scanning is performed by the app.
     */
    suspend fun getIoDevices(): List<IoDevice> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<IoDevice>()
        
        // 1. Read the list of devices (e.g., "mmcblk0,sda,sdb")
        val deviceListStr = getProp(PROP_DEVICE_LIST, "")
        if (deviceListStr.isBlank()) {
            Log.w(TAG, "No devices found in prop: $PROP_DEVICE_LIST. Is AIO Controller running?")
            return@withContext emptyList()
        }

        val deviceNames = deviceListStr.split(",").filter { it.isNotBlank() }
        Log.d(TAG, "AIO Controller reported devices: $deviceNames")

        // 2. Fetch details for each device
        deviceNames.forEach { name ->
            // Read prop: persist.sys.rianixia.io.dev.mmcblk0
            // Expected format: "[mq-deadline] none kyber" OR "none"
            val schedString = getProp("$PROP_DEV_PREFIX$name", "")
            
            if (schedString.isNotBlank()) {
                val (current, available) = parseSchedulerString(schedString)
                // Construct IoDevice object
                // We fake the path since we can't read /sys/block anyway, 
                // but the UI might want to display something technical.
                val fakePath = "/sys/block/$name"
                devices.add(IoDevice(name, fakePath, current, available))
            } else {
                Log.w(TAG, "Missing scheduler info for $name")
            }
        }

        devices.sortedBy { it.name }
    }

    private fun parseSchedulerString(content: String): Pair<String, List<String>> {
        val available = mutableListOf<String>()
        var current = "none" // Default if brackets missing
        
        // Handle "none [mq-deadline] kyber"
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

    fun getTargetScheduler(): String {
        return getProp(PROP_TARGET_SCHED, "")
    }

    fun setTargetScheduler(scheduler: String) {
        // App only updates the "Target" property.
        // AIO Controller is responsible for watching this prop and applying it.
        Log.i(TAG, "Requesting global scheduler change: $scheduler")
        setProp(PROP_TARGET_SCHED, scheduler)
    }

    private fun getProp(key: String, default: String): String {
        return try {
            val p = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            reader.readLine()?.trim()?.takeIf { it.isNotEmpty() } ?: default
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read prop $key", e)
            default
        }
    }

    private fun setProp(key: String, value: String) {
        try {
            // Depending on permissions, this might need 'su', 
            // but standard 'setprop' usually works if the prop is not restricted.
            // If it fails, fallback to root wrapper.
            Runtime.getRuntime().exec("setprop $key $value")
        } catch (e: Exception) {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "setprop $key $value"))
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to set prop $key", e2)
            }
        }
    }
}