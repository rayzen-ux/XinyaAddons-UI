package com.rianixia.settings.overlay.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class SafetyResetReceiver : BroadcastReceiver() {
    
    companion object {
        private const val PROP_SAFETY_MODE = "persist.sys.rianixia.safe_mode_state"
        // Prefixes to target for reset
        private val DANGEROUS_PREFIXES = listOf(
            "persist.sys.rianixia.io.",
            "persist.sys.rianixia.cpu.",
            "persist.sys.rianixia.uv."
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (isSafetyModeEnabled()) {
                Log.w("XinyaSafety", "Safety Mode Active! Resetting dangerous configurations...")
                resetDangerousProps()
            }
        }
    }

    private fun isSafetyModeEnabled(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec("getprop $PROP_SAFETY_MODE")
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val result = reader.readLine()?.trim()
            result == "1"
        } catch (e: Exception) {
            false
        }
    }

    private fun resetDangerousProps() {
        // Since we can't easily "list" props without parsing `getprop` output,
        // and we can't "delete" persist props easily without root/resetprop,
        // we will attempt to set critical known sub-props to safe defaults or empty.
        
        // Note: This logic assumes the app has root access or system permissions to write these props.
        // If not, this serves as the logic layer that would interface with a root shell.
        
        val resetCommands = mutableListOf<String>()
        
        // 1. Reset IO Scheduler target
        resetCommands.add("setprop persist.sys.rianixia.io.scheduler \"\"")
        
        // 2. Reset CPU Governors and Modes
        resetCommands.add("setprop persist.sys.rianixia.cpu.mode \"\"")
        resetCommands.add("setprop persist.sys.rianixia.cpu.gov \"\"")
        resetCommands.add("setprop persist.sys.rianixia.cpu.saver \"\"")
        
        // 3. Reset Undervolts (Assuming UV keys follow the uv prefix)
        // We iterate generic keys or just clear the main switch if exists
        resetCommands.add("setprop persist.sys.rianixia.uv.enabled 0")
        
        // Execute resets
        resetCommands.forEach { cmd ->
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            } catch (e: Exception) {
                Log.e("XinyaSafety", "Failed to execute safety reset: $cmd", e)
            }
        }
    }
}