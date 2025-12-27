package com.rianixia.settings.overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rianixia.settings.overlay.ui.screens.*
import com.rianixia.settings.overlay.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XinyaTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        // Only show BottomBar on top-level destinations
                        val topLevelRoutes = listOf("home", "performance", "gaming", "system")
                        val isTopLevel = currentDestination?.route in topLevelRoutes
                        
                        if (isTopLevel) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                val items = listOf(
                                    Screen.Home,
                                    Screen.Performance,
                                    Screen.Gaming,
                                    Screen.System
                                )
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = { Text(screen.title) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("performance") { PerformanceScreen(navController) }
                        composable("gaming") { GamingScreen(navController) }
                        composable("system") { SystemScreen(navController) }
                        
                        // Deep Links (Sub Screens)
                        composable("cpu_control") { CpuControlScreen(navController) }
                        composable("undervolt") { UndervoltScreen(navController) }
                        composable("halo_lighting") { HaloLightingScreen(navController) }
                        composable("screen_reso") { ScreenResoScreen(navController) }
                        composable("game_boost") { GameBoostScreen(navController) }
                        composable("spoof_dashboard") { SpoofDashboardScreen(navController) }
                        composable("autocut") { AutoCutScreen(navController) }
                        composable("io_scheduler") { IoSchedulerScreen(navController) }
                        composable("thermal_control") { ThermalControlScreen(navController) }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Performance : Screen("performance", "Perf", Icons.Rounded.Speed)
    object Gaming : Screen("gaming", "Game", Icons.Rounded.Gamepad)
    object System : Screen("system", "Sys", Icons.Rounded.SettingsInputComponent)
}