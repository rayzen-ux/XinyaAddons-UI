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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rianixia.settings.overlay.ui.components.*
import com.rianixia.settings.overlay.ui.screens.*
import com.rianixia.settings.overlay.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XinyaTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                
                // Initialize Haze State
                val hazeState = remember { HazeState() }
                
                val pagerState = rememberPagerState(pageCount = { 3 })
                val scope = rememberCoroutineScope()
                
                var isBarsVisible by remember { mutableStateOf(true) }
                
                val nestedScrollConnection = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            if (available.y < 0) isBarsVisible = false
                            else if (available.y > 0) isBarsVisible = true
                            return Offset.Zero
                        }
                    }
                }

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
                        containerColor = Color.Transparent,
                        modifier = Modifier.nestedScroll(nestedScrollConnection)
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "root_pager",
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                // [FIX] Explicitly set background BEFORE hazeSource
                                .background(MaterialTheme.colorScheme.background)
                                // [FIX] Apply hazeSource without parameters (it picks up the layer below)
                                .hazeSource(state = hazeState),
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
                            composable("spoof_dashboard") { SpoofDashboardScreen(navController) }
                            composable("battery_center") { BatteryCenterScreen(navController) }
                            composable("io_scheduler") { IoSchedulerScreen(navController) }
                            composable("thermal_control") { ThermalControlScreen(navController) }
                            composable("azenith") { AZenithScreen(navController) }
                            composable("game_boost") { AZenithScreen(navController) }
                        }
                    }

                    if (isRoot) {
                        AnimatedVisibility(
                            visible = isBarsVisible,
                            enter = slideInVertically { -it } + fadeIn(),
                            exit = slideOutVertically { -it } + fadeOut(),
                            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = { (context as? Activity)?.finish() },
                                    shape = CircleShape,
                                    shadowElevation = 8.dp,
                                    color = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .hazeEffect(
                                            state = hazeState,
                                            style = HazeStyle(
                                                blurRadius = 24.dp,
                                                tint = HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                                                noiseFactor = 0.05f
                                            )
                                        )
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = ".method getAddons",
                                    style = MaterialTheme.typography.headlineSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.Rounded.Extension,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isBarsVisible,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                        ) {
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
                                hazeState = hazeState
                            )
                        }
                    }
                }
            }
        }
    }
}