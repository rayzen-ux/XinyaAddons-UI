package com.rianixia.settings.overlay.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap?,
    val isEnabled: Boolean
)

data class AZenithState(
    val isGlobalEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val apps: List<AppItem> = emptyList()
)

class AZenithViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AZenithState())
    val uiState: StateFlow<AZenithState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("azenith_config", Context.MODE_PRIVATE)

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            // Load global switch state
            val global = prefs.getBoolean("global_enabled", false)
            _uiState.update { it.copy(isGlobalEnabled = global) }

            // Load Apps on IO thread
            val appList = withContext(Dispatchers.IO) {
                loadInstalledApps()
            }
            
            _uiState.update { 
                it.copy(apps = appList, isLoading = false) 
            }
        }
    }

    private fun loadInstalledApps(): List<AppItem> {
        val pm = getApplication<Application>().packageManager
        // Query installed apps
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return installed.filter { appInfo ->
            // Filter out system apps, keep only user apps
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.map { appInfo ->
            val label = pm.getApplicationLabel(appInfo).toString()
            val pkg = appInfo.packageName
            val icon = pm.getApplicationIcon(appInfo).toBitmap().asImageBitmap()
            val enabled = prefs.getBoolean("app_$pkg", false) // Default to false

            AppItem(label, pkg, icon, enabled)
        }.sortedBy { it.label }
    }

    fun toggleGlobal(enabled: Boolean) {
        prefs.edit().putBoolean("global_enabled", enabled).apply()
        _uiState.update { it.copy(isGlobalEnabled = enabled) }
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("app_$packageName", enabled).apply()
        
        _uiState.update { state ->
            val updatedApps = state.apps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isEnabled = enabled)
                } else {
                    app
                }
            }
            state.copy(apps = updatedApps)
        }
    }

    // Helper to convert Drawable to Bitmap for Compose
    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return this.bitmap
        
        val bitmap = Bitmap.createBitmap(
            intrinsicWidth.coerceAtLeast(1),
            intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}