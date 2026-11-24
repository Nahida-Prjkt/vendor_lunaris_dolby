/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import org.lunaris.dolby.ui.components.FloatingNavToolbar
import org.lunaris.dolby.ui.viewmodel.AppProfileViewModel
import org.lunaris.dolby.ui.viewmodel.DolbyViewModel
import org.lunaris.dolby.ui.viewmodel.EqualizerViewModel

sealed class Screen(val route: String) {
    object Settings : Screen("settings")
    object Equalizer : Screen("equalizer")
    object Advanced : Screen("advanced")
    object AppProfiles : Screen("app_profiles")
    object ImportExport : Screen("import_export")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    dolbyViewModel: DolbyViewModel,
    equalizerViewModel: EqualizerViewModel,
    navController: NavController
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var toolbarHeightPx by remember { mutableIntStateOf(0) }
    val toolbarBottomPadding = with(density) { toolbarHeightPx.toDp() } + 36.dp

    val currentRoute = when (pagerState.targetPage) {
        0 -> "settings"
        1 -> "equalizer"
        2 -> "advanced"
        else -> "settings"
    }

    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage == 1) {
            equalizerViewModel.loadEqualizer()
        }
    }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> ModernDolbySettingsScreen(
                        viewModel = dolbyViewModel,
                        navController = navController,
                        bottomPadding = toolbarBottomPadding,
                    )
                    1 -> ModernEqualizerScreen(
                        viewModel = equalizerViewModel,
                        navController = navController,
                        bottomPadding = toolbarBottomPadding,
                    )
                    2 -> ModernAdvancedSettingsScreen(
                        viewModel = dolbyViewModel,
                        navController = navController,
                        bottomPadding = toolbarBottomPadding,
                    )
                }
            }
            FloatingNavToolbar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                        when (route) {
                            "settings" -> pagerState.animateScrollToPage(0)
                            "equalizer" -> pagerState.animateScrollToPage(1)
                            "advanced" -> pagerState.animateScrollToPage(2)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    )
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    .onSizeChanged { toolbarHeightPx = it.height },
            )
        }
    }
}

@Composable
fun DolbyNavHost(
    dolbyViewModel: DolbyViewModel,
    equalizerViewModel: EqualizerViewModel
) {
    val navController = rememberNavController()
    val spatialOffsetSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val spatialFloatSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    NavHost(
        navController = navController,
        startDestination = "main_pager",
        enterTransition = {
            slideInHorizontally(animationSpec = spatialOffsetSpec) { it / 5 } +
                fadeIn(animationSpec = effectsSpec)
        },
        exitTransition = {
            scaleOut(targetScale = 0.96f, animationSpec = spatialFloatSpec) +
                fadeOut(animationSpec = effectsSpec)
        },
        popEnterTransition = {
            scaleIn(initialScale = 0.96f, animationSpec = spatialFloatSpec) +
                fadeIn(animationSpec = effectsSpec)
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = spatialOffsetSpec) { it / 5 } +
                fadeOut(animationSpec = effectsSpec)
        }
    ) {
        composable("main_pager") {
            MainPagerScreen(
                dolbyViewModel = dolbyViewModel,
                equalizerViewModel = equalizerViewModel,
                navController = navController
            )
        }

        composable(Screen.AppProfiles.route) {
            val context = LocalContext.current
            val appProfileViewModel: AppProfileViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
                    context.applicationContext as android.app.Application
                )
            )

            AppProfileScreen(
                viewModel = appProfileViewModel,
                navController = navController
            )
        }

        composable(Screen.ImportExport.route) {
            PresetImportExportScreen(
                viewModel = equalizerViewModel,
                navController = navController
            )
        }
    }
}
