package com.rianixia.settings.overlay.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class IoDevice(
    val name: String,
    val path: String,
    val currentScheduler: String,
    val availableSchedulers: List<String>
)

object IORepository {
    private const val TAG = "IORepo"

    suspend fun getIoDevices(): List<IoDevice> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<IoDevice>()
        val blockDir = File("/sys/block")
        
        blockDir.listFiles()?.forEach { dev ->
            val schedulerFile = File("${dev.absolutePath}/queue/scheduler")
            if (schedulerFile.exists()) {
                try {
                    val content = RootShell.readFile(schedulerFile.absolutePath)
                    if (content.isNotBlank()) {
                        val (current, available) = parseSchedulerString(content)
                        devices.add(IoDevice(dev.name, dev.absolutePath, current, available))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read scheduler for ${dev.name}", e)
                }
            }
        }
        devices.sortedBy { it.name }
    }

    suspend fun setScheduler(device: IoDevice, scheduler: String) = withContext(Dispatchers.IO) {
        RootShell.writeFile("${device.path}/queue/scheduler", scheduler)
    }

    suspend fun setAllSchedulers(scheduler: String) = withContext(Dispatchers.IO) {
        getIoDevices().forEach { device ->
            RootShell.writeFile("${device.path}/queue/scheduler", scheduler)
        }
    }

    private fun parseSchedulerString(content: String): Pair<String, List<String>> {
        val available = mutableListOf<String>()
        var current = "none"
        content.split(Regex("\\s+")).forEach { token ->
            if (token.startsWith("[") && token.endsWith("]")) {
                current = token.substring(1, token.length - 1)
                available.add(current)
            } else if (token.isNotBlank()) {
                available.add(token)
            }
        }
        return Pair(current, available)
    }
}
