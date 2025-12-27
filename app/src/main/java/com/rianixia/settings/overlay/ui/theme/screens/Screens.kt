package com.rianixia.settings.overlay.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rianixia.settings.overlay.ui.components.*

// ==========================================
// MAIN TABS
// ==========================================

@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(Modifier.padding(8.dp)) {
                Text(
                    "SNAPDRAGON 8 GEN 3", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Rianixia Prototype X1", 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), 
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    StatusBadge("100% HEALTH", MaterialTheme.colorScheme.tertiary)
                    StatusBadge("32°C", Color(0xFF64B5F6))
                    StatusBadge("AC POWER", MaterialTheme.colorScheme.primary)
                    StatusBadge("120Hz LTPO", MaterialTheme.colorScheme.secondary)
                }
            }
        }

        item {
            XinyaCard(header = "Live Load Monitor") {
                MockLiveGraph(MaterialTheme.colorScheme.primary, 2.0f)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Active Governor", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Text("schedutil", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Text("Peak Freq", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Text("3.30 GHz", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickCard(
                    Modifier.weight(1f).clickable { navController.navigate("cpu_control") }, 
                    "PERFORMANCE", 
                    "CPU Control", 
                    Icons.Rounded.Speed, 
                    MaterialTheme.colorScheme.primary
                )
                QuickCard(
                    Modifier.weight(1f).clickable { navController.navigate("game_boost") }, 
                    "GAMING", 
                    "AZenith Boost", 
                    Icons.Rounded.RocketLaunch, 
                    MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            XinyaCard(header = "System Status Snapshot") {
                SnapshotRow("Screen", "1440p @ 120Hz", Icons.Rounded.Monitor)
                Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
                SnapshotRow("Thermal", "Adaptive Mode", Icons.Rounded.Thermostat)
                Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
                SnapshotRow("Charging", "AutoCut Enabled", Icons.Rounded.BatteryChargingFull)
            }
        }
    }
}

@Composable
fun PerformanceScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader("Performance Engine", "System core & clock management") }
        item { 
            NavRow("CPU Clock & Governor", "Per-cluster frequency control", Icons.Rounded.Memory) { navController.navigate("cpu_control") }
        }
        item { 
            NavRow("Undervolt Control", "Voltage offset & efficiency", Icons.Rounded.Bolt) { navController.navigate("undervolt") }
        }
        item { 
            NavRow("I/O Scheduler", "Storage queue management", Icons.Rounded.Storage) { navController.navigate("io_scheduler") }
        }
    }
}

@Composable
fun GamingScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader("Gaming Hub", "Environment & identity spoofing") }
        item { 
            NavRow("AZenith Game Boost", "Performance profile injection", Icons.Rounded.RocketLaunch) { navController.navigate("game_boost") }
        }
        item { 
            NavRow("HaloLighting", "Mecha-style visual notifications", Icons.Rounded.Stream) { navController.navigate("halo_lighting") }
        }
    }
}

@Composable
fun SystemScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader("System Engine", "Low-level UI & hardware control") }
        item { 
            NavRow("Identity & Spoofing", "PIF, Netflix, & Device Props", Icons.Rounded.Fingerprint) { navController.navigate("spoof_dashboard") }
        }
        item { 
            NavRow("Thermal Control", "Protection & throttling limits", Icons.Rounded.Whatshot) { navController.navigate("thermal_control") }
        }
        item { 
            NavRow("ScreenReso", "Resolution & density control", Icons.Rounded.DisplaySettings) { navController.navigate("screen_reso") }
        }
        item { 
            NavRow("AutoCut Config", "Charging protection rules", Icons.Rounded.ElectricBolt) { navController.navigate("autocut") }
        }
        item {
            XinyaCard(header = "System Props") {
                 XinyaToggle("Disable FLAG_SECURE", "Allow screenshots in restricted apps", Icons.Rounded.Screenshot, false, {}, isRisk = true)
                 Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 4.dp))
                 XinyaToggle("Remove Tap-to-Rotate", "Hide rotation button in navbar", Icons.Rounded.ScreenRotation, true, {})
            }
        }
    }
}

