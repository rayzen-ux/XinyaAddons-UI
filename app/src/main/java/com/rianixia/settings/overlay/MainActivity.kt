package com.rianixia.settings.overlay

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
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
                                
                                // CHANGED: New Integrity & Spoofing Screen
                                composable("integrity_spoofing") { IntegrityNSpoofingScreen(navController) }
                            }
                        }
                    }

                    if (isRoot) {
                        // App Bar - manages its own haze internally
                        GradientBlurAppBar(
                            title = ".method getAddons",
                            icon = Icons.Rounded.Extension,
                            onBackClick = { (context as? Activity)?.finish() },
                            hazeState = appBarHazeState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )

                        // Bottom Navigation - uses separate haze state
                        SlidingPillNavBar(
                            pagerState = pagerState,
                            items = listOf(
                                Triple("Home", "Home", Icons.Rounded.Home),
                                Triple("Performance", "Perf", Icons.Rounded.Speed),
                                Triple("System", "Sys", Icons.Rounded.SettingsInputComponent)
                            ),
                            onItemClick = { index ->
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            hazeState = navBarHazeState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                        )
                    }
                }
            }
        }
    }
}