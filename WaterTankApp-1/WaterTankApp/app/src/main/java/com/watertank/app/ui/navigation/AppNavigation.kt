package com.watertank.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.watertank.app.ui.screens.calculator.CalculatorScreen
import com.watertank.app.ui.screens.chlorine.ChlorineScreen
import com.watertank.app.ui.screens.home.HomeScreen
import com.watertank.app.ui.screens.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val CALCULATOR = "calculator"
    const val CHLORINE = "chlorine"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onOpenCalculator = { nav.navigate(Routes.CALCULATOR) },
                onOpenChlorine = { nav.navigate(Routes.CHLORINE) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.CALCULATOR) {
            CalculatorScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.CHLORINE) {
            ChlorineScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
