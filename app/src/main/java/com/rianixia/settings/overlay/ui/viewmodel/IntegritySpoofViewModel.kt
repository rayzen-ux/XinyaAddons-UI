package com.rianixia.settings.overlay.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GamePropProfile(
    val key: String,
    var brand: String,
    var manufacturer: String,
    var model: String,
    var device: String = "",
    var packages: MutableList<String> = mutableListOf()
)

data class ResolvedPackage(
    val packageName: String,
    val label: String,
    val isInstalled: Boolean
)

data class InstalledAppInfo(
    val label: String,
    val packageName: String
)

data class IntegritySpoofState(
    val isPifEnabled: Boolean = false,
    val isPifAutoUpdate: Boolean = false,
    val pifUpdateMode: PifUpdateMode = PifUpdateMode.ON_REBOOT,
    val isPifUpdating: Boolean = false,
    
    val isPhotosEnabled: Boolean = false,
    val isNetflixEnabled: Boolean = false,
    
    // Game Props State
    val isGamePropsEnabled: Boolean = false,
    val gameProfiles: List<GamePropProfile> = emptyList(),
    val resolvedPackages: Map<String, ResolvedPackage> = emptyMap(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    
    val hasUnsavedChanges: Boolean = false, // New flag for pending saves
    
    val showImportError: Boolean = false,
    val isLoading: Boolean = true,
    val isAppsLoading: Boolean = true
)

enum class PifUpdateMode {
    ON_REBOOT,
    PERIODIC
}

class IntegritySpoofViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(IntegritySpoofState())
    val uiState: StateFlow<IntegritySpoofState> = _uiState.asStateFlow()

    private val gamePropsFile = File(application.filesDir, "gameprops.json")
    
    // Manual Modifications Required:
    // None. The code is self-contained. 

    // System Properties Constants
    private object Props {
        const val PIF_ENABLE = "persist.sys.rianixia.pif.enable"
        const val PIF_AUTO_UPDATE = "persist.sys.rianixia.pif-auto"
        const val PIF_AUTO_INTERVAL = "persist.sys.rianixia.pif-auto.interval"
        
        const val PHOTOS_ENABLE = "persist.sys.rianixia.photos.unlimited"
        const val NETFLIX_ENABLE = "persist.sys.rianixia.netflix.unlock"
        const val GAME_ENABLE = "persist.sys.rianixia.game.props"
        const val GAME_CHANGED = "persist.sys.rianixia.game.changed"
    }

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            // 1. Fast Load
            val sysProps = withContext(Dispatchers.IO) {
                mapOf(
                    "pif" to getSystemProp(Props.PIF_ENABLE),
                    "auto" to getSystemProp(Props.PIF_AUTO_UPDATE),
                    "interval" to getSystemProp(Props.PIF_AUTO_INTERVAL),
                    "photos" to getSystemProp(Props.PHOTOS_ENABLE),
                    "netflix" to getSystemProp(Props.NETFLIX_ENABLE),
                    "game" to getSystemProp(Props.GAME_ENABLE)
                )
            }

            val intervalVal = sysProps["interval"] ?: "reboot"
            val mode = if (intervalVal.contains("periodic", ignoreCase = true)) {
                PifUpdateMode.PERIODIC
            } else {
                PifUpdateMode.ON_REBOOT
            }

            _uiState.update {
                it.copy(
                    isPifEnabled = sysProps["pif"] == "1" || sysProps["pif"] == "true",
                    isPifAutoUpdate = sysProps["auto"] == "true" || sysProps["auto"] == "1",
                    pifUpdateMode = mode,
                    isPhotosEnabled = sysProps["photos"] == "1" || sysProps["photos"] == "true",
                    isNetflixEnabled = sysProps["netflix"] == "1" || sysProps["netflix"] == "true",
                    isGamePropsEnabled = sysProps["game"] == "1" || sysProps["game"] == "true",
                    isLoading = false,
                    hasUnsavedChanges = false // Ensure clean state on load
                )
            }

            // 2. Heavy Load
            launch(Dispatchers.IO) {
                ensureGamePropsFileExists()
                val profiles = readGamePropsJson()
                val resolved = resolvePackageInfo(profiles)
                
                _uiState.update { 
                    it.copy(gameProfiles = profiles, resolvedPackages = resolved) 
                }

                val allApps = loadAllInstalledApps()
                _uiState.update { 
                    it.copy(installedApps = allApps, isAppsLoading = false) 
                }
            }
        }
    }

    // ==========================================
    // System Prop Helpers
    // ==========================================

    private fun getSystemProp(key: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            process.inputStream.bufferedReader().use { it.readText().trim() }
        } catch (e: Exception) {
            ""
        }
    }

    private fun setSystemProp(key: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Runtime.getRuntime().exec("setprop $key $value")
            } catch (e: Exception) {
                Log.e("IntegrityViewModel", "Failed to set prop $key", e)
            }
        }
    }

    // ==========================================
    // File Management
    // ==========================================

    private fun ensureGamePropsFileExists() {
        if (!gamePropsFile.exists()) {
            copyAssetToInternal()
        }
    }

    private fun copyAssetToInternal() {
        try {
            val assetManager = getApplication<Application>().assets
            val assets = assetManager.list("")
            if (assets?.contains("gameprops.json") == true) {
                assetManager.open("gameprops.json").use { input ->
                    gamePropsFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                gamePropsFile.writeText("{}")
            }
        } catch (e: Exception) {
            if(!gamePropsFile.exists()) gamePropsFile.writeText("{}")
        }
    }

    private fun readGamePropsJson(): List<GamePropProfile> {
        val profiles = mutableListOf<GamePropProfile>()
        try {
            if (!gamePropsFile.exists()) return emptyList()
            val content = gamePropsFile.readText()
            if (content.isBlank()) return emptyList()

            val json = JSONObject(content)
            json.keys().forEach { key ->
                val obj = json.getJSONObject(key)
                val pkgsJson = obj.optJSONArray("PKGNAMES") ?: JSONArray()
                val pkgList = mutableListOf<String>()
                for (i in 0 until pkgsJson.length()) {
                    pkgList.add(pkgsJson.getString(i))
                }
                profiles.add(
                    GamePropProfile(
                        key = key,
                        brand = obj.optString("BRAND", ""),
                        manufacturer = obj.optString("MANUFACTURER", ""),
                        model = obj.optString("MODEL", ""),
                        device = obj.optString("DEVICE", ""),
                        packages = pkgList
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("IntegrityViewModel", "Error reading gameprops.json", e)
        }
        return profiles
    }

    private fun writeGamePropsJson(profiles: List<GamePropProfile>) {
        try {
            val root = JSONObject()
            profiles.forEach { profile ->
                val obj = JSONObject()
                obj.put("BRAND", profile.brand)
                obj.put("MANUFACTURER", profile.manufacturer)
                obj.put("MODEL", profile.model)
                if (profile.device.isNotEmpty()) obj.put("DEVICE", profile.device)
                val pkgArray = JSONArray()
                profile.packages.forEach { pkgArray.put(it) }
                obj.put("PKGNAMES", pkgArray)
                root.put(profile.key, obj)
            }
            gamePropsFile.writeText(root.toString(2))
            
            notifyGamePropsChanged()
            
        } catch (e: IOException) {
            Log.e("IntegrityViewModel", "Error writing gameprops.json", e)
        }
    }

    private fun notifyGamePropsChanged() {
        setSystemProp(Props.GAME_CHANGED, "false")
        setSystemProp(Props.GAME_CHANGED, "true")
    }

    // ==========================================
    // Import / Export / Reset / Save
    // ==========================================

    // NEW: Explicit Save Function
    fun saveGamePropsChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentProfiles = _uiState.value.gameProfiles
            writeGamePropsJson(currentProfiles)
            _uiState.update { it.copy(hasUnsavedChanges = false) }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "Changes saved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun exportPresetToDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Determine if we should export the FILE on disk or the current STATE
                // Typically export exports the current state.
                val profiles = _uiState.value.gameProfiles
                // Construct JSON from state
                val root = JSONObject()
                profiles.forEach { profile ->
                    val obj = JSONObject()
                    obj.put("BRAND", profile.brand)
                    obj.put("MANUFACTURER", profile.manufacturer)
                    obj.put("MODEL", profile.model)
                    if (profile.device.isNotEmpty()) obj.put("DEVICE", profile.device)
                    val pkgArray = JSONArray()
                    profile.packages.forEach { pkgArray.put(it) }
                    obj.put("PKGNAMES", pkgArray)
                    root.put(profile.key, obj)
                }

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val destFile = File(downloadsDir, "gameprops_export_$timestamp.json")
                
                destFile.writeText(root.toString(2))
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Saved to Downloads/gameprops_export_$timestamp.json", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("IntegrityViewModel", "Export failed", e)
            }
        }
    }

    fun importPreset(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = getApplication<Application>().contentResolver
                contentResolver.openInputStream(uri)?.use { input ->
                    val content = input.bufferedReader().use { it.readText() }
                    JSONObject(content) // Validate
                    gamePropsFile.writeText(content)
                }

                val profiles = readGamePropsJson()
                val resolved = resolvePackageInfo(profiles)
                _uiState.update { 
                    it.copy(
                        gameProfiles = profiles, 
                        resolvedPackages = resolved,
                        hasUnsavedChanges = false // Import writes immediately, so no pending changes
                    ) 
                }
                notifyGamePropsChanged()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Preset imported successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("IntegrityViewModel", "Import failed", e)
                _uiState.update { it.copy(showImportError = true) }
            }
        }
    }

    fun dismissImportError() {
        _uiState.update { it.copy(showImportError = false) }
    }

    fun resetToTemplate() {
        viewModelScope.launch(Dispatchers.IO) {
            copyAssetToInternal()
            val profiles = readGamePropsJson()
            val resolved = resolvePackageInfo(profiles)
            _uiState.update { 
                it.copy(
                    gameProfiles = profiles, 
                    resolvedPackages = resolved,
                    hasUnsavedChanges = false
                ) 
            }
            notifyGamePropsChanged()
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "Reset to default template", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // Actions
    // ==========================================

    fun togglePif(enabled: Boolean) {
        _uiState.update { it.copy(isPifEnabled = enabled) }
        setSystemProp(Props.PIF_ENABLE, if(enabled) "1" else "0")
    }

    fun togglePifAutoUpdate(enabled: Boolean) {
        _uiState.update { it.copy(isPifAutoUpdate = enabled) }
        setSystemProp(Props.PIF_AUTO_UPDATE, if(enabled) "true" else "false")
    }

    fun setPifUpdateMode(mode: PifUpdateMode) {
        _uiState.update { it.copy(pifUpdateMode = mode) }
        val value = if (mode == PifUpdateMode.ON_REBOOT) "reboot" else "periodic"
        setSystemProp(Props.PIF_AUTO_INTERVAL, value)
    }

    fun triggerPifUpdate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPifUpdating = true) }
            withContext(Dispatchers.IO) { Thread.sleep(2000) }
            _uiState.update { it.copy(isPifUpdating = false) }
        }
    }

    fun togglePhotosSpoof(enabled: Boolean) {
        _uiState.update { it.copy(isPhotosEnabled = enabled) }
        setSystemProp(Props.PHOTOS_ENABLE, if(enabled) "1" else "0")
    }

    fun toggleNetflixSpoof(enabled: Boolean) {
        _uiState.update { it.copy(isNetflixEnabled = enabled) }
        setSystemProp(Props.NETFLIX_ENABLE, if(enabled) "1" else "0")
    }

    fun toggleGamePropsSpoof(enabled: Boolean) {
        _uiState.update { it.copy(isGamePropsEnabled = enabled) }
        setSystemProp(Props.GAME_ENABLE, if(enabled) "1" else "0")
    }

    // ==========================================
    // Profile Management Helpers (Memory Only)
    // ==========================================

    private fun loadAllInstalledApps(): List<InstalledAppInfo> {
        val pm = getApplication<Application>().packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.map {
            InstalledAppInfo(
                label = pm.getApplicationLabel(it).toString(),
                packageName = it.packageName
            )
        }.sortedBy { it.label.lowercase() }
    }

    private fun resolvePackageInfo(profiles: List<GamePropProfile>): Map<String, ResolvedPackage> {
        val pm = getApplication<Application>().packageManager
        val map = HashMap<String, ResolvedPackage>()
        profiles.flatMap { it.packages }.distinct().forEach { pkg ->
            try {
                val info = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(info).toString()
                map[pkg] = ResolvedPackage(pkg, label, true)
            } catch (e: PackageManager.NameNotFoundException) {
                map[pkg] = ResolvedPackage(pkg, pkg, false)
            }
        }
        return map
    }

    fun addProfile(key: String) {
        val currentList = _uiState.value.gameProfiles.toMutableList()
        if (currentList.any { it.key == key }) return
        val newProfile = GamePropProfile(key, "Generic", "Generic", "Model X", "", mutableListOf())
        currentList.add(newProfile)
        updateLocalState(currentList)
    }

    fun updateProfileField(key: String, field: String, value: String) {
        val currentList = _uiState.value.gameProfiles.map { profile ->
            if (profile.key == key) {
                when (field) {
                    "BRAND" -> profile.copy(brand = value)
                    "MANUFACTURER" -> profile.copy(manufacturer = value)
                    "MODEL" -> profile.copy(model = value)
                    "DEVICE" -> profile.copy(device = value)
                    else -> profile
                }
            } else profile
        }
        updateLocalState(currentList)
    }

    fun addPackageToProfile(profileKey: String, packageName: String) {
        if (packageName.isBlank()) return
        val currentList = _uiState.value.gameProfiles.map { profile ->
            if (profile.key == profileKey && !profile.packages.contains(packageName)) {
                val newPkgs = profile.packages.toMutableList()
                newPkgs.add(packageName)
                profile.copy(packages = newPkgs)
            } else profile
        }
        updateLocalState(currentList)
    }

    fun removePackageFromProfile(profileKey: String, packageName: String) {
        val currentList = _uiState.value.gameProfiles.map { profile ->
            if (profile.key == profileKey) {
                val newPkgs = profile.packages.toMutableList()
                newPkgs.remove(packageName)
                profile.copy(packages = newPkgs)
            } else profile
        }
        updateLocalState(currentList)
    }

    fun deleteProfile(key: String) {
        val currentList = _uiState.value.gameProfiles.filter { it.key != key }
        updateLocalState(currentList)
    }

    // UPDATED: Updates Memory State Only - Doesn't Write to File
    private fun updateLocalState(profiles: List<GamePropProfile>) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolved = resolvePackageInfo(profiles)
            _uiState.update { 
                it.copy(
                    gameProfiles = profiles, 
                    resolvedPackages = resolved,
                    hasUnsavedChanges = true // Mark as modified
                ) 
            }
        }
    }
}