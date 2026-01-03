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
    
    val showImportError: Boolean = false,
    val isLoading: Boolean = true
)

enum class PifUpdateMode {
    ON_REBOOT,
    PERIODIC
}

class IntegritySpoofViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(IntegritySpoofState())
    val uiState: StateFlow<IntegritySpoofState> = _uiState.asStateFlow()

    private val gamePropsFile = File(application.filesDir, "gameprops.json")
    private val prefs = application.getSharedPreferences("integrity_config", Context.MODE_PRIVATE)

    // System Properties Constants
    private object Props {
        const val PIF_ENABLE = "persist.sys.rianixia.integrity.play_fix"
        const val PHOTOS_ENABLE = "persist.sys.rianixia.integrity.photos_unlimited"
        const val NETFLIX_ENABLE = "persist.sys.rianixia.integrity.netflix_unlock"
        const val GAME_ENABLE = "persist.sys.rianixia.integrity.game_props"
        const val GAME_CHANGED = "persist.sys.rianixia.integrity.gameprops-changed"
    }

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ensureGamePropsFileExists()
            }

            // Load UI state from Prefs (Persist UI state locally)
            val pifEnabled = prefs.getBoolean("pif_enabled", false)
            val pifAuto = prefs.getBoolean("pif_auto_update", false)
            val pifModeIdx = prefs.getInt("pif_update_mode", 0)
            val photos = prefs.getBoolean("photos_enabled", false)
            val netflix = prefs.getBoolean("netflix_enabled", false)
            val gamePropsEnabled = prefs.getBoolean("gameprops_enabled", false)

            // Initial Prop Sync
            setSystemProp(Props.PIF_ENABLE, if(pifEnabled) "1" else "0")
            setSystemProp(Props.PHOTOS_ENABLE, if(photos) "1" else "0")
            setSystemProp(Props.NETFLIX_ENABLE, if(netflix) "1" else "0")
            setSystemProp(Props.GAME_ENABLE, if(gamePropsEnabled) "1" else "0")

            val profiles = withContext(Dispatchers.IO) { readGamePropsJson() }
            val resolved = withContext(Dispatchers.IO) { resolvePackageInfo(profiles) }
            val allApps = withContext(Dispatchers.IO) { loadAllInstalledApps() }

            _uiState.update {
                it.copy(
                    isPifEnabled = pifEnabled,
                    isPifAutoUpdate = pifAuto,
                    pifUpdateMode = PifUpdateMode.values().getOrElse(pifModeIdx) { PifUpdateMode.ON_REBOOT },
                    isPhotosEnabled = photos, // UPDATED NAME
                    isNetflixEnabled = netflix, // UPDATED NAME
                    isGamePropsEnabled = gamePropsEnabled,
                    gameProfiles = profiles,
                    resolvedPackages = resolved,
                    installedApps = allApps,
                    isLoading = false
                )
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
            
            // Notify AI Service
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
    // Import / Export / Reset
    // ==========================================

    fun exportPresetToDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!gamePropsFile.exists()) return@launch
                
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val destFile = File(downloadsDir, "gameprops_export_$timestamp.json")
                
                gamePropsFile.copyTo(destFile, overwrite = true)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Saved to Downloads/gameprops_export_$timestamp.json", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("IntegrityViewModel", "Export failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun importPreset(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = getApplication<Application>().contentResolver
                contentResolver.openInputStream(uri)?.use { input ->
                    val content = input.bufferedReader().use { it.readText() }
                    // Validate JSON
                    JSONObject(content) 
                    
                    // Write to internal file
                    gamePropsFile.writeText(content)
                }

                // Refresh State
                val profiles = readGamePropsJson()
                val resolved = resolvePackageInfo(profiles)
                _uiState.update { 
                    it.copy(gameProfiles = profiles, resolvedPackages = resolved) 
                }
                
                // Trigger Update
                notifyGamePropsChanged()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Preset imported successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("IntegrityViewModel", "Import failed", e)
                // Trigger the error dialog state instead of a simple toast
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
                it.copy(gameProfiles = profiles, resolvedPackages = resolved) 
            }
            notifyGamePropsChanged()
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "Reset to default template", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // Actions & Prop Binding
    // ==========================================

    fun togglePif(enabled: Boolean) {
        prefs.edit().putBoolean("pif_enabled", enabled).apply()
        _uiState.update { it.copy(isPifEnabled = enabled) }
        setSystemProp(Props.PIF_ENABLE, if(enabled) "1" else "0")
    }

    fun togglePifAutoUpdate(enabled: Boolean) {
        prefs.edit().putBoolean("pif_auto_update", enabled).apply()
        _uiState.update { it.copy(isPifAutoUpdate = enabled) }
    }

    fun setPifUpdateMode(mode: PifUpdateMode) {
        prefs.edit().putInt("pif_update_mode", mode.ordinal).apply()
        _uiState.update { it.copy(pifUpdateMode = mode) }
    }

    fun triggerPifUpdate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPifUpdating = true) }
            withContext(Dispatchers.IO) { Thread.sleep(2000) }
            _uiState.update { it.copy(isPifUpdating = false) }
        }
    }

    fun togglePhotosSpoof(enabled: Boolean) {
        prefs.edit().putBoolean("photos_enabled", enabled).apply()
        _uiState.update { it.copy(isPhotosEnabled = enabled) } // UPDATED NAME
        setSystemProp(Props.PHOTOS_ENABLE, if(enabled) "1" else "0")
    }

    fun toggleNetflixSpoof(enabled: Boolean) {
        prefs.edit().putBoolean("netflix_enabled", enabled).apply()
        _uiState.update { it.copy(isNetflixEnabled = enabled) } // UPDATED NAME
        setSystemProp(Props.NETFLIX_ENABLE, if(enabled) "1" else "0")
    }

    fun toggleGamePropsSpoof(enabled: Boolean) {
        prefs.edit().putBoolean("gameprops_enabled", enabled).apply()
        _uiState.update { it.copy(isGamePropsEnabled = enabled) }
        setSystemProp(Props.GAME_ENABLE, if(enabled) "1" else "0")
    }

    private fun setSystemProp(key: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Assuming standard root shell execution
                Runtime.getRuntime().exec("setprop $key $value")
            } catch (e: Exception) {
                Log.e("IntegrityViewModel", "Failed to set prop $key", e)
            }
        }
    }

    // ==========================================
    // Profile Management & Helper
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
        saveAndRefresh(currentList)
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
        saveAndRefresh(currentList)
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
        saveAndRefresh(currentList)
    }

    fun removePackageFromProfile(profileKey: String, packageName: String) {
        val currentList = _uiState.value.gameProfiles.map { profile ->
            if (profile.key == profileKey) {
                val newPkgs = profile.packages.toMutableList()
                newPkgs.remove(packageName)
                profile.copy(packages = newPkgs)
            } else profile
        }
        saveAndRefresh(currentList)
    }

    fun deleteProfile(key: String) {
        val currentList = _uiState.value.gameProfiles.filter { it.key != key }
        saveAndRefresh(currentList)
    }

    private fun saveAndRefresh(profiles: List<GamePropProfile>) {
        viewModelScope.launch(Dispatchers.IO) {
            writeGamePropsJson(profiles)
            val resolved = resolvePackageInfo(profiles)
            _uiState.update { it.copy(gameProfiles = profiles, resolvedPackages = resolved) }
        }
    }
}