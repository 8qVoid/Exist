package com.exist.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exist.app.ExistApp
import com.exist.app.MainActivity
import com.exist.app.core.navigation.ExistRoute
import com.exist.app.presentation.archive.ArchiveScreen
import com.exist.app.presentation.archive.ArchiveViewModel
import com.exist.app.presentation.analytics.AnalyticsScreen
import com.exist.app.presentation.analytics.AnalyticsViewModel
import com.exist.app.presentation.auth.AuthScreen
import com.exist.app.presentation.auth.AuthViewModel
import com.exist.app.presentation.camera.CameraScreen
import com.exist.app.presentation.camera.CaptureViewModel
import com.exist.app.presentation.daydetail.DayDetailScreen
import com.exist.app.presentation.daydetail.DayDetailViewModel
import com.exist.app.presentation.highlights.HighlightsScreen
import com.exist.app.presentation.highlights.HighlightsViewModel
import com.exist.app.presentation.home.HomeScreen
import com.exist.app.presentation.home.HomeViewModel
import com.exist.app.presentation.memoryedit.MemoryEditScreen
import com.exist.app.presentation.memoryedit.MemoryEditViewModel
import com.exist.app.presentation.profile.ProfileScreen
import com.exist.app.presentation.recap.RecapScreen
import com.exist.app.presentation.recap.RecapViewModel
import com.exist.app.presentation.settings.SettingsScreen
import com.exist.app.presentation.settings.SettingsViewModel