// ==========================================
// SUB SCREENS (DEEP LINKS)
// ==========================================

@Composable
fun SpoofDashboardScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("Identity Matrix") { navController.popBackStack() } }
        
        item {
            XinyaCard(header = "Play Integrity Fix (PIF)") {
                XinyaToggle("Auto-Update PIF", "Danda's auto-json updater", Icons.Rounded.Autorenew, true, {})
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Check for Updates Now")
                }
            }
        }

        item {
            XinyaCard(header = "Media & Storage Spoof") {
                XinyaToggle("Spoof Netflix", "Force L1 Widevine & HDR", Icons.Rounded.Movie, true, {})
                Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 4.dp))
                XinyaToggle("Spoof Google Photos", "Unlimited storage flags", Icons.Rounded.PhotoLibrary, true, {})
            }
        }

        item {
            XinyaCard(header = "GameProps: Device Mapping") {
                var selectedDevice by remember { mutableStateOf("ROG Phone 8") }
                val devices = listOf("ROG Phone 8", "RedMagic 9 Pro", "Black Shark 5", "Custom")
                
                devices.forEach { device ->
                     Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selectedDevice = device }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                             Text(device, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                             if(device == "Custom") Text("Edit manual props below", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(
                            selected = selectedDevice == device, 
                            onClick = { selectedDevice = device },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                
                if(selectedDevice == "Custom") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = "Xiaomi 14 Ultra",
                        onValueChange = {},
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Device Profile")
                }
            }
        }
    }
}

@Composable
fun CpuControlScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("CPU Control") { navController.popBackStack() } }
        item {
            XinyaCard(header = "Governor Selection") {
                var selectedGov by remember { mutableStateOf("schedutil") }
                val govs = listOf("schedutil", "performance", "powersave", "conservative")
                govs.forEach { gov ->
                    Row(
                        Modifier.fillMaxWidth().clickable { selectedGov = gov }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(gov, color = if(selectedGov == gov) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        if(selectedGov == gov) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            XinyaCard(header = "Cluster 0 (Little)") {
                XinyaSlider(value = 1.8f, onValueChange = {}, valueRange = 0.3f..2.2f, label = "Max Freq", suffix = " GHz")
            }
        }
        item {
            XinyaCard(header = "Cluster 1 (Big)") {
                XinyaSlider(value = 3.3f, onValueChange = {}, valueRange = 0.8f..3.3f, label = "Max Freq", suffix = " GHz")
            }
        }
    }
}

@Composable
fun UndervoltScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("Advanced Undervolt") { navController.popBackStack() } }
        item {
            WarningCard("Improper voltage settings will cause System of Death (SOD). Adjust in small increments.")
        }
        item {
            XinyaCard(header = "CPU EEM Offsets") {
                XinyaSlider(value = -50f, onValueChange = {}, valueRange = -120f..0f, label = "Little Core", suffix = " mV")
                XinyaSlider(value = -35f, onValueChange = {}, valueRange = -100f..0f, label = "Big Core", suffix = " mV")
                XinyaSlider(value = -25f, onValueChange = {}, valueRange = -100f..0f, label = "Prime Core", suffix = " mV")
            }
        }
        item {
            XinyaCard(header = "GPU EEM Offsets") {
                XinyaSlider(value = -25f, onValueChange = {}, valueRange = -50f..0f, label = "Adreno Offset", suffix = " mV")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {}, 
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text("Reset") }
                Button(
                    onClick = {}, 
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Apply") }
            }
        }
    }
}

@Composable
fun ThermalControlScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("Thermal Engine") { navController.popBackStack() } }
        
        item {
            XinyaCard(header = "Operation Mode") {
                val modes = listOf("Adaptive (Default)", "Performance", "Disabled (Dangerous)")
                var selectedMode by remember { mutableStateOf("Adaptive (Default)") }
                
                modes.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().clickable { selectedMode = mode }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            mode, 
                            color = if(mode.contains("Dangerous")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        RadioButton(selected = selectedMode == mode, onClick = { selectedMode = mode })
                    }
                }
            }
        }

        item {
             XinyaCard(header = "Manual Control") {
                 var expanded by remember { mutableStateOf(false) }
                 Column {
                     XinyaSlider(value = 45f, onValueChange = {}, valueRange = 35f..60f, label = "Throttle Start", suffix = "°C")
                     
                     Spacer(Modifier.height(8.dp))
                     Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
                         Text("Advanced Config", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                         Icon(if(expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = MaterialTheme.colorScheme.primary)
                     }
                     
                     if(expanded) {
                         Spacer(Modifier.height(16.dp))
                         XinyaSlider(value = 90f, onValueChange = {}, valueRange = 80f..110f, label = "Emergency Cutoff", suffix = "°C")
                     }
                 }
             }
        }
    }
}

