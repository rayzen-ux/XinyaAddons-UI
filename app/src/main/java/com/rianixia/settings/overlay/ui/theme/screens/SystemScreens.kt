package com.rianixia.settings.overlay.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rianixia.settings.overlay.R
import com.rianixia.settings.overlay.ui.components.*

// ==========================================
// ROOT SYSTEM DASHBOARD
// ==========================================

@Composable
fun SystemScreen(navController: NavController) {
    MaterialGlassScaffold {
        BouncyLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header
            item {
                Column(Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                    Text(
                        stringResource(R.string.system_core),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        stringResource(R.string.hardware_matrix),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 2. Primary Module: Identity Matrix
            item {
                IdentityMatrixCard(onClick = { navController.navigate("spoof_dashboard") })
            }

            // 3. Hardware Grid (2x2)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Halo Visuals
                    SystemModuleCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.vis_fx),
                        title = stringResource(R.string.halo_light),
                        icon = Icons.Rounded.Stream,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = { navController.navigate("halo_lighting") }
                    )
                    // Display Config
                    SystemModuleCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.disp_cfg),
                        title = stringResource(R.string.resolution),
                        icon = Icons.Rounded.DisplaySettings,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { navController.navigate("screen_reso") }
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Thermal Engine
                    SystemModuleCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.hw_therm),
                        title = stringResource(R.string.thermal),
                        icon = Icons.Rounded.Whatshot,
                        color = MaterialTheme.colorScheme.error,
                        onClick = { navController.navigate("thermal_control") }
                    )
                    // Power Source
                    SystemModuleCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.pwr_src),
                        title = stringResource(R.string.battery),
                        icon = Icons.Rounded.BatteryStd,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("battery_center") }
                    )
                }
            }

            // 4. Kernel Flags (Toggle Bank)
            item {
                SystemPropsBank()
            }
        }
    }
}

// ==========================================
// DASHBOARD COMPONENTS
// ==========================================

@Composable
fun IdentityMatrixCard(onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val border = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    // Animation for scanning line
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "y"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(28.dp))
            .clickable { onClick() }
    ) {
        // Scanner Visual
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
            val yPos = size.height * scanY
            drawLine(
                color = color,
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 2.dp.toPx()
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0f), color.copy(alpha = 0.5f)),
                    startY = yPos - 50f,
                    endY = yPos
                ),
                topLeft = Offset(0f, yPos - 50f),
                size = androidx.compose.ui.geometry.Size(size.width, 50f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.id_matrix_label),
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black, 
                    color = color
                )
                Icon(Icons.Rounded.Fingerprint, null, tint = color)
            }

            Column {
                Text(
                    stringResource(R.string.identity_spoofing),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.identity_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SystemModuleCard(
    modifier: Modifier,
    label: String,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.8f))
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SystemPropsBank() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            stringResource(R.string.kernel_flags),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        XinyaToggle(
            title = stringResource(R.string.secure_flag),
            subtitle = stringResource(R.string.secure_flag_desc),
            icon = Icons.Rounded.Screenshot,
            checked = false,
            onCheckedChange = {},
            isRisk = true
        )
        
        MaterialDivider()
        
        XinyaToggle(
            title = stringResource(R.string.force_rotation),
            subtitle = stringResource(R.string.force_rotation_desc),
            icon = Icons.Rounded.ScreenRotation,
            checked = true,
            onCheckedChange = {}
        )
    }
}

// ==========================================
// SUB-SCREENS
// ==========================================

@Composable
fun SpoofDashboardScreen(navController: NavController) {
    MaterialGlassScaffold {
        BouncyLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 80.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SubScreenHeader(stringResource(R.string.spoof_dashboard)) { navController.popBackStack() } }
            item { MaterialGlassCard(header = stringResource(R.string.pif_title)) { XinyaToggle(stringResource(R.string.auto_update_pif), stringResource(R.string.danda_updater), Icons.Rounded.Autorenew, true, {}) } }
        }
    }
}

@Composable
fun HaloLightingScreen(navController: NavController) {
    MaterialGlassScaffold {
        BouncyLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 80.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SubScreenHeader(stringResource(R.string.halo_light)) { navController.popBackStack() } }
            item { MaterialGlassCard { Text(stringResource(R.string.preview), color = MaterialTheme.colorScheme.onSurface) } }
        }
    }
}

@Composable
fun ScreenResoScreen(navController: NavController) {
    MaterialGlassScaffold {
        BouncyLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 80.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SubScreenHeader(stringResource(R.string.resolution)) { navController.popBackStack() } }
            item { MaterialGlassCard(header = stringResource(R.string.resolution)) { Text("FHD+", color = MaterialTheme.colorScheme.onSurface) } }
        }
    }
}