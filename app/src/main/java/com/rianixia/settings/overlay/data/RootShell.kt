package com.rianixia.settings.overlay.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream

object RootShell {
    suspend fun exec(vararg commands: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            for (cmd in commands) {
                os.writeBytes("$cmd\n")
            }
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setProp(key: String, value: String): Boolean {
        return exec("setprop $key $value")
    }

    suspend fun writeFile(path: String, value: String): Boolean {
        return exec("echo '$value' > $path")
    }

    suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
            process.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) { "" }
    }
}
