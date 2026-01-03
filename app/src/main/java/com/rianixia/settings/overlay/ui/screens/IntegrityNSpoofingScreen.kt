package com.rianixia.settings.overlay.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rianixia.settings.overlay.ui.components.*
import com.rianixia.settings.overlay.ui.viewmodel.GamePropProfile
import com.rianixia.settings.overlay.ui.viewmodel.InstalledAppInfo
import com.rianixia.settings.overlay.ui.viewmodel.IntegritySpoofViewModel
import com.rianixia.settings.overlay.ui.viewmodel.PifUpdateMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun IntegrityNSpoofingScreen(
    navController: NavController,
    viewModel: IntegritySpoofViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val hazeState = remember { HazeState() }
    
    // Dialog States
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var activeProfileForAddPkg by remember { mutableStateOf<String?>(null) }
    
    // Import Launcher
    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importPreset(it) }
    }

    MaterialGlassScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) {
                BouncyLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 100.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ------------------------------------
                    // SECTION: Play Integrity Fix
                    // ------------------------------------
                    item {
                        SectionHeader(Icons.Rounded.Security, "Play Integrity Fix")
                    }

                    item {
                        MaterialGlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            // Primary Toggle
                            XinyaToggle(
                                title = "Enable PIF Module",
                                subtitle = "Injects integrity fix props",
                                icon = Icons.Rounded.Shield,
                                checked = state.isPifEnabled,
                                onCheckedChange = { viewModel.togglePif(it) }
                            )
                            
                            MaterialDivider()
                            
                            // Auto Update Configuration
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Update, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.onSurface, 
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Auto-Update", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        if (state.isPifAutoUpdate) "Active: ${state.pifUpdateMode}" else "Disabled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = state.isPifAutoUpdate,
                                    onCheckedChange = { viewModel.togglePifAutoUpdate(it) }
                                )
                            }

                            AnimatedVisibility(visible = state.isPifAutoUpdate) {
                                Column(Modifier.padding(start = 40.dp, top = 8.dp, bottom = 8.dp)) {
                                    UpdateModeSelector(
                                        currentMode = state.pifUpdateMode,
                                        onModeSelect = { viewModel.setPifUpdateMode(it) }
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            // Manual Action
                            Button(
                                onClick = { viewModel.triggerPifUpdate() },
                                enabled = !state.isPifUpdating,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (state.isPifUpdating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Updating...", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                } else {
                                    Icon(Icons.Rounded.SystemUpdateAlt, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Update PIF Fingerprint Now", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }

                    // ------------------------------------
                    // SECTION: Standard Spoofing
                    // ------------------------------------
                    item {
                        SectionHeader(Icons.Rounded.Masks, "General Spoofing")
                    }

                    item {
                        MaterialGlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            XinyaToggle(
                                title = "Google Photos Unlimited",
                                subtitle = "Spoofs Pixel XL for original quality backup",
                                icon = Icons.Rounded.PhotoLibrary,
                                checked = state.isPhotosEnabled,
                                onCheckedChange = { viewModel.togglePhotosSpoof(it) }
                            )
                            MaterialDivider()
                            XinyaToggle(
                                title = "Netflix HDR Unlock",
                                subtitle = "Spoofs model for Widevine L1/HDR",
                                icon = Icons.Rounded.Movie,
                                checked = state.isNetflixEnabled,
                                onCheckedChange = { viewModel.toggleNetflixSpoof(it) }
                            )
                        }
                    }

                    // ------------------------------------
                    // SECTION: Game Props (JSON Driven)
                    // ------------------------------------
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 24.dp), 
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SectionHeader(Icons.Rounded.Gamepad, "Game Props (JSON)", Modifier.weight(1f))
                            IconButton(
                                onClick = { showAddProfileDialog = true },
                                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Add, "Add Profile", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
                    // Master Toggle
                    item {
                        MaterialGlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            XinyaToggle(
                                title = "Enable Game Props Spoof",
                                subtitle = "Applies profiles below to target apps",
                                icon = Icons.Rounded.SportsEsports,
                                checked = state.isGamePropsEnabled,
                                onCheckedChange = { viewModel.toggleGamePropsSpoof(it) }
                            )
                            
                            MaterialDivider()
                            
                            // Preset Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(onClick = { viewModel.resetToTemplate() }) {
                                    Icon(Icons.Rounded.RestartAlt, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Reset")
                                }
                                TextButton(onClick = { jsonImportLauncher.launch("application/json") }) {
                                    Icon(Icons.Rounded.UploadFile, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Import")
                                }
                                TextButton(onClick = { viewModel.exportPresetToDownloads() }) {
                                    Icon(Icons.Rounded.SaveAlt, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Save")
                                }
                            }
                        }
                    }

                    items(state.gameProfiles.size, key = { state.gameProfiles[it].key }) { index ->
                        val profile = state.gameProfiles[index]
                        GameProfileCard(
                            profile = profile,
                            resolvedPackages = state.resolvedPackages,
                            onUpdateField = { f, v -> viewModel.updateProfileField(profile.key, f, v) },
                            onDelete = { viewModel.deleteProfile(profile.key) },
                            onAddPackage = { activeProfileForAddPkg = profile.key },
                            onRemovePackage = { pkg -> viewModel.removePackageFromProfile(profile.key, pkg) }
                        )
                    }
                }
            }

            // Header Overlay
            GradientBlurAppBar(
                title = "Integrity & Spoofing",
                icon = Icons.Rounded.Fingerprint,
                onBackClick = { navController.popBackStack() },
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
    
    // Dialogs
    if (showAddProfileDialog) {
        SimpleInputDialog(
            title = "New Device Profile",
            label = "Profile ID (e.g. ROG6)",
            onDismiss = { showAddProfileDialog = false },
            onConfirm = { 
                viewModel.addProfile(it)
                showAddProfileDialog = false
            }
        )
    }
    
    if (activeProfileForAddPkg != null) {
        AppSelectionDialog(
            apps = state.installedApps,
            onDismiss = { activeProfileForAddPkg = null },
            onPackageSelected = { pkg ->
                viewModel.addPackageToProfile(activeProfileForAddPkg!!, pkg)
                activeProfileForAddPkg = null
            }
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier.padding(start = 24.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UpdateModeSelector(currentMode: PifUpdateMode, onModeSelect: (PifUpdateMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PifUpdateMode.values().forEach { mode ->
            val selected = mode == currentMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.secondaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clickable { onModeSelect(mode) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (mode == PifUpdateMode.ON_REBOOT) "On Reboot" else "Periodic",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GameProfileCard(
    profile: GamePropProfile,
    resolvedPackages: Map<String, com.rianixia.settings.overlay.ui.viewmodel.ResolvedPackage>,
    onUpdateField: (String, String) -> Unit,
    onDelete: () -> Unit,
    onAddPackage: () -> Unit,
    onRemovePackage: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")

    MaterialGlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        // Card Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(bottom = if (expanded) 12.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha=0.4f)), 
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile.key.take(2).uppercase(), 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.key, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${profile.packages.size} target apps", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.ExpandMore, 
                null, 
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Expanded Content
        AnimatedVisibility(visible = expanded) {
            Column {
                MaterialDivider()
                
                // Device Fields
                GlassTextField(label = "Brand", value = profile.brand, onValueChange = { onUpdateField("BRAND", it) })
                GlassTextField(label = "Manufacturer", value = profile.manufacturer, onValueChange = { onUpdateField("MANUFACTURER", it) })
                GlassTextField(label = "Model", value = profile.model, onValueChange = { onUpdateField("MODEL", it) })
                GlassTextField(label = "Device (Opt)", value = profile.device, onValueChange = { onUpdateField("DEVICE", it) })
                
                Spacer(Modifier.height(16.dp))
                
                // Package List
                Text(
                    "Target Packages",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    profile.packages.forEach { pkg ->
                        val info = resolvedPackages[pkg]
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                if (info != null && info.isInstalled) {
                                    Text(info.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(pkg, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Text(pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Not Installed", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                }
                            }
                            IconButton(onClick = { onRemovePackage(pkg) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    // Add Package Button
                    Button(
                        onClick = onAddPackage,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), contentColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Target Package")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Delete Profile
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha=0.3f))
                ) {
                    Text("Delete Profile")
                }
            }
        }
    }
}

@Composable
private fun GlassTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        )
    }
}

@Composable
private fun SimpleInputDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AppSelectionDialog(
    apps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onPackageSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Application") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )
                
                Box(Modifier.height(300.dp)) {
                    LazyColumn {
                        items(filteredApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPackageSelected(app.packageName) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(app.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}