@Composable
fun HaloLightingScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("HaloLighting") { navController.popBackStack() } }
        item {
            XinyaCard {
                Box(
                    Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(80.dp).border(4.dp, Brush.sweepGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)), CircleShape))
                    Text("PREVIEW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            XinyaCard(header = "Active Mode") {
                val modes = listOf("Static", "Breathe", "Reactive", "Rainbow")
                modes.forEach { mode ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(mode, color = MaterialTheme.colorScheme.onSurface)
                        RadioButton(
                            selected = mode == "Breathe", 
                            onClick = {},
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenResoScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("ScreenReso") { navController.popBackStack() } }
        item {
            XinyaCard(header = "Resolution") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResoChip(Modifier.weight(1f), "FHD+", "1080p", false)
                    ResoChip(Modifier.weight(1f), "QHD+", "1440p", true)
                }
            }
        }
        item {
            XinyaCard(header = "Density Control") {
                XinyaSlider(value = 480f, onValueChange = {}, valueRange = 320f..640f, label = "DPI Value")
            }
        }
    }
}

@Composable
fun GameBoostScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("AZenith Boost") { navController.popBackStack() } }
        item {
            XinyaCard(header = "Active Profiles") {
                BoostItem("Genshin Impact", true)
                BoostItem("PUBG Mobile", false)
                BoostItem("Call of Duty", true)
            }
        }
    }
}

@Composable
fun AutoCutScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("AutoCut Config") { navController.popBackStack() } }
        item {
            XinyaCard(header = "Global Settings") {
                XinyaToggle("Global Bypass Charging", "Separate power from battery", Icons.Rounded.BatteryChargingFull, true, {})
                
                Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
                
                Text("Advanced Threshold", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                XinyaSlider(value = 80f, onValueChange = {}, valueRange = 50f..100f, label = "Stop Charging At", suffix = "%")
                
                Spacer(Modifier.height(8.dp))
                Text(
                    "Logic: Power bypass activates when battery ≥ 80%. Charging resumes if battery drops below this limit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            XinyaCard(header = "AI Smart Mode") {
                XinyaToggle("AI AutoCut", "Learn usage patterns", Icons.Rounded.AutoAwesome, false, {})
            }
        }
    }
}

@Composable
fun IoSchedulerScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SubScreenHeader("I/O Scheduler") { navController.popBackStack() } }
        item {
            XinyaCard(header = "Active Scheduler") {
                listOf("mq-deadline", "kyber", "bfq", "none").forEach { sch ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(sch, color = if(sch == "mq-deadline") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        if(sch == "mq-deadline") StatusBadge("SYSTEM DEFAULT", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ==========================================
// UTILS
// ==========================================

@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun NavRow(title: String, sub: String, icon: ImageVector, onClick: () -> Unit) {
    XinyaCard(modifier = Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickCard(modifier: Modifier, label: String, title: String, icon: ImageVector, accent: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Black)
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SnapshotRow(label: String, value: String, icon: ImageVector) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BoostItem(app: String, active: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
        Spacer(Modifier.width(12.dp))
        Text(app, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        XinyaSwitch(active, {})
    }
}

@Composable
fun ResoChip(modifier: Modifier, title: String, sub: String, selected: Boolean) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MockLiveGraph(color: Color, speed: Float) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(0f, size.height / 2)
        for (i in 0..size.width.toInt() step 20) {
            val y = (size.height / 2) + (Math.sin(i.toDouble() / 50) * 30).toFloat()
            path.lineTo(i.toFloat(), y)
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
    }
}