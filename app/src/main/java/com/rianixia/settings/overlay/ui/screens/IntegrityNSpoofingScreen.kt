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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rianixia.settings.overlay.R
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
                        SectionHeader(Icons.Rounded.Security, stringResource(R.string.pif_section_title))
                    }

                    item {
                        MaterialGlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            if (state.isLoading) {
                                // Simple skeleton loading for immediate feedback
                                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            } else {
                                // Primary Toggle
                                XinyaToggle(
                                    title = stringResource(R.string.pif_enable_title),
                                    subtitle = stringResource(R.string.pif_enable_desc),
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
                                        Text(stringResource(R.string.pif_auto_update), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        
                                        val modeStr = if (state.pifUpdateMode == PifUpdateMode.ON_REBOOT) stringResource(R.string.pif_mode_reboot) else stringResource(R.string.pif_mode_periodic)
                                        Text(
                                            if (state.isPifAutoUpdate) stringResource(R.string.pif_active_fmt, modeStr) else stringResource(R.string.status_disabled),
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
                                        Text(stringResource(R.string.pif_updating), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    } else {
                                        Icon(Icons.Rounded.SystemUpdateAlt, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.pif_update_now), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }
                    }

                    // ------------------------------------
                    // SECTION: Standard Spoofing
                    // ------------------------------------
                    item {
                        SectionHeader(Icons.Rounded.Masks, stringResource(R.string.spoof_general_title))
                    }

                    item {
                        MaterialGlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            XinyaToggle(
                                title = stringResource(R.string.spoof_photos_title),
                                subtitle = stringResource(R.string.spoof_photos_desc),
                                icon = Icons.Rounded.PhotoLibrary,
                                checked = state.isPhotosEnabled,
                                onCheckedChange = { viewModel.togglePhotosSpoof(it) }
                            )
                            MaterialDivider()
                            XinyaToggle(
                                title = stringResource(R.string.spoof_netflix_title),
                                subtitle = stringResource(R.string.spoof_netflix_desc),
                                icon = Icons.Rounded.Movie,
                                checked = state.isNetflixEnabled,
                                onCheckedChange = { viewModel.toggleNetflixSpoof(it) }
                            )
                        }
                    }

                    // ------------------------------------
                    // SECTION: Game Props (Redesigned)
                    // ------------------------------------
                    item { Spacer(Modifier.height(16.dp)) }

                    // Header & Control Panel
                    item {
                        GamePropsControlPanel(
                            enabled = state.isGamePropsEnabled,
                            onToggle = { viewModel.toggleGamePropsSpoof(it) },
                            onReset = { viewModel.resetToTemplate() },
                            onImport = { jsonImportLauncher.launch("application/json") },
                            onExport = { viewModel.exportPresetToDownloads() },
                            onAddProfile = { showAddProfileDialog = true }
                        )
                    }

                    // Profile List
                    if (state.isGamePropsEnabled) {
                        items(state.gameProfiles.size, key = { state.gameProfiles[it].key }) { index ->
                            val profile = state.gameProfiles[index]
                            RedesignedGameProfileCard(
                                profile = profile,
                                resolvedPackages = state.resolvedPackages,
                                onUpdateField = { f, v -> viewModel.updateProfileField(profile.key, f, v) },
                                onDelete = { viewModel.deleteProfile(profile.key) },
                                onAddPackage = { activeProfileForAddPkg = profile.key },
                                onRemovePackage = { pkg -> viewModel.removePackageFromProfile(profile.key, pkg) }
                            )
                        }
                    }
                    
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }

            // Header Overlay
            GradientBlurAppBar(
                title = stringResource(R.string.spoof_dashboard),
                icon = Icons.Rounded.Fingerprint,
                onBackClick = { navController.popBackStack() },
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Save FAB
            AnimatedVisibility(
                visible = state.hasUnsavedChanges,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .navigationBarsPadding() // Ensures it sits above system nav bar
            ) {
                FloatingActionButton(
                    onClick = { viewModel.saveGamePropsChanges() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Save, null)
                }
            }
        }
    }
    
    // Import Error Dialog
    if (state.showImportError) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportError() },
            icon = { Icon(Icons.Rounded.Error, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.dialog_import_failed_title)) },
            text = { Text(stringResource(R.string.dialog_import_failed_msg)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissImportError() }) { Text(stringResource(R.string.btn_ok)) }
            }
        )
    }
    
    // Dialogs
    if (showAddProfileDialog) {
        SimpleInputDialog(
            title = stringResource(R.string.dialog_new_profile_title),
            label = stringResource(R.string.dialog_new_profile_hint),
            onDismiss = { showAddProfileDialog = false },
            onConfirm = { 
                viewModel.addProfile(it)
                showAddProfileDialog = false
            }
        )
    }
    
    if (activeProfileForAddPkg != null) {
        if (state.isAppsLoading) {
            // Show loading dialog if apps aren't ready
            AlertDialog(
                onDismissRequest = { activeProfileForAddPkg = null },
                icon = { CircularProgressIndicator() },
                title = { Text("Loading Apps") },
                text = { Text("Please wait while we scan installed applications...") },
                confirmButton = {}
            )
        } else {
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
}

// ------------------------------------------------------------------------
// REDESIGNED COMPONENTS
// ------------------------------------------------------------------------

@Composable
private fun GamePropsControlPanel(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onReset: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onAddProfile: () -> Unit
) {
    MaterialGlassCard(
        modifier = Modifier.padding(horizontal = 16.dp),
        borderColor = if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else null
    ) {
        Column {
            // Header Row with Toggle
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled) MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Gamepad, 
                        null, 
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.spoof_game_title), 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.spoof_game_desc), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }

            // Global Actions & Add Profile (Only Visible when Enabled)
            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    MaterialDivider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Utility Actions
                        Row(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = onReset) { 
                                Icon(Icons.Rounded.RestartAlt, stringResource(R.string.btn_reset), tint = MaterialTheme.colorScheme.onSurfaceVariant) 
                            }
                            IconButton(onClick = onImport) { 
                                Icon(Icons.Rounded.UploadFile, stringResource(R.string.btn_import), tint = MaterialTheme.colorScheme.onSurfaceVariant) 
                            }
                            IconButton(onClick = onExport) { 
                                Icon(Icons.Rounded.SaveAlt, stringResource(R.string.btn_export), tint = MaterialTheme.colorScheme.primary) 
                            }
                        }

                        // Add Profile Action
                        Button(
                            onClick = onAddProfile,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_new_profile))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RedesignedGameProfileCard(
    profile: GamePropProfile,
    resolvedPackages: Map<String, com.rianixia.settings.overlay.ui.viewmodel.ResolvedPackage>,
    onUpdateField: (String, String) -> Unit,
    onDelete: () -> Unit,
    onAddPackage: () -> Unit,
    onRemovePackage: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")
    
    // Calculate active app count
    val installedCount = profile.packages.count { resolvedPackages[it]?.isInstalled == true }
    val totalCount = profile.packages.size

    MaterialGlassCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        containerColor = if(expanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        borderColor = if (expanded) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else null
    ) {
        Column {
            // Collapsed Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Identity Badge
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.key.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Info
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            profile.key, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "${profile.brand} ${profile.model} • $totalCount Apps ($installedCount active)",
                        style = MaterialTheme.typography.bodySmall,
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    
                    // SECTION: Device Identity (Technical Specs)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.spoof_identity_header),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { TechSpecField(stringResource(R.string.prop_brand), profile.brand) { onUpdateField("BRAND", it) } }
                            Box(Modifier.weight(1f)) { TechSpecField(stringResource(R.string.prop_manufacturer), profile.manufacturer) { onUpdateField("MANUFACTURER", it) } }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { TechSpecField(stringResource(R.string.prop_model), profile.model) { onUpdateField("MODEL", it) } }
                            Box(Modifier.weight(1f)) { TechSpecField(stringResource(R.string.prop_device), profile.device) { onUpdateField("DEVICE", it) } }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // SECTION: Target Apps
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.spoof_target_header),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Mini Add Button
                            TextButton(
                                onClick = onAddPackage,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_add_app), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // App List
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (profile.packages.isEmpty()) {
                                Text(
                                    stringResource(R.string.spoof_no_apps),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            profile.packages.forEach { pkg ->
                                val info = resolvedPackages[pkg]
                                val isInstalled = info?.isInstalled == true
                                
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isInstalled) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Indicator
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isInstalled) Color.Green.copy(alpha=0.7f) else MaterialTheme.colorScheme.outline.copy(alpha=0.5f))
                                    )
                                    
                                    Spacer(Modifier.width(12.dp))
                                    
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            if (isInstalled) info!!.label else pkg,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if(isInstalled) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if(isInstalled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f),
                                            maxLines = 1
                                        )
                                        if (isInstalled) {
                                            Text(
                                                pkg,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = { onRemovePackage(pkg) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.error.copy(alpha=0.7f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    
                    // Footer Actions
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                            .clickable { onDelete() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.btn_delete_profile),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TechSpecField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    Column {
        Text(
            label.uppercase(), 
            style = MaterialTheme.typography.labelSmall, 
            fontSize = 9.sp, 
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(2.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .padding(vertical = 8.dp, horizontal = 10.dp)
        )
    }
}

// Re-using previous helpers (SectionHeader, UpdateModeSelector, etc.)
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
                    text = if (mode == PifUpdateMode.ON_REBOOT) stringResource(R.string.pif_mode_reboot) else stringResource(R.string.pif_mode_periodic),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    // Legacy helper kept for potential reuse in other cards, but RedesignedGameProfileCard uses TechSpecField
    val focusManager = LocalFocusManager.current
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
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
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(label) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text(stringResource(R.string.btn_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } }
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
        title = { Text(stringResource(R.string.dialog_select_app)) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.dialog_search)) },
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
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } }
    )
}