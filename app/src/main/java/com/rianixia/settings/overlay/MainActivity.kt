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
import com.rianixia.settings.overlay.ui.components.*
import com.rianixia.settings.overlay.ui.screens.*
import com.rianixia.settings.overlay.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

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
                
                // Separate haze states - app bar manages its own blur internally
                val appBarHazeState = remember { HazeState() }
                val navBarHazeState = remember { HazeState() }
                
                val pagerState = rememberPagerState(pageCount = { 3 })
                val scope = rememberCoroutineScope()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val isRoot = currentDestination?.route == "root_pager"

                // Overflow Menu State
                var showMenu by remember { mutableStateOf(false) }

                // --- Navigation Bar Positioning Logic ---
                // We use WindowInsets to determine if the user is using 3-button nav or gestures.
                // 3-Button Nav typically has a large bottom inset (> 30dp).
                // Gesture Nav typically has a small handle or 0 inset (~16-24dp).
                val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                
                // If inset is large (Buttons), raise the navbar by adding the inset.
                // If inset is small (Gestures), keep the default 24.dp position.
                val bottomNavPadding = if (navBarInset > 30.dp) 24.dp + navBarInset else 24.dp
                // ----------------------------------------

                BackHandler {
                    if (isRoot) {
                        (context as? Activity)?.finish()
                    } else {
                        navController.popBackStack()
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    Scaffold(
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        // Content Box: Removed .padding(innerPadding) to fix black/white status bar issues
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                // Only apply navbar haze source
                                .hazeSource(state = navBarHazeState)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = "root_pager",
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
                                
                                // NEW: About and Settings
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

                                    // Dropdown Menu using material components styled to match glass look
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

                        // Bottom Navigation with Dynamic Padding
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
                                .padding(bottom = bottomNavPadding) // Applied dynamic padding here
                        )
                    }
                }
            }
        }
    }
}