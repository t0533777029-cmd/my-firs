package com.proporit.app.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proporit.app.data.ReminderRepository
import com.proporit.app.ui.SimpleViewModelFactory
import com.proporit.app.ui.calendar.CalendarScreen
import com.proporit.app.ui.calendar.CalendarViewModel
import com.proporit.app.ui.home.HomeScreen
import com.proporit.app.ui.home.HomeViewModel
import com.proporit.app.ui.settings.SettingsScreen
import com.proporit.app.ui.settings.SettingsViewModel
import com.proporit.app.ui.theme.Blue400
import com.proporit.app.ui.theme.Cyan400
import com.proporit.app.ui.theme.Ink0
import com.proporit.app.ui.theme.Ink500
import com.proporit.app.ui.theme.Navy950

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("home", "Главная", Icons.Filled.Home),
    Tab("calendar", "Календарь", Icons.Filled.CalendarMonth),
    Tab("settings", "Настройки", Icons.Filled.Settings),
)

@Composable
fun AppNav(repository: ReminderRepository) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Navy950,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar(containerColor = Navy950) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan400,
                            selectedTextColor = Cyan400,
                            unselectedIconColor = Ink500,
                            unselectedTextColor = Ink500,
                            indicatorColor = Ink0.copy(alpha = 0.06f),
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Navy950)
        ) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    val vm: HomeViewModel = viewModel(factory = SimpleViewModelFactory { HomeViewModel(repository) })
                    HomeScreen(vm)
                }
                composable("calendar") {
                    val vm: CalendarViewModel = viewModel(factory = SimpleViewModelFactory { CalendarViewModel(repository) })
                    CalendarScreen(vm)
                }
                composable("settings") {
                    val vm: SettingsViewModel = viewModel(factory = SimpleViewModelFactory { SettingsViewModel(repository) })
                    SettingsScreen(vm)
                }
            }
        }
    }
}
