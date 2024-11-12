package com.theft_preventer.app.data.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.theft_preventer.app.data.presentation.login.LoginScreen
import com.theft_preventer.app.data.presentation.overview.OverviewScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "login"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onNavigateToOverview = {
                    navController.navigate("overview") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("overview") {
            OverviewScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("overview") { inclusive = true }
                    }
                }
            )
        }
    }
}