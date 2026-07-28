package com.alkanyazilim.wellnesapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alkanyazilim.wellnesapp.ui.home.HomeScreen
import com.alkanyazilim.wellnesapp.ui.steps.StepsDetailScreen
import com.alkanyazilim.wellnesapp.ui.steps.StepsScreen
import com.alkanyazilim.wellnesapp.ui.water.WaterScreen
import com.alkanyazilim.wellnesapp.ui.water.WaterReminderSettingsScreen
import com.alkanyazilim.wellnesapp.ui.tasks.TasksScreen
import com.alkanyazilim.wellnesapp.ui.workout.WorkoutHubScreen
import com.alkanyazilim.wellnesapp.ui.workout.RunHistoryScreen

@Composable
fun AppNavigation(startDestination: String? = null) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination ?: Screen.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = navController) }
            composable(Screen.Steps.route) { StepsScreen(navController = navController) }
            composable(Screen.Water.route) { WaterScreen(navController = navController) }
            composable(Screen.Tasks.route) { TasksScreen() }
            composable(Screen.Workout.route) { WorkoutHubScreen(navController = navController) }
            composable(Screen.WaterReminderSettings.route) {
                WaterReminderSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(com.alkanyazilim.wellnesapp.ui.SETTINGS_ROUTE) {
                com.alkanyazilim.wellnesapp.ui.settings.SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "steps_detail/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateString = backStackEntry.arguments?.getString("date") ?: ""
                StepsDetailScreen(
                    dateString = dateString,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("run_history") {
                RunHistoryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    screen.icon?.let {
                        Icon(it, contentDescription = screen.title)
                    }
                },
                label = { Text(screen.title) }
            )
        }
    }
}