@Composable
fun ExistNavHost(
    modifier: Modifier = Modifier,
    startRouteOverride: String? = null,
    onConsumeStartRoute: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as ExistApp
    val memoryRepository = app.container.memoryRepository
    val authRepository = app.container.authRepository
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = appViewModelFactory {
        AuthViewModel(authRepository, memoryRepository)
    }
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val startDestination = if (authState.session.isAuthenticated && !authState.session.needsOnboarding) {
        ExistRoute.Home.route
    } else {
        ExistRoute.Auth.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val tabRoutes = setOf(
        ExistRoute.Home.route,
        ExistRoute.Highlights.route,
        ExistRoute.Analytics.route,
        ExistRoute.Profile.route
    )
    val showTabs = authState.session.isAuthenticated && !authState.session.needsOnboarding && currentRoute in tabRoutes

    LaunchedEffect(authState.session.isAuthenticated, authState.session.needsOnboarding) {
        if (authState.session.isAuthenticated && !authState.session.needsOnboarding) {
            if (navController.currentDestination?.route == ExistRoute.Auth.route) {
                navController.navigate(ExistRoute.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            }
        } else {
            if (navController.currentDestination?.route != ExistRoute.Auth.route) {
                navController.navigate(ExistRoute.Auth.route) {
                    popUpTo(0)
                }
            }
        }
    }

    LaunchedEffect(startRouteOverride, authState.session.isAuthenticated, authState.session.needsOnboarding) {
        if (startRouteOverride == MainActivity.ROUTE_CAMERA && authState.session.isAuthenticated && !authState.session.needsOnboarding) {
            navController.navigate(ExistRoute.Camera.route)
            onConsumeStartRoute()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showTabs) {
                NavigationBar {
                    val items = listOf(
                        TabItem("Dashboard", ExistRoute.Home.route, Icons.Rounded.Home),
                        TabItem("Highlights", ExistRoute.Highlights.route, Icons.Rounded.AutoStories),
                        TabItem("Analytics", ExistRoute.Analytics.route, Icons.Rounded.Analytics),
                        TabItem("Profile", ExistRoute.Profile.route, Icons.Rounded.Person)
                    )
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ExistRoute.Auth.route) {
                AuthScreen(viewModel = authViewModel)
            }

            composable(ExistRoute.Home.route) {
                val viewModel: HomeViewModel = appViewModelFactory {
                    HomeViewModel(memoryRepository)
                }

                HomeScreen(
                    viewModel = viewModel,
                    onTakeProof = { navController.navigate(ExistRoute.Camera.route) },
                    onOpenToday = { dayKey -> navController.navigate(ExistRoute.DayDetail.create(dayKey)) },
                    onOpenArchive = { navController.navigate(ExistRoute.Archive.route) },
                    onOpenProfile = { navController.navigate(ExistRoute.Profile.route) },
                    onOpenRecap = { dayKey -> navController.navigate(ExistRoute.Recap.create(dayKey)) }
                )
            }

            composable(ExistRoute.Highlights.route) {
                val viewModel: HighlightsViewModel = appViewModelFactory {
                    HighlightsViewModel(memoryRepository)
                }
                HighlightsScreen(
                    viewModel = viewModel,
                    onOpenDay = { dayKey -> navController.navigate(ExistRoute.DayDetail.create(dayKey)) },
                    onOpenRecap = { dayKey -> navController.navigate(ExistRoute.Recap.create(dayKey)) },
                    onOpenArchive = { navController.navigate(ExistRoute.Archive.route) }
                )
            }

            composable(ExistRoute.Analytics.route) {
                val viewModel: AnalyticsViewModel = appViewModelFactory {
                    AnalyticsViewModel(memoryRepository)
                }
                AnalyticsScreen(viewModel = viewModel)
            }

            composable(ExistRoute.Profile.route) {
                val viewModel: SettingsViewModel = appViewModelFactory {
                    SettingsViewModel(memoryRepository, authRepository, app)
                }
                ProfileScreen(
                    viewModel = viewModel,
                    onOpenAdvancedSettings = { navController.navigate(ExistRoute.Settings.route) }
                )
            }

            composable(ExistRoute.Camera.route) {
                val viewModel: CaptureViewModel = appViewModelFactory {
                    CaptureViewModel(memoryRepository)
                }

                CameraScreen(
                    viewModel = viewModel,
                    onSaved = {
                        navController.popBackStack()
                        navController.navigate(ExistRoute.Home.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ExistRoute.DayDetail.route) { backStackEntry ->
                val dayKey = backStackEntry.arguments?.getString("dayKey").orEmpty()
                val viewModel: DayDetailViewModel = appViewModelFactory {
                    DayDetailViewModel(memoryRepository, dayKey)
                }

                DayDetailScreen(
                    dayKey = dayKey,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRecap = { navController.navigate(ExistRoute.Recap.create(dayKey)) },
                    onEditMemory = { memoryId -> navController.navigate(ExistRoute.MemoryEdit.create(memoryId)) }
                )
            }

            composable(ExistRoute.Archive.route) {
                val viewModel: ArchiveViewModel = appViewModelFactory {
                    ArchiveViewModel(memoryRepository)
                }

                ArchiveScreen(
                    viewModel = viewModel,
                    onOpenDay = { dayKey -> navController.navigate(ExistRoute.DayDetail.create(dayKey)) },
                    onOpenRecap = { dayKey -> navController.navigate(ExistRoute.Recap.create(dayKey)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ExistRoute.Recap.route) { backStackEntry ->
                val dayKey = backStackEntry.arguments?.getString("dayKey").orEmpty()
                val viewModel: RecapViewModel = appViewModelFactory {
                    RecapViewModel(memoryRepository, dayKey)
                }

                RecapScreen(
                    dayKey = dayKey,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ExistRoute.Settings.route) {
                val viewModel: SettingsViewModel = appViewModelFactory {
                    SettingsViewModel(memoryRepository, authRepository, app)
                }

                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ExistRoute.MemoryEdit.route) { backStackEntry ->
                val memoryId = backStackEntry.arguments?.getString("memoryId")?.toLongOrNull() ?: -1L
                val viewModel: MemoryEditViewModel = appViewModelFactory {
                    MemoryEditViewModel(memoryRepository, memoryId)
                }

                MemoryEditScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private data class TabItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private inline fun <reified T : ViewModel> appViewModelFactory(
    crossinline create: () -> T
): T {
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
    return viewModel(factory = factory)
}
