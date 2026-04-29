package com.rianixia.settings.overlay

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rianixia.settings.overlay.data.AppPreferences
import com.rianixia.settings.overlay.ui.components.*
import com.rianixia.settings.overlay.ui.screens.*
import com.rianixia.settings.overlay.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Edge-to-Edge to allow drawing behind status/nav bars
        enableEdgeToEdge()
        
        setContent {
            XinyaTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                
                // Determine Start Destination based on First Run
                val isFirstRun = remember { AppPreferences.isFirstRun(context) }
                val startDestination = if (isFirstRun) "setup_wizard" else "root_pager"
                
                // Separate haze states - app bar manages its own blur internally
                val appBarHazeState = remember { HazeState() }
                val navBarHazeState = remember { HazeState() }
                
                val pagerState = rememberPagerState(pageCount = { 3 })
                val scope = rememberCoroutineScope()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                // Main root pager identification
                val isRoot = currentDestination?.route == "root_pager"
                // Hide bars during setup wizard
                val isSetup = currentDestination?.route == "setup_wizard"

                // Overflow Menu State
                var showMenu by remember { mutableStateOf(false) }

                // --- Navigation Bar Positioning Logic ---
                val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val bottomNavPadding = if (navBarInset > 30.dp) 24.dp + navBarInset else 24.dp
                // ----------------------------------------

                BackHandler {
                    if (isRoot) {
                        (context as? Activity)?.finish()
                    } else if (!isSetup) {
                        // Allow back nav unless we are in setup wizard
                        navController.popBackStack()
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    // [NEW] Global Background Layer
                    // This sits behind the Scaffold and persists across pager swipes
                    GlobalBackground()

                    Scaffold(
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        // Content Box
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                // [REMOVED] Background color to allow GlobalBackground to show through
                                // Only apply navbar haze source if we are not in setup wizard to avoid weird blurring on plain bg
                                .let { if (!isSetup) it.hazeSource(state = navBarHazeState) else it }
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                modifier = Modifier.fillMaxSize(),
                                enterTransition = { 
                                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300))
                                },
                                exitTransition = { 
                                    slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut(tween(300))
                                },
                                popEnterTransition = { 
                                    slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeIn(tween(300))
                                },
                                popExitTransition = { 
                                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(tween(300))
                                }
                            ) {
                                // NEW: Setup Wizard Route
                                composable("setup_wizard") {
                                    SetupWizardScreen(navController)
                                }

                                composable(
                                    route = "root_pager",
                                    enterTransition = { fadeIn(tween(300)) },
                                    exitTransition = { fadeOut(tween(300)) }
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        when(page) {
                                            0 -> HomeScreen(navController)
                                            1 -> PerformanceScreen(navController)
                                            2 -> SystemScreen(navController)
                                        }
                                    }
                                }
                                
                                composable("cpu_control") { CpuControlScreen(navController) }
                                composable("undervolt") { UndervoltScreen(navController) }
                                composable("halo_lighting") { HaloLightingScreen(navController) }
                                composable("screen_reso") { ScreenResoScreen(navController) }
                                composable("battery_center") { BatteryCenterScreen(navController) }
                                composable("io_scheduler") { IoSchedulerScreen(navController) }
                                composable("thermal_control") { ThermalControlScreen(navController) }
                                composable("azenith") { AZenithScreen(navController) }
                                composable("game_boost") { AZenithScreen(navController) }
                                composable("integrity_spoofing") { IntegrityNSpoofingScreen(navController) }
                                
                                composable("about") { AboutScreen(navController) }
                                composable("settings") { SettingsScreen(navController) }
                            }
                        }
                    }

                    if (isRoot) {
                        // App Bar with Overflow Menu
                        GradientBlurAppBar(
                            title = stringResource(R.string.app_title_toolkit),
                            icon = Icons.Rounded.Extension,
                            onBackClick = { (context as? Activity)?.finish() },
                            hazeState = appBarHazeState,
                            modifier = Modifier.align(Alignment.TopCenter),
                            actions = {
                                Box {
                                    Surface(
                                        onClick = { showMenu = true },
                                        shape = CircleShape,
                                        color = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ) {
                                        Box(
                                            modifier = Modifier.size(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Rounded.MoreVert, null)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        offset = DpOffset(0.dp, 8.dp),
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.menu_settings)) },
                                            onClick = { 
                                                showMenu = false
                                                navController.navigate("settings") 
                                            },
                                            leadingIcon = { Icon(Icons.Rounded.Settings, null, modifier = Modifier.size(20.dp)) }
                                        )
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.menu_about)) },
                                            onClick = { 
                                                showMenu = false
                                                navController.navigate("about") 
                                            },
                                            leadingIcon = { Icon(Icons.Rounded.Info, null, modifier = Modifier.size(20.dp)) }
                                        )
                                    }
                                }
                            }
                        )

                        // Bottom Navigation
                        SlidingPillNavBar(
                            pagerState = pagerState,
                            items = listOf(
                                Triple(stringResource(R.string.nav_home), stringResource(R.string.nav_home), Icons.Rounded.Home),
                                Triple(stringResource(R.string.nav_perf), stringResource(R.string.nav_perf), Icons.Rounded.Speed),
                                Triple(stringResource(R.string.nav_sys), stringResource(R.string.nav_sys), Icons.Rounded.SettingsInputComponent)
                            ),
                            onItemClick = { index ->
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            hazeState = navBarHazeState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = bottomNavPadding)
                        )
                    }
                }
            }
        }
    }

    /**
     * Checks for the required ROM property.
     * Returns true if either Oplus or Nothing OS prop contains "xia", false otherwise.
     */
    private fun checkRomEnvironment(): Boolean {
        return try {
            // Check Oplus Prop
            if (getSystemProperty("ro.build.version.oplusrom.display").contains("xia")) {
                return true
            }
            // Check Nothing Prop
            if (getSystemProperty("ro.nothing.version.id").contains("xia")) {
